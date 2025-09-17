package com.snazzyatoms.proshield.expansions;

import java.time.Instant;
import java.util.UUID;

public class ExpansionRequest {

    public enum Status { PENDING, APPROVED, DENIED, EXPIRED }

    private final UUID requester;
    private final int amount;
    private final Instant timestamp;
    private Status status;
    private String denyReason; // ✅ consistent name
    private UUID reviewer;     // ✅ consistent name

    public ExpansionRequest(UUID requester, int amount) {
        this(requester, amount, Instant.now(), Status.PENDING, null, null);
    }

    public ExpansionRequest(UUID requester, int amount, Instant timestamp, Status status) {
        this(requester, amount, timestamp, status, null, null);
    }

    public ExpansionRequest(UUID requester, int amount, Instant timestamp, Status status, String denyReason, UUID reviewer) {
        this.requester = requester;
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = status;
        this.denyReason = denyReason;
        this.reviewer = reviewer;
    }

    public UUID getRequester() { return requester; }
    public int getAmount() { return amount; }
    public Instant getTimestamp() { return timestamp; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    // ✅ fixed: use deny
    public String getDenyReason() { return denyReason; }
    public void setDenyReason(String denyReason) { this.denyReason = denyReason; }

    // ✅ fixed: use reviewer
    public UUID getReviewer() { return reviewer; }
    public void setReviewer(UUID reviewer) { this.reviewer = reviewer; }

    public boolean isApproved() { return status == Status.APPROVED; }
}
