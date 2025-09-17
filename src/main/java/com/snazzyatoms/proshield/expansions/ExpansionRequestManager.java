// src/main/java/com/snazzyatoms/proshield/expansions/ExpansionRequestManager.java
package com.snazzyatoms.proshield.expansions;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExpansionRequestManager {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    private final Map<UUID, List<ExpansionRequest>> requests = new ConcurrentHashMap<>();

    private final File file;
    private FileConfiguration data;

    public ExpansionRequestManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.messages = plugin.getMessagesUtil();

        this.file = new File(plugin.getDataFolder(), "expansions.yml");
        reload();

        // ✅ Expire old requests every hour
        long ticksPerHour = 20L * 60 * 60;
        Bukkit.getScheduler().runTaskTimer(plugin, this::expireOldRequests, ticksPerHour, ticksPerHour);
    }

    /* ---------------------
     * YAML Persistence
     * --------------------- */
    public void reload() {
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException ignored) {}
        }
        data = YamlConfiguration.loadConfiguration(file);
        loadRequests();
    }

    public void save() {
        saveRequests();
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save expansions.yml: " + e.getMessage());
        }
    }

    private void loadRequests() {
        requests.clear();
        if (!data.isConfigurationSection("requests")) return;

        for (String uuidStr : data.getConfigurationSection("requests").getKeys(false)) {
            UUID owner = UUID.fromString(uuidStr);
            List<Map<?, ?>> rawList = data.getMapList("requests." + uuidStr);
            List<ExpansionRequest> list = new ArrayList<>();
            for (Map<?, ?> raw : rawList) {
                try {
                    ExpansionRequest req = ExpansionRequest.fromMap((Map<String, Object>) raw);
                    if (req != null) list.add(req);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load expansion request for " + uuidStr + ": " + e.getMessage());
                }
            }
            requests.put(owner, list);
        }
    }

    private void saveRequests() {
        data.set("requests", null); // clear
        for (Map.Entry<UUID, List<ExpansionRequest>> entry : requests.entrySet()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (ExpansionRequest req : entry.getValue()) {
                list.add(req.toMap());
            }
            data.set("requests." + entry.getKey().toString(), list);
        }
    }

    /* ---------------------
     * PLAYER REQUEST MENU
     * --------------------- */
    public void openPlayerRequestMenu(Player player) {
        String title = plugin.getConfig().getString("gui.menus.expansion-request.title", "&aRequest Expansion");
        int size = plugin.getConfig().getInt("gui.menus.expansion-request.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        List<Integer> amounts = plugin.getConfig().getIntegerList("claims.expansion.step-options");
        if (amounts.isEmpty()) amounts = Arrays.asList(10, 15, 20, 25);

        int slot = 0;
        for (int amt : amounts) {
            inv.setItem(slot++, simpleItem(Material.EMERALD_BLOCK,
                    "&aExpand by " + amt + " blocks",
                    "&7Click to request this expansion.",
                    "&7Requires admin approval."));
        }

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    public void handlePlayerRequestClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true); // ✅ Prevent dragging Back/Exit items

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = org.bukkit.ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        if (name.equalsIgnoreCase("Back")) {
            plugin.getGuiManager().openMain(player);
            return;
        }
        if (name.equalsIgnoreCase("Exit")) {
            player.closeInventory();
            return;
        }
        if (!name.startsWith("Expand by")) return;

        int amount;
        try {
            amount = Integer.parseInt(name.split(" ")[2]);
        } catch (Exception e) {
            return;
        }

        Plot plot = plotManager.getPlotByOwner(player.getUniqueId());
        if (plot == null) {
            messages.send(player, "&cYou don’t own a claim to expand.");
            return;
        }

        ExpansionRequest req = new ExpansionRequest(player.getUniqueId(), amount, Instant.now(),
                ExpansionRequest.Status.PENDING, null, null);

        requests.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(req);
        save(); // ✅ Save immediately
        messages.send(player, messages.get("expansion.request-sent")
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
                req.setReviewedBy(admin);

                Plot plot = plotManager.getPlotByOwner(target);
                if (plot != null) {
                    plotManager.expandPlot(plot.getId(), req.getAmount());
                }

                save(); // ✅ Persist change
                notifyPlayer(target, messages.get("expansion.approved")
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
                req.setReviewedBy(admin);
                req.setDenyReason(reason);

                save(); // ✅ Persist change
                notifyPlayer(target, messages.get("expansion.denied")
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
                    notifyPlayer(owner, messages.get("expansion.expired")
                            .replace("{days}", String.valueOf(expireDays)));
                }
            }
        }
        save(); // ✅ Persist expiry
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
