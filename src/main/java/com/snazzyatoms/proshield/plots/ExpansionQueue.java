// src/main/java/com/snazzyatoms/proshield/plots/ExpansionQueue.java
package com.snazzyatoms.proshield.plots;

import java.util.*;

public class ExpansionQueue {

    private final List<ExpansionRequest> requests = new ArrayList<>();

    public void add(ExpansionRequest req) {
        requests.add(req);
    }

    public List<ExpansionRequest> getAll() {
        return new ArrayList<>(requests); // copy to avoid modification issues
    }

    public void remove(ExpansionRequest req) {
        requests.remove(req);
    }

    public boolean hasPending(UUID playerId) {
        return requests.stream().anyMatch(r -> r.getPlayerId().equals(playerId));
    }
}
