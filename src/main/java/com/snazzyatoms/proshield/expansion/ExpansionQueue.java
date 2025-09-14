// src/main/java/com/snazzyatoms/proshield/expansion/ExpansionQueue.java
package com.snazzyatoms.proshield.expansion;

import java.util.*;

/**
 * ExpansionQueue
 * ----------------
 * Central manager for handling expansion requests.
 *
 * - Stores all requests (per player).
 * - Allows admins to view pending/approved/denied requests.
 * - Used by GUI handlers to render requests and process approvals/denials.
 */
public class ExpansionQueue {

    private static final Map<UUID, List<ExpansionRequest>> requests = new HashMap<>();

    /**
     * Submit a new expansion request for a player.
     */
    public static ExpansionRequest submitRequest(UUID playerId, int extraRadius) {
        ExpansionRequest request = new ExpansionRequest(playerId, extraRadius);
        requests.computeIfAbsent(playerId, k -> new ArrayList<>()).add(request);
        return request;
    }

    /**
     * Get all requests for a specific player.
     */
    public static List<ExpansionRequest> getRequests(UUID playerId) {
        return requests.getOrDefault(playerId, Collections.emptyList());
    }

    /**
     * Get all pending requests (for admin GUI).
     */
    public static List<ExpansionRequest> getPendingRequests() {
        List<ExpansionRequest> pending = new ArrayList<>();
        for (List<ExpansionRequest> list : requests.values()) {
            for (ExpansionRequest req : list) {
                if (req.isPending()) {
                    pending.add(req);
                }
            }
        }
        return pending;
    }

    /**
     * Approve a request instantly.
     */
    public static void approveRequest(ExpansionRequest request) {
        request.approve();
    }

    /**
     * Deny a request with a reason.
     */
    public static void denyRequest(ExpansionRequest request, String reason) {
        request.deny(reason);
    }

    /**
     * Simple utility to clear all requests (e.g., on plugin disable).
     */
    public static void clear() {
        requests.clear();
    }
}
