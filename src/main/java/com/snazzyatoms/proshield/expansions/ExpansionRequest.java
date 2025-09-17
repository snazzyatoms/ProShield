package com.snazzyatoms.proshield.expansions;

import java.time.Instant;
import java.util.UUID;

public class ExpansionRequest {

    public enum Status { PENDING, APPROVED, DENIED, EXPIRED }

    private final UUID requester;
    private final int amount;
    private final Instant timestamp;
    private Status status;
    private String denialReason;
    private UUID reviewedBy;

    public ExpansionRequest(UUID requester, int amount) {
        this(requester, amount, Instant.now(), Status.PENDING, null, null);
    }

    // ✅ Middle constructor required by manager
    public ExpansionRequest(UUID requester, int amount, Instant timestamp, Status status) {
        this(requester, amount, timestamp, status, null, null);
    }

    public ExpansionRequest(UUID requester, int amount, Instant timestamp, Status status, String denialReason, UUID reviewedBy) {
        this.requester = requester;
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = status;
        this.denialReason = denialReason;
        this.reviewedBy = reviewedBy;
    }

    public UUID getRequester() { return requester; }
    public int getAmount() { return amount; }
    public Instant getTimestamp() { return timestamp; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    // ✅ Proper getter/setter for denial reason
    public String getDenialReason() { return denialReason; }
    public void setDenialReason(String denialReason) { this.denialReason = denialReason; }

    // ✅ Correct reviewedBy methods (matches ExpansionRequestManager)
    public UUID getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UUID reviewedBy) { this.reviewedBy = reviewedBy; }

    public boolean isApproved() { return status == Status.APPROVED; }
}
