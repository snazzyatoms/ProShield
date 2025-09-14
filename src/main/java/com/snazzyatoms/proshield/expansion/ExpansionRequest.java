// src/main/java/com/snazzyatoms/proshield/expansion/ExpansionRequest.java
package com.snazzyatoms.proshield.expansion;

import com.snazzyatoms.proshield.plots.Plot;

import java.util.UUID;

/**
 * ExpansionRequest
 * ----------------
 * Represents a player's request to expand their claim.
 * Keeps track of status, time, denial reason, and ties into Plot.
 */
public class ExpansionRequest {

    public enum Status {
        PENDING,
        APPROVED,
        DENIED
    }

    private final UUID playerId;
    private final int extraRadius;
    private final long requestTime;

    private Status status;
    private String denialReason; // Optional reason if denied

    public ExpansionRequest(UUID playerId, int extraRadius) {
        this.playerId = playerId;
        this.extraRadius = extraRadius;
        this.requestTime = System.currentTimeMillis();
        this.status = Status.PENDING;
    }

    // --- Getters ---
    public UUID getPlayerId() {
        return playerId;
    }

    public int getExtraRadius() {
        return extraRadius;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public Status getStatus() {
        return status;
    }

    public String getDenialReason() {
        return denialReason;
    }

    // --- State transitions ---
    public void approve(Plot plot) {
        if (plot != null && status == Status.PENDING) {
            plot.expand(extraRadius); // 🔑 actually expand claim
        }
        this.status = Status.APPROVED;
        this.denialReason = null;
    }

    public void deny(String reason) {
        this.status = Status.DENIED;
        this.denialReason = reason;
    }

    public boolean isPending() {
        return this.status == Status.PENDING;
    }

    public boolean isApproved() {
        return this.status == Status.APPROVED;
    }

    public boolean isDenied() {
        return this.status == Status.DENIED;
    }

    // --- Utility for GUI display ---
    public String getSummary() {
        String base = "Request +" + extraRadius + " blocks";
        return switch (status) {
            case PENDING -> base + " (§ePending§r)";
            case APPROVED -> base + " (§aApproved§r)";
            case DENIED -> base + " (§cDenied§r: " + (denialReason != null ? denialReason : "No reason") + ")";
        };
    }
}
