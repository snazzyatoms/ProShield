package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * PlotManager
 * - In-memory map<world, map<chunkX, map<chunkZ, Plot>>>
 * - Handles claim, unclaim, lookup, info.
 * - Now supports radius-based expansion (square growth in chunks).
 *
 * NOTE on expansions:
 * - We treat the approved "amount" as an EXTRA RADIUS (in chunks), not blocks.
 * - Expansion grows a bounding rectangle of the owner's claimed area per world
 *   outward by extraRadius on all sides and claims any unclaimed chunks within.
 * - Chunks owned by other players are skipped (no overlaps).
 * - saveAll() is called after successful expansion so persistence can write to disk.
 */
public class PlotManager {

    private final ProShield plugin;
    private final MessagesUtil messages;

    // world -> x -> z -> Plot
    private final Map<String, Map<Integer, Map<Integer, Plot>>> plots = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
    }

    /* ========================
     * Index helpers
     * ======================== */
    private Map<Integer, Map<Integer, Plot>> worldIndex(String world) {
        return plots.computeIfAbsent(world.toLowerCase(Locale.ROOT), w -> new HashMap<>());
    }

    private Map<Integer, Plot> xIndex(String world, int x) {
        return worldIndex(world).computeIfAbsent(x, ix -> new HashMap<>());
    }

    private Plot get(String world, int x, int z) {
        Map<Integer, Map<Integer, Plot>> w = plots.get(world.toLowerCase(Locale.ROOT));
        if (w == null) return null;
        Map<Integer, Plot> row = w.get(x);
        if (row == null) return null;
        return row.get(z);
    }

    private void put(Plot plot) {
        xIndex(plot.getWorld(), plot.getX()).put(plot.getZ(), plot);
    }

    private void remove(String world, int x, int z) {
        Map<Integer, Map<Integer, Plot>> w = plots.get(world.toLowerCase(Locale.ROOT));
        if (w == null) return;
        Map<Integer, Plot> row = w.get(x);
        if (row == null) return;
        row.remove(z);
        if (row.isEmpty()) w.remove(x);
        if (w.isEmpty()) plots.remove(world.toLowerCase(Locale.ROOT));
    }

    private boolean isClaimed(String world, int x, int z) {
        return get(world, x, z) != null;
    }

    /* ========================
     * Persistence stubs
     * ======================== */
    public void loadAll() {
        // TODO: Load plots from disk
        // When loaded, call put(plot);
    }

    public void saveAll() {
        // TODO: Save plots to disk
    }

    /* ========================
     * Lookups
     * ======================== */
    public Plot getPlot(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        Chunk c = loc.getChunk();
        return get(loc.getWorld().getName(), c.getX(), c.getZ());
    }

    public UUID getClaimIdAt(Location loc) {
        Plot p = getPlot(loc);
        return p != null ? p.getId() : null;
    }

    public Plot getPlotByChunk(String world, int x, int z) {
        return get(world, x, z);
    }

    /**
     * Get all plots owned by a player across all worlds.
     */
    public Set<Plot> getPlotsByOwner(UUID owner) {
        Set<Plot> out = new HashSet<>();
        for (Map<Integer, Map<Integer, Plot>> w : plots.values()) {
            for (Map<Integer, Plot> row : w.values()) {
                for (Plot p : row.values()) {
                    if (p.getOwner().equals(owner)) out.add(p);
                }
            }
        }
        return out;
    }

    /**
     * Get all plots owned by a player in a specific world.
     */
    public Set<Plot> getPlotsByOwnerInWorld(UUID owner, String world) {
        Set<Plot> out = new HashSet<>();
        Map<Integer, Map<Integer, Plot>> w = plots.get(world.toLowerCase(Locale.ROOT));
        if (w == null) return out;
        for (Map<Integer, Plot> row : w.values()) {
            for (Plot p : row.values()) {
                if (p.getOwner().equals(owner)) out.add(p);
            }
        }
        return out;
    }

    /* ========================
     * Claim ops
     * ======================== */
    public boolean createPlot(UUID owner, Location at) {
        if (at == null || at.getWorld() == null) return false;
        Chunk c = at.getChunk();
        String w = at.getWorld().getName();
        if (get(w, c.getX(), c.getZ()) != null) return false;

        Plot plot = new Plot(UUID.randomUUID(), w, c.getX(), c.getZ(), owner);
        put(plot);
        return true;
    }

    public boolean removePlot(Location at) {
        if (at == null || at.getWorld() == null) return false;
        Chunk c = at.getChunk();
        Plot existing = get(at.getWorld().getName(), c.getX(), c.getZ());
        if (existing == null) return false;
        remove(at.getWorld().getName(), c.getX(), c.getZ());
        return true;
    }

    public void claimPlot(Player player) {
        if (player == null || player.getLocation() == null) return;
        Location at = player.getLocation();
        Plot existing = getPlot(at);
        if (existing != null) {
            String ownerName = plugin.getServer().getOfflinePlayer(existing.getOwner()).getName();
            messages.send(player, plugin.getMessagesConfig().getString("claim.already-owned", "&cThis chunk is already claimed by {owner}.")
                    .replace("{owner}", ownerName == null ? "Unknown" : ownerName));
            return;
        }
        if (createPlot(player.getUniqueId(), at)) {
            messages.send(player, plugin.getMessagesConfig().getString("claim.success", "&aYou successfully claimed this chunk."));
            saveAll();
        } else {
            messages.send(player, "&cFailed to claim here.");
        }
    }

    public void unclaimPlot(Player player) {
        if (player == null) return;
        Location at = player.getLocation();
        Plot p = getPlot(at);
        if (p == null) {
            messages.send(player, plugin.getMessagesConfig().getString("error.no-claim", "&cYou are not standing in a claim."));
            return;
        }
        if (!p.getOwner().equals(player.getUniqueId()) && !player.hasPermission("proshield.admin")) {
            messages.send(player, plugin.getMessagesConfig().getString("claim.not-owner", "&cYou are not the owner of this claim."));
            return;
        }
        if (removePlot(at)) {
            messages.send(player, plugin.getMessagesConfig().getString("claim.unclaimed", "&aYou unclaimed this chunk."));
            saveAll();
        } else {
            messages.send(player, "&cFailed to unclaim here.");
        }
    }

    /* ========================
     * Info
     * ======================== */
    public void sendClaimInfo(Player player) {
        Location at = player.getLocation();
        Plot plot = getPlot(at);
        if (plot == null) {
            messages.send(player, plugin.getMessagesConfig().getString("error.no-claim", "&cNo claim at your location."));
            return;
        }

        OfflinePlayer owner = plugin.getServer().getOfflinePlayer(plot.getOwner());
        String ownerName = (owner.getName() != null ? owner.getName() : owner.getUniqueId().toString());

        messages.send(player, "&6--- Claim Info ---");
        messages.send(player, "&eWorld: &f" + plot.getWorld());
        messages.send(player, "&eChunk: &f" + plot.getX() + ", " + plot.getZ());
        messages.send(player, "&eOwner: &f" + ownerName);
        messages.send(player, "&eTrusted: &f" + plot.getTrusted().size());

        if (plot.getFlags().isEmpty()) {
            messages.send(player, "&eFlags: &7None set.");
        } else {
            messages.send(player, "&eFlags: &f" + String.join(", ", plot.getFlags()));
        }
    }

    /* ========================
     * Radius-based Expansion
     * ======================== */

    /**
     * Expands a player's claims by growing the bounding rectangle of their claimed
     * chunks in each world outward by extraRadius (in chunks) on all sides.
     *
     * - Skips chunks already owned by the player (no duplicate).
     * - Skips chunks owned by others (no overlapping).
     * - Returns true if any new chunk was successfully claimed.
     *
     * IMPORTANT: The "extraRadius" unit is in CHUNKS (not blocks).
     * This should be called after an admin approves an ExpansionRequest.
     */
    public boolean expandClaim(UUID owner, int extraRadius) {
        if (extraRadius <= 0) return false;

        boolean anyAdded = false;

        // Per-world expansion to keep worlds independent and predictable
        Set<String> worldsWithOwnership = getWorldsWithOwner(owner);
        for (String world : worldsWithOwnership) {
            Set<Plot> ownedInWorld = getPlotsByOwnerInWorld(owner, world);
            if (ownedInWorld.isEmpty()) continue;

            // Compute bounding box of currently owned chunks
            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
            for (Plot p : ownedInWorld) {
                minX = Math.min(minX, p.getX());
                maxX = Math.max(maxX, p.getX());
                minZ = Math.min(minZ, p.getZ());
                maxZ = Math.max(maxZ, p.getZ());
            }

            // Expand the bounds
            int targetMinX = minX - extraRadius;
            int targetMaxX = maxX + extraRadius;
            int targetMinZ = minZ - extraRadius;
            int targetMaxZ = maxZ + extraRadius;

            // Claim every unowned chunk within the expanded rectangle,
            // but do NOT take chunks belonging to someone else.
            for (int x = targetMinX; x <= targetMaxX; x++) {
                Map<Integer, Plot> row = xIndex(world, x); // ensure map exists for quick lookups
                for (int z = targetMinZ; z <= targetMaxZ; z++) {
                    Plot existing = get(world, x, z);
                    if (existing != null) {
                        // already claimed; skip if it's ours, otherwise leave it alone
                        continue;
                    }
                    // claim it for owner
                    Plot newPlot = new Plot(UUID.randomUUID(), world, x, z, owner);
                    row.put(z, newPlot);
                    anyAdded = true;
                }
            }
        }

        if (anyAdded) {
            saveAll(); // persist after change
        }
        return anyAdded;
    }

    private Set<String> getWorldsWithOwner(UUID owner) {
        Set<String> worlds = new HashSet<>();
        for (Map.Entry<String, Map<Integer, Map<Integer, Plot>>> e : plots.entrySet()) {
            String world = e.getKey();
            for (Map<Integer, Plot> row : e.getValue().values()) {
                for (Plot p : row.values()) {
                    if (p.getOwner().equals(owner)) {
                        worlds.add(world);
                        break;
                    }
                }
            }
        }
        return worlds;
    }
}
