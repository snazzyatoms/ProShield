package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

/**
 * Central GUI Manager for ProShield
 * Handles opening menus, tracking chat input for reasons/roles,
 * and processing inventory click events.
 */
public class GUIManager {

    private final ProShield plugin;
    private final MessagesUtil messages;
    private final ClaimRoleManager roleManager;

    // Chat-input trackers
    private final Map<UUID, Boolean> awaitingReason = new HashMap<>();
    private final Map<UUID, Boolean> awaitingRoleAction = new HashMap<>();

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
        this.roleManager = plugin.getRoleManager();
    }

    // ---------------------------
    // GUI Menu Handling
    // ---------------------------
    public void openMenu(Player player, String menuKey) {
        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection menuSec = cfg.getConfigurationSection("gui.menus." + menuKey);
        if (menuSec == null) {
            player.sendMessage(ChatColor.RED + "Menu not found: " + menuKey);
            return;
        }

        String title = ChatColor.translateAlternateColorCodes('&',
                menuSec.getString("title", "&7Menu"));
        int size = menuSec.getInt("size", 27);

        Inventory inv = Bukkit.createInventory(null, size, title);

        if (menuSec.isConfigurationSection("items")) {
            for (String slotKey : menuSec.getConfigurationSection("items").getKeys(false)) {
                int slot = Integer.parseInt(slotKey);
                String name = menuSec.getString("items." + slotKey + ".name", "Unnamed");
                String material = menuSec.getString("items." + slotKey + ".material", "STONE");
                String perm = menuSec.getString("items." + slotKey + ".permission", "");

                ItemStack item = new ItemStack(Material.matchMaterial(material) != null
                        ? Material.matchMaterial(material) : Material.STONE);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Permission: " + perm);
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                inv.setItem(slot, item);
            }
        }

        player.openInventory(inv);
    }

    public void handleClick(InventoryClickEvent event) {
        if (event.getView().getTitle().contains("ProShield")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();

            if (clicked != null && clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
                String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                messages.send(player, "&eClicked: &f" + itemName);
                // TODO: tie into actual action logic (claim, trust, flags, etc.)
            }
        }
    }

    // ---------------------------
    // Expansion Deny Reason Flow
    // ---------------------------
    public boolean isAwaitingReason(Player player) {
        return awaitingReason.getOrDefault(player.getUniqueId(), false);
    }

    public void provideManualReason(Player player, String reason) {
        awaitingReason.remove(player.getUniqueId());
        player.sendMessage(ChatColor.RED + "Expansion denied. Reason: " + ChatColor.GRAY + reason);
        // TODO: hook into ExpansionManager when available
    }

    public void setAwaitingReason(Player player, boolean waiting) {
        awaitingReason.put(player.getUniqueId(), waiting);
    }

    // ---------------------------
    // Role Chat Flow
    // ---------------------------
    public boolean isAwaitingRoleAction(Player player) {
        return awaitingRoleAction.getOrDefault(player.getUniqueId(), false);
    }

    public void handleRoleChatInput(Player player, String message) {
        awaitingRoleAction.remove(player.getUniqueId());
        roleManager.assignRoleViaChat(player, message); // tie into ClaimRoleManager
        player.sendMessage(ChatColor.GREEN + "Processed role input: " + ChatColor.GRAY + message);
    }

    public void setAwaitingRoleAction(Player player, boolean waiting) {
        awaitingRoleAction.put(player.getUniqueId(), waiting);
    }

    // ---------------------------
    // Utilities
    // ---------------------------
    public File getGuiDataFile() {
        return new File(plugin.getDataFolder(), "guis.yml");
    }
}
