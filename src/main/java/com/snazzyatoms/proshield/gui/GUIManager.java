package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
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
    private final PlotManager plotManager;

    // Chat-input trackers
    private final Map<UUID, Boolean> awaitingReason = new HashMap<>();
    private final Map<UUID, Boolean> awaitingRoleAction = new HashMap<>();

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
        this.roleManager = plugin.getRoleManager();
        this.plotManager = plugin.getPlotManager();
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
                    if (!perm.isEmpty()) {
                        lore.add(ChatColor.GRAY + "Permission: " + perm);
                    }
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                inv.setItem(slot, item);
            }
        }

        player.openInventory(inv);
    }

    public void handleClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().contains("ProShield")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) {
            return;
        }

        String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        // ---- Button Action Mapping ----
        switch (itemName.toLowerCase()) {
            case "claim land":
                plotManager.claimPlot(player);
                messages.send(player, "&aYou claimed this land.");
                player.closeInventory();
                break;

            case "unclaim land":
                plotManager.unclaimPlot(player);
                messages.send(player, "&cYou unclaimed this land.");
                player.closeInventory();
                break;

            case "claim info":
                plotManager.sendClaimInfo(player);
                player.closeInventory();
                break;

            case "trusted players":
                roleManager.openTrustedPlayersMenu(player);
                player.closeInventory();
                break;

            case "set role":
                setAwaitingRoleAction(player, true);
                messages.send(player, "&eType the role you want to assign in chat.");
                player.closeInventory();
                break;

            case "flags":
                plotManager.openFlagsMenu(player);
                player.closeInventory();
                break;

            default:
                messages.send(player, "&eClicked: &f" + itemName);
                break;
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
        // hook into ExpansionManager when available
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
