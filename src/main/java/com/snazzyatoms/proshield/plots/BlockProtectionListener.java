// path: src/main/java/com/snazzyatoms/proshield/plots/BlockProtectionListener.java
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
import org.bukkit.event.block.*;
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
    private final PlotManager plots;

    private boolean mobGrief;
    private boolean endermanTeleportDenied;
    private boolean fireSpread, fireBurn;
    private boolean igniteFlint, igniteLava, igniteLightning, igniteExplosion, igniteSpread;

    private boolean creeper, tnt, wither, witherSkull, enderCrystal, enderDragon;

    private boolean containersEnabled;
    private boolean interactionsEnabled;
    private String interactionsMode;
    private final Set<Material> interactionSet = new HashSet<>();

    private boolean bucketEmptyBlock, bucketFillBlock;

    private boolean endermanGrief, ravagerGrief, silverfishGrief, dragonGrief, witherGrief;

    private ClaimRole roleBuild;
    private ClaimRole roleInteract;
    private ClaimRole roleContainers;
    private ClaimRole roleBuckets;

    public BlockProtectionListener(PlotManager plotManager) {
        this.plugin = ProShield.getInstance();
        this.plots  = plotManager;
        reloadProtectionConfig();
    }

    public final void reloadProtectionConfig() {
        var cfg = plugin.getConfig();

        roleBuild      = ClaimRole.from(cfg.getString("protection.roles.build", "BUILDER"), ClaimRole.BUILDER);
        roleInteract   = ClaimRole.from(cfg.getString("protection.roles.interact", "MEMBER"), ClaimRole.MEMBER);
        roleContainers = ClaimRole.from(cfg.getString("protection.roles.open_containers", "CONTAINER"), ClaimRole.CONTAINER);
        roleBuckets    = ClaimRole.from(cfg.getString("protection.roles.use_buckets", "BUILDER"), ClaimRole.BUILDER);

        mobGrief = cfg.getBoolean("protection.mob-grief", true);

        creeper = cfg.getBoolean("protection.creeper-explosions", true);
        tnt = cfg.getBoolean("protection.tnt-explosions", true);
        wither = cfg.getBoolean("protection.wither-explosions", true);
        witherSkull = cfg.getBoolean("protection.wither-skull-explosions", true);
        enderCrystal = cfg.getBoolean("protection.ender-crystal-explosions", true);
        enderDragon = cfg.getBoolean("protection.ender-dragon-explosions", true);

        fireSpread = cfg.getBoolean("protection.fire.spread", true);
        fireBurn   = cfg.getBoolean("protection.fire.burn", true);
        igniteFlint      = cfg.getBoolean("protection.fire.ignite.flint_and_steel", true);
        igniteLava       = cfg.getBoolean("protection.fire.ignite.lava", true);
        igniteLightning  = cfg.getBoolean("protection.fire.ignite.lightning", true);
        igniteExplosion  = cfg.getBoolean("protection.fire.ignite.explosion", true);
        igniteSpread     = cfg.getBoolean("protection.fire.ignite.spread", true);

        containersEnabled   = cfg.getBoolean("protection.containers", true);
        interactionsEnabled = cfg.getBoolean("protection.interactions.enabled", true);
        interactionsMode    = cfg.getString("protection.interactions.mode", "blacklist").toLowerCase();

        interactionSet.clear();
        for (String c : cfg.getStringList("protection.interactions.categories")) {
            switch (c.toLowerCase()) {
                case "doors" -> addEndsWith("_DOOR");
                case "trapdoors" -> addEndsWith("_TRAPDOOR");
                case "fence_gates" -> addEndsWith("_FENCE_GATE");
                case "buttons" -> addEndsWith("_BUTTON");
                case "levers" -> interactionSet.add(Material.LEVER);
                case "pressure_plates" -> addEndsWith("_PRESSURE_PLATE");
            }
        }
        for (String s : cfg.getStringList("protection.interactions.list")) {
            try { interactionSet.add(Material.valueOf(s.toUpperCase())); } catch (IllegalArgumentException ignored) {}
        }

        bucketEmptyBlock = cfg.getBoolean("protection.buckets.block-empty", true);
        bucketFillBlock  = cfg.getBoolean("protection.buckets.block-fill", true);

        endermanGrief  = cfg.getBoolean("protection.entity-grief.enderman", true);
        ravagerGrief   = cfg.getBoolean("protection.entity-grief.ravager", true);
        silverfishGrief= cfg.getBoolean("protection.entity-grief.silverfish", true);
        dragonGrief    = cfg.getBoolean("protection.entity-grief.ender-dragon", true);
        witherGrief    = cfg.getBoolean("protection.entity-grief.wither", true);

        endermanTeleportDenied = cfg.getBoolean("protection.entity-teleport.enderman", true);
    }

    private boolean bypass(Player p) { return p.hasPermission("proshield.bypass"); }

    private boolean lackingRole(Player p, Location loc, ClaimRole need, String msg) {
        if (bypass(p)) return false;
        if (!plots.isClaimed(loc)) return false;
        if (plots.hasRoleAtLeast(loc, p.getUniqueId(), need)) return false;
        p.sendMessage(prefix() + ChatColor.RED + msg);
        return true;
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }

    private void addEndsWith(String suffix) {
        for (Material m : Material.values()) {
            if (m.name().endsWith(suffix)) interactionSet.add(m);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (lackingRole(e.getPlayer(), e.getBlock().getLocation(), roleBuild, "You cannot break blocks here!")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (lackingRole(e.getPlayer(), e.getBlock().getLocation(), roleBuild, "You cannot place blocks here!")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        Block b = e.getClickedBlock();
        if (b == null) return;

        Location loc = b.getLocation();

        if (containersEnabled) {
            BlockState st = b.getState();
            if (st instanceof Container) {
                if (lackingRole(e.getPlayer(), loc, roleContainers, "You cannot open containers here!")) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        if (interactionsEnabled && interactionSet.contains(b.getType())) {
            if (lackingRole(e.getPlayer(), loc, roleInteract, "You cannot use that here!")) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (!bucketEmptyBlock) return;
        Block b = e.getBlockClicked();
        if (b == null) return;
        if (lackingRole(e.getPlayer(), b.getLocation(), roleBuckets, "You cannot pour here!")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (!bucketFillBlock) return;
        Block b = e.getBlockClicked();
        if (b == null) return;
        if (lackingRole(e.getPlayer(), b.getLocation(), roleBuckets, "You cannot take this source!")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent e) {
        if (!fireSpread) return;
        if (e.getNewState().getType() == Material.FIRE && plots.isClaimed(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBurn(BlockBurnEvent e) {
        if (!fireBurn) return;
        if (plots.isClaimed(e.getBlock().getLocation())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent e) {
        if (!plots.isClaimed(e.getBlock().getLocation())) return;
        switch (e.getCause()) {
            case FLINT_AND_STEEL -> { if (igniteFlint) e.setCancelled(true); }
            case LAVA -> { if (igniteLava) e.setCancelled(true); }
            case LIGHTNING -> { if (igniteLightning) e.setCancelled(true); }
            case EXPLOSION -> { if (igniteExplosion) e.setCancelled(true); }
            case SPREAD -> { if (igniteSpread) e.setCancelled(true); }
            default -> {}
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        if (!mobGrief) return;
        EntityType t = e.getEntityType();
        boolean blockInsideClaims =
                (t == EntityType.CREEPER && creeper) ||
                (t == EntityType.PRIMED_TNT && tnt) ||
                (t == EntityType.WITHER && wither) ||
                (t == EntityType.WITHER_SKULL && witherSkull) ||
                (t == EntityType.ENDER_CRYSTAL && enderCrystal) ||
                (t == EntityType.ENDER_DRAGON && enderDragon);
        if (blockInsideClaims) {
            e.blockList().removeIf(b -> plots.isClaimed(b.getLocation()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (!plots.isClaimed(e.getBlock().getLocation())) return;
        EntityType t = e.getEntityType();
        if ((t == EntityType.ENDERMAN && endermanGrief) ||
            (t == EntityType.RAVAGER && ravagerGrief) ||
            (t == EntityType.SILVERFISH && silverfishGrief) ||
            (t == EntityType.ENDER_DRAGON && dragonGrief) ||
            (t == EntityType.WITHER && witherGrief)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEndermanTeleport(EntityTeleportEvent e) {
        if (!endermanTeleportDenied) return;
        if (!(e.getEntity() instanceof Enderman)) return;
        Location to = e.getTo();
        if (to != null && plots.isClaimed(to)) e.setCancelled(true);
    }
}
