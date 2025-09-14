package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansion.ExpansionQueue;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.plots.Plot;
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

public class GUIManager {

    private final ProShield plugin;
    private static final Map<UUID, ExpansionRequest> awaitingReason = new HashMap<>();
    private static final Map<UUID, String> awaitingRoleAction = new HashMap<>(); // add/remove

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    // --- Expansion Deny Reasons ---
    public static boolean isAwaitingReason(Player player) {
        return awaitingReason.containsKey(player.getUniqueId());
    }

    public static void cancelAwaiting(Player player) {
        awaitingReason.remove(player.getUniqueId());
        awaitingRoleAction.remove(player.getUniqueId());
    }

    // --- Role Management ---
    public static boolean isAwaitingRoleAction(Player player) {
        return awaitingRoleAction.containsKey(player.getUniqueId());
    }

    public static String getRoleAction(Player player) {
        return awaitingRoleAction.get(player.getUniqueId());
    }

    public static void setRoleAction(Player player, String action) {
        awaitingRoleAction.put(player.getUniqueId(), action);
    }

    // --- Open Menu from Config ---
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
                if (!lore.isEmpty()) {
                    lore.replaceAll(line -> ChatColor.translateAlternateColorCodes('&', line));
                    meta.setLore(lore);
                }

                stack.setItemMeta(meta);
                inv.setItem(slot, stack);
            }
        }

        // Inject trusted players dynamically if this is the roles menu
        if (menuName.equalsIgnoreCase("roles")) {
            Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
            if (plot != null) {
                int slot = 9; // start listing trusted players from slot 9 upwards
                for (UUID trustedId : plot.getTrusted()) {
                    if (slot >= size - 1) break; // avoid overwriting control buttons
                    OfflinePlayer trustedPlayer = Bukkit.getOfflinePlayer(trustedId);

                    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                    ItemMeta meta = head.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(ChatColor.AQUA + trustedPlayer.getName());
                        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Trusted Player"));
                        head.setItemMeta(meta);
                    }
                    inv.setItem(slot, head);
                    slot++;
                }
            } else {
                plugin.getMessagesUtil().send(player, "&cStand inside your claim to see trusted players.");
            }
        }

        player.openInventory(inv);
    }

    // --- Handle Clicks ---
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        String title = event.getView().getTitle();
        String name = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        // MAIN MENU
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

        // FLAGS MENU
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
                openMenu(player, "flags");
            }
            return;
        }

        // ADMIN EXPANSIONS MENU
        if (title.contains("Expansion Requests")) {
            switch (name.toLowerCase(Locale.ROOT)) {
                case "pending requests" -> showPendingRequests(player);
                case "approve selected" -> handleExpansionApproval(player);
                case "deny selected" -> openMenu(player, "deny-reasons");
                case "back" -> openMenu(player, "main");
            }
            return;
        }

        // DENY REASONS MENU
        if (title.contains("Deny Reasons")) {
            List<ExpansionRequest> pending = ExpansionQueue.getPendingRequests();
            if (pending.isEmpty()) {
                plugin.getMessagesUtil().send(player, "&7No requests to deny.");
                openMenu(player, "admin-expansions");
                return;
            }

            ExpansionRequest req = pending.get(0);

            if (name.equalsIgnoreCase("back")) {
                openMenu(player, "admin-expansions");
                return;
            }

            if (name.equalsIgnoreCase("other")) {
                awaitingReason.put(player.getUniqueId(), req);
                plugin.getMessagesUtil().send(player, "&eType your denial reason in chat...");
                player.closeInventory();
                return;
            }

            ExpansionQueue.denyRequest(req, name);
            plugin.getMessagesUtil().send(player, "&cRequest denied (" + name + ").");
            openMenu(player, "admin-expansions");
            return;
        }

        // ROLES MENU
        if (title.contains("Trusted Players")) {
            switch (name.toLowerCase(Locale.ROOT)) {
                case "add player" -> {
                    awaitingRoleAction.put(player.getUniqueId(), "add");
                    plugin.getMessagesUtil().send(player, "&eType the name of the player you want to trust in chat...");
                    player.closeInventory();
                }
                case "remove player" -> {
                    awaitingRoleAction.put(player.getUniqueId(), "remove");
                    plugin.getMessagesUtil().send(player, "&eType the name of the player you want to untrust in chat...");
                    player.closeInventory();
                }
                case "back" -> openMenu(player, "main");
            }
            return;
        }
    }

    // --- Helpers ---
    private void toggleFlag(Plot plot, String flag, Player player) {
        boolean current = plot.getFlag(flag, false);
        plot.setFlag(flag, !current);

        MessagesUtil messages = plugin.getMessagesUtil();
        messages.send(player, current ? "&c" + flag + " disabled." : "&a" + flag + " enabled.");
    }

    private void showPendingRequests(Player player) {
        List<ExpansionRequest> pending = ExpansionQueue.getPendingRequests();
        if (pending.isEmpty()) {
            plugin.getMessagesUtil().send(player, "&7No pending requests.");
            return;
        }

        for (ExpansionRequest req : pending) {
            String pName = Bukkit.getOfflinePlayer(req.getPlayerId()).getName();
            plugin.getMessagesUtil().send(player,
                    "&eRequest: " + pName + " +" + req.getExtraRadius() + " blocks (" +
                            (System.currentTimeMillis() - req.getRequestTime()) / 1000 + "s ago)");
        }
    }

    private void handleExpansionApproval(Player player) {
        List<ExpansionRequest> pending = ExpansionQueue.getPendingRequests();
        if (pending.isEmpty()) {
            plugin.getMessagesUtil().send(player, "&7No requests to approve.");
            return;
        }

        ExpansionRequest req = pending.get(0);
        ExpansionQueue.approveRequest(req);

        Player target = Bukkit.getPlayer(req.getPlayerId());
        if (target != null) {
            target.sendMessage(ChatColor.GREEN + "Your expansion request was approved!");
        }
        plugin.getMessagesUtil().send(player, "&aRequest approved.");
    }

    public static void provideManualReason(Player admin, String reason, ProShield plugin) {
        ExpansionRequest req = awaitingReason.remove(admin.getUniqueId());
        if (req == null) {
            plugin.getMessagesUtil().send(admin, "&7No pending request to deny.");
            return;
        }

        ExpansionQueue.denyRequest(req, reason);
        Player target = Bukkit.getPlayer(req.getPlayerId());
        if (target != null) {
            target.sendMessage(ChatColor.RED + "Your expansion request was denied: " + reason);
        }
        plugin.getMessagesUtil().send(admin, "&cRequest denied (" + reason + ").");
    }
}
