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

    public ExpansionRequest(UUID requester, int amount, Instant timestamp, Status status) {
        this.requester = requester;
        this.amount = amount;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.status = status != null ? status : Status.PENDING;
    }

    // Full constructor (used during loading from file)
    public ExpansionRequest(UUID requester, int amount, Instant timestamp, Status status, String denialReason, UUID reviewedBy) {
        this(requester, amount, timestamp, status);
        this.denialReason = denialReason;
        this.reviewedBy = reviewedBy;
    }

    public UUID getRequester() { return requester; }
    public int getAmount() { return amount; }
    public Instant getTimestamp() { return timestamp; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getDenialReason() { return denialReason; }
    public void setDenialReason(String reason) { this.denialReason = reason; }

    public UUID getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UUID reviewedBy) { this.reviewedBy = reviewedBy; }

    /** @deprecated Use getStatus() == Status.APPROVED */
    @Deprecated
    public boolean isApproved() { return status == Status.APPROVED; }
}
