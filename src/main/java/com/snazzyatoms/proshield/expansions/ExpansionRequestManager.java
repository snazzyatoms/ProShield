// src/main/java/com/snazzyatoms/proshield/expansions/ExpansionRequestManager.java
package com.snazzyatoms.proshield.expansions;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ExpansionRequestManager (v1.2.6)
 * --------------------------------
 * - Handles creation, approval, denial, expiry of expansion requests
 * - Persists requests to expansions.yml
 * - Respects cooldown-hours and expire-days from config.yml
 * - Provides clean logs + messages for admins/players
 */
public class ExpansionRequestManager {

    private final ProShield plugin;
    private final MessagesUtil messages;
    private final Map<UUID, List<ExpansionRequest>> requests = new ConcurrentHashMap<>();

    private final File dataFile;
    private FileConfiguration dataConfig;

    public ExpansionRequestManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
        this.dataFile = new File(plugin.getDataFolder(), "expansions.yml");
        load();
    }

    /* -------------------
     * Request Lifecycle
     * ------------------- */

    public ExpansionRequest createRequest(UUID requester, int amount) {
        // Enforce cooldown
        int cooldownHours = plugin.getConfig().getInt("claims.expansion.cooldown-hours", 6);
        Duration cooldown = Duration.ofHours(cooldownHours);

        List<ExpansionRequest> playerReqs = requests.computeIfAbsent(requester, k -> new ArrayList<>());
        Optional<ExpansionRequest> recent = playerReqs.stream()
                .filter(r -> r.getStatus() == ExpansionRequest.Status.PENDING)
                .max(Comparator.comparing(ExpansionRequest::getTimestamp));

        if (recent.isPresent() &&
                Duration.between(recent.get().getTimestamp(), Instant.now()).compareTo(cooldown) < 0) {
            messages.send(Bukkit.getPlayer(requester),
                    messages.getOrDefault("messages.expansion.cooldown",
                            "&cYou must wait before submitting another expansion request."));
            return null;
        }

        ExpansionRequest req = new ExpansionRequest(requester, amount);
        playerReqs.add(req);
        saveAll();
        plugin.getLogger().info("Created expansion request: " + requester + " for +" + amount + " blocks");
        messages.send(Bukkit.getPlayer(requester),
                messages.getOrDefault("messages.expansion.request-sent",
                        "&eYour expansion request for +{blocks} blocks has been sent to admins.")
                        .replace("{blocks}", String.valueOf(amount)));
        return req;
    }

    public void approveRequest(ExpansionRequest req, UUID reviewer) {
        req.setStatus(ExpansionRequest.Status.APPROVED);
        req.setReviewedBy(reviewer);
        saveAll();
        plugin.getLogger().info("Approved expansion request from " + req.getRequester() +
                " for +" + req.getAmount());
        messages.send(Bukkit.getPlayer(req.getRequester()),
                messages.getOrDefault("messages.expansion.approved",
                        "&aYour expansion request for +{blocks} blocks has been approved!")
                        .replace("{blocks}", String.valueOf(req.getAmount())));
    }

    public void denyRequest(ExpansionRequest req, UUID reviewer, String reason) {
        req.setStatus(ExpansionRequest.Status.DENIED);
        req.setReviewedBy(reviewer);
        req.setDenialReason(reason);
        saveAll();
        plugin.getLogger().info("Denied expansion request from " + req.getRequester() +
                " (" + reason + ")");
        messages.send(Bukkit.getPlayer(req.getRequester()),
                messages.getOrDefault("messages.expansion.denied",
                        "&cYour expansion request was denied. Reason: {reason}")
                        .replace("{reason}", reason));
    }

    /** Expire old pending requests (called on load/reload). */
    public void expireOldRequests() {
        int expireDays = plugin.getConfig().getInt("claims.expansion.expire-days", 30);
        Duration expiry = Duration.ofDays(expireDays);

        for (List<ExpansionRequest> list : requests.values()) {
            for (ExpansionRequest req : list) {
                if (req.getStatus() == ExpansionRequest.Status.PENDING &&
                        Duration.between(req.getTimestamp(), Instant.now()).compareTo(expiry) > 0) {
                    req.setStatus(ExpansionRequest.Status.EXPIRED);
                    plugin.getLogger().info("Expired expansion request from " + req.getRequester()
                            + " (" + req.getAmount() + " blocks, older than " + expireDays + " days)");
                }
            }
        }
        saveAll();
    }

    /* -------------------
     * Getters
     * ------------------- */

    public List<ExpansionRequest> getRequests(UUID requester) {
        return requests.getOrDefault(requester, Collections.emptyList());
    }

    public List<ExpansionRequest> getAllRequests() {
        return requests.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<ExpansionRequest> getPendingRequests() {
        return getAllRequests().stream()
                .filter(r -> r.getStatus() == ExpansionRequest.Status.PENDING)
                .collect(Collectors.toList());
    }

    public List<ExpansionRequest> getPendingRequestsFor(UUID requester) {
        return getRequests(requester).stream()
                .filter(r -> r.getStatus() == ExpansionRequest.Status.PENDING)
                .collect(Collectors.toList());
    }

    public OfflinePlayer getOfflinePlayer(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }

    /* -------------------
     * Persistence
     * ------------------- */

    public void saveAll() {
        dataConfig.set("requests", null);

        for (Map.Entry<UUID, List<ExpansionRequest>> entry : requests.entrySet()) {
            String base = "requests." + entry.getKey();
            List<ExpansionRequest> list = entry.getValue();

            for (int i = 0; i < list.size(); i++) {
                ExpansionRequest req = list.get(i);
                String path = base + "." + i;
                dataConfig.set(path + ".amount", req.getAmount());
                dataConfig.set(path + ".timestamp", req.getTimestamp().toString());
                dataConfig.set(path + ".status", req.getStatus().name());
                if (req.getReviewedBy() != null) {
                    dataConfig.set(path + ".reviewedBy", req.getReviewedBy().toString());
                }
                if (req.getDenialReason() != null) {
                    dataConfig.set(path + ".reason", req.getDenialReason());
                }
            }
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save expansions.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void load() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create expansions.yml");
            }
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        requests.clear();

        if (!dataConfig.contains("requests")) return;

        for (String uuidStr : dataConfig.getConfigurationSection("requests").getKeys(false)) {
            try {
                UUID playerUuid = UUID.fromString(uuidStr);
                List<ExpansionRequest> list = new ArrayList<>();

                for (String index : dataConfig.getConfigurationSection("requests." + uuidStr).getKeys(false)) {
                    String base = "requests." + uuidStr + "." + index;
                    int amount = dataConfig.getInt(base + ".amount", 16);
                    String tsStr = dataConfig.getString(base + ".timestamp", Instant.now().toString());
                    Instant ts = Instant.parse(tsStr);

                    ExpansionRequest.Status status = ExpansionRequest.Status.valueOf(
                            dataConfig.getString(base + ".status", "PENDING"));
                    UUID reviewer = null;
                    if (dataConfig.contains(base + ".reviewedBy")) {
                        reviewer = UUID.fromString(dataConfig.getString(base + ".reviewedBy"));
                    }
                    String reason = dataConfig.getString(base + ".reason");

                    ExpansionRequest req = new ExpansionRequest(playerUuid, amount, ts);
                    req.setStatus(status);
                    req.setReviewedBy(reviewer);
                    req.setDenialReason(reason);

                    list.add(req);
                }
                requests.put(playerUuid, list);

            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to load expansion requests for " + uuidStr + ": " + ex.getMessage());
            }
        }

        expireOldRequests();
    }

    /** Clears all requests from memory and disk. */
    public void clear() {
        requests.clear();
        if (dataConfig != null) {
            dataConfig.set("requests", null);
            try {
                dataConfig.save(dataFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to clear expansions.yml: " + e.getMessage());
            }
        }
    }
}
