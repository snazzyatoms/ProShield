package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashSet;
import java.util.Set;

public class BlockProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    // cached toggles
    private boolean containers;
    private boolean mobGrief;
    private boolean creeper;
    private boolean tnt;
    private boolean wither;
    private boolean witherSkull;
    private boolean enderCrystal;
    private boolean enderDragon;

    private boolean fireSpread;
    private boolean fireBurn;
    private boolean igniteFlint;
    private boolean igniteLava;
    private boolean igniteLightning;
    private boolean igniteExplosion;
    private boolean igniteSpread;

    private boolean interactionsEnabled;
    private String interactionsMode; // blacklist | whitelist
    private final Set<Material> interactionSet = new HashSet<>();

    private boolean bucketEmptyBlock;
    private boolean bucketFillBlock;

    private boolean endermanGrief;
    private boolean ravagerGrief;
    private boolean silverfishGrief;
    private boolean dragonGrief;
    private boolean witherGrief;

    private boolean endermanTeleportDenied;

    public BlockProtectionListener(PlotManager plotManager) {
        this.plugin = ProShield.getInstance();
        this.plotManager = plotManager;
        reloadProtectionConfig();
    }

    /** Re-read config (called from /proshield reload) */
    public final void reloadProtectionConfig() {
        containers = plugin.getConfig().getBoolean("protection.containers", true);
        mobGrief  = plugin.getConfig().getBoolean("protection.mob-grief", true);

        creeper      = plugin.getConfig().getBoolean("protection.creeper-explosions", true);
        tnt          = plugin.getConfig().getBoolean("protection.tnt-explosions", true);
        wither       = plugin.getConfig().getBoolean("protection.wither-explosions", true);
        witherSkull  = plugin.getConfig().getBoolean("protection.wither-skull-explosions", true);
        enderCrystal = plugin.getConfig().getBoolean("protection.ender-crystal-explosions", true);
        enderDragon  = plugin.getConfig().getBoolean("protection.ender-dragon-explosions", true);

        fireSpread      = plugin.getConfig().getBoolean("protection.fire.spread", true);
        fireBurn        = plugin.getConfig().getBoolean("protection.fire.burn", true);
        igniteFlint     = plugin.getConfig().getBoolean("protection.fire.ignite.flint_and_steel", true);
        igniteLava      = plugin.getConfig().getBoolean("protection.fire.ignite.lava", true);
        igniteLightning = plugin.getConfig().getBoolean("protection.fire.ignite.lightning", true);
        igniteExplosion = plugin.getConfig().getBoolean("protection.fire.ignite.explosion", true);
        igniteSpread    = plugin.getConfig().getBoolean("protection.fire.ignite.spread", true);

        interactionsEnabled = plugin.getConfig().getBoolean("protection.interactions.enabled", true);
        interactionsMode    = plugin.getConfig().getString("protection.interactions.mode", "blacklist").toLowerCase();

        // Build interaction material set from categories + explicit list
        interactionSet.clear();
        var cats = plugin.getConfig().getStringList("protection.interactions.categories");
        for (String c : cats) {
            switch (c.toLowerCase()) {
                case "doors":           addDoorMaterials(); break;
                case "trapdoors":       addTrapdoorMaterials(); break;
                case "fence_gates":     addFenceGateMaterials(); break;
                case "buttons":         addButtonMaterials(); break;
                case "levers":          interactionSet.add(Material.LEVER); break;
                case "pressure_plates": addPressurePlateMaterials(); break;
                default: break;
            }
        }
        for (String m : plugin.getConfig().getStringList("protection.interactions.list")) {
            try { interactionSet.add(Material.valueOf(m.toUpperCase())); } catch (IllegalArgumentException ignored) {}
        }

        bucketEmptyBlock = plugin.getConfig().getBoolean("protection.buckets.block-empty", true);
        bucketFillBlock  = plugin.getConfig().getBoolean("protection.buckets.block-fill", true);

        endermanGrief = plugin.getConfig().getBoolean("protection.entity-grief.enderman", true);
        ravagerGrief  = plugin.getConfig().getBoolean("protection.entity-grief.ravager", true);
        silverfishGrief = plugin.getConfig().getBoolean("protection.entity-grief.silverfish", true);
        dragonGrief   = plugin.getConfig().getBoolean("protection.entity-grief.ender-dragon", true);
        witherGrief   = plugin.getConfig().getBoolean("protection.entity-grief.wither", true);

        endermanTeleportDenied = plugin.getConfig().getBoolean("protection.entity-teleport.enderman", true);
    }

    private boolean denyIfNeeded(Player p, Location loc, String msg) {
        if (p.hasPermission("proshield.bypass")) return false;             // admins bypass
        if (!plotManager.isClaimed(loc)) return false;                     // not claimed -> allow
        if (plotManager.isTrustedOrOwner(p.getUniqueId(), loc)) return false; // owner/trusted -> allow
        p.sendMessage(ChatColor.RED + msg);
        return true;
    }

    /* ===== Blocks: break/place ===== */
    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (denyIfNeeded(e.getPlayer(), e.getBlock().getLocation(), "You cannot break blocks here!")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (denyIfNeeded(e.getPlayer(), e.getBlock().getLocation(), "You cannot place blocks here!")) {
            e.setCancelled(true);
        }
    }

    /* ===== Containers + Interactions ===== */
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        Block b = e.getClickedBlock();
        if (b == null) return;

        // Containers
        if (containers) {
            BlockState st = b.getState();
            if (st instanceof Container) {
                if (denyIfNeeded(e.getPlayer(), b.getLocation(), "You cannot open containers here!")) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        // Non-container interactions with proper blacklist/whitelist behavior
        if (interactionsEnabled) {
            boolean claimed = plotManager.isClaimed(b.getLocation());
            if (claimed && !e.getPlayer().hasPermission("proshield.bypass")) {
                boolean isOwnerOrTrusted = plotManager.isTrustedOrOwner(e.getPlayer().getUniqueId(), b.getLocation());

                if (!isOwnerOrTrusted) {
                    if ("blacklist".equals(interactionsMode)) {
                        // If material is in the set, block it
                        if (interactionSet.contains(b.getType())) {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage(ChatColor.RED + "You cannot use that here!");
                        }
                    } else { // whitelist
                        // If material is NOT in the set, block it
                        if (!interactionSet.contains(b.getType())) {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage(ChatColor.RED + "You cannot use that here!");
                        }
                    }
                }
            }
        }
    }

    /* ===== Buckets ===== */
    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (!bucketEmptyBlock) return;
        Block b = e.getBlockClicked();
        if (b == null) return;
        if (plotManager.isClaimed(b.getLocation())
                && !plotManager.isTrustedOrOwner(e.getPlayer().getUniqueId(), b.getLocation())
                && !e.getPlayer().hasPermission("proshield.bypass")) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "You cannot pour here!");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (!bucketFillBlock) return;
        Block b = e.getBlockClicked();
        if (b == null) return;
        if (plotManager.isClaimed(b.getLocation())
                && !plotManager.isTrustedOrOwner(e.getPlayer().getUniqueId(), b.getLocation())
                && !e.getPlayer().hasPermission("proshield.bypass")) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "You cannot take this source!");
        }
    }

    /* ===== Fire control ===== */
    @EventHandler(ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent e) {
        if (!fireSpread) return;
        if (e.getNewState().getType() == Material.FIRE && plotManager.isClaimed(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBurn(BlockBurnEvent e) {
        if (!fireBurn) return;
        if (plotManager.isClaimed(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent e) {
        if (e.isCancelled()) return;
        Location loc = e.getBlock().getLocation();
        if (!plotManager.isClaimed(loc)) return;

        switch (e.getCause()) {
            case FLINT_AND_STEEL: if (igniteFlint)     e.setCancelled(true); break;
            case LAVA:            if (igniteLava)      e.setCancelled(true); break;
            case LIGHTNING:       if (igniteLightning) e.setCancelled(true); break;
            case EXPLOSION:       if (igniteExplosion) e.setCancelled(true); break;
            case SPREAD:          if (igniteSpread)    e.setCancelled(true); break;
            default: break;
        }
    }

    /* ===== Explosions ===== */
    @EventHandler(ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        if (!mobGrief) return;
        EntityType type = e.getEntityType();
        boolean blockInsideClaims =
                (type == EntityType.CREEPER       && creeper) ||
                (type == EntityType.PRIMED_TNT    && tnt) ||
                (type == EntityType.WITHER        && wither) ||
                (type == EntityType.WITHER_SKULL  && witherSkull) ||
                (type == EntityType.ENDER_CRYSTAL && enderCrystal) ||
                (type == EntityType.ENDER_DRAGON  && enderDragon);

        if (blockInsideClaims) {
            e.blockList().removeIf(block -> plotManager.isClaimed(block.getLocation()));
        }
    }

    /* ===== Entity grief (e.g., Enderman picking blocks) ===== */
    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        EntityType type = e.getEntityType();
        if (!plotManager.isClaimed(e.getBlock().getLocation())) return;

        if ((type == EntityType.ENDERMAN     && endermanGrief) ||
            (type == EntityType.RAVAGER      && ravagerGrief) ||
            (type == EntityType.SILVERFISH   && silverfishGrief) ||
            (type == EntityType.ENDER_DRAGON && dragonGrief) ||
            (type == EntityType.WITHER       && witherGrief)) {
            e.setCancelled(true);
        }
    }

    /* ===== Enderman teleport denial ===== */
    @EventHandler(ignoreCancelled = true)
    public void onEndermanTeleport(EntityTeleportEvent e) {
        if (!endermanTeleportDenied) return;
        if (!(e.getEntity() instanceof Enderman)) return;

        Location to = e.getTo();
        if (to != null && plotManager.isClaimed(to)) {
            e.setCancelled(true);
        }
    }

    /* ===== helpers to build interaction sets ===== */
    private void addDoorMaterials() {
        for (Material m : Material.values()) if (m.name().endsWith("_DOOR")) interactionSet.add(m);
    }
    private void addTrapdoorMaterials() {
        for (Material m : Material.values()) if (m.name().endsWith("_TRAPDOOR")) interactionSet.add(m);
    }
    private void addFenceGateMaterials() {
        for (Material m : Material.values()) if (m.name().endsWith("_FENCE_GATE")) interactionSet.add(m);
    }
    private void addButtonMaterials() {
        for (Material m : Material.values()) if (m.name().endsWith("_BUTTON")) interactionSet.add(m);
    }
    private void addPressurePlateMaterials() {
        for (Material m : Material.values()) if (m.name().endsWith("_PRESSURE_PLATE")) interactionSet.add(m);
    }
}
