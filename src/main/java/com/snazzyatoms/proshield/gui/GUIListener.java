// src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    // Routing table for GUIs
    private final Map<String, BiConsumer<Player, InventoryClickEvent>> clickHandlers = new HashMap<>();

    public GUIListener(ProShield plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
        registerHandlers();
    }

    // Optional legacy constructor
    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = (guiManager != null ? guiManager : plugin.getGuiManager());
        registerHandlers();
    }

    /**
     * Fetches normalized GUI title from messages.yml (with fallback).
     */
    private String getGuiTitle(String key, String fallback) {
        String raw = plugin.getMessagesUtil().getOrDefault("messages.gui.titles." + key, fallback);
        if (raw == null || raw.isBlank()) raw = fallback;
        return ChatColor.stripColor(raw).toLowerCase(Locale.ROOT).trim();
    }

    private void registerHandlers() {
        clickHandlers.put(getGuiTitle("main", "proshield menu"), guiManager::handleMainClick);
        clickHandlers.put(getGuiTitle("trusted", "trusted players"), guiManager::handleTrustedClick);
        clickHandlers.put(getGuiTitle("assign-role", "assign role"), guiManager::handleAssignRoleClick);
        clickHandlers.put(getGuiTitle("flags", "claim flags"), guiManager::handleFlagsClick);
        clickHandlers.put(getGuiTitle("admin", "admin tools"), guiManager::handleAdminClick);
        clickHandlers.put(getGuiTitle("world-controls", "world controls"), guiManager::handleWorldControlsClick);
        clickHandlers.put(getGuiTitle("deny-reasons", "deny reasons"), guiManager::handleDenyReasonClick);
        clickHandlers.put(getGuiTitle("request-expansion", "request expansion"), guiManager::handlePlayerExpansionRequestClick);
        clickHandlers.put(getGuiTitle("expansion-requests", "expansion requests"), guiManager::handleExpansionReviewClick);
        clickHandlers.put(getGuiTitle("expansion-history", "expansion history"), guiManager::handleHistoryClick);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (guiManager == null) return; // safety guard

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String rawTitle = event.getView().getTitle();
        if (rawTitle == null || rawTitle.isBlank()) return;

        String cleanTitle = ChatColor.stripColor(rawTitle);
        if (cleanTitle == null || cleanTitle.isBlank()) return;

        String lowerTitle = cleanTitle.toLowerCase(Locale.ROOT).trim();

        // Route clicks only if title matches our handlers
        for (Map.Entry<String, BiConsumer<Player, InventoryClickEvent>> entry : clickHandlers.entrySet()) {
            if (lowerTitle.contains(entry.getKey())) {
                event.setCancelled(true); // block vanilla behavior

                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info("[GUIListener] " + player.getName()
                            + " clicked in " + cleanTitle
                            + " (slot " + event.getSlot() + ")");
                }

                try {
                    entry.getValue().accept(player, event);
                } catch (Exception ex) {
                    plugin.getLogger().warning("[GUIListener] Error handling click in " + cleanTitle + ": " + ex.getMessage());
                    ex.printStackTrace();
                }
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (guiManager == null) return; // safety guard

        String rawTitle = event.getView().getTitle();
        if (rawTitle == null || rawTitle.isBlank()) return;

        String cleanTitle = ChatColor.stripColor(rawTitle);
        if (cleanTitle == null || cleanTitle.isBlank()) return;

        String lowerTitle = cleanTitle.toLowerCase(Locale.ROOT).trim();
        UUID uuid = player.getUniqueId();

        if (lowerTitle.contains(getGuiTitle("assign-role", "assign role"))) {
            guiManager.clearPendingRoleAssignment(uuid);
            if (plugin.isDebugEnabled()) {
                plugin.getLogger().info("[GUIListener] Cleared pending role assignment for " + player.getName());
            }
        } else if (lowerTitle.contains(getGuiTitle("deny-reasons", "deny reasons"))) {
            guiManager.clearPendingDenyTarget(uuid);
            if (plugin.isDebugEnabled()) {
                plugin.getLogger().info("[GUIListener] Cleared pending deny target for " + player.getName());
            }
        }
    }
}
