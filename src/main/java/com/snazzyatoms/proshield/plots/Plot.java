package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Represents a protected claim (plot).
 * Stores owner, trusted players/roles, and per-claim flags.
 */
public class Plot {

    private final UUID owner;
    private final Chunk chunk;
    private final Set<UUID> trustedPlayers;
    private final Map<String, Boolean> flags; // Per-claim toggle flags

    public Plot(UUID owner, Chunk chunk) {
        this.owner = owner;
        this.chunk = chunk;
        this.trustedPlayers = new HashSet<>();
        this.flags = new HashMap<>();
    }

    public UUID getOwner() {
        return owner;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public Set<UUID> getTrustedPlayers() {
        return trustedPlayers;
    }

    public void addTrusted(UUID player) {
        trustedPlayers.add(player);
    }

    public void removeTrusted(UUID player) {
        trustedPlayers.remove(player);
    }

    public boolean isTrusted(UUID player) {
        return trustedPlayers.contains(player);
    }

    // -------------------------------
    // FLAG SYSTEM
    // -------------------------------

    /**
     * Toggle or set a flag inside this claim.
     */
    public void setFlag(String key, boolean value) {
        flags.put(key.toLowerCase(Locale.ROOT), value);
    }

    /**
     * Check if a flag is enabled in this claim.
     * Defaults to false if not explicitly set.
     */
    public boolean isFlagEnabled(String key) {
        return flags.getOrDefault(key.toLowerCase(Locale.ROOT), false);
    }

    /**
     * Get all flags stored for this plot.
     */
    public Map<String, Boolean> getFlags() {
        return Collections.unmodifiableMap(flags);
    }

    /**
     * Load claim flags from a config section.
     */
    public void loadFlags(ConfigurationSection section) {
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            flags.put(key.toLowerCase(Locale.ROOT), section.getBoolean(key));
        }
    }

    /**
     * Save claim flags to a config section.
     */
    public void saveFlags(ConfigurationSection section) {
        for (Map.Entry<String, Boolean> entry : flags.entrySet()) {
            section.set(entry.getKey(), entry.getValue());
        }
    }

    // -------------------------------
    // SERIALIZATION
    // -------------------------------

    /**
     * Save this plot into config storage.
     */
    public void save(ConfigurationSection section) {
        section.set("owner", owner.toString());

        List<String> trusted = new ArrayList<>();
        for (UUID uuid : trustedPlayers) {
            trusted.add(uuid.toString());
        }
        section.set("trusted", trusted);

        ConfigurationSection flagSec = section.createSection("flags");
        saveFlags(flagSec);
    }

    /**
     * Load this plot from config storage.
     */
    public static Plot load(ConfigurationSection section, Chunk chunk) {
        UUID owner = UUID.fromString(section.getString("owner"));
        Plot plot = new Plot(owner, chunk);

        List<String> trusted = section.getStringList("trusted");
        for (String uuidStr : trusted) {
            plot.addTrusted(UUID.fromString(uuidStr));
        }

        ConfigurationSection flagSec = section.getConfigurationSection("flags");
        plot.loadFlags(flagSec);

        return plot;
    }
}
