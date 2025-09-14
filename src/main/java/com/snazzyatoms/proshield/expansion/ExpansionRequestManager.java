package com.snazzyatoms.proshield.expansion;

import java.util.*;

/**
 * ExpansionRequestManager
 *
 * ✅ Stores all expansion requests
 * ✅ Tracks which request an admin has selected
 */
public class ExpansionRequestManager {
    private static final List<ExpansionRequest> requests = new ArrayList<>();

    // Map: Admin UUID → Currently selected request (playerId)
    private static final Map<UUID, UUID> adminSelections = new HashMap<>();

    public static void addRequest(ExpansionRequest request) {
        requests.add(request);
    }

    public static List<ExpansionRequest> getRequests() {
        return Collections.unmodifiableList(requests);
    }

    public static void removeRequest(UUID playerId) {
        requests.removeIf(req -> req.getPlayerId().equals(playerId));

        // Clean up any admin selections pointing to this request
        adminSelections.values().removeIf(id -> id.equals(playerId));
    }

    public static boolean hasRequests() {
        return !requests.isEmpty();
    }

    /**
     * Select a request for an admin to review
     */
    public static void selectRequest(UUID adminId, UUID playerId) {
        adminSelections.put(adminId, playerId);
    }

    /**
     * Get the currently selected request for an admin
     */
    public static ExpansionRequest getSelectedRequest(UUID adminId) {
        UUID playerId = adminSelections.get(adminId);
        if (playerId == null) return null;

        return requests.stream()
                .filter(r -> r.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Clear an admin's selection (e.g., after approve/deny)
     */
    public static void clearSelection(UUID adminId) {
        adminSelections.remove(adminId);
    }
}
