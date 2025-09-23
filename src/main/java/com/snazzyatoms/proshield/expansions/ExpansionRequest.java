// src/main/java/com/snazzyatoms/proshield/expansions/ExpansionRequest.java
package com.snazzyatoms.proshield.expansions;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ExpansionRequest (ProShield v1.2.6)
 *
 * Represents a single expansion request by a player.
 * Stores: requester, amount, status, timestamps, reviewer, denial reason.
 * Serializable to / from YAML maps for persistence.
 */
public class ExpansionRequest {

    public enum Status {
        PENDING,
        APPROVED,
        DENIED,
        EXPIRED
    }

    private final UUID id;           // Unique request ID
    private final UUID requester;    // Player who requested
    private final int amount;        // Blocks requested
    private final Instant createdAt; // When request was made

    private Status status;           // Pending / Approved / Denied / Expired
    private UUID reviewedBy;         // Admin UUID
    private Instant reviewedAt;      // When it was reviewed
    private String denialReason;     // Optional denial reason

    /* -------------------
     * Constructors
     * ------------------- */

    /** Convenience constructor: new request, pending by default */
    public ExpansionRequest(UUID requester, int amount) {
        this(UUID.randomUUID(), requester, amount,
                Status.PENDING, null, null, null, Instant.now());
    }

    /** Full constructor */
    public ExpansionRequest(UUID id, UUID requester, int amount, Status status,
                            UUID reviewedBy, Instant reviewedAt,
                            String denialReason, Instant createdAt) {
        this.id = id;
        this.requester = requester;
        this.amount = amount;
        this.status = status;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.denialReason = denialReason;
        this.createdAt = createdAt;
    }

    /* -------------------
     * Getters / Setters
     * ------------------- */

    public UUID getId() { return id; }
    public UUID getRequester() { return requester; }
    public int getAmount() { return amount; }
    public Instant getCreatedAt() { return createdAt; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public UUID getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UUID reviewedBy) { this.reviewedBy = reviewedBy; }

    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getDenialReason() { return denialReason; }
    public void setDenialReason(String denialReason) { this.denialReason = denialReason; }

    public boolean isApproved() { return status == Status.APPROVED; }
    public boolean isPending() { return status == Status.PENDING; }
    public boolean isDenied() { return status == Status.DENIED; }
    public boolean isExpired() { return status == Status.EXPIRED; }

    /* -------------------
     * Serialization
     * ------------------- */

    /** Convert this request into a map for YAML storage */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id.toString());
        map.put("requester", requester.toString());
        map.put("amount", amount);
        map.put("status", status.name());
        map.put("createdAt", createdAt.toEpochMilli());
        if (reviewedBy != null) map.put("reviewedBy", reviewedBy.toString());
        if (reviewedAt != null) map.put("reviewedAt", reviewedAt.toEpochMilli());
        if (denialReason != null) map.put("denialReason", denialReason);
        return map;
    }

    /** Restore an ExpansionRequest from a YAML map */
    public static ExpansionRequest fromMap(Map<String, Object> map) {
        try {
            UUID id = map.containsKey("id") ? UUID.fromString((String) map.get("id")) : UUID.randomUUID();
            UUID requester = UUID.fromString((String) map.get("requester"));
            int amount = (int) map.get("amount");
            Status status = Status.valueOf((String) map.get("status"));

            Instant createdAt = map.containsKey("createdAt")
                    ? Instant.ofEpochMilli(((Number) map.get("createdAt")).longValue())
                    : Instant.now();

            UUID reviewedBy = map.containsKey("reviewedBy") && map.get("reviewedBy") != null
                    ? UUID.fromString((String) map.get("reviewedBy"))
                    : null;

            Instant reviewedAt = map.containsKey("reviewedAt") && map.get("reviewedAt") != null
                    ? Instant.ofEpochMilli(((Number) map.get("reviewedAt")).longValue())
                    : null;

            String denialReason = (String) map.getOrDefault("denialReason", null);

            return new ExpansionRequest(id, requester, amount, status,
                    reviewedBy, reviewedAt, denialReason, createdAt);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* -------------------
     * Compatibility Shims (for GUIManager 1.2.6)
     * ------------------- */

    /** Shortcut used in GUIManager to approve a request */
    public void approve() {
        this.status = Status.APPROVED;
        this.reviewedAt = Instant.now();
        this.denialReason = null;
    }

    /** Shortcut used in GUIManager to deny a request */
    public void deny() {
        this.status = Status.DENIED;
        this.reviewedAt = Instant.now();
    }
}
