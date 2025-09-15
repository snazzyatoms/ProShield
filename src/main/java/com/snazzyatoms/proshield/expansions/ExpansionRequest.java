package com.snazzyatoms.proshield.expansions;

import org.bukkit.Location;

import java.util.UUID;

public class ExpansionRequest {

    public enum Status { PENDING, APPROVED, DENIED }

    private final UUID requester;
    private final int blocks;
    private final long timestamp;
    private final Location requestedAt; // 🔹 Where the request was made

    private Status status;
    private String denyReason;

    public ExpansionRequest(UUID requester, int blocks, Location requestedAt) {
        this.requester = requester;
        this.blocks = blocks;
        this.requestedAt = requestedAt;
        this.timestamp = System.currentTimeMillis();
        this.status = Status.PENDING;
    }

    /* ==================== Accessors ==================== */

    public UUID getRequester() { return requester; }
    public int getBlocks() { return blocks; }
    public long getTimestamp() { return timestamp; }

    public Location getRequestedAt() { return requestedAt; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getDenyReason() { return denyReason; }
    public void setDenyReason(String denyReason) { this.denyReason = denyReason; }
}
