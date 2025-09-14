// src/main/java/com/snazzyatoms/proshield/expansion/ExpansionRequest.java
package com.snazzyatoms.proshield.expansion;

import java.util.UUID;

public class ExpansionRequest {
    private final UUID playerId;
    private final int extraRadius;
    private final long requestTime;

    public ExpansionRequest(UUID playerId, int extraRadius, long requestTime) {
        this.playerId = playerId;
        this.extraRadius = extraRadius;
        this.requestTime = requestTime;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getExtraRadius() {
        return extraRadius;
    }

    public long getRequestTime() {
        return requestTime;
    }
}
