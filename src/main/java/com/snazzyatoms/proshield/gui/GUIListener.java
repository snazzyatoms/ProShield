package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        event.setCancelled(true); // no item movement
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        String menuTag = pdc.get(menuKeyTag, PersistentDataType.STRING);
        String targetName = pdc.get(targetNameTag, PersistentDataType.STRING);

        // Role editor (toggle per-player perms)
        if ("role-editor".equalsIgnoreCase(menuTag) && targetName != null) {
            Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
            if (plot == null) return;

            String key = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();
            ClaimRoleManager roleManager = plugin.getRoleManager();

            if (key.equalsIgnoreCase("back")) {
                Bukkit.getScheduler().runTask(plugin, () -> guiManager.openMenu(player, "untrust"));
                return;
            }
            if (key.equalsIgnoreCase("close")) {
                player.closeInventory();
                return;
            }

            // Toggle only known keys
            if (roleManager.getPermissions(plot.getId(), targetName).containsKey(key)) {
                boolean current = roleManager.getPermissions(plot.getId(), targetName).getOrDefault(key, false);
                roleManager.setPermission(plot.getId(), targetName, key, !current);
                Bukkit.getScheduler().runTask(plugin, () -> guiManager.openRoleEditor(player, targetName));
            }
            return;
        }

        // Trust menu: clicking a head trusts that player
        if ("trust".equalsIgnoreCase(menuTag) && targetName != null) {
            player.performCommand("trust " + targetName);
            // Open role editor immediately for convenience
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.openRoleEditor(player, targetName));
            return;
        }

        // Untrust menu: clicking a head opens role editor (from there they can untrust via /untrust if desired)
        if ("untrust".equalsIgnoreCase(menuTag) && targetName != null) {
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.openRoleEditor(player, targetName));
            return;
        }

        // Static/config-driven menus
        if (menuTag != null && menuTag.startsWith("static:")) {
            String menuName = menuTag.substring("static:".length());
            ConfigurationSection itemSec = plugin.getConfig()
                .getConfigurationSection("gui.menus." + menuName + ".items." + event.getRawSlot());
            if (itemSec == null) return;

            String action = itemSec.getString("action", "").trim();
            if (action.isEmpty()) return;

            String lower = action.toLowerCase();

            if (lower.startsWith("command:")) {
                String cmd = action.substring("command:".length()).trim();
                if (!cmd.isEmpty()) {
                    // All "proshield flag X" routes hit ProShieldCommand.toggleFlag()
                    player.performCommand(cmd);
                }
            } else if (lower.startsWith("menu:")) {
                String targetMenu = action.substring("menu:".length()).trim();
                if (!targetMenu.isEmpty()) guiManager.openMenu(player, targetMenu);
            } else if (lower.equals("close")) {
                player.closeInventory();
            } else {
                plugin.getLogger().warning("[GUI] Unknown action in " + menuTag + " slot " + event.getRawSlot() + ": " + action);
            }
        }
    }
}
