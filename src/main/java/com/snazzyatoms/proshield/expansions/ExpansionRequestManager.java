package com.snazzyatoms.proshield.expansions;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ExpansionRequestManager
 * Handles storage, approval/denial, and retrieval of ExpansionRequests.
 */
public class ExpansionRequestManager {

    private final ProShield plugin;
    private final Map<UUID, List<ExpansionRequest>> requests = new ConcurrentHashMap<>();

    public ExpansionRequestManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /* -------------------
     * Request Lifecycle
     * ------------------- */

    public ExpansionRequest createRequest(UUID requester, int amount) {
        ExpansionRequest req = new ExpansionRequest(requester, amount);
        requests.computeIfAbsent(requester, k -> new ArrayList<>()).add(req);
        return req;
    }

    public List<ExpansionRequest> getRequests(UUID requester) {
        return requests.getOrDefault(requester, Collections.emptyList());
    }

    public List<ExpansionRequest> getAllRequests() {
        return requests.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    public List<ExpansionRequest> getPendingRequests() {
        return getAllRequests().stream()
                .filter(r -> r.getStatus() == ExpansionRequest.Status.PENDING)
                .collect(Collectors.toList());
    }

    public void approveRequest(ExpansionRequest req, UUID reviewer) {
        req.setStatus(ExpansionRequest.Status.APPROVED);
        req.setReviewedBy(reviewer);
        plugin.getLogger().info("Approved expansion request from " + req.getRequester() + " for " + req.getAmount());
    }

    public void denyRequest(ExpansionRequest req, UUID reviewer, String reason) {
        req.setStatus(ExpansionRequest.Status.DENIED);
        req.setReviewedBy(reviewer);
        req.setDenialReason(reason); // ✅ Corrected method
        plugin.getLogger().info("Denied expansion request from " + req.getRequester() + " (" + reason + ")");
    }

    public void expireOldRequests() {
        Instant cutoff = Instant.now().minusSeconds(7 * 24 * 3600); // 7 days
        for (List<ExpansionRequest> list : requests.values()) {
            for (ExpansionRequest req : list) {
                if (req.getStatus() == ExpansionRequest.Status.PENDING &&
                        req.getTimestamp().isBefore(cutoff)) {
                    req.setStatus(ExpansionRequest.Status.EXPIRED);
                    plugin.getLogger().info("Expired expansion request from " + req.getRequester());
                }
            }
        }
    }

    /* -------------------
     * Persistence
     * ------------------- */

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        for (Map.Entry<UUID, List<ExpansionRequest>> e : requests.entrySet()) {
            List<Map<String, Object>> serialized = e.getValue().stream()
                    .map(ExpansionRequest::toMap)
                    .collect(Collectors.toList());
            data.put(e.getKey().toString(), serialized);
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    public void deserialize(Map<String, Object> data) {
        requests.clear();
        for (Map.Entry<String, Object> e : data.entrySet()) {
            try {
                UUID playerId = UUID.fromString(e.getKey());
                List<Map<String, Object>> list = (List<Map<String, Object>>) e.getValue();
                List<ExpansionRequest> rebuilt = new ArrayList<>();
                for (Map<String, Object> m : list) {
                    ExpansionRequest req = ExpansionRequest.fromMap(m);
                    if (req != null) rebuilt.add(req);
                }
                requests.put(playerId, rebuilt);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /* -------------------
     * Utility
     * ------------------- */

    public OfflinePlayer getOfflinePlayer(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public void clear() {
        requests.clear();
    }
}
