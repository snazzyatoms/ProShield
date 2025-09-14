// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GUIManager {

    private final ProShield plugin;
    private static final Map<UUID, ExpansionRequest> awaitingReason = new HashMap<>();

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    // ------------------------
    // Awaiting reason helpers
    // ------------------------
    public static boolean isAwaitingReason(Player player) {
        return awaitingReason.containsKey(player.getUniqueId());
    }

    public static void cancelAwaiting(Player player) {
        awaitingReason.remove(player.getUniqueId());
    }

    /** Called by AdminGUIListener when admin picks "Other" */
    public static void startAwaitingReason(Player admin, ExpansionRequest req) {
        awaitingReason.put(admin.getUniqueId(), req);
    }

    /** Called by ChatListener once admin types a reason */
    public static void provideManualReason(Player admin, String reason, ProShield plugin) {
        ExpansionRequest req = awaitingReason.remove(admin.getUniqueId());
        if (req == null) {
            plugin.getMessagesUtil().send(admin, "&7No pending request to deny.");
            return;
        }

        // Here you could also update ExpansionQueue/RequestManager to mark denied
        Player target = Bukkit.getPlayer(req.getPlayerId());
        if (target != null) {
            target.sendMessage(ChatColor.RED + "Your expansion request was denied: " + reason);
        }

        plugin.getMessagesUtil().send(admin, "&cRequest denied (" + reason + ").");
    }

    // ------------------------
    // Menus
    // ------------------------
    public void openMenu(Player player, String menuName) {
        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus." + menuName);
        if (menuSec == null) {
            plugin.getLogger().warning("Menu not found in config: " + menuName);
            return;
        }

        String title = ChatColor.translateAlternateColorCodes('&', menuSec.getString("title", "Menu"));
        int size = menuSec.getInt("size", 27);
        Inventory inv = Bukkit.createInventory(null, size, title);

        ConfigurationSection itemsSec = menuSec.getConfigurationSection("items");
        if (itemsSec != null) {
            for (Map.Entry<String, Object> entry : itemsSec.getValues(false).entrySet()) {
                String slotStr = entry.getKey();
                ConfigurationSection itemSec = itemsSec.getConfigurationSection(slotStr);
                if (itemSec == null) continue;

                int slot = Integer.parseInt(slotStr);
                Material mat = Material.matchMaterial(itemSec.getString("material", "STONE"));
                if (mat == null) mat = Material.STONE;

                ItemStack stack = new ItemStack(mat);
                ItemMeta meta = stack.getItemMeta();
                if (meta == null) continue;

                String name = itemSec.getString("name", "");
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

                List<String> lore = itemSec.getStringList("lore");
                if (lore != null && !lore.isEmpty()) {
                    lore.replaceAll(line -> ChatColor.translateAlternateColorCodes('&', line));
                    meta.setLore(lore);
                }

                stack.setItemMeta(meta);
                inv.setItem(slot, stack);
            }
        }

        player.openInventory(inv);
    }

    // ------------------------
    // Click forwarding
    // ------------------------
    public void handleClick(InventoryClickEvent event) {
        // delegate only — AdminGUIListener handles expansion buttons
    }
}
