package com.snazzyatoms.proshield.expansions;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single expansion request made by a player.
 * Includes requester UUID, amount requested, timestamp, status, reason for denial, and reviewer UUID.
 */
public class ExpansionRequest {

    public enum Status {
        PENDING, APPROVED, DENIED, EXPIRED
    }

    private final UUID requester;
    private final int amount;
    private final Instant timestamp;
    private Status status;
    private String denialReason;         // Optional reason for denial
    private UUID reviewedBy;             // UUID of admin who reviewed the request

    public ExpansionRequest(UUID requester, int amount, Instant timestamp, Status status) {
        this.requester = requester;
        this.amount = amount;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.status = status != null ? status : Status.PENDING;
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

    // Backward compatibility
    @Deprecated
    public boolean isApproved() {
        return status == Status.APPROVED;
    }

    public boolean isPending() {
        return status == Status.PENDING;
    }

    public boolean isDenied() {
        return status == Status.DENIED;
    }

    public boolean isExpired() {
        return status == Status.EXPIRED;
    }

    public boolean isFinalized() {
        return status == Status.APPROVED || status == Status.DENIED || status == Status.EXPIRED;
    }

    @Override
    public String toString() {
        return "ExpansionRequest{" +
                "requester=" + requester +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                ", status=" + status +
                ", denialReason='" + denialReason + '\'' +
                ", reviewedBy=" + reviewedBy +
                '}';
    }
}
