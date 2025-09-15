package com.snazzyatoms.proshield.expansions;

import java.util.UUID;

public class ExpansionRequest {

    public enum Status {
        PENDING,
        APPROVED,
        DENIED
    }

    private final UUID requester;
    private final int blocks;
    private final long timestamp;
    private Status status;
    private String denyReason;

    public ExpansionRequest(UUID requester, int blocks) {
        this.requester = requester;
        this.blocks = blocks;
        this.timestamp = System.currentTimeMillis();
        this.status = Status.PENDING;
    }

    public UUID getRequester() { return requester; }
    public int getBlocks() { return blocks; }
    public long getTimestamp() { return timestamp; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getDenyReason() { return denyReason; }
    public void setDenyReason(String denyReason) { this.denyReason = denyReason; }
}
