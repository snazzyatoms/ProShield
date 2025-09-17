// src/main/java/com/snazzyatoms/proshield/expansions/ExpansionRequestManager.java
package com.snazzyatoms.proshield.expansions;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ExpansionRequestManager
 * - Handles persistence, submission, and review of claim expansion requests
 * - Synchronized with GUIManager + MessagesUtil (v1.2.5)
 */
public class ExpansionRequestManager {

    private final ProShield plugin;
    private final MessagesUtil messages;

    private final File file;
    private YamlConfiguration data;

    // Cache: Player UUID → List of requests
    private final Map<UUID, List<ExpansionRequest>> requests = new HashMap<>();

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
        load();
    }

    /* ====================================
     * Public API
     * ==================================== */

    /**
     * Player submits a new request.
     */
    public void submitRequest(UUID player, int amount) {
        ExpansionRequest request = new ExpansionRequest(player, amount, Instant.now(), ExpansionRequest.Status.PENDING);
        requests.computeIfAbsent(player, k -> new ArrayList<>()).add(request);
        save();

        OfflinePlayer op = Bukkit.getOfflinePlayer(player);
        String name = op != null && op.getName() != null ? op.getName() : player.toString();
        messages.debug("New expansion request: " + name + " +" + amount + " blocks");
    }

    /**
     * Approve a request by timestamp.
     */
    public void approve(UUID player, Instant timestamp, UUID reviewedBy) {
        ExpansionRequest request = getRequest(player, timestamp);
        if (request == null) return;

        request.setStatus(ExpansionRequest.Status.APPROVED);
        request.setReviewedBy(reviewedBy);
        save();

        String who = getPlayerName(player);
        messages.debug("Approved expansion for " + who + " (" + request.getAmount() + " blocks)");
    }

    /**
     * Deny a request by timestamp.
     */
    public void deny(UUID player, Instant timestamp, UUID reviewedBy, String reason) {
        ExpansionRequest request = getRequest(player, timestamp);
        if (request == null) return;

        request.setStatus(ExpansionRequest.Status.DENIED);
        request.setReviewedBy(reviewedBy);
        request.setDenialReason(reason);
        save();

        String who = getPlayerName(player);
        messages.debug("Denied expansion for " + who + " (" + reason + ")");
    }

    /**
     * Convenience: Approve by UUID (latest request).
     */
    public void approveRequest(UUID player) {
        List<ExpansionRequest> list = getRequests(player);
        if (list.isEmpty()) return;
        ExpansionRequest latest = list.get(list.size() - 1);
        approve(player, latest.getTimestamp(), null);
    }

    /**
     * Convenience: Deny by UUID (latest request).
     */
    public void denyRequest(UUID player, String reason) {
        List<ExpansionRequest> list = getRequests(player);
        if (list.isEmpty()) return;
        ExpansionRequest latest = list.get(list.size() - 1);
        deny(player, latest.getTimestamp(), null, reason);
    }

    /**
     * Expire pending requests older than N days.
     */
    public void expireOldRequests(int days) {
        Instant cutoff = Instant.now().minusSeconds(days * 86400L);
        for (List<ExpansionRequest> list : requests.values()) {
            for (ExpansionRequest r : list) {
                if (r.getStatus() == ExpansionRequest.Status.PENDING && r.getTimestamp().isBefore(cutoff)) {
                    r.setStatus(ExpansionRequest.Status.EXPIRED);
                }
            }
        }
        save();
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
