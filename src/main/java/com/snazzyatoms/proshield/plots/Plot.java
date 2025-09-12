package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.*;

public class Plot {

    private final UUID id;          // Unique claim id
    private final UUID owner;       // Owner of the claim
    private final Chunk chunk;      // Chunk reference

    private final Set<String> trustedPlayers = new HashSet<>();
    private String claimName;

    public Plot(Chunk chunk, UUID owner) {
        this.id = UUID.randomUUID();
        this.chunk = chunk;
        this.owner = owner;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public String getClaimName() {
        return claimName != null ? claimName : "Claim " + chunk.getX() + "," + chunk.getZ();
    }

    public void setClaimName(String claimName) {
        this.claimName = claimName;
    }

    public void addTrusted(String name) {
        trustedPlayers.add(name.toLowerCase());
    }

    public void removeTrusted(String name) {
        trustedPlayers.remove(name.toLowerCase());
    }

    public boolean isTrusted(String name) {
        return trustedPlayers.contains(name.toLowerCase());
    }

    public List<String> getTrustedNames() {
        return new ArrayList<>(trustedPlayers);
    }

    public String getOwnerNameSafe() {
        return owner != null ? owner.toString() : "Unknown";
    }

    public boolean isOwner(UUID uuid) {
        return owner.equals(uuid);
    }

    public boolean contains(Location loc) {
        return loc.getChunk().equals(this.chunk);
    }
}
