package com.snazzyatoms.proshield.plots;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

/**
 * Represents a single land claim.
 */
public class Plot {

    private final UUID id;
    private final UUID owner;
    private final String worldName;
    private final int chunkX;
    private final int chunkZ;
    private final PlotSettings settings;

    public Plot(UUID id, UUID owner, Chunk chunk) {
        this.id = id;
        this.owner = owner;
        this.worldName = chunk.getWorld().getName();
        this.chunkX = chunk.getX();
        this.chunkZ = chunk.getZ();
        this.settings = new PlotSettings();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public PlotSettings getSettings() {
        return settings;
    }

    /**
     * Load plot data from config section.
     */
    public static Plot fromConfig(UUID id, ConfigurationSection section) {
        UUID owner = UUID.fromString(section.getString("owner"));
        String worldName = section.getString("world");
        int x = section.getInt("x");
        int z = section.getInt("z");

        Chunk chunk = Bukkit.getWorld(worldName).getChunkAt(x, z);
        Plot plot = new Plot(id, owner, chunk);

        // load settings (PvP, keep-items, etc.)
        ConfigurationSection settingsSection = section.getConfigurationSection("settings");
        if (settingsSection != null) {
            plot.getSettings().load(settingsSection);
        }

        return plot;
    }

    /**
     * Save plot data into config section.
     */
    public void save(ConfigurationSection section) {
        section.set("owner", owner.toString());
        section.set("world", worldName);
        section.set("x", chunkX);
        section.set("z", chunkZ);

        ConfigurationSection settingsSection = section.createSection("settings");
        settings.save(settingsSection);
    }
}
