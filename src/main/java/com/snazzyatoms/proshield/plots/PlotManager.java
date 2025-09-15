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
 * - Persistence stubs left for your existing save/load (yml/json/db).
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
}
