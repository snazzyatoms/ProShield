// path: src/main/java/com/snazzyatoms/proshield/plots/BlockProtectionListener.java
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
    private Set<String> categorySet = new HashSet<>();  // lower-case keys e.g. "doors"
    private Set<Material> listSet = new HashSet<>();

    private enum Mode { WHITELIST, BLACKLIST }

    public BlockProtectionListener(PlotManager plotManager) {
        this.plotManager = plotManager;
        reloadInteractionConfig();
    }

    /** Re-reads interaction settings from config (call after /reload if you like). */
    public void reloadInteractionConfig() {
        var cfg = ProShield.getInstance().getConfig();
        ConfigurationSection sec = cfg.getConfigurationSection("protection.interactions");

        // defaults if section missing
        if (sec == null) {
            interactEnabled = true;
            interactMode = Mode.BLACKLIST;
            categorySet.clear();
            // sensible defaults for blacklist mode
            categorySet.add("doors");
            categorySet.add("trapdoors");
            categorySet.add("fence_gates");
            categorySet.add("buttons");
            categorySet.add("levers");
            categorySet.add("pressure_plates");
            listSet.clear();
            // optional extras (none by default)
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
            try {
                listSet.add(Material.valueOf(n.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) { /* ignore bad material names */ }
        }
    }

    private boolean isBypassing(Player p) {
        return p.hasMetadata("proshield_bypass");
    }

    private boolean denyIfNeeded(Player p, Location loc, String msg) {
        // Allow admins in bypass mode
        if (isBypassing(p)) return false;

        // If not claimed, allow
        if (!plotManager.isClaimed(loc)) return false;

        // Owner/trusted allowed
        if (plotManager.isTrustedOrOwner(p.getUniqueId(), loc)) return false;

        // Otherwise deny
        p.sendMessage(ChatColor.RED + msg);
        return true;
    }

    // ========== Block break/place ==========
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

    // ========== Interactions ==========
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        Block b = e.getClickedBlock();
        if (b == null) return;

        // Containers (chests/barrels/shulkers/etc.)
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

        // Non-container interactive blocks (doors, buttons, etc.) via whitelist/blacklist
        if (!interactEnabled) return;

        Material type = b.getType();
        boolean matchesConfigured = matchesConfigured(type);

        boolean shouldDeny;
        if (interactMode == Mode.WHITELIST) {
            // only deny if block is NOT in whitelist
            shouldDeny = !matchesConfigured;
        } else {
            // blacklist: deny if block IS in blacklist
            shouldDeny = matchesConfigured;
        }

        if (shouldDeny) {
            if (denyIfNeeded(e.getPlayer(), b.getLocation(), "You cannot interact here!")) {
                e.setCancelled(true);
            }
        }
    }

    // Determines if a material matches any of the configured categories or explicit list
    private boolean matchesConfigured(Material m) {
        if (listSet.contains(m)) return true;

        String name = m.name();

        // Categories (pattern-based so we don't have to enumerate every wood type)
        if (categorySet.contains("doors") && name.endsWith("_DOOR")) return true;
        if (categorySet.contains("trapdoors") && name.endsWith("_TRAPDOOR")) return true;
        if (categorySet.contains("fence_gates") && name.endsWith("_FENCE_GATE")) return true;
        if (categorySet.contains("buttons") && name.endsWith("_BUTTON")) return true;
        if (categorySet.contains("pressure_plates") && name.endsWith("_PRESSURE_PLATE")) return true;
        if (categorySet.contains("levers") && name.equals("LEVER")) return true;

        // extra named categories
        if (categorySet.contains("bells") && name.equals("BELL")) return true;
        if (categorySet.contains("note_blocks") && name.equals("NOTE_BLOCK")) return true;
        if (categorySet.contains("lecterns") && name.equals("LECTERN")) return true;

        return false;
    }

    // ========== PvP toggle in claims ==========
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        boolean allowPvPInClaims = ProShield.getInstance().getConfig().getBoolean("protection.pvp-in-claims", false);
        if (allowPvPInClaims) return;

        if (e.getDamager() instanceof Player damager && e.getEntity() instanceof Player) {
            if (isBypassing(damager)) return;
            Location loc = e.getEntity().getLocation();
            if (plotManager.isClaimed(loc)) {
                e.setCancelled(true);
            }
        }
    }

    // ========== Explosion grief toggle ==========
    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        boolean preventGrief = ProShield.getInstance().getConfig().getBoolean("protection.mob-grief", true);
        if (!preventGrief) return;

        EntityType type = e.getEntityType();
        if (type == EntityType.CREEPER || type == EntityType.PRIMED_TNT) {
            e.blockList().removeIf(block -> plotManager.isClaimed(block.getLocation()));
        }
    }
}
