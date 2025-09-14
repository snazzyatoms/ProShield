package com.snazzyatoms.proshield.expansion;

import java.util.*;

public class ExpansionQueue {

    private static final Map<UUID, List<ExpansionRequest>> requests = new HashMap<>();

    public static ExpansionRequest submitRequest(UUID playerId, int extraRadius) {
        ExpansionRequest request = new ExpansionRequest(playerId, extraRadius);
        requests.computeIfAbsent(playerId, k -> new ArrayList<>()).add(request);
        return request;
    }

    public static List<ExpansionRequest> getRequests(UUID playerId) {
        return requests.getOrDefault(playerId, Collections.emptyList());
    }

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

    public static void approveRequest(ExpansionRequest request) {
        request.approve();
    }

    public static void denyRequest(ExpansionRequest request, String reason) {
        request.deny(reason);
    }

    public static void clear() {
        requests.clear();
    }
}
