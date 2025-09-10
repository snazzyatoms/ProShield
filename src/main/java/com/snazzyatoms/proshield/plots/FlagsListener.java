package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUICache;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;

/**
 * Handles toggles in the Claim Flags submenu (per-claim overrides).
 */
public class FlagsListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final GUICache guiCache;

    public FlagsListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager, GUICache guiCache) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.guiCache = guiCache;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlagMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory inv = event.getInventory();

        // Only handle ProShield GUIs
        if (!guiCache.isProShieldGUI(inv)) return;

        String title = ChatColor.stripColor(inv.getTitle());
        if (!"Claim Flags".equalsIgnoreCase(title)) return; // Only handle flags GUI

        event.setCancelled(true);

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) {
            player.sendMessage(plugin.getPrefix() + "§cYou must be inside your claim to edit flags.");
            return;
        }

        // Only owners and co-owners can toggle flags
        ClaimRole role = roleManager.getRole(plot, player);
        if (!(role == ClaimRole.OWNER || role == ClaimRole.COOWNER)) {
            player.sendMessage(plugin.getPrefix() + "§cYou do not have permission to edit flags here.");
            return;
        }

        int slot = event.getRawSlot();
        Map<String, Integer> slots = plugin.getConfig().getConfigurationSection("gui.slots.flags").getValues(false)
                .entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, e -> (Integer) e.getValue()));

        // PvP toggle
        if (slot == slots.get("pvp")) {
            boolean newValue = !plot.isFlagEnabled("pvp");
            plot.setFlag("pvp", newValue);
            player.sendMessage(plugin.getPrefix() + "§ePvP inside this claim is now: " +
                    (newValue ? "§aENABLED" : "§cDISABLED"));
            return;
        }

        // Explosions toggle
        if (slot == slots.get("explosions")) {
            boolean newValue = !plot.isFlagEnabled("explosions");
            plot.setFlag("explosions", newValue);
            player.sendMessage(plugin.getPrefix() + "§eExplosions inside this claim are now: " +
                    (newValue ? "§aALLOWED" : "§cBLOCKED"));
            return;
        }

        // Fire toggle
        if (slot == slots.get("fire")) {
            boolean newValue = !plot.isFlagEnabled("fire");
            plot.setFlag("fire", newValue);
            player.sendMessage(plugin.getPrefix() + "§eFire spread inside this claim is now: " +
                    (newValue ? "§aALLOWED" : "§cBLOCKED"));
            return;
        }

        // Keep Items toggle
        if (slot == slots.get("keep-items")) {
            if (!plugin.getConfig().getBoolean("claims.keep-items.allow-per-claim-toggle", true)) {
                player.sendMessage(plugin.getPrefix() + "§cPer-claim item persistence is disabled globally.");
                return;
            }
            boolean newValue = !plot.isFlagEnabled("keep-items");
            plot.setFlag("keep-items", newValue);
            player.sendMessage(plugin.getPrefix() + "§eKeep-drops inside this claim is now: " +
                    (newValue ? "§aENABLED" : "§cDISABLED"));
        }

        // Back button
        if (slot == slots.get("back")) {
            guiCache.openMainMenu(player, false);
        }
    }
}
