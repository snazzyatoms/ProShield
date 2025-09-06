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

public class BlockProtectionListener implements Listener {

    private final PlotManager plotManager;

    // Interaction config cache
    private boolean interactEnabled = true;
    private Mode interactMode = Mode.BLACKLIST;
    private Set<String> categorySet = new HashSet<>();  // "doors", "buttons", ...
    private Set<Material> listSet = new HashSet<>();

    private enum Mode { WHITELIST, BLACKLIST }

    public BlockProtectionListener(PlotManager plotManager) {
        this.plotManager = plotManager;
        reloadInteractionConfig();
    }

    public void reloadInteractionConfig() {
        var cfg = ProShield.getInstance().getConfig();
        ConfigurationSection sec = cfg.getConfigurationSection("protection.interactions");

        if (sec == null) {
            interactEnabled = true;
            interactMode = Mode.BLACKLIST;
            categorySet.clear();
            categorySet.add("doors");
            categorySet.add("trapdoors");
            categorySet.add("fence_gates");
            categorySet.add("buttons");
            categorySet.add("levers");
            categorySet.add("pressure_plates");
            listSet.clear();
            return;
        }

        interactEnabled = sec.getBoolean("enabled", true);
        String modeStr = sec.getString("mode", "blacklist").toUpperCase(Locale.ROOT);
        interactMode = "WHITELIST".equals(modeStr) ? Mode.WHITELIST : Mode.BLACKLIST;

        categorySet.clear();
        for (String cat : sec.getStringList("categories")) {
            if (cat != null) categorySet.add(cat.trim().toLowerCase(Locale.ROOT));
        }

        listSet.clear();
        List<String> names = sec.getStringList("list");
        for (String n : names) {
            if (n == null) continue;
            try { listSet.add(Material.valueOf(n.trim().toUpperCase(Locale.ROOT))); }
            catch (IllegalArgumentException ignored) {}
        }
    }

    private boolean isBypassing(Player p) { return p.hasMetadata("proshield_bypass"); }

    private boolean denyIfNeeded(Player p, Location loc, String msg) {
        if (isBypassing(p)) return false;
        if (!plotManager.isClaimed(loc)) return false;
        if (plotManager.isTrustedOrOwner(p.getUniqueId(), loc)) return false;
        p.sendMessage(ChatColor.RED + msg);
        return true;
    }

    // Place/Break
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (denyIfNeeded(e.getPlayer(), e.getBlock().getLocation(), "You cannot break blocks here!")) e.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (denyIfNeeded(e.getPlayer(), e.getBlock().getLocation(), "You cannot place blocks here!")) e.setCancelled(true);
    }

    // Interactions (containers + doors/buttons/etc.)
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        Block b = e.getClickedBlock();
        if (b == null) return;

        // Containers
        boolean protectContainers = ProShield.getInstance().getConfig().getBoolean("protection.containers", true);
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
        if (!interactEnabled) return;
        Material type = b.getType();
        boolean matches = matchesConfigured(type);
        boolean shouldDeny = (interactMode == Mode.WHITELIST) ? !matches : matches;

        if (shouldDeny) {
            if (denyIfNeeded(e.getPlayer(), b.getLocation(), "You cannot interact here!")) e.setCancelled(true);
        }
    }

    private boolean matchesConfigured(Material m) {
        if (listSet.contains(m)) return true;
        String name = m.name();
        if (categorySet.contains("doors") && name.endsWith("_DOOR")) return true;
        if (categorySet.contains("trapdoors") && name.endsWith("_TRAPDOOR")) return true;
        if (categorySet.contains("fence_gates") && name.endsWith("_FENCE_GATE")) return true;
        if (categorySet.contains("buttons") && name.endsWith("_BUTTON")) return true;
        if (categorySet.contains("pressure_plates") && name.endsWith("_PRESSURE_PLATE")) return true;
        if (categorySet.contains("levers") && name.equals("LEVER")) return true;
        return false;
    }

    // PvP
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        boolean allowPvPInClaims = ProShield.getInstance().getConfig().getBoolean("protection.pvp-in-claims", false);
        if (allowPvPInClaims) return;
        if (e.getDamager() instanceof Player damager && e.getEntity() instanceof Player) {
            if (isBypassing(damager)) return;
            Location loc = e.getEntity().getLocation();
            if (plotManager.isClaimed(loc)) e.setCancelled(true);
        }
    }

    // Explosions
    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        var cfg = ProShield.getInstance().getConfig();
        boolean preventGrief = cfg.getBoolean("protection.mob-grief", true);
        if (!preventGrief) return;

        EntityType t = e.getEntityType();
        boolean blockCreeper = cfg.getBoolean("protection.creeper-explosions", true);
        boolean blockTnt = cfg.getBoolean("protection.tnt-explosions", true);

        if ((t == EntityType.CREEPER && blockCreeper) || (t == EntityType.PRIMED_TNT && blockTnt)) {
            e.blockList().removeIf(block -> plotManager.isClaimed(block.getLocation()));
        }
    }

    // Buckets
    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (!ProShield.getInstance().getConfig().getBoolean("protection.buckets.block-empty", true)) return;
        Block target = e.getBlockClicked() != null ? e.getBlockClicked().getRelative(e.getBlockFace()) : null;
        if (target == null) return;
        if (denyIfNeeded(e.getPlayer(), target.getLocation(), "You cannot pour here!")) e.setCancelled(true);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (!ProShield.getInstance().getConfig().getBoolean("protection.buckets.block-fill", true)) return;
        Block target = e.getBlockClicked();
        if (target == null) return;
        if (denyIfNeeded(e.getPlayer(), target.getLocation(), "You cannot take from here!")) e.setCancelled(true);
    }
}
