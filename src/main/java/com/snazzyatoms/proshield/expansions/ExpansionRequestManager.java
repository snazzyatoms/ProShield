// src/main/java/com/snazzyatoms/proshield/expansions/ExpansionRequestManager.java
package com.snazzyatoms.proshield.expansions;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ExpansionRequestManager
 * - Handles persistence, submission, and review of claim expansion requests
 * - Auto-expires pending requests older than config-defined days
 * - Synchronized with GUIManager + messages.yml (v1.2.5)
 */
public class ExpansionRequestManager {

    private final ProShield plugin;
    private final MessagesUtil messages;

    private final File file;
    private YamlConfiguration data;

    // Cache: Player UUID → List of requests
    private final Map<UUID, List<ExpansionRequest>> requests = new HashMap<>();

    // Configurable expiry (days)
    private final int expireDays;

    public ExpansionRequestManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
        this.file = new File(plugin.getDataFolder(), "expansions.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
                data = new YamlConfiguration();
                data.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create expansions.yml: " + e.getMessage());
            }
        }

        this.data = YamlConfiguration.loadConfiguration(file);
        this.expireDays = plugin.getConfig().getInt("claims.expansion.expire-days", 30);

        load();
        expireOldRequests(expireDays);

        // Run expiry every 12h in case server stays up long
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> expireOldRequests(expireDays),
                20L * 60 * 60 * 12, 20L * 60 * 60 * 12);
    }

    /* ====================================
     * Player-facing GUI (Request menu)
     * ==================================== */
    public void openPlayerRequestMenu(Player player) {
        boolean enabled = plugin.getConfig().getBoolean("claims.expansion.enabled", true);
        if (!enabled) {
            messages.send(player, messages.get("messages.expansion-disabled"));
            return;
        }

        String title = plugin.getConfig().getString("gui.menus.expansion-request.title", "&eExpansion Request");
        int size = plugin.getConfig().getInt("gui.menus.expansion-request.size", 27);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        List<Integer> steps = plugin.getConfig().getIntegerList("claims.expansion.step-options");
        if (steps == null || steps.isEmpty()) steps = Arrays.asList(10, 15, 20, 25);

        int slot = 10;
        for (int step : steps) {
            ItemStack emerald = new ItemStack(Material.EMERALD);
            ItemMeta meta = emerald.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(messages.color("&a+" + step + " blocks"));
                meta.setLore(Arrays.asList(
                        messages.color("&7Click to request"),
                        messages.color("&7Cooldown applies if configured")
                ));
                emerald.setItemMeta(meta);
            }
            inv.setItem(slot++, emerald);
        }

        // Simple exit
        ItemStack exit = new ItemStack(Material.BARRIER);
        ItemMeta xmeta = exit.getItemMeta();
        if (xmeta != null) {
            xmeta.setDisplayName(messages.color("&cExit"));
            exit.setItemMeta(xmeta);
        }
        inv.setItem(size - 1, exit);

        player.openInventory(inv);
    }

    /** Click handler for the player request menu */
    public void handlePlayerRequestClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String dn = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (dn == null) return;

        if (dn.equalsIgnoreCase("Exit")) {
            player.closeInventory();
            return;
        }

        if (dn.startsWith("+")) {
            // Parse "+<num> blocks"
            try {
                String num = dn.replace("+", "").replace(" blocks", "").trim();
                int amount = Integer.parseInt(num);

                // (Optional) cooldown enforcement hint – if you have one
                // int cooldownHrs = plugin.getConfig().getInt("claims.expansion.cooldown-hours", 6);

                submitRequest(player.getUniqueId(), amount);
                messages.send(player, messages.get("messages.expansion-request")
                        .replace("{blocks}", String.valueOf(amount)));
                player.closeInventory();
            } catch (NumberFormatException ignored) { }
        }
    }

    /* ====================================
     * Public API
     * ==================================== */

    public void submitRequest(UUID player, int amount) {
        ExpansionRequest request = new ExpansionRequest(player, amount, Instant.now(), ExpansionRequest.Status.PENDING);
        requests.computeIfAbsent(player, k -> new ArrayList<>()).add(request);
        save();

        String who = getPlayerName(player);
        messages.debug("New expansion request: " + who + " +" + amount + " blocks");
    }

    public void approve(UUID player, Instant timestamp, UUID reviewedBy) {
        ExpansionRequest request = getRequest(player, timestamp);
        if (request == null) return;

        request.setStatus(ExpansionRequest.Status.APPROVED);
        request.setReviewedBy(reviewedBy);
        save();

        String who = getPlayerName(player);
        messages.debug("Approved expansion for " + who + " (" + request.getAmount() + " blocks)");

        OfflinePlayer op = Bukkit.getOfflinePlayer(player);
        if (op.isOnline()) {
            messages.send(op.getPlayer(),
                    messages.get("messages.expansion-approved")
                            .replace("{blocks}", String.valueOf(request.getAmount())));
        }
    }

    public void deny(UUID player, Instant timestamp, UUID reviewedBy, String reasonKey) {
        ExpansionRequest request = getRequest(player, timestamp);
        if (request == null) return;

        request.setStatus(ExpansionRequest.Status.DENIED);
        request.setReviewedBy(reviewedBy);

        String reasonMsg = plugin.getConfig().getString("messages.deny-reasons." + reasonKey, reasonKey);
        request.setDenialReason(reasonMsg);
        save();

        String who = getPlayerName(player);
        messages.debug("Denied expansion for " + who + " (" + reasonMsg + ")");

        OfflinePlayer op = Bukkit.getOfflinePlayer(player);
        if (op.isOnline()) {
            messages.send(op.getPlayer(),
                    messages.get("messages.expansion-denied")
                            .replace("{reason}", reasonMsg));
        }
    }

    public void approveRequest(UUID player) {
        List<ExpansionRequest> list = getRequests(player);
        if (list.isEmpty()) return;
        ExpansionRequest latest = list.get(list.size() - 1);
        approve(player, latest.getTimestamp(), null);
    }

    public void denyRequest(UUID player, String reasonKey) {
        List<ExpansionRequest> list = getRequests(player);
        if (list.isEmpty()) return;
        ExpansionRequest latest = list.get(list.size() - 1);
        deny(player, latest.getTimestamp(), null, reasonKey);
    }

    public void expireOldRequests(int days) {
        Instant cutoff = Instant.now().minusSeconds(days * 86400L);
        boolean changed = false;

        for (List<ExpansionRequest> list : requests.values()) {
            for (ExpansionRequest r : list) {
                if (r.getStatus() == ExpansionRequest.Status.PENDING && r.getTimestamp().isBefore(cutoff)) {
                    r.setStatus(ExpansionRequest.Status.EXPIRED);
                    changed = true;

                    OfflinePlayer op = Bukkit.getOfflinePlayer(r.getRequester());
                    if (op.isOnline()) {
                        String line = plugin.getConfig().getString("messages.expansion-expired",
                                "&eYour expansion request has expired after {days} days without review.")
                                .replace("{days}", String.valueOf(days));
                        messages.send(op.getPlayer(), line);
                    }
                }
            }
        }
        if (changed) {
            save();
            messages.debug("Expired old expansion requests older than " + days + " days.");
        }
    }

    public List<ExpansionRequest> getAllRequests() {
        return requests.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    public List<ExpansionRequest> getPendingRequests() {
        return getAllRequests().stream()
                .filter(r -> r.getStatus() == ExpansionRequest.Status.PENDING)
                .collect(Collectors.toList());
    }

    public List<ExpansionRequest> getRequests(UUID player) {
        return requests.getOrDefault(player, new ArrayList<>());
    }

    public ExpansionRequest getRequest(UUID player, Instant timestamp) {
        return requests.getOrDefault(player, new ArrayList<>()).stream()
                .filter(r -> r.getTimestamp().equals(timestamp))
                .findFirst()
                .orElse(null);
    }

    /* ====================================
     * Persistence
     * ==================================== */

    public void save() {
        data = new YamlConfiguration();
        for (Map.Entry<UUID, List<ExpansionRequest>> entry : requests.entrySet()) {
            String playerKey = entry.getKey().toString();
            List<ExpansionRequest> list = entry.getValue();
            ConfigurationSection section = data.createSection(playerKey);
            int i = 0;
            for (ExpansionRequest r : list) {
                ConfigurationSection req = section.createSection(String.valueOf(i++));
                req.set("amount", r.getAmount());
                req.set("timestamp", r.getTimestamp().toString());
                req.set("status", r.getStatus().name());
                if (r.getDenialReason() != null) req.set("denialReason", r.getDenialReason());
                if (r.getReviewedBy() != null) req.set("reviewedBy", r.getReviewedBy().toString());
            }
        }
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save expansions.yml: " + e.getMessage());
        }
    }

    public void load() {
        requests.clear();
        for (String key : data.getKeys(false)) {
            try {
                UUID player = UUID.fromString(key);
                ConfigurationSection section = data.getConfigurationSection(key);
                if (section == null) continue;

                List<ExpansionRequest> list = new ArrayList<>();
                for (String subKey : section.getKeys(false)) {
                    ConfigurationSection req = section.getConfigurationSection(subKey);
                    if (req == null) continue;

                    int amount = req.getInt("amount");
                    Instant timestamp = Instant.parse(req.getString("timestamp"));
                    ExpansionRequest.Status status = ExpansionRequest.Status.valueOf(req.getString("status"));

                    ExpansionRequest request = new ExpansionRequest(player, amount, timestamp, status);

                    if (req.contains("denialReason")) {
                        request.setDenialReason(req.getString("denialReason"));
                    }

                    if (req.contains("reviewedBy")) {
                        try {
                            request.setReviewedBy(UUID.fromString(req.getString("reviewedBy")));
                        } catch (IllegalArgumentException ignored) {}
                    }

                    list.add(request);
                }
                requests.put(player, list);
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Invalid UUID in expansions.yml: " + key);
            }
        }
    }

    /* ====================================
     * Helpers
     * ==================================== */

    private String getPlayerName(UUID uuid) {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(uuid);
        return owner != null && owner.getName() != null
                ? owner.getName()
                : uuid.toString().substring(0, 8);
    }
}
