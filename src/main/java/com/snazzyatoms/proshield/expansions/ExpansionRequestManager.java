package com.snazzyatoms.proshield.expansions;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class ExpansionRequestManager {

    private final ProShield plugin;
    private final File file;
    private YamlConfiguration data;

    private final Map<UUID, List<ExpansionRequest>> requests = new HashMap<>();

    public ExpansionRequestManager(ProShield plugin) {
        this.plugin = plugin;
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

    public void submitRequest(UUID player, int amount) {
        ExpansionRequest request = new ExpansionRequest(player, amount, Instant.now(), ExpansionRequest.Status.PENDING);
        requests.computeIfAbsent(player, k -> new ArrayList<>()).add(request);
        save();
    }

    public void approve(UUID player, Instant timestamp, UUID reviewedBy) {
        ExpansionRequest request = getRequest(player, timestamp);
        if (request == null) return;
        request.setStatus(ExpansionRequest.Status.APPROVED);
        request.setReviewedBy(reviewedBy);
        save();
    }

    public void deny(UUID player, Instant timestamp, UUID reviewedBy, String reason) {
        ExpansionRequest request = getRequest(player, timestamp);
        if (request == null) return;
        request.setStatus(ExpansionRequest.Status.DENIED);
        request.setReviewedBy(reviewedBy);
        request.setDenialReason(reason);
        save();
    }

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
}
