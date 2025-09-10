package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Represents a single land claim (plot).
 * - Stores owner, location, trusted players/roles
 * - Holds per-claim settings (PvP, keep items, explosions, fire, mob grief)
 */
public class Plot {

    private final UUID owner;
    private final String world;
    private final int x;
    private final int z;

    // Trusted players (UUID → role string)
    private final Map<UUID, String> trusted = new HashMap<>();

    // Claim-specific settings (PvP, explosions, etc.)
    private final PlotSettings settings = new PlotSettings();

    public Plot(UUID owner, Chunk chunk) {
        this.owner = owner;
        this.world = chunk.getWorld().getName();
        this.x = chunk.getX();
        this.z = chunk.getZ();
    }

    public Plot(UUID owner, String world, int x, int z) {
        this.owner = owner;
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public PlotSettings getSettings() {
        return settings;
    }

    public Map<UUID, String> getTrusted() {
        return trusted;
    }

    public boolean isTrusted(UUID uuid) {
        return trusted.containsKey(uuid);
    }

    public void trust(UUID uuid, String role) {
        trusted.put(uuid, role);
    }

    public void untrust(UUID uuid) {
        trusted.remove(uuid);
    }

    /**
     * Load this plot's data from config.
     */
    public void loadFromConfig(ConfigurationSection section) {
        // === Trusted players ===
        ConfigurationSection trustedSec = section.getConfigurationSection("trusted");
        if (trustedSec != null) {
            for (String uuidStr : trustedSec.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                String role = trustedSec.getString(uuidStr, "member");
                trusted.put(uuid, role);
            }
        }

        // === Settings (new) ===
        ConfigurationSection settingsSec = section.getConfigurationSection("settings");
        if (settingsSec != null) {
            settings.setKeepItemsEnabled(settingsSec.getBoolean("keep-items", false));
            settings.setPvpEnabled(settingsSec.getBoolean("pvp", false));
            settings.setExplosionsEnabled(settingsSec.getBoolean("explosions", false));
            settings.setFireEnabled(settingsSec.getBoolean("fire", false));
            settings.setMobGriefEnabled(settingsSec.getBoolean("mob-grief", false));
        }
    }

    /**
     * Save this plot's data to config.
     */
    public void saveToConfig(ConfigurationSection section) {
        // === Owner ===
        section.set("owner", owner.toString());

        // === Trusted players ===
        ConfigurationSection trustedSec = section.createSection("trusted");
        for (Map.Entry<UUID, String> entry : trusted.entrySet()) {
            trustedSec.set(entry.getKey().toString(), entry.getValue());
        }

        // === Settings (new) ===
        ConfigurationSection settingsSec = section.createSection("settings");
        settingsSec.set("keep-items", settings.isKeepItemsEnabled());
        settingsSec.set("pvp", settings.isPvpEnabled());
        settingsSec.set("explosions", settings.isExplosionsEnabled());
        settingsSec.set("fire", settings.isFireEnabled());
        settingsSec.set("mob-grief", settings.isMobGriefEnabled());
    }

    @Override
    public String toString() {
        return "Plot{" +
                "owner=" + owner +
                ", world='" + world + '\'' +
                ", x=" + x +
                ", z=" + z +
                ", trusted=" + trusted +
                ", settings=" + settings +
                '}';
    }
}
