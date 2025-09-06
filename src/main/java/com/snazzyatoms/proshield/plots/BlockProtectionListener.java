package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Reads all protection settings from config.yml:
 *  - limits, expiry handled in PlotManager (not here)
 *  - protection.containers
 *  - protection.pvp-in-claims
 *  - protection.mob-grief (master toggle for explosion block breaking)
 *    + creeper-explosions
 *    + tnt-explosions
 *    + wither-explosions
 *    + wither-skull-explosions
 *    + ender-crystal-explosions
 *    + ender-dragon-explosions
 *  - protection.fire.spread / fire.burn
 *  - protection.fire.ignite.{flint_and_steel, lava, lightning, explosion, spread}
 *  - protection.interactions.{enabled,mode,categories,list}
 *  - protection.buckets.{block-empty,block-fill}
 *
 * Supports live reload via reloadInteractionConfig().
 */
public class BlockProtectionListener implements Listener {

    private final PlotManager plotManager;

    // === Cached config flags ===
    private boolean protectContainers = true;

    // PvP
    private boolean pvpInClaims = false;

    // Explosions
    private boolean preventMobGrief = true;
    private boolean blockCreeper = true;
    private boolean blockTnt = true;
    private boolean blockWither = true;
    private boolean blockWitherSkull = true;
    private boolean blockEnderCrystal = true;
    private boolean blockEnderDragon = true;

    // Fire
    private boolean fireSpread = true; // cancel spread in claims
    private boolean fireBurn   = true; // cancel block burn in claims (handled via spread/ignite below)
    private boolean igniteFlint = true;
    private boolean igniteLava = true;
    private boolean igniteLightning = true;
    private boolean igniteExplosion = true;
    private boolean igniteSpread = true;

    // Buckets
    private boolean bucketsBlockEmpty = true;
    private boolean bucketsBlockFill = true;

    // Interactions
    private boolean interactionsEnabled = true;
    private Mode interactionsMode = Mode.BLACKLIST;
    private final Set<String> interactionCategories = new HashSet<>();
    private final Set<Material> interactionList = new HashSet<>();
    private enum Mode { WHITELIST, BLACKLIST }

    public BlockProtectionListener(PlotManager plotManager) {
        this.plotManager = plotManager;
        reloadInteractionConfig(); // also loads all base flags
    }

    /** Called from ProShield.reloadAllConfigs() to re-read config without re-registering listeners. */
    public void reloadInteractionConfig() {
        var cfg = ProShield.getInstance().getConfig();

        // Base toggles
        protectContainers = cfg.getBoolean("protection.containers", true);
        pvpInClaims = cfg.getBoolean("protection.pvp-in-claims", false);

        // Explosion protection
        preventMobGrief   = cfg.getBoolean("protection.mob-grief", true);
        blockCreeper      = cfg.getBoolean("protection.creeper-explosions", true);
        blockTnt          = cfg.getBoolean("protection.tnt-explosions", true);
        blockWither       = cfg.getBoolean("protection.wither-explosions", true);
        blockWitherSkull  = cfg.getBoolean("protection.wither-skull-explosions", true);
        blockEnderCrystal = cfg.getBoolean("protection.ender-crystal-explosions", true);
        blockEnderDragon  = cfg.getBoolean("protection.ender-dragon-explosions", true);

        // Fire
        ConfigurationSection fire = cfg.getConfigurationSection("protection.fire");
        fireSpread   = fire != null ? fire.getBoolean("spread", true) : true;
        fireBurn     = fire != null ? fire.getBoolean("burn", true)   : true;

        ConfigurationSection ignite = fire != null ? fire.getConfigurationSection("ignite") : null;
        igniteFlint     = ignite != null ? ignite.getBoolean("flint_and_steel", true) : true;
        igniteLava      = ignite != null ? ignite.getBoolean("lava", true)            : true;
        igniteLightning = ignite != null ? ignite.getBoolean("lightning", true)       : true;
        igniteExplosion = ignite != null ? ignite.getBoolean("explosion", true)       : true;
        igniteSpread    = ignite != null ? ignite.getBoolean("spread", true)          : true;

        // Buckets
        bucketsBlockEmpty = cfg.getBoolean("protection.buckets.block-empty", true);
        bucketsBlockFill  = cfg.getBoolean("protection.buckets.block-fill", true);

        // Interactions
        ConfigurationSection sec = cfg.getConfigurationSection("protection.interactions");
        if (sec == null) {
            interactionsEnabled = true;
            interactionsMode = Mode.BLACKLIST;
            interactionCategories.clear();
            interactionCategories.add("doors");
            interactionCategories.add("trapdoors");
            interactionCategories.add("fence_gates");
            interactionCategories.add("buttons");
            interactionCategories.add("levers");
            interactionCategories.add("pressure_plates");
            interactionList.clear();
            return;
        }

        interactionsEnabled = sec.getBoolean("enabled", true);
        String mode = sec.getString("mode", "blacklist").trim().toUpperCase(Locale.ROOT);
        interactionsMode = "WHITELIST".equals(mode) ? Mode.WHITELIST : Mode.BLACKLIST;

        interactionCategories.clear();
        List<String> cats = sec.getStringList("categories");
        if (cats != null) {
            for (String c : cats) if (c != null) interactionCategories.add(c.trim().toLowerCase(Locale.ROOT));
        }

        interactionList.clear();
        List<String> list = sec.getStringList("list");
        if (list != null) {
            for (String n : list) {
                if (n == null) continue;
                try { interactionList.add(Material.valueOf(n.trim().toUpperCase(Locale.ROOT))); }
                catch (IllegalArgumentException ignored) { }
            }
        }
    }

    // ===== Common helpers =====

    private boolean isBypassing(Player p) { return p.hasMetadata("proshield_bypass"); }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                ProShield.getInstance().getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }

    private boolean denyIfNeeded(Player p, Location loc, String msg) {
        if (isBypassing(p)) return false;
        if (!plotManager.isClaimed(loc)) return false;
        if (plotManager.isTrustedOrOwner(p.getUniqueId(), loc)) return false;
        p.sendMessage(prefix() + ChatColor.RED + msg);
        return true;
    }

    private boolean isClaimed(Location loc) {
        return plotManager.isClaimed(loc);
    }

    // ===== Block place/break =====

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (denyIfNeeded(e.getPlayer(), e.getBlock().getLocation(), "You cannot break blocks here!")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (denyIfNeeded(e.getPlayer(), e.getBlock().getLocation(), "You cannot place blocks here!")) {
            e.setCancelled(true);
        }
    }

    // ===== Interactions: containers + doors/buttons/levers/etc. =====

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return; // avoid double firing
        Block b = e.getClickedBlock();
        if (b == null) return;

        // Containers
        if (protectContainers) {
            BlockState st = b.getState();
            if (st instanceof Container) {
                if (denyIfNeeded(e.getPlayer(), b.getLocation(), "You cannot open containers here!")) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        // Non-container interactables
        if (!interactionsEnabled) return;
        Material type = b.getType();
        boolean matches = matchesConfigured(type);
        boolean shouldDeny = (interactionsMode == Mode.WHITELIST) ? !matches : matches;
        if (shouldDeny) {
            if (denyIfNeeded(e.getPlayer(), b.getLocation(), "You cannot interact here!")) e.setCancelled(true);
        }
    }

    private boolean matchesConfigured(Material m) {
        if (interactionList.contains(m)) return true;
        String n = m.name();
        if (interactionCategories.contains("doors") && n.endsWith("_DOOR")) return true;
        if (interactionCategories.contains("trapdoors") && n.endsWith("_TRAPDOOR")) return true;
        if (interactionCategories.contains("fence_gates") && n.endsWith("_FENCE_GATE")) return true;
        if (interactionCategories.contains("buttons") && n.endsWith("_BUTTON")) return true;
        if (interactionCategories.contains("pressure_plates") && n.endsWith("_PRESSURE_PLATE")) return true;
        if (interactionCategories.contains("levers") && n.equals("LEVER")) return true;
        return false;
    }

    // ===== PvP toggle inside claims =====

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (pvpInClaims) return; // allow PvP if true
        if (e.getDamager() instanceof Player damager && e.getEntity() instanceof Player) {
            if (isBypassing(damager)) return;
            Location loc = e.getEntity().getLocation();
            if (isClaimed(loc)) e.setCancelled(true);
        }
    }

    // ===== Explosions: block-breaking protection inside claims =====

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if (!preventMobGrief) return;

        EntityType t = e.getEntityType();
        boolean block =
                (t == EntityType.CREEPER      && blockCreeper)      ||
                (t == EntityType.PRIMED_TNT   && blockTnt)          ||
                (t == EntityType.WITHER       && blockWither)       ||
                (t == EntityType.WITHER_SKULL && blockWitherSkull)  ||
                (t == EntityType.ENDER_CRYSTAL && blockEnderCrystal)||
                (t == EntityType.ENDER_DRAGON && blockEnderDragon);

        if (!block) return;

        e.blockList().removeIf(b -> isClaimed(b.getLocation()));
    }

    // ===== Entity grief that isn't an explosion (Enderman, Ravager, Silverfish, Dragon block change, etc.) =====

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        EntityType type = e.getEntityType();
        Location loc = e.getBlock().getLocation();
        if (!isClaimed(loc)) return;

        // Map to config toggles (all default true = block grief in claims)
        boolean cancel = switch (type) {
            case ENDERMAN     -> ProShield.getInstance().getConfig().getBoolean("protection.entity-grief.enderman", true);
            case RAVAGER      -> ProShield.getInstance().getConfig().getBoolean("protection.entity-grief.ravager", true);
            case SILVERFISH   -> ProShield.getInstance().getConfig().getBoolean("protection.entity-grief.silverfish", true);
            case ENDER_DRAGON -> ProShield.getInstance().getConfig().getBoolean("protection.entity-grief.ender-dragon", true);
            case WITHER       -> ProShield.getInstance().getConfig().getBoolean("protection.entity-grief.wither", true);
            default -> false;
        };

        if (cancel) e.setCancelled(true);
    }

    // ===== Fire control =====

    // Prevent fire spread within claims
    @EventHandler
    public void onBlockSpread(BlockSpreadEvent e) {
        if (!fireSpread) return;
        // Only care when the result becomes FIRE (classic fire spread)
        if (e.getBlock().getType() == Material.FIRE || e.getNewState().getType() == Material.FIRE) {
            if (isClaimed(e.getBlock().getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    // Prevent ignition within claims depending on cause
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (!isClaimed(e.getBlock().getLocation())) return;

        BlockIgniteEvent.IgniteCause cause = e.getCause();
        switch (cause) {
            case FLINT_AND_STEEL -> {
                if (!igniteFlint) return; // not blocking this cause
                // If a player is igniting inside a claim they don't own/trust, cancel
                Player p = e.getPlayer();
                if (p != null && !isBypassing(p) && !plotManager.isTrustedOrOwner(p.getUniqueId(), e.getBlock().getLocation())) {
                    e.setCancelled(true);
                    p.sendMessage(prefix() + ChatColor.RED + "You cannot ignite fire here!");
                }
            }
            case LAVA -> { if (igniteLava) e.setCancelled(true); }
            case LIGHTNING -> { if (igniteLightning) e.setCancelled(true); }
            case EXPLOSION -> { if (igniteExplosion) e.setCancelled(true); }
            case SPREAD -> { if (igniteSpread) e.setCancelled(true); }
            default -> { /* other causes left untouched */ }
        }
    }

    // ===== Buckets =====

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (!bucketsBlockEmpty) return;
        Block clicked = e.getBlockClicked();
        if (clicked == null) return;
        Block target = clicked.getRelative(e.getBlockFace());
        if (target == null) return;
        if (denyIfNeeded(e.getPlayer(), target.getLocation(), "You cannot pour here!")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (!bucketsBlockFill) return;
        Block clicked = e.getBlockClicked();
        if (clicked == null) return;
        if (denyIfNeeded(e.getPlayer(), clicked.getLocation(), "You cannot take from here!")) {
            e.setCancelled(true);
        }
    }
}
