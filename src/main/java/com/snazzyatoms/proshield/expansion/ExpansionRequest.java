package com.snazzyatoms.proshield.expansion;

import java.util.UUID;

public class ExpansionRequest {

    public enum Status { PENDING, APPROVED, DENIED }

    private final UUID playerId;
    private final int extraRadius;
    private final long requestTime;

    private Status status;
    private String denialReason;

    public ExpansionRequest(UUID playerId, int extraRadius) {
        this.playerId = playerId;
        this.extraRadius = extraRadius;
        this.requestTime = System.currentTimeMillis();
        this.status = Status.PENDING;
    }

    public UUID getPlayerId() { return playerId; }
    public int getExtraRadius() { return extraRadius; }
    public long getRequestTime() { return requestTime; }
    public Status getStatus() { return status; }
    public String getDenialReason() { return denialReason; }

    public void approve() {
        this.status = Status.APPROVED;
        this.denialReason = null;
    }

    public void deny(String reason) {
        this.status = Status.DENIED;
        this.denialReason = reason;
    }

    public boolean isPending() { return status == Status.PENDING; }
}
