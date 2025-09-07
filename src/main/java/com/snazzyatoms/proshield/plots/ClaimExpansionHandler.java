package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 1.2 scaffold: expandable border claims.
 * For now, it treats each expanded chunk as an individual claim owned by the same player,
 * while enforcing a per-claim max chunk count from config (future: union shapes).
 */
public class ClaimExpansionHandler {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public enum Direction { NORTH, SOUTH, EAST, WEST }

    public ClaimExpansionHandler(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    /** Expand a claim by adding N chunks in one direction from the player's current chunk. */
    public boolean expand(UUID owner, Location base, Direction dir, int amount) {
        if (amount <= 0) return false;

        // limit: max chunks per "region" (soft-limited until we store regions)
        int maxChunks = plugin.getConfig().getInt("limits.max-claim-chunks", 16);

        // Gather current "region" size = all adjacent owned chunks (simple flood)
        Set<String> region = collectContiguous(owner, base);
        if (region.size() >= maxChunks) return false;

        Chunk chunk = base.getChunk();
        int cx = chunk.getX();
        int cz = chunk.getZ();

        int added = 0;
        for (int i = 1; i <= amount; i++) {
            int nx = cx, nz = cz;
            switch (dir) {
                case NORTH -> nz = cz - i;
                case SOUTH -> nz = cz + i;
                case EAST  -> nx = cx + i;
                case WEST  -> nx = cx - i;
            }
            Location target = new Location(base.getWorld(), (nx << 4) + 8, base.getY(), (nz << 4) + 8);
            String k = key(target);
            if (plotManager.isClaimed(target)) continue;

            // size guard
            if (region.size() + added + 1 > maxChunks) break;

            if (plotManager.createClaim(owner, target)) {
                added++;
            }
        }
        return added > 0;
    }

    private String key(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getChunk().getX() + ":" + loc.getChunk().getZ();
    }

    // Very simple contiguous collection (4-neighbour BFS across owned chunks)
    private Set<String> collectContiguous(UUID owner, Location start) {
        Set<String> visited = new HashSet<>();
        Set<String> queue = new HashSet<>();
        queue.add(key(start));

        while (!queue.isEmpty()) {
            String k = queue.iterator().next();
            queue.remove(k);
            if (!visited.add(k)) continue;

            String[] p = k.split(":");
            String world = p[0];
            int x = Integer.parseInt(p[1]);
            int z = Integer.parseInt(p[2]);

            // check owner for this chunk
            Location center = new Location(start.getWorld(), (x << 4) + 8, start.getY(), (z << 4) + 8);
            if (!plotManager.isOwner(owner, center)) continue;

            // neighbours
            queue.add(world + ":" + (x + 1) + ":" + z);
            queue.add(world + ":" + (x - 1) + ":" + z);
            queue.add(world + ":" + x + ":" + (z + 1));
            queue.add(world + ":" + x + ":" + (z - 1));
        }
        return visited;
    }
}
