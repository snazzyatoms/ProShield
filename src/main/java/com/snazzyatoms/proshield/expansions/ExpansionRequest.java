package com.snazzyatoms.proshield.expansions;

import java.util.UUID;

/**
 * Represents a pending claim expansion request.
 * Stores requester, target claim, requested increase, and timestamp.
 */
public class ExpansionRequest {

    private final UUID requester;      // Player UUID
    private final UUID plotId;         // Claim/Plot ID
    private final int amount;          // How many blocks to expand radius
    private final long timestamp;      // When the request was made

    public ExpansionRequest(UUID requester, UUID plotId, int amount, long timestamp) {
        this.requester = requester;
        this.plotId = plotId;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    /** Player UUID who made the request */
    public UUID getRequester() {
        return requester;
    }

    /** Target plot/claim ID */
    public UUID getPlotId() {
        return plotId;
    }

    /** Amount of blocks to expand */
    public int getAmount() {
        return amount;
    }

    /** When request was created (ms since epoch) */
    public long getTimestamp() {
        return timestamp;
    }
}
