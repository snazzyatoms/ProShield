// src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final NamespacedKey menuKeyTag;
    private final NamespacedKey targetNameTag;

    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.menuKeyTag = new NamespacedKey(plugin, "menuKey");
        this.targetNameTag = new NamespacedKey(plugin, "targetName");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        event.setCancelled(true); // Prevent taking/moving items
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        String menuTag = pdc.get(menuKeyTag, PersistentDataType.STRING);
        String targetName = pdc.get(targetNameTag, PersistentDataType.STRING);

        // ======================================================
        // 1. Handle Role Editor (dynamic per-player perms)
        // ======================================================
        if ("role-editor".equalsIgnoreCase(menuTag) && targetName != null) {
            Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
            if (plot == null) return;

            String key = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();
            ClaimRoleManager roleManager = plugin.getRoleManager();

            if ("close".equalsIgnoreCase(key)) {
                player.closeInventory();
                playClick(player);
                return;
            }

            if ("back".equalsIgnoreCase(key)) {
                playClick(player);
                Bukkit.getScheduler().runTask(plugin, () -> guiManager.openMenu(player, "untrust"));
                return;
            }

            if (roleManager.getPermissions(plot.getId(), targetName).containsKey(key)) {
                boolean current = roleManager.getPermissions(plot.getId(), targetName).getOrDefault(key, false);
                roleManager.setPermission(plot.getId(), targetName, key, !current);

                if (plugin.getConfig().getBoolean("messages.admin-flag-chat", true)) {
                    player.sendMessage(ChatColor.YELLOW + "Toggled " + key + " for " + targetName
                            + " → " + (!current ? ChatColor.GREEN + "Allowed" : ChatColor.RED + "Denied"));
                }
                playClick(player);

                Bukkit.getScheduler().runTask(plugin, () -> guiManager.openRoleEditor(player, targetName));
            }
            return;
        }

        // ======================================================
        // 2. Handle Untrust menu → open Role Editor
        // ======================================================
        if ("untrust".equalsIgnoreCase(menuTag) && targetName != null) {
            playClick(player);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.openRoleEditor(player, targetName));
            return;
        }

        // ======================================================
        // 3. Handle static/config-driven menus
        // ======================================================
        if (menuTag != null && menuTag.startsWith("static:")) {
            ConfigurationSection items = plugin.getConfig()
                    .getConfigurationSection("gui.menus." + menuTag.substring(7) + ".items");
            if (items == null) return;

            ConfigurationSection itemSec = items.getConfigurationSection(String.valueOf(event.getRawSlot()));
            if (itemSec == null) return;

            String action = itemSec.getString("action", "").trim();
            if (action.isEmpty()) return;

            String lower = action.toLowerCase();

            if (lower.startsWith("command:")) {
                String cmd = action.substring("command:".length()).trim();

                if (cmd.equalsIgnoreCase("proshield reload")) {
                    if (player.isOp() || player.hasPermission("proshield.admin.reload")) {
                        plugin.reloadConfig();
                        playAdminAction(player);
                        player.sendMessage(ChatColor.GREEN + "ProShield config reloaded!");
                    }
                    return;
                }

                if (!cmd.isEmpty()) {
                    playClick(player);
                    Bukkit.dispatchCommand(player, cmd);
                }

            } else if (lower.startsWith("menu:")) {
                String targetMenu = action.substring("menu:".length()).trim();
                if (!targetMenu.isEmpty()) {
                    playClick(player);
                    guiManager.openMenu(player, targetMenu);
                }

            } else if (lower.equals("close")) {
                playClick(player);
                player.closeInventory();

            } else {
                plugin.getLogger().warning("[GUI] Unknown action in " + menuTag + " slot "
                        + event.getRawSlot() + ": " + action);
            }
        }
    }

    private void playClick(Player player) {
        String sound = plugin.getConfig().getString("sounds.button-click", "UI_BUTTON_CLICK");
        try {
            player.playSound(player.getLocation(), sound, 1f, 1f);
        } catch (Exception ignored) {}
    }

    private void playAdminAction(Player player) {
        String sound = plugin.getConfig().getString("sounds.admin-action", "ENTITY_EXPERIENCE_ORB_PICKUP");
        try {
            player.playSound(player.getLocation(), sound, 1f, 1f);
        } catch (Exception ignored) {}
    }
}
