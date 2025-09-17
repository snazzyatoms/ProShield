package com.snazzyatoms.proshield.expansions;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public class ExpansionRequest {

    public enum Status {
        PENDING, APPROVED, DENIED, EXPIRED
    }

    private final UUID requester;
    private final int amount;
    private final Instant timestamp;

    private Status status;
    private String denialReason; // Reason for denial (optional)
    private UUID reviewedBy;     // Admin who approved/denied

    public ExpansionRequest(UUID requester, int amount, Instant timestamp, Status status) {
        this.requester = Objects.requireNonNull(requester, "Requester cannot be null");
        this.amount = Math.max(1, amount);
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.status = status != null ? status : Status.PENDING;
    }

    // Quick constructor for pending requests
    public ExpansionRequest(UUID requester, int amount) {
        this(requester, amount, Instant.now(), Status.PENDING);
    }

    // Getters
    public UUID getRequester() { return requester; }
    public int getAmount() { return amount; }
    public Instant getTimestamp() { return timestamp; }
    public Status getStatus() { return status; }
    public String getDenialReason() { return denialReason; }
    public UUID getReviewedBy() { return reviewedBy; }

    // Status transitions
    public void approve(UUID admin) {
        this.status = Status.APPROVED;
        this.reviewedBy = admin;
    }

    public void deny(UUID admin, String reason) {
        this.status = Status.DENIED;
        this.reviewedBy = admin;
        this.denialReason = reason != null ? reason : "No reason provided.";
    }

    public void expire() {
        this.status = Status.EXPIRED;
    }

    public void setStatus(Status status) {
        this.status = status != null ? status : Status.PENDING;
    }

    // Status checks
    public boolean isPending() { return status == Status.PENDING; }
    public boolean isApproved() { return status == Status.APPROVED; }
    public boolean isDenied() { return status == Status.DENIED; }
    public boolean isExpired() { return status == Status.EXPIRED; }

    // Legacy compatibility
    /** @deprecated Use isApproved() instead */
    @Deprecated
    public boolean approved() { return status == Status.APPROVED; }

    public String getFormattedTimestamp() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault());
        return fmt.format(timestamp);
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
