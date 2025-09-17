package com.snazzyatoms.proshield.expansions;

import java.time.Instant;
import java.util.UUID;

public class ExpansionRequest {

    public enum Status { PENDING, APPROVED, DENIED, EXPIRED }

    private final UUID requester;
    private final int amount;
    private final Instant timestamp;
    private Status status;
    private UUID reviewedBy;
    private String denialReason;

    public ExpansionRequest(UUID requester, int amount, Instant timestamp, Status status, UUID reviewedBy, String denialReason) {
        this.requester = requester;
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = status;
        this.reviewedBy = reviewedBy;
        this.denialReason = denialReason;
    }

    public UUID getRequester() { return requester; }
    public int getAmount() { return amount; }
    public Instant getTimestamp() { return timestamp; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public UUID getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UUID reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getDenialReason() { return denialReason; }
    public void setDenyReason(String denialReason) { this.denialReason = denialReason; }

    public boolean isApproved() { return status == Status.APPROVED; }
}
