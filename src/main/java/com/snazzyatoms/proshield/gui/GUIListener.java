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

import java.util.Set;

public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final NamespacedKey menuKeyTag;
    private final NamespacedKey targetNameTag;

    // Allowed toggle keys (kept in sync with ROLE_PERMISSIONS in GUIManager)
    private static final Set<String> TOGGLE_KEYS = Set.of("build", "interact", "containers", "vehicles", "unclaim");

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
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        String menuTag = pdc.get(menuKeyTag, PersistentDataType.STRING);
        String targetName = pdc.get(targetNameTag, PersistentDataType.STRING);

        if (menuTag == null) return;

        // ======================================================
        // 1. Role Editor (per-player perms)
        // ======================================================
        if ("role-editor".equalsIgnoreCase(menuTag) && targetName != null) {
            Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
            if (plot == null) return;

            String key = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();
            ClaimRoleManager roleManager = plugin.getRoleManager();

            if (key.equals("close")) {
                player.closeInventory();
                playClick(player);
                return;
            }

            if (key.equals("back")) {
                playClick(player);
                Bukkit.getScheduler().runTask(plugin, () -> guiManager.openMenu(player, "untrust"));
                return;
            }

            if (TOGGLE_KEYS.contains(key)) {
                boolean current = roleManager.getPermissions(plot.getId(), targetName).getOrDefault(key, false);
                roleManager.setPermission(plot.getId(), targetName, key, !current);

                if (plugin.getConfig().getBoolean("messages.debug-prefix", false)) {
                    plugin.getLogger().info("[Debug] " + player.getName() + " toggled " + key +
                            " for " + targetName + " in claim " + plot.getId() + " → " + !current);
                }

                playClick(player);
                Bukkit.getScheduler().runTask(plugin, () -> guiManager.openRoleEditor(player, targetName));
            }
            return;
        }

        // ======================================================
        // 2. Untrust menu → open Role Editor
        // ======================================================
        if ("untrust".equalsIgnoreCase(menuTag) && targetName != null) {
            playClick(player);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.openRoleEditor(player, targetName));
            return;
        }

        // ======================================================
        // 3. Static/config-driven menus
        // ======================================================
        if (menuTag.startsWith("static:")) {
            String menuName = menuTag.substring(7);
            ConfigurationSection menu = plugin.getConfig().getConfigurationSection("gui.menus." + menuName);
            if (menu == null) return;

            ConfigurationSection items = menu.getConfigurationSection("items");
            if (items == null) return;

            ConfigurationSection item = items.getConfigurationSection(String.valueOf(event.getRawSlot()));
            if (item == null) return;

            String action = item.getString("action", "").trim();
            if (action.isEmpty()) return;

            handleAction(player, action);
            playClick(player);
        }
    }

    private void handleAction(Player player, String action) {
        String lower = action.toLowerCase();

        if (lower.startsWith("command:")) {
            String cmd = action.substring("command:".length()).trim();

            if (cmd.equalsIgnoreCase("proshield reload")) {
                if (player.isOp() || player.hasPermission("proshield.admin.reload")) {
                    plugin.reloadConfig();
                    String sound = plugin.getConfig().getString("sounds.admin-action", "ENTITY_EXPERIENCE_ORB_PICKUP");
                    try {
                        player.playSound(player.getLocation(), sound, 1f, 1f);
                    } catch (Exception ignored) {}
                }
                return;
            }

            if (!cmd.isEmpty()) Bukkit.dispatchCommand(player, cmd);

        } else if (lower.startsWith("menu:")) {
            String targetMenu = action.substring("menu:".length()).trim();
            if (!targetMenu.isEmpty()) guiManager.openMenu(player, targetMenu);

        } else if (lower.equals("close")) {
            player.closeInventory();

        } else {
            plugin.getLogger().warning("[GUI] Unknown action: " + action);
        }
    }

    private void playClick(Player player) {
        String sound = plugin.getConfig().getString("sounds.button-click", "UI_BUTTON_CLICK");
        try {
            player.playSound(player.getLocation(), sound, 1f, 1f);
        } catch (Exception ignored) {}
    }
}
