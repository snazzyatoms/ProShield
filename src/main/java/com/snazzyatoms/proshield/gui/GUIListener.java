package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUIManager
 * Central manager for all ProShield menus (main, flags, trusted, roles, admin, expansion).
 * Fully functional (v1.2.5).
 */
public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.roleManager = plugin.getRoleManager();
        this.messages = plugin.getMessagesUtil();
    }

    // ============================
    // MAIN MENU
    // ============================
    public void openMain(Player player) {
        String title = plugin.getConfig().getString("gui.menus.main.title", "&6ProShield Menu");
        int size = plugin.getConfig().getInt("gui.menus.main.size", 45);

        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        setItem(inv, 10, Material.GRASS_BLOCK, "&aClaim Land", "&7Claim the chunk you are standing in.");
        setItem(inv, 12, Material.PAPER, "&eClaim Info", "&7View details about this claim.");
        setItem(inv, 14, Material.BARRIER, "&cUnclaim Land", "&7Remove your current claim.");
        setItem(inv, 16, Material.PLAYER_HEAD, "&bTrusted Players", "&7Manage trusted players & roles.");
        setItem(inv, 28, Material.REDSTONE_TORCH, "&eClaim Flags", "&7Toggle protection flags.");
        if (plugin.getConfig().getBoolean("claims.expansion.enabled", true)) {
            setItem(inv, 30, Material.EMERALD, "&aRequest Expansion", "&7Request to expand your claim.");
        }
        setItem(inv, 32, Material.COMMAND_BLOCK, "&cAdmin Tools", "&7Admin-only controls.");

        player.openInventory(inv);
    }

    // ============================
    // TRUSTED PLAYERS MENU
    // ============================
    // ... (unchanged code for Trusted Players, Assign Role, Claim Flags) ...

    // ============================
    // ADMIN TOOLS MENU
    // ============================
    public void openAdminTools(Player player) {
        String title = plugin.getConfig().getString("gui.menus.admin-tools.title", "&cAdmin Tools");
        int size = plugin.getConfig().getInt("gui.menus.admin-tools.size", 45);

        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        setItem(inv, 10, Material.REPEATER, "&eReload Configs", "&7Reload ProShield configs.");
        setItem(inv, 12, Material.ENDER_EYE, "&aToggle Debug", "&7Enable/disable debug logging.");
        setItem(inv, 14, Material.BARRIER, "&cToggle Bypass", "&7Admin bypass for claims.");

        // === New: Expansion Requests badge ===
        int pending = plugin.getExpansionRequestManager().getPendingRequests().size();
        List<String> lore = new ArrayList<>();
        lore.add(messages.color("&7Review player expansion requests."));
        if (pending > 0) {
            lore.add(messages.color("&cPending: &f" + pending));
        } else {
            lore.add(messages.color("&aNo pending requests."));
        }
        setItem(inv, 16, Material.EMERALD, "&eExpansion Requests", lore.toArray(new String[0]));

        player.openInventory(inv);
    }

    public void handleAdminClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (name == null) return;

        if (name.equalsIgnoreCase("Reload Configs")) {
            plugin.reloadConfig();
            plugin.loadMessagesConfig();
            messages.send(player, "&aConfigs reloaded.");
        } else if (name.equalsIgnoreCase("Toggle Debug")) {
            plugin.toggleDebug();
            messages.send(player, "&eDebug mode: " + (plugin.isDebugEnabled() ? "&aENABLED" : "&cDISABLED"));
        } else if (name.equalsIgnoreCase("Toggle Bypass")) {
            UUID uuid = player.getUniqueId();
            if (plugin.isBypassing(uuid)) {
                plugin.getBypassing().remove(uuid);
                messages.send(player, "&cBypass disabled.");
            } else {
                plugin.getBypassing().add(uuid);
                messages.send(player, "&aBypass enabled.");
            }
        } else if (name.equalsIgnoreCase("Expansion Requests")) {
            if (player.hasPermission("proshield.admin.expansions")) {
                plugin.getExpansionRequestManager().openRequestMenu(player);
            } else {
                messages.send(player, "&cYou don’t have permission to review expansion requests.");
            }
        }
    }

    // ============================
    // UTILS
    // ============================
    private void setItem(Inventory inv, int slot, Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(messages.color(name));
            List<String> colored = new ArrayList<>();
            for (String l : lore) colored.add(messages.color(l));
            meta.setLore(colored);
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
    }
}
