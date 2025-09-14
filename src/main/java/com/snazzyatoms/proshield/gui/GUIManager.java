// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.expansion.ExpansionRequestManager;
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

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GUIManager {

    private final ProShield plugin;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens a menu based on the config name.
     */
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
                    for (int i = 0; i < lore.size(); i++) {
                        lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
                    }
                    meta.setLore(lore);
                }

                stack.setItemMeta(meta);
                inv.setItem(slot, stack);
            }
        }

        player.openInventory(inv);
    }

    /**
     * Handles clicks from GUIListener.
     */
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        String title = event.getView().getTitle();
        String name = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        // ------------------------
        // MAIN MENU
        // ------------------------
        if (title.contains("ProShield Menu")) {
            switch (name.toLowerCase(Locale.ROOT)) {
                case "claim land" -> player.performCommand("claim");
                case "claim info" -> player.performCommand("proshield info");
                case "unclaim land" -> player.performCommand("unclaim");
                case "trusted players" -> openMenu(player, "roles");
                case "claim flags" -> openMenu(player, "flags");
                case "admin tools" -> openMenu(player, "admin-expansions");
            }
            return;
        }

        // ------------------------
        // FLAGS MENU
        // ------------------------
        if (title.contains("Claim Flags")) {
            Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
            if (plot == null) {
                plugin.getMessagesUtil().send(player, "&cYou must stand inside a claim.");
                return;
            }

            switch (name.toLowerCase(Locale.ROOT)) {
                case "explosions" -> toggleFlag(plot, "explosions", player);
                case "buckets" -> toggleFlag(plot, "buckets", player);
                case "item frames" -> toggleFlag(plot, "item-frames", player);
                case "armor stands" -> toggleFlag(plot, "armor-stands", player);
                case "containers" -> toggleFlag(plot, "containers", player);
                case "pets" -> toggleFlag(plot, "pets", player);
                case "pvp" -> toggleFlag(plot, "pvp", player);
                case "safe zone" -> toggleFlag(plot, "safezone", player);
                case "back" -> openMenu(player, "main");
            }

            if (!name.equalsIgnoreCase("back")) {
                openMenu(player, "flags"); // refresh GUI
            }
            return;
        }

        // ------------------------
        // ADMIN EXPANSIONS MENU
        // ------------------------
        if (title.contains("Expansion Requests")) {
            switch (name.toLowerCase(Locale.ROOT)) {
                case "pending requests" -> showPendingRequests(player);
                case "approve selected" -> handleExpansionApproval(player);
                case "deny selected" -> handleExpansionDenial(player);
                case "back" -> openMenu(player, "main");
            }
            return;
        }

        // ------------------------
        // ROLES MENU (placeholder for now)
        // ------------------------
        if (title.contains("Trusted Players")) {
            if (name.equalsIgnoreCase("back")) {
                openMenu(player, "main");
            } else {
                plugin.getMessagesUtil().send(player, "&7This feature is still being developed.");
            }
        }
    }

    // ------------------------
    // Helper methods
    // ------------------------

    private void toggleFlag(Plot plot, String flag, Player player) {
        boolean current = plot.getFlag(flag, false);
        plot.setFlag(flag, !current);

        MessagesUtil messages = plugin.getMessagesUtil();
        if (current) {
            messages.send(player, "&c" + flag + " disabled.");
        } else {
            messages.send(player, "&a" + flag + " enabled.");
        }
    }

    private void showPendingRequests(Player player) {
        if (!ExpansionRequestManager.hasRequests()) {
            plugin.getMessagesUtil().send(player, "&7No pending requests.");
            return;
        }

        for (ExpansionRequest req : ExpansionRequestManager.getRequests()) {
            String pName = Bukkit.getOfflinePlayer(req.getPlayerId()).getName();
            plugin.getMessagesUtil().send(player,
                    "&eRequest: " + pName + " +"
                            + req.getExtraRadius() + " blocks ("
                            + (System.currentTimeMillis() - req.getRequestTime()) / 1000 + "s ago)");
        }
    }

    private void handleExpansionApproval(Player player) {
        if (!ExpansionRequestManager.hasRequests()) {
            plugin.getMessagesUtil().send(player, "&7No requests to approve.");
            return;
        }

        ExpansionRequest req = ExpansionRequestManager.getRequests().get(0); // simple demo: approve first
        plugin.getPlotManager().expandClaim(req.getPlayerId(), req.getExtraRadius(), player);
        ExpansionRequestManager.removeRequest(req);
    }

    private void handleExpansionDenial(Player player) {
        if (!ExpansionRequestManager.hasRequests()) {
            plugin.getMessagesUtil().send(player, "&7No requests to deny.");
            return;
        }

        ExpansionRequest req = ExpansionRequestManager.getRequests().get(0); // demo: deny first
        ExpansionRequestManager.removeRequest(req);

        Player target = Bukkit.getPlayer(req.getPlayerId());
        if (target != null) {
            target.sendMessage(ChatColor.RED + "Your expansion request was denied by an admin.");
        }

        plugin.getMessagesUtil().send(player, "&cRequest denied.");
    }
}
