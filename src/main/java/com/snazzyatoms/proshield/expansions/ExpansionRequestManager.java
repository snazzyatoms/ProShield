package com.snazzyatoms.proshield.expansions;

import java.time.Instant;
import java.util.UUID;

public class ExpansionRequest {

    public enum Status { PENDING, APPROVED, DENIED, EXPIRED }

    private final UUID requester;
    private final int amount;
    private final Instant timestamp;
    private Status status;

    public ExpansionRequest(UUID requester, int amount, Instant timestamp, Status status) {
        this.requester = requester;
        this.amount = amount;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.status = status != null ? status : Status.PENDING;
    }

    public UUID getRequester() { return requester; }
    public int getAmount() { return amount; }
    public Instant getTimestamp() { return timestamp; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    // Legacy shim for older code paths
    /** @deprecated Use getStatus() == Status.APPROVED */
    @Deprecated
    public boolean isApproved() { return status == Status.APPROVED; }
}
