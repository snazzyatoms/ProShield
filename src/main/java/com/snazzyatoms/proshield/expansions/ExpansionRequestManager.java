package com.snazzyatoms.proshield.expansions;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.UUID;

/**
 * Handles creation, approval/denial, and storage of ExpansionRequests.
 * Pure manager (no GUI code here).
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

    public void approveRequest(ExpansionRequest req, UUID reviewer) {
        req.setStatus(ExpansionRequest.Status.APPROVED);
        req.setReviewedBy(reviewer);
        plugin.getLogger().info("Approved expansion request from " + req.getRequester() +
                " for " + req.getAmount());
    }

    public void denyRequest(ExpansionRequest req, UUID reviewer, String reason) {
        req.setStatus(ExpansionRequest.Status.DENIED);
        req.setReviewedBy(reviewer);
        req.setDenialReason(reason);
        plugin.getLogger().info("Denied expansion request from " + req.getRequester() +
                " (" + reason + ")");
    }

    /* -------------------
     * Utilities
     * ------------------- */

    public OfflinePlayer getOfflinePlayer(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public void clear() {
        requests.clear();
    }

    /**
     * Reload the expansion request manager.
     * Currently just clears requests (future: load from disk if persistence is added).
     */
    public void reload() {
        clear();
        plugin.getLogger().info("[ProShield] ExpansionRequestManager reloaded.");
    }
}
