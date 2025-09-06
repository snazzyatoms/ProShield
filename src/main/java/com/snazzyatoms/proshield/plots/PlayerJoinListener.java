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
import org.bukkit.event.block.BlockPlaceEvent;
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
 *  - protection.containers
 *  - protection.pvp-in-claims
 *  - protection.mob-grief, creeper-explosions, tnt-explosions
 *  - protection.interactions.{enabled,mode,categories,list}
 *  - protection.buckets.{block-empty,block-fill}
 *
 * Supports live reload via reloadInteractionConfig().
 */
public class BlockProtectionListener implements Listener {

    private final PlotManager plotManager;

    // Cached config flags
    private boolean protectContainers = true;
    private boolean pvpInClaims = false;
    private boolean preventMobGrief = true;
    private boolean blockCreeper = true;
    private boolean blockTnt = true;

    private boolean bucketsBlockEmpty = true;
    private boolean bucketsBlockFill = true;

    // Interaction config cache
    private boolean interactionsEnabled = true;
    private Mode interactionsMode = Mode.BLACKLIST;
    private final Set<String> interactionCategories = new HashSet<>(); // doors, trapdoors, fence_gates, buttons, levers, pressure_plates
    private final Set<Material> interactionList = new HashSet<>();

    private enum Mode { WHITELIST, BLACKLIST }

    public BlockProtectionListener(PlotManager plotManager) {
        this.plotManager = plotManager;
        reloadInteractionConfig(); // also loads all base flags
    }

    /**
     * Called from ProShield.reloadAllConfigs() to re-read config without re-registering listeners.
     */
    public void reloadInteractionConfig() {
        var cfg = ProShield.getInstance().getConfig();

        // Base toggles
        protectContainers = cfg.getBoolean("protection.containers", true);
        pvpInClaims = cfg.getBoolean("protection.pvp-in-claims", false);
        preventMobGrief = cfg.getBoolean("protection.mob-grief", true);
        blockCreeper = cfg.getBoolean("protection.creeper-explosions", true);
        blockTnt = cfg.getBoolean("protection.tnt-explosions", true);

        bucketsBlockEmpty = cfg.getBoolean("protection.buckets.block-empty", true);
        bucketsBlockFill = cfg.getBoolean("protection.buckets.block-fill", true);

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
            for (String c : cats) {
                if (c != null) interactionCategories.add(c.trim().toLowerCase(Locale.ROOT));
            }
        }

        interactionList.clear();
        List<String> list = sec.getStringList("list");
        if (list != null) {
            for (String n : list) {
                if (n == null) continue;
                try {
                    interactionList.add(Material.valueOf(n.trim().toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException ignored) { /* skip invalid material */ }
            }
        }
    }

    // ===== Common helpers =====

    private boolean isBypassing(Player p) {
        return p.hasMetadata("proshield_bypass");
    }

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
        // ignore off-hand to avoid double trigger
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;

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

        // Non-container interactables based on categories/list
        if (!interactionsEnabled) return;

        Material type = b.getType();
        boolean matches = matchesConfigured(type);
        // BLACKLIST: deny when matches; WHITELIST: deny when NOT matches
        boolean shouldDeny = (interactionsMode == Mode.WHITELIST) ? !matches : matches;

        if (shouldDeny) {
            if (denyIfNeeded(e.getPlayer(), b.getLocation(), "You cannot interact here!")) {
                e.setCancelled(true);
            }
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
        if (pvpInClaims) return; // if true, allow PvP
        if (e.getDamager() instanceof Player damager && e.getEntity() instanceof Player) {
            if (isBypassing(damager)) return;
            Location loc = e.getEntity().getLocation();
            if (plotManager.isClaimed(loc)) e.setCancelled(true);
        }
    }

    // ===== Explosions: creeper/TNT blocked inside claimed chunks =====

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if (!preventMobGrief) return;

        EntityType t = e.getEntityType();
        if ((t == EntityType.CREEPER && blockCreeper) || (t == EntityType.PRIMED_TNT && blockTnt)) {
            e.blockList().removeIf(block -> plotManager.isClaimed(block.getLocation()));
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
