package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class Claim {
    private final UUID owner;
    private final String ownerName;
    private final Chunk chunk;
    private long lastActive;

    private final Map<UUID, String> trustedPlayers = new HashMap<>();
    private final Map<String, Boolean> flags = new HashMap<>();

    public Claim(UUID owner, String ownerName, Chunk chunk) {
        this.owner = owner;
        this.ownerName = ownerName;
        this.chunk = chunk;
        this.lastActive = System.currentTimeMillis();

        // Default flags (inherit from config.yml normally, but hard defaults here)
        flags.put("pvp", false);
        flags.put("fire", false);
        flags.put("explosions", false);
        flags.put("entity-grief", false);
    }

    // ==========================
    // Ownership
    // ==========================

    public UUID getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public boolean isOwner(Player p) {
        return p.getUniqueId().equals(owner);
    }

    // ==========================
    // Trusted Players
    // ==========================

    public void trust(UUID player, String role) {
        trustedPlayers.put(player, role.toLowerCase());
        touch();
    }

    public void untrust(UUID player) {
        trustedPlayers.remove(player);
        touch();
    }

    public boolean isTrusted(UUID player, String minRole) {
        if (trustedPlayers.containsKey(player)) {
            String role = trustedPlayers.get(player);
            return roleRank(role) >= roleRank(minRole);
        }
        return false;
    }

    public Map<UUID, String> getTrustedPlayers() {
        return Collections.unmodifiableMap(trustedPlayers);
    }

    // Role hierarchy
    private int roleRank(String role) {
        return switch (role.toLowerCase()) {
            case "visitor" -> 0;
            case "member" -> 1;
            case "container" -> 2;
            case "builder" -> 3;
            case "co-owner" -> 4;
            default -> 0;
        };
    }

    // ==========================
    // Flags
    // ==========================

    public void setFlag(String flag, boolean value) {
        flags.put(flag.toLowerCase(), value);
        touch();
    }

    public boolean getFlag(String flag) {
        return flags.getOrDefault(flag.toLowerCase(), false);
    }

    public Map<String, Boolean> getFlags() {
        return Collections.unmodifiableMap(flags);
    }

    // ==========================
    // Claim Location
    // ==========================

    public Chunk getChunk() {
        return chunk;
    }

    public Location getCenter() {
        int x = (chunk.getX() << 4) + 8;
        int z = (chunk.getZ() << 4) + 8;
        return new Location(chunk.getWorld(), x, chunk.getWorld().getHighestBlockYAt(x, z), z);
    }

    // ==========================
    // Expiry / Activity
    // ==========================

    public long getLastActive() {
        return lastActive;
    }

    public void touch() {
        this.lastActive = System.currentTimeMillis();
    }
}
