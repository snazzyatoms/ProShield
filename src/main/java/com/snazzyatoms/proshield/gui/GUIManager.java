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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUIManager
 * - Builds config-driven menus
 * - Handles clicks (called by GUIListener)
 * - Implements chat capture for manual deny reason
 * - Renders dynamic Expansion Requests list
 *
 * IMPORTANT: Register this class as a Listener in ProShield:
 *   Bukkit.getPluginManager().registerEvents(guiManager, this);
 */
public class GUIManager implements Listener {

    private final ProShield plugin;

    // Admin → which request they've selected (from the dynamic list)
    private final Map<UUID, ExpansionRequest> selectedRequest = new HashMap<>();

    // Admin → awaiting manual deny reason?
    private final Set<UUID> awaitingReason = new HashSet<>();

    // Admin → slot→request mapping for the dynamic "Pending Expansion Requests" menu
    private final Map<UUID, Map<Integer, ExpansionRequest>> requestSlotMaps = new HashMap<>();

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    // -------------------------------------------------------
    // Public API
    // -------------------------------------------------------

    /** Opens a menu defined in config under gui.menus.<menuName> */
    public void openMenu(Player player, String menuName) {
        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus." + menuName);
        if (menuSec == null) {
            plugin.getLogger().warning("Menu not found in config: " + menuName);
            return;
        }

        String title = color(menuSec.getString("title", "Menu"));
        int size = menuSec.getInt("size", 27);
        Inventory inv = Bukkit.createInventory(null, size, title);

        ConfigurationSection itemsSec = menuSec.getConfigurationSection("items");
        if (itemsSec != null) {
            for (Map.Entry<String, Object> entry : itemsSec.getValues(false).entrySet()) {
                String slotStr = entry.getKey();
                ConfigurationSection itemSec = itemsSec.getConfigurationSection(slotStr);
                if (itemSec == null) continue;

                int slot;
                try { slot = Integer.parseInt(slotStr); }
                catch (NumberFormatException ex) { continue; }

                inv.setItem(slot, buildItemFromSection(itemSec));
            }
        }

        player.openInventory(inv);
    }

    /** Build a simple named item */
    private ItemStack simpleItem(Material mat, String name, List<String> lore) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(name));
            if (lore != null && !lore.isEmpty()) {
                List<String> ll = new ArrayList<>(lore.size());
                for (String s : lore) ll.add(color(s));
                meta.setLore(ll);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    /** Called by GUIListener on any inventory click in our GUIs */
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        String title = event.getView().getTitle();
        String name = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        event.setCancelled(true);

        // MAIN MENU
        if (title.contains("ProShield Menu")) {
            switch (name.toLowerCase(Locale.ROOT)) {
                case "claim land" -> player.performCommand("claim");
                case "claim info" -> player.performCommand("proshield info");
                case "unclaim land" -> player.performCommand("unclaim");
                // If you add entries like "Claim Flags" or "Trusted Players" to main, support them here:
                case "claim flags" -> openMenu(player, "flags");
                case "admin tools" -> {
                    if (player.isOp()) openMenu(player, "admin-expansions");
                    else plugin.getMessagesUtil().send(player, "&cNo permission.");
                }
            }
            return;
        }

        // FLAGS MENU
        if (title.contains("Claim Flags")) {
            handleFlagsClick(player, name);
            return;
        }

        // ADMIN-EXPANSIONS MENU (hub)
        if (title.contains("Expansion Requests")) {
            switch (name.toLowerCase(Locale.ROOT)) {
                case "pending requests" -> openExpansionRequestsList(player);  // dynamic list
                case "approve selected" -> approveSelected(player);
                case "deny selected" -> openMenu(player, "deny-reasons");
                case "back" -> openMenu(player, "admin"); // falls back to admin menu if you define one
            }
            return;
        }

        // PENDING EXPANSION REQUESTS (dynamic)
        if (title.contains("Pending Expansion Requests")) {
            Map<Integer, ExpansionRequest> map = requestSlotMaps.get(player.getUniqueId());
            if (map != null && map.containsKey(event.getSlot())) {
                ExpansionRequest req = map.get(event.getSlot());
                selectedRequest.put(player.getUniqueId(), req);
                plugin.getMessagesUtil().send(player, "&aSelected request: &e" +
                        Bukkit.getOfflinePlayer(req.getPlayerId()).getName() +
                        " &7(+" + req.getExtraRadius() + " blocks)");
            } else {
                // Back?
                String lower = name.toLowerCase(Locale.ROOT);
                if (lower.equals("back")) {
                    openMenu(player, "admin-expansions");
                }
            }
            return;
        }

        // DENY-REASONS MENU (configurable)
        if (title.contains("Deny Reasons")) {
            // We read the action from the config item to decide behavior
            // However, since the listener only knows the clicked item, we derive by its name,
            // OR better: re-open the config item by matching name (lenient). Simpler approach:
            // Name "Back" -> go back. Name "Other" -> manual. Otherwise, use the item display name as reason text.
            String lower = name.toLowerCase(Locale.ROOT);
            if (lower.equals("back")) {
                openMenu(player, "admin-expansions");
                return;
            }

            ExpansionRequest req = selectedRequest.get(player.getUniqueId());
            if (req == null) {
                plugin.getMessagesUtil().send(player, "&cNo request selected. Open &ePending Requests&c and click one.");
                return;
            }

            // If admin clicked a specific canned reason named "Other" (or "Manual"), ask for manual input
            if (lower.equals("other") || lower.equals("manual")) {
                startManualReason(player, req);
            } else {
                // Use the clicked item's display name as the reason text (configurable!)
                denyWithReason(player, req, name);
            }
            return;
        }
    }

    // -------------------------------------------------------
    // Flags handling
    // -------------------------------------------------------
    private void handleFlagsClick(Player player, String itemName) {
        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
        if (plot == null) {
            plugin.getMessagesUtil().send(player, "&cYou must stand inside a claim.");
            return;
        }

        switch (itemName.toLowerCase(Locale.ROOT)) {
            case "explosions" -> toggleFlag(plot, "explosions", player);
            case "buckets" -> toggleFlag(plot, "buckets", player);
            case "item frames" -> toggleFlag(plot, "item-frames", player);
            case "armor stands" -> toggleFlag(plot, "armor-stands", player);
            case "containers" -> toggleFlag(plot, "containers", player);
            case "pets" -> toggleFlag(plot, "pets", player);
            case "pvp" -> toggleFlag(plot, "pvp", player);
            case "safe zone" -> toggleFlag(plot, "safezone", player);
            case "back" -> {
                openMenu(player, "main");
                return;
            }
        }
        // refresh GUI
        openMenu(player, "flags");
    }

    private void toggleFlag(Plot plot, String flag, Player player) {
        boolean current = plot.getFlag(flag, false);
        plot.setFlag(flag, !current);
        MessagesUtil m = plugin.getMessagesUtil();
        if (current) m.send(player, "&c" + flag + " disabled."); else m.send(player, "&a" + flag + " enabled.");
    }

    // -------------------------------------------------------
    // Expansion requests (dynamic list)
    // -------------------------------------------------------
    private void openExpansionRequestsList(Player admin) {
        if (!admin.isOp() && !admin.hasPermission("proshield.admin")) {
            plugin.getMessagesUtil().send(admin, "&cNo permission.");
            return;
        }

        List<ExpansionRequest> list = ExpansionRequestManager.getRequests();
        int size = 54;
        Inventory inv = Bukkit.createInventory(null, size, color("&cPending Expansion Requests"));

        Map<Integer, ExpansionRequest> slotMap = new HashMap<>();
        int slot = 0;

        if (!list.isEmpty()) {
            for (ExpansionRequest req : list) {
                if (slot >= size - 9) break; // leave bottom row for controls
                String pName = Bukkit.getOfflinePlayer(req.getPlayerId()).getName();
                long ageSec = Math.max(0, (System.currentTimeMillis() - req.getRequestTime()) / 1000);

                ItemStack paper = simpleItem(
                        Material.PAPER,
                        "&e" + (pName != null ? pName : req.getPlayerId().toString()),
                        Arrays.asList(
                                "&7+&f" + req.getExtraRadius() + " blocks",
                                "&7Requested &f" + ageSec + "s&7 ago",
                                "&8Click to select"
                        )
                );
                inv.setItem(slot, paper);
                slotMap.put(slot, req);
                slot++;
            }
        } else {
            inv.setItem(13, simpleItem(Material.GRAY_DYE, "&7No pending requests", Collections.emptyList()));
        }

        // Back button
        inv.setItem(49, simpleItem(Material.BARRIER, "&cBack", Arrays.asList("&7Return to Admin Menu")));

        requestSlotMaps.put(admin.getUniqueId(), slotMap);
        admin.openInventory(inv);
    }

    private void approveSelected(Player admin) {
        if (!admin.isOp() && !admin.hasPermission("proshield.admin")) {
            plugin.getMessagesUtil().send(admin, "&cNo permission.");
            return;
        }
        ExpansionRequest req = selectedRequest.get(admin.getUniqueId());
        if (req == null) {
            plugin.getMessagesUtil().send(admin, "&cNo request selected. Open &ePending Requests&c and click one.");
            return;
        }

        boolean instant = plugin.getConfig().getBoolean("claims.instant-apply", true);
        if (instant) {
            // Apply immediately
            plugin.getPlotManager().expandClaim(req.getPlayerId(), req.getExtraRadius());
            ExpansionRequestManager.removeRequest(req);

            Player target = Bukkit.getPlayer(req.getPlayerId());
            if (target != null) {
                sendMsg(target, "messages.expansion-approved",
                        Map.of("blocks", String.valueOf(req.getExtraRadius())));
            }
            plugin.getMessagesUtil().send(admin, "&aApproved &e+" + req.getExtraRadius() + " &ablocks.");
        } else {
            // Deferred mode: leave it in the queue and just notify for now
            plugin.getMessagesUtil().send(admin,
                    "&eApproved. Will apply after restart (claims.instant-apply=false).");
            // (We intentionally do not remove it so it can be applied later by your own maintenance flow)
        }
    }

    private void denyWithReason(Player admin, ExpansionRequest req, String reasonText) {
        ExpansionRequestManager.removeRequest(req);

        Player target = Bukkit.getPlayer(req.getPlayerId());
        if (target != null) {
            sendMsg(target, "messages.expansion-denied", Map.of("reason", reasonText));
        }
        plugin.getMessagesUtil().send(admin, "&cDenied request &7(&f" + reasonText + "&7).");
        selectedRequest.remove(admin.getUniqueId());
    }

    private void startManualReason(Player admin, ExpansionRequest req) {
        awaitingReason.add(admin.getUniqueId());
        selectedRequest.put(admin.getUniqueId(), req);
        plugin.getMessagesUtil().send(admin,
                "&eType the denial reason in chat. (&7or type &ccancel&7 to abort& e)");
        // Keep GUI open or let them type — Bukkit chat works fine either way.
    }

    // -------------------------------------------------------
    // Chat capture for manual deny reason
    // -------------------------------------------------------
    @EventHandler
    public void onAdminChat(AsyncPlayerChatEvent event) {
        Player admin = event.getPlayer();
        if (!awaitingReason.contains(admin.getUniqueId())) return;

        event.setCancelled(true); // don't broadcast this
        String msg = event.getMessage().trim();

        if (msg.equalsIgnoreCase("cancel")) {
            awaitingReason.remove(admin.getUniqueId());
            selectedRequest.remove(admin.getUniqueId());
            plugin.getMessagesUtil().send(admin, "&7Manual deny cancelled.");
            return;
        }

        ExpansionRequest req = selectedRequest.get(admin.getUniqueId());
        if (req == null) {
            awaitingReason.remove(admin.getUniqueId());
            plugin.getMessagesUtil().send(admin, "&cNo request in context. Open the list and select one.");
            return;
        }

        // Use typed message as reason
        denyWithReason(admin, req, msg);
        awaitingReason.remove(admin.getUniqueId());
    }

    // -------------------------------------------------------
    // Utilities
    // -------------------------------------------------------
    private ItemStack buildItemFromSection(ConfigurationSection sec) {
        Material mat = Material.matchMaterial(sec.getString("material", "STONE"));
        if (mat == null) mat = Material.STONE;

        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(sec.getString("name", "")));
            List<String> lore = sec.getStringList("lore");
            if (lore != null && !lore.isEmpty()) {
                List<String> out = new ArrayList<>(lore.size());
                for (String s : lore) out.add(color(s));
                meta.setLore(out);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }

    private void sendMsg(Player p, String path, Map<String, String> vars) {
        String raw = plugin.getConfig().getString(path, "");
        if (raw == null || raw.isEmpty()) return;
        String msg = raw;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            msg = msg.replace("{" + e.getKey() + "}", e.getValue());
        }
        p.sendMessage(color(msg));
    }
}
