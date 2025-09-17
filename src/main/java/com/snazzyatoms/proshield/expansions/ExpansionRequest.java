package com.snazzyatoms.proshield.expansions;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExpansionRequest {

    public enum Status { PENDING, APPROVED, DENIED, EXPIRED }

    private final UUID requester;
    private final int amount;
    private final Instant timestamp;
    private Status status;
    private UUID reviewedBy;
    private String denialReason;

    // Convenience constructor (fresh request, defaults to PENDING)
    public ExpansionRequest(UUID requester, int amount) {
        this(requester, amount, Instant.now(), Status.PENDING, null, null);
    }

    // Full constructor
    public ExpansionRequest(UUID requester, int amount, Instant timestamp, Status status,
                            UUID reviewedBy, String denialReason) {
        this.requester = requester;
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = status;
        this.reviewedBy = reviewedBy;
        this.denialReason = denialReason;
    }

    /* -------------------
     * Getters / Setters
     * ------------------- */
    public UUID getRequester() { return requester; }
    public int getAmount() { return amount; }
    public Instant getTimestamp() { return timestamp; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public UUID getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UUID reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getDenialReason() { return denialReason; }
    public void setDenialReason(String denialReason) { this.denialReason = denialReason; } // ✅ Correct name

    public boolean isApproved() { return status == Status.APPROVED; }

    /* -------------------
     * Serialization
     * ------------------- */

    /** Convert to a map for saving into YAML/JSON configs */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("requester", requester.toString());
        map.put("amount", amount);
        map.put("timestamp", timestamp.toString());
        map.put("status", status.name());
        if (reviewedBy != null) map.put("reviewedBy", reviewedBy.toString());
        if (denialReason != null) map.put("denialReason", denialReason);
        return map;
    }

    /** Rebuild from a saved map */
    public static ExpansionRequest fromMap(Map<String, Object> map) {
        try {
            UUID requester = UUID.fromString((String) map.get("requester"));
            int amount = (int) map.get("amount");
            Instant ts = Instant.parse((String) map.get("timestamp"));
            Status status = Status.valueOf((String) map.get("status"));

            UUID reviewedBy = map.containsKey("reviewedBy")
                    ? UUID.fromString((String) map.get("reviewedBy"))
                    : null;
            String denialReason = (String) map.getOrDefault("denialReason", null);

            return new ExpansionRequest(requester, amount, ts, status, reviewedBy, denialReason);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
