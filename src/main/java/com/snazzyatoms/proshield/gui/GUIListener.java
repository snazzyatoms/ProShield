package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Unified GUIListener (config-driven)
 *
 * Reads slot → action mapping from config.yml (gui.*.slots)
 * so menu layout can be changed without editing code.
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final Map<String, Map<Integer, String>> slotMappings = new HashMap<>();

    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        loadMappings();
    }

    private void loadMappings() {
        slotMappings.clear();
        ConfigurationSection guiSection = plugin.getConfig().getConfigurationSection("gui");
        if (guiSection == null) return;

        for (String menu : guiSection.getKeys(false)) {
            ConfigurationSection slots = guiSection.getConfigurationSection(menu + ".slots");
            if (slots == null) continue;

            Map<Integer, String> map = new HashMap<>();
            for (String key : slots.getKeys(false)) {
                int slot = slots.getInt(key, -1);
                if (slot >= 0) {
                    map.put(slot, key.toLowerCase());
                }
            }
            slotMappings.put(menu.toLowerCase(), map);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (title == null) return;

        // Cancel movement in ProShield GUIs
        if (title.contains("ProShield") || title.contains("Claim") || title.contains("Roles")) {
            event.setCancelled(true);
        }

        // Find mapping by menu type
        String menuType = title.toLowerCase().contains("admin") ? "admin" : "main";
        Map<Integer, String> map = slotMappings.get(menuType);
        if (map == null) return;

        String action = map.get(event.getSlot());
        if (action == null) return;

        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());

        // Handle actions
        switch (action) {
            case "claim" -> plugin.getCommand("claim").execute(player, "claim", new String[0]);
            case "unclaim" -> plugin.getCommand("unclaim").execute(player, "unclaim", new String[0]);
            case "info" -> guiManager.openInfoMenu(player, plot);
            case "trust" -> guiManager.openTrustMenu(player, menuType.equals("admin"));
            case "untrust" -> guiManager.openUntrustMenu(player, menuType.equals("admin"));
            case "roles" -> guiManager.openRolesGUI(player, plot, menuType.equals("admin"));
            case "flags" -> guiManager.openFlagsMenu(player, menuType.equals("admin"));
            case "back" -> guiManager.openMain(player);
        }
    }
}
