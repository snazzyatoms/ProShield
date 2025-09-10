package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Claim> claims = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    public ProShield getPlugin() {
        return plugin;
    }

    public boolean isInsideClaim(Location loc) {
        String key = getChunkKey(loc.getChunk());
        return claims.containsKey(key);
    }

    public Claim getClaim(Location loc) {
        return claims.get(getChunkKey(loc.getChunk()));
    }

    public void addClaim(Location loc, UUID owner) {
        claims.put(getChunkKey(loc.getChunk()), new Claim(owner));
    }

    public void removeClaim(Location loc) {
        claims.remove(getChunkKey(loc.getChunk()));
    }

    private String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + "," + chunk.getZ();
    }

    /**
     * Calculate distance from a location to the nearest claim border
     */
    public double distanceToClaimBorder(Location loc, double maxRadius) {
        Claim claim = getClaim(loc);
        if (claim == null) return maxRadius + 1;

        Chunk chunk = loc.getChunk();
        double cx = (chunk.getX() << 4) + 8; // center X of chunk
        double cz = (chunk.getZ() << 4) + 8; // center Z of chunk
        double half = 8; // half chunk size

        double dx = Math.max(0, Math.abs(loc.getX() - cx) - half);
        double dz = Math.max(0, Math.abs(loc.getZ() - cz) - half);

        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Find nearest border point of the claim (approx for repel vector)
     */
    public Location closestClaimBorder(Location loc) {
        Chunk chunk = loc.getChunk();
        World world = loc.getWorld();
        if (world == null) return null;

        double bx = Math.max(Math.min(loc.getX(), (chunk.getX() << 4) + 15), (chunk.getX() << 4));
        double bz = Math.max(Math.min(loc.getZ(), (chunk.getZ() << 4) + 15), (chunk.getZ() << 4));

        return new Location(world, bx, loc.getY(), bz);
    }
}
