// src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    // Recommended constructor: plugin only
    public GUIListener(ProShield plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
    }

    // Legacy fallback (safe)
    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager != null ? guiManager : plugin.getGuiManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String title = event.getView().getTitle();
        if (title == null) return;

        // Normalize for easier checks
        String lowerTitle = title.toLowerCase(Locale.ROOT);

        // Only handle our plugin menus
        if (!(lowerTitle.contains("proshield")
                || lowerTitle.contains("trusted players")
                || lowerTitle.contains("assign role")
                || lowerTitle.contains("claim flags")
                || lowerTitle.contains("admin tools")
                || lowerTitle.contains("expansion requests")
                || lowerTitle.contains("expansion history")
                || lowerTitle.contains("request expansion")
                || lowerTitle.contains("deny reasons")
                || lowerTitle.contains("world controls"))) {
            return;
        }

        // Prevent vanilla item movement inside GUIs
        event.setCancelled(true);

        // === Menu routing ===
        if (lowerTitle.startsWith("proshield menu")) {
            guiManager.handleMainClick(player, event);

        } else if (lowerTitle.startsWith("trusted players")) {
            guiManager.handleTrustedClick(player, event);

        } else if (lowerTitle.startsWith("assign role")) {
            guiManager.handleAssignRoleClick(player, event);

        } else if (lowerTitle.startsWith("claim flags")) {
            guiManager.handleFlagsClick(player, event);

        } else if (lowerTitle.startsWith("admin tools")) {
            guiManager.handleAdminClick(player, event);

        } else if (lowerTitle.startsWith("world controls")) {
            guiManager.handleWorldControlsClick(player, event);

        } else if (lowerTitle.startsWith("deny reasons")) {
            guiManager.handleDenyReasonClick(player, event);

        } else if (lowerTitle.startsWith("request expansion")) {
            // ✅ Player’s Request Expansion menu
            guiManager.handlePlayerExpansionRequestClick(player, event);

        } else if (lowerTitle.startsWith("expansion requests")) {
            // ✅ Admin’s Expansion Review menu
            guiManager.handleExpansionReviewClick(player, event);

        } else if (lowerTitle.startsWith("expansion history")) {
            // ✅ Pagination titles like "Expansion History (Page 1)"
            guiManager.handleHistoryClick(player, event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (title == null) return;

        String lowerTitle = title.toLowerCase(Locale.ROOT);

        // Clean up pending state when menus close
        if (lowerTitle.startsWith("assign role")) {
            guiManager.clearPendingRoleAssignment(player.getUniqueId());

        } else if (lowerTitle.startsWith("deny reasons")) {
            guiManager.clearPendingDenyTarget(player.getUniqueId());
        }
    }
}
