package com.snazzyatoms.proshield.expansions;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a player's expansion request for their claim.
 * Includes metadata such as status, denial reason, and reviewed admin.
 */
public class ExpansionRequest {

    public enum Status {
        PENDING,
        APPROVED,
        DENIED,
        EXPIRED
    }

    private final UUID requester;
    private final int amount;
    private final Instant timestamp;

    private Status status;
    private String denialReason;   // Optional: Only used if DENIED
    private UUID reviewedBy;       // Optional: Only used if reviewed

    // Basic constructor for new requests
    public ExpansionRequest(UUID requester, int amount) {
        this.requester = requester;
        this.amount = amount;
        this.timestamp = Instant.now();
        this.status = Status.PENDING;
    }

    // Full constructor for loading from file or advanced use
    public ExpansionRequest(UUID requester, int amount, Instant timestamp, Status status, String denialReason, UUID reviewedBy) {
        this.requester = requester;
        this.amount = amount;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.status = status != null ? status : Status.PENDING;
        this.denialReason = denialReason;
        this.reviewedBy = reviewedBy;
    }

    public UUID getRequester() {
        return requester;
    }

    public int getAmount() {
        return amount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getDenialReason() {
        return denialReason;
    }

    public void setDenialReason(String denialReason) {
        this.denialReason = denialReason;
    }

    public UUID getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(UUID reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    /** @deprecated Use getStatus() == Status.APPROVED */
    @Deprecated
    public boolean isApproved() {
        return status == Status.APPROVED;
    }
}
