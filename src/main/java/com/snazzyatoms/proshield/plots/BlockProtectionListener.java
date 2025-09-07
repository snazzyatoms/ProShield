package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockBurnEvent;

import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTeleportEvent;

import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.inventory.EquipmentSlot;

import java.util.*;

/**
 * Claim protection listener with:
 *  - Block place/break denial in other players' claims
 *  - Container access control
 *  - Interactions (doors/buttons/etc.) with blacklist/whitelist + explicit materials
 *  - PvP toggle inside claims
 *  - Explosion block-breaking filtering (creeper/TNT/wither/etc.)
 *  - Fire spread/ignite/burn control
 *  - Entity grief block-change denial (Enderman/Ravager/Silverfish/Dragon/Wither)
 *  - Enderman teleport denial inside claims
 *
 *  Per-world overrides:
 *    config.worlds.<world>.<same structure as protection>  (merged over global defaults)
 *
 *  Supports live reload via reloadProtectionConfig().
 */
public class BlockProtectionListener implements Listener {

    private final PlotManager plotManager;

    // Cached global defaults + per-world merged settings
    private WorldProtectionSettings defaultSettings = new WorldProtectionSettings();
    private final Map<String, WorldProtectionSettings> worldOverrides = new HashMap<>();

    public BlockProtectionListener(PlotManager plotManager) {
        this.plotManager = plotManager;
        reloadProtectionConfig(); // initial load
    }

    // ------------------------------------------------------------------------
    // Reload & Settings
    // ------------------------------------------------------------------------

    public void reloadProtectionConfig() {
        ProShield pl = ProShield.getInstance();
        var cfg = pl.getConfig();

        // Load defaults from protection: ... (global)
        defaultSettings = WorldProtectionSettings.fromConfig(cfg.getConfigurationSection("protection"));

        // Load per-world overrides: worlds.<world>.protection
        worldOverrides.clear();
        ConfigurationSection worlds = cfg.getConfigurationSection("worlds");
        if (worlds != null) {
            for (String wName : worlds.getKeys(false)) {
                ConfigurationSection p = worlds.getConfigurationSection(wName + ".protection");
                if (p != null) {
                    WorldProtectionSettings merged = WorldProtectionSettings.fromConfig(p);
                    // merge on top of defaults
                    merged = WorldProtectionSettings.merge(defaultSettings, merged);
                    worldOverrides.put(wName, merged);
                }
            }
        }
    }

    // Keep backward compatibility if ProShield calls old name
    public void reloadInteractionConfig() { reloadProtectionConfig(); }

    private WorldProtectionSettings settingsFor(World w) {
        if (w == null) return defaultSettings;
        return worldOverrides.getOrDefault(w.getName(), defaultSettings);
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

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

    private static boolean nameEnds(Material m, String suffix) {
        return m.name().endsWith(suffix);
    }

    // ------------------------------------------------------------------------
    // Place/Break
    // ------------------------------------------------------------------------

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

    // ------------------------------------------------------------------------
    // Interactions: containers + doors/buttons/levers/etc.
    // ------------------------------------------------------------------------

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return; // avoid double firing
        Block b = e.getClickedBlock();
        if (b == null) return;

        WorldProtectionSettings s = settingsFor(b.getWorld());

        // Containers
        if (s.containers) {
            BlockState st = b.getState();
            if (st instanceof Container) {
                if (denyIfNeeded(e.getPlayer(), b.getLocation(), "You cannot open containers here!")) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        // Non-container interactables
        if (!s.interactionsEnabled) return;

        Material type = b.getType();
        boolean matches = s.matchesInteraction(type);
        boolean shouldDeny = (s.interactionsMode == WorldProtectionSettings.Mode.WHITELIST) ? !matches : matches;
        if (shouldDeny) {
            if (denyIfNeeded(e.getPlayer(), b.getLocation(), "You cannot interact here!")) e.setCancelled(true);
        }
    }

    // ------------------------------------------------------------------------
    // PvP inside claims
    // ------------------------------------------------------------------------

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player damager) || !(e.getEntity() instanceof Player)) return;
        if (isBypassing(damager)) return;

        WorldProtectionSettings s = settingsFor(e.getEntity().getWorld());
        if (s.pvpInClaims) return; // allow PvP if true

        Location loc = e.getEntity().getLocation();
        if (plotManager.isClaimed(loc)) e.setCancelled(true);
    }

    // ------------------------------------------------------------------------
    // Explosions (block breaking inside claims)
    // ------------------------------------------------------------------------

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        WorldProtectionSettings s = settingsFor(e.getEntity() != null ? e.getEntity().getWorld() : null);
        if (!s.mobGrief) return;

        EntityType t = (e.getEntity() != null) ? e.getEntity().getType() : null;
        boolean block = false;
        if (t == EntityType.CREEPER) block = s.blockCreeper;
        else if (t == EntityType.PRIMED_TNT) block = s.blockTnt;
        else if (t == EntityType.WITHER) block = s.blockWither;
        else if (t == EntityType.WITHER_SKULL) block = s.blockWitherSkull;
        else if (t == EntityType.ENDER_CRYSTAL) block = s.blockEnderCrystal;
        else if (t == EntityType.ENDER_DRAGON) block = s.blockEnderDragon;

        if (!block) return;

        e.blockList().removeIf(b -> plotManager.isClaimed(b.getLocation()));
    }

    // ------------------------------------------------------------------------
    // Entity grief (non-explosion) block changes
    // ------------------------------------------------------------------------

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        WorldProtectionSettings s = settingsFor(e.getBlock().getWorld());
        if (!plotManager.isClaimed(e.getBlock().getLocation())) return;

        EntityType type = e.getEntityType();
        boolean cancel = switch (type) {
            case ENDERMAN     -> s.griefEnderman;
            case RAVAGER      -> s.griefRavager;
            case SILVERFISH   -> s.griefSilverfish;
            case ENDER_DRAGON -> s.griefEnderDragon;
            case WITHER       -> s.griefWither;
            default -> false;
        };
        if (cancel) e.setCancelled(true);
    }

    // ------------------------------------------------------------------------
    // Enderman teleport denial inside claims
    // ------------------------------------------------------------------------

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent e) {
        if (e.getEntityType() != EntityType.ENDERMAN) return;
        WorldProtectionSettings s = settingsFor(e.getTo() != null ? e.getTo().getWorld() : null);
        if (!s.teleportEnderman) return;
        if (e.getTo() != null && plotManager.isClaimed(e.getTo())) {
            e.setCancelled(true);
        }
    }

    // ------------------------------------------------------------------------
    // Fire: spread, ignite, burn
    // ------------------------------------------------------------------------

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent e) {
        WorldProtectionSettings s = settingsFor(e.getBlock().getWorld());
        if (!s.fireSpread) return;
        if (e.getBlock().getType() == Material.FIRE || e.getNewState().getType() == Material.FIRE) {
            if (plotManager.isClaimed(e.getBlock().getLocation())) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        WorldProtectionSettings s = settingsFor(e.getBlock().getWorld());
        if (!plotManager.isClaimed(e.getBlock().getLocation())) return;

        BlockIgniteEvent.IgniteCause cause = e.getCause();
        switch (cause) {
            case FLINT_AND_STEEL -> {
                if (!s.igniteFlint) return; // not blocking this cause
                Player p = e.getPlayer();
                if (p != null && !isBypassing(p) && !plotManager.isTrustedOrOwner(p.getUniqueId(), e.getBlock().getLocation())) {
                    e.setCancelled(true);
                    p.sendMessage(prefix() + ChatColor.RED + "You cannot ignite fire here!");
                }
            }
            case LAVA      -> { if (s.igniteLava) e.setCancelled(true); }
            case LIGHTNING -> { if (s.igniteLightning) e.setCancelled(true); }
            case EXPLOSION -> { if (s.igniteExplosion) e.setCancelled(true); }
            case SPREAD    -> { if (s.igniteSpread) e.setCancelled(true); }
            default -> { /* leave others */ }
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
        WorldProtectionSettings s = settingsFor(e.getBlock().getWorld());
        if (!s.fireBurn) return;
        if (plotManager.isClaimed(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    // ------------------------------------------------------------------------
    // Buckets
    // ------------------------------------------------------------------------

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        WorldProtectionSettings s = settingsFor(e.getBlockClicked() != null ? e.getBlockClicked().getWorld() : null);
        if (!s.bucketEmpty) return;
        Block clicked = e.getBlockClicked();
        if (clicked == null) return;
        Block target = clicked.getRelative(e.getBlockFace());
        if (target == null) return;
        if (denyIfNeeded(e.getPlayer(), target.getLocation(), "You cannot pour here!")) e.setCancelled(true);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        WorldProtectionSettings s = settingsFor(e.getBlockClicked() != null ? e.getBlockClicked().getWorld() : null);
        if (!s.bucketFill) return;
        Block clicked = e.getBlockClicked();
        if (clicked == null) return;
        if (denyIfNeeded(e.getPlayer(), clicked.getLocation(), "You cannot take from here!")) e.setCancelled(true);
    }

    // ------------------------------------------------------------------------
    // Settings holder (global defaults or per-world)
    // ------------------------------------------------------------------------

    private static class WorldProtectionSettings {
        // Containers
        boolean containers = true;

        // PvP
        boolean pvpInClaims = false;

        // Explosions
        boolean mobGrief = true;
        boolean blockCreeper = true;
        boolean blockTnt = true;
        boolean blockWither = true;
        boolean blockWitherSkull = true;
        boolean blockEnderCrystal = true;
        boolean blockEnderDragon = true;

        // Fire
        boolean fireSpread = true;
        boolean fireBurn   = true;
        boolean igniteFlint = true;
        boolean igniteLava = true;
        boolean igniteLightning = true;
        boolean igniteExplosion = true;
        boolean igniteSpread = true;

        // Buckets
        boolean bucketEmpty = true;
        boolean bucketFill  = true;

        // Interactions
        enum Mode { WHITELIST, BLACKLIST }
        boolean interactionsEnabled = true;
        Mode interactionsMode = Mode.BLACKLIST;
        Set<String> interactionCategories = new HashSet<>(Arrays.asList(
                "doors","trapdoors","fence_gates","buttons","levers","pressure_plates"
        ));
        Set<Material> interactionList = new HashSet<>();

        // Entity grief (non-explosion)
        boolean griefEnderman = true;
        boolean griefRavager = true;
        boolean griefSilverfish = true;
        boolean griefEnderDragon = true;
        boolean griefWither = true;

        // Teleport
        boolean teleportEnderman = true;

        static WorldProtectionSettings fromConfig(ConfigurationSection sec) {
            WorldProtectionSettings s = new WorldProtectionSettings();
            if (sec == null) return s;

            s.containers   = sec.getBoolean("containers", s.containers);
            s.pvpInClaims  = sec.getBoolean("pvp-in-claims", s.pvpInClaims);

            s.mobGrief          = sec.getBoolean("mob-grief", s.mobGrief);
            s.blockCreeper      = sec.getBoolean("creeper-explosions", s.blockCreeper);
            s.blockTnt          = sec.getBoolean("tnt-explosions", s.blockTnt);
            s.blockWither       = sec.getBoolean("wither-explosions", s.blockWither);
            s.blockWitherSkull  = sec.getBoolean("wither-skull-explosions", s.blockWitherSkull);
            s.blockEnderCrystal = sec.getBoolean("ender-crystal-explosions", s.blockEnderCrystal);
            s.blockEnderDragon  = sec.getBoolean("ender-dragon-explosions", s.blockEnderDragon);

            ConfigurationSection fire = sec.getConfigurationSection("fire");
            if (fire != null) {
                s.fireSpread = fire.getBoolean("spread", s.fireSpread);
                s.fireBurn   = fire.getBoolean("burn", s.fireBurn);
                ConfigurationSection ign = fire.getConfigurationSection("ignite");
                if (ign != null) {
                    s.igniteFlint     = ign.getBoolean("flint_and_steel", s.igniteFlint);
                    s.igniteLava      = ign.getBoolean("lava", s.igniteLava);
                    s.igniteLightning = ign.getBoolean("lightning", s.igniteLightning);
                    s.igniteExplosion = ign.getBoolean("explosion", s.igniteExplosion);
                    s.igniteSpread    = ign.getBoolean("spread", s.igniteSpread);
                }
            }

            ConfigurationSection buckets = sec.getConfigurationSection("buckets");
            if (buckets != null) {
                s.bucketEmpty = buckets.getBoolean("block-empty", s.bucketEmpty);
                s.bucketFill  = buckets.getBoolean("block-fill",  s.bucketFill);
            }

            ConfigurationSection inter = sec.getConfigurationSection("interactions");
            if (inter != null) {
                s.interactionsEnabled = inter.getBoolean("enabled", s.interactionsEnabled);
                String mode = inter.getString("mode", "blacklist").trim().toUpperCase(Locale.ROOT);
                s.interactionsMode = "WHITELIST".equals(mode) ? Mode.WHITELIST : Mode.BLACKLIST;

                s.interactionCategories = new HashSet<>();
                List<String> cats = inter.getStringList("categories");
                if (cats != null) for (String c : cats)
                    if (c != null) s.interactionCategories.add(c.trim().toLowerCase(Locale.ROOT));

                s.interactionList = new HashSet<>();
                List<String> list = inter.getStringList("list");
                if (list != null) for (String n : list) {
                    if (n == null) continue;
                    try { s.interactionList.add(Material.valueOf(n.trim().toUpperCase(Locale.ROOT))); }
                    catch (IllegalArgumentException ignored) { }
                }
            }

            ConfigurationSection eg = sec.getConfigurationSection("entity-grief");
            if (eg != null) {
                s.griefEnderman   = eg.getBoolean("enderman", s.griefEnderman);
                s.griefRavager    = eg.getBoolean("ravager", s.griefRavager);
                s.griefSilverfish = eg.getBoolean("silverfish", s.griefSilverfish);
                s.griefEnderDragon= eg.getBoolean("ender-dragon", s.griefEnderDragon);
                s.griefWither     = eg.getBoolean("wither", s.griefWither);
            }

            ConfigurationSection tp = sec.getConfigurationSection("entity-teleport");
            if (tp != null) {
                s.teleportEnderman = tp.getBoolean("enderman", s.teleportEnderman);
            }

            return s;
        }

        static WorldProtectionSettings merge(WorldProtectionSettings base, WorldProtectionSettings over) {
            WorldProtectionSettings r = new WorldProtectionSettings();

            // Booleans: choose override values
            r.containers = over.containers;
            r.pvpInClaims = over.pvpInClaims;

            r.mobGrief = over.mobGrief;
            r.blockCreeper = over.blockCreeper;
            r.blockTnt = over.blockTnt;
            r.blockWither = over.blockWither;
            r.blockWitherSkull = over.blockWitherSkull;
            r.blockEnderCrystal = over.blockEnderCrystal;
            r.blockEnderDragon = over.blockEnderDragon;

            r.fireSpread = over.fireSpread;
            r.fireBurn = over.fireBurn;
            r.igniteFlint = over.igniteFlint;
            r.igniteLava = over.igniteLava;
            r.igniteLightning = over.igniteLightning;
            r.igniteExplosion = over.igniteExplosion;
            r.igniteSpread = over.igniteSpread;

            r.bucketEmpty = over.bucketEmpty;
            r.bucketFill = over.bucketFill;

            r.interactionsEnabled = over.interactionsEnabled;
            r.interactionsMode = over.interactionsMode;
            r.interactionCategories = new HashSet<>(over.interactionCategories);
            r.interactionList = new HashSet<>(over.interactionList);

            r.griefEnderman = over.griefEnderman;
            r.griefRavager = over.griefRavager;
            r.griefSilverfish = over.griefSilverfish;
            r.griefEnderDragon = over.griefEnderDragon;
            r.griefWither = over.griefWither;

            r.teleportEnderman = over.teleportEnderman;

            // If someone wants a “partial” override in the future, we can add tri-state;
            // for now we treat per-world section as an explicit full set (sane defaults applied before).
            return r;
        }

        boolean matchesInteraction(Material m) {
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
    }
}
