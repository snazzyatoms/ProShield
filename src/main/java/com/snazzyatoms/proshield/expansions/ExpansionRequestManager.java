package com.snazzyatoms.proshield.expansions;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExpansionRequestManager {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    private final Map<UUID, List<ExpansionRequest>> requests = new ConcurrentHashMap<>();

    public ExpansionRequestManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.messages = plugin.getMessagesUtil();

        // ✅ Schedule expiry task (runs once per hour)
        long ticksPerHour = 20L * 60 * 60;
        Bukkit.getScheduler().runTaskTimer(plugin, this::expireOldRequests, ticksPerHour, ticksPerHour);
    }

    /* ---------------------
     * PLAYER REQUEST MENU
     * --------------------- */
    public void openPlayerRequestMenu(Player player) {
        String title = plugin.getConfig().getString("gui.menus.expansion-request.title", "&aRequest Expansion");
        int size = plugin.getConfig().getInt("gui.menus.expansion-request.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        // Pull amounts from config
        List<Integer> amounts = plugin.getConfig().getIntegerList("claims.expansion.step-options");
        if (amounts.isEmpty()) amounts = Arrays.asList(10, 15, 20, 25);

        int slot = 0;
        for (int amt : amounts) {
            inv.setItem(slot++, simpleItem(Material.EMERALD_BLOCK,
                    "&aExpand by " + amt + " blocks",
                    "&7Click to request this expansion.",
                    "&7Requires admin approval."));
        }

        // ✅ Add Back & Exit buttons
        placeNavButtons(inv);

        player.openInventory(inv);
    }

    public void handlePlayerRequestClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = org.bukkit.ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        // ✅ Handle back & exit
        if (name.equalsIgnoreCase("Back")) {
            plugin.getGuiManager().openMain(player);
            return;
        }
        if (name.equalsIgnoreCase("Exit")) {
            player.closeInventory();
            return;
        }

        if (!name.startsWith("Expand by")) return;

        String[] parts = name.split(" ");
        int amount;
        try {
            amount = Integer.parseInt(parts[2]);
        } catch (Exception e) {
            return;
        }

        // ✅ Get player’s claim (only one claim supported for now)
        Plot plot = plotManager.getPlotByOwner(player.getUniqueId());
        if (plot == null) {
            messages.send(player, "&cYou don’t own a claim to expand.");
            return;
        }

        ExpansionRequest req = new ExpansionRequest(player.getUniqueId(), amount, Instant.now(),
                ExpansionRequest.Status.PENDING, null, null);

        requests.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(req);
        messages.send(player, plugin.getMessagesUtil().get("messages.expansion-request")
                .replace("{blocks}", String.valueOf(amount)));
        player.closeInventory();
    }

    /* ---------------------
     * REQUEST STORAGE
     * --------------------- */
    public Collection<ExpansionRequest> getPendingRequests() {
        expireOldRequests();
        List<ExpansionRequest> all = new ArrayList<>();
        for (List<ExpansionRequest> list : requests.values()) {
            for (ExpansionRequest req : list) {
                if (req.getStatus() == ExpansionRequest.Status.PENDING) {
                    all.add(req);
                }
            }
        }
        return all;
    }

    public Collection<ExpansionRequest> getAllRequests() {
        expireOldRequests();
        List<ExpansionRequest> all = new ArrayList<>();
        for (List<ExpansionRequest> list : requests.values()) {
            all.addAll(list);
        }
        return all;
    }

    public void approve(UUID target, Instant ts, UUID admin) {
        List<ExpansionRequest> list = requests.getOrDefault(target, List.of());
        for (ExpansionRequest req : list) {
            if (req.getTimestamp().equals(ts) && req.getStatus() == ExpansionRequest.Status.PENDING) {
                req.setStatus(ExpansionRequest.Status.APPROVED);
                req.setReviewer(admin);

                Plot plot = plotManager.getPlotByOwner(target);
                if (plot != null) {
                    plotManager.expandPlot(plot.getId(), req.getAmount());
                }

                notifyPlayer(target, messages.get("messages.expansion-approved")
                        .replace("{blocks}", String.valueOf(req.getAmount())));
                break;
            }
        }
    }

    public void deny(UUID target, Instant ts, UUID admin, String reason) {
        List<ExpansionRequest> list = requests.getOrDefault(target, List.of());
        for (ExpansionRequest req : list) {
            if (req.getTimestamp().equals(ts) && req.getStatus() == ExpansionRequest.Status.PENDING) {
                req.setStatus(ExpansionRequest.Status.DENIED);
                req.setReviewer(admin);
                req.setDenyReason(reason);
                notifyPlayer(target, messages.get("messages.expansion-denied")
                        .replace("{reason}", reason));
                break;
            }
        }
    }

    /* ---------------------
     * EXPIRY SYSTEM
     * --------------------- */
    private void expireOldRequests() {
        int expireDays = plugin.getConfig().getInt("claims.expansion.expire-days", 30);
        Instant cutoff = Instant.now().minus(expireDays, ChronoUnit.DAYS);

        for (UUID owner : requests.keySet()) {
            for (ExpansionRequest req : requests.get(owner)) {
                if (req.getStatus() == ExpansionRequest.Status.PENDING &&
                        req.getTimestamp().isBefore(cutoff)) {
                    req.setStatus(ExpansionRequest.Status.EXPIRED);

                    notifyPlayer(owner, messages.get("messages.expansion-expired")
                            .replace("{days}", String.valueOf(expireDays)));
                }
            }
        }
    }

    /* ---------------------
     * UTILITIES
     * --------------------- */
    private ItemStack simpleItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(messages.color(name));
            List<String> colored = new ArrayList<>();
            for (String l : lore) colored.add(messages.color(l));
            meta.setLore(colored);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void placeNavButtons(Inventory inv) {
        int size = inv.getSize();
        inv.setItem(size - 9, simpleItem(Material.ARROW, "&eBack", "&7Return to previous menu"));
        inv.setItem(size - 1, simpleItem(Material.BARRIER, "&cExit", "&7Close this menu"));
    }

    private void notifyPlayer(UUID target, String message) {
        Player p = Bukkit.getPlayer(target);
        if (p != null && p.isOnline()) {
            messages.send(p, message);
        }
    }
}
