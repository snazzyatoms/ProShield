package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

/**
 * Minimal-yet-compatible PlotManager that exposes
 * all methods other classes reference.
 */
public class PlotManager {

    private final ProShield plugin;
    private final MessagesUtil messages;

    // Indexes
    private final Map<UUID, Plot> claims = new HashMap<>();
    private final Map<String, UUID> indexByChunk = new HashMap<>();
    // key: world:x:z

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
    }

    // ---------- Keys ----------
    private String key(Location loc) {
        Chunk c = loc.getChunk();
        return loc.getWorld().getName() + ":" + c.getX() + ":" + c.getZ();
    }
    private String key(String world, int x, int z) {
        return world + ":" + x + ":" + z;
    }

    // ---------- Lookup ----------
    public Plot getPlot(Location location) { // compatibility method
        return getClaimAt(location);
    }

    public Plot getClaimAt(Location location) {
        UUID id = indexByChunk.get(key(location));
        return id != null ? claims.get(id) : null;
    }

    public UUID getClaimIdAt(Location location) {
        return indexByChunk.get(key(location));
    }

    public Plot getPlotById(UUID id) {
        return claims.get(id);
    }

    // ---------- Mutations ----------
    public Plot createPlot(UUID ownerId, Location loc) {
        String world = loc.getWorld().getName();
        int cx = loc.getChunk().getX();
        int cz = loc.getChunk().getZ();
        if (indexByChunk.containsKey(key(loc))) return claims.get(indexByChunk.get(key(loc)));

        UUID id = UUID.randomUUID();
        Plot plot = new Plot(id, ownerId, world, cx, cz);
        addClaim(id, plot);
        return plot;
    }

    public void addClaim(UUID id, Plot plot) {
        claims.put(id, plot);
        indexByChunk.put(key(plot.getWorldName(), plot.getChunkX(), plot.getChunkZ()), id);
    }

    public void removePlot(Location loc) {
        UUID id = indexByChunk.remove(key(loc));
        if (id != null) claims.remove(id);
    }

    public void removeClaim(UUID id) {
        Plot p = claims.remove(id);
        if (p != null) {
            indexByChunk.remove(key(p.getWorldName(), p.getChunkX(), p.getChunkZ()));
        }
    }

    // ---------- Player helpers used by commands ----------
    public void claimPlot(Player player) {
        Plot existing = getPlot(player.getLocation());
        if (existing != null) {
            player.sendMessage(ChatColor.RED + "This chunk is already claimed by " + existing.getOwnerName() + ".");
            return;
        }
        Plot plot = createPlot(player.getUniqueId(), player.getLocation());
        player.sendMessage(ChatColor.GREEN + "You claimed this chunk. (" + plot.getChunkX() + ", " + plot.getChunkZ() + ")");
    }

    public void unclaimPlot(Player player) {
        Plot plot = getPlot(player.getLocation());
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "This chunk is not claimed.");
            return;
        }
        if (!plot.isOwner(player.getUniqueId()) && !player.hasPermission("proshield.admin")) {
            player.sendMessage(ChatColor.RED + "Only the claim owner (or admin) can unclaim.");
            return;
        }
        removePlot(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "You unclaimed this chunk.");
    }

    public void sendClaimInfo(Player player) {
        Plot plot = getPlot(player.getLocation());
        if (plot == null) {
            player.sendMessage(ChatColor.YELLOW + "This chunk is unclaimed.");
            return;
        }
        player.sendMessage(ChatColor.AQUA + "Claim Info:");
        player.sendMessage(ChatColor.GRAY + " Owner: " + plot.getOwnerName());
        player.sendMessage(ChatColor.GRAY + " World: " + plot.getWorldName());
        player.sendMessage(ChatColor.GRAY + " Chunk: " + plot.getChunkX() + ", " + plot.getChunkZ());
    }

    // ---------- Persistence stubs ----------
    public void saveAll() {
        // TODO: persist to claims.yml
        File f = new File(plugin.getDataFolder(), "claims.yml");
        // serialize 'claims' as needed (omitted for brevity)
    }

    public void loadAll() {
        // TODO: load from claims.yml
        // rebuild indexes into 'claims' and 'indexByChunk'
    }
}
