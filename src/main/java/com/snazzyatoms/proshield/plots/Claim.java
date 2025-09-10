package com.snazzyatoms.proshield.plots;

import java.util.*;

/**
 * Represents a single ProShield claim.
 * Stores owner, trusted players with roles, flags, and metadata.
 */
public class Claim {

    private UUID owner;
    private final String chunkKey;

    // Trusted players: UUID → role (visitor, member, builder, etc.)
    private final Map<UUID, String> trusted = new HashMap<>();

    // Per-claim flags (PvP, explosions, fire, etc.)
    private final Map<String, Boolean> flags = new HashMap<>();

    // Metadata (for expiry, timestamps, etc.)
    private final Map<String, Object> meta = new HashMap<>();

    public Claim(UUID owner, String chunkKey) {
        this.owner = owner;
        this.chunkKey = chunkKey;

        // Default flags
        flags.put("pvp", false);
        flags.put("explosions", false);
        flags.put("fire", false);
        flags.put("mob-grief", false);
    }

    // === Core Access ===
    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID newOwner) {
        this.owner = newOwner;
    }

    public String getChunkKey() {
        return chunkKey;
    }

    // === Trusted Players ===
    public Map<UUID, String> getTrusted() {
        return trusted;
    }

    public void addTrusted(UUID uuid, String role) {
        trusted.put(uuid, role.toLowerCase(Locale.ROOT));
    }

    public void removeTrusted(UUID uuid) {
        trusted.remove(uuid);
    }

    public boolean isTrusted(UUID uuid) {
        return trusted.containsKey(uuid);
    }

    public String getRole(UUID uuid) {
        return trusted.getOrDefault(uuid, "visitor");
    }

    // === Flags ===
    public Map<String, Boolean> getFlags() {
        return flags;
    }

    public void setFlag(String flag, boolean value) {
        flags.put(flag.toLowerCase(Locale.ROOT), value);
    }

    public boolean getFlag(String flag) {
        return flags.getOrDefault(flag.toLowerCase(Locale.ROOT), false);
    }

    // === Metadata ===
    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(String key, Object value) {
        meta.put(key, value);
    }

    public Object getMeta(String key) {
        return meta.get(key);
    }

    public long getLastActive() {
        Object lastActive = meta.get("lastActive");
        if (lastActive instanceof Long l) {
            return l;
        }
        return System.currentTimeMillis();
    }

    public void updateLastActive() {
        meta.put("lastActive", System.currentTimeMillis());
    }
}
