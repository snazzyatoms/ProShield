package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.expansion.ExpansionRequestManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminGUIManager {

    private final ProShield plugin;
    private final ExpansionRequestManager requestManager;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public AdminGUIManager(ProShield plugin, ExpansionRequestManager requestManager, PlotManager plotManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.requestManager = requestManager;
        this.plotManager = plotManager;
        this.messages = messages;
    }

    /**
     * Opens the Admin main GUI.
     */
    public void openAdminMenu(Player player) {
        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus.admin-expansions");
        if (menuSec == null) {
            messages.send(player, "&cAdmin expansions menu not found in config.");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, menuSec.getInt("size", 27),
                menuSec.getString("title", "Admin Menu"));

        for (String key : menuSec.getKeys(false)) {
            if (!menuSec.isConfigurationSection(key)) continue;
            ConfigurationSection itemSec = menuSec.getConfigurationSection(key);

            int slot;
            try {
                slot = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                continue;
            }

            Material mat = Material.matchMaterial(itemSec.getString("material", "BARRIER"));
            if (mat == null) mat = Material.BARRIER;

            ItemStack stack = new ItemStack(mat);
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(MessagesUtil.color(itemSec.getString("name", "&cUnnamed")));
                List<String> lore = new ArrayList<>();
                for (String line : itemSec.getStringList("lore")) {
                    lore.add(MessagesUtil.color(line));
                }
                meta.setLore(lore);
                stack.setItemMeta(meta);
            }
            inv.setItem(slot, stack);
        }

        player.openInventory(inv);
    }

    /**
     * Opens the list of expansion requests.
     */
    public void openExpansionRequestsMenu(Player player) {
        List<ExpansionRequest> requests = requestManager.getAllRequests();
        int size = Math.max(27, ((requests.size() / 9) + 1) * 9);
        Inventory inv = Bukkit.createInventory(null, size, MessagesUtil.color("&cExpansion Requests"));

        int slot = 0;
        for (ExpansionRequest req : requests) {
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            if (meta != null) {
                String name = plugin.getPlotManager().getPlayerName(req.getPlayerId());
                meta.setDisplayName(MessagesUtil.color("&eRequest: &f" + name));
                List<String> lore = new ArrayList<>();
                lore.add(MessagesUtil.color("&7Extra radius: &b+" + req.getExtraRadius()));
                lore.add(MessagesUtil.color("&7Requested: &f" + (System.currentTimeMillis() - req.getRequestTime()) / 1000 + "s ago"));
                lore.add(MessagesUtil.color("&aLeft-click to Approve"));
                lore.add(MessagesUtil.color("&cRight-click to Deny"));
                meta.setLore(lore);
                paper.setItemMeta(meta);
            }
            inv.setItem(slot++, paper);
        }

        if (requests.isEmpty()) {
            ItemStack empty = new ItemStack(Material.BARRIER);
            ItemMeta meta = empty.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(MessagesUtil.color("&cNo Requests"));
                meta.setLore(List.of(MessagesUtil.color("&7There are currently no pending requests.")));
                empty.setItemMeta(meta);
            }
            inv.setItem(13, empty);
        }

        player.openInventory(inv);
    }

    /**
     * Approves a request instantly (if enabled in config).
     */
    public void approveRequest(Player admin, ExpansionRequest req) {
        UUID playerId = req.getPlayerId();
        Player target = Bukkit.getPlayer(playerId);

        int newRadius = plotManager.expandClaimRadius(playerId, req.getExtraRadius());
        requestManager.removeRequest(req);

        if (target != null && target.isOnline()) {
            messages.send(target, plugin.getConfig().getString("messages.expansion-approved")
                    .replace("{blocks}", String.valueOf(req.getExtraRadius())));
        }

        messages.send(admin, "&aApproved expansion for &f" +
                plugin.getPlotManager().getPlayerName(playerId) +
                " &7→ &b" + newRadius + " blocks radius.");
    }

    /**
     * Denies a request with a reason.
     */
    public void denyRequest(Player admin, ExpansionRequest req, String reason) {
        UUID playerId = req.getPlayerId();
        Player target = Bukkit.getPlayer(playerId);

        requestManager.removeRequest(req);

        if (target != null && target.isOnline()) {
            messages.send(target, plugin.getConfig().getString("messages.expansion-denied")
                    .replace("{reason}", reason));
        }

        messages.send(admin, "&cDenied expansion for &f" +
                plugin.getPlotManager().getPlayerName(playerId) +
                " &7→ &eReason: " + reason);
    }
}
