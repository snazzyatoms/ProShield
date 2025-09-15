package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * PlotManager
 * -----------
 * - Keeps an index of plots by ID and by (world, chunkX, chunkZ)
 * - Utility methods used across commands, GUI, roles, and expansion system
 * - Provides convenience overloads (by Location, UUID) to keep older call sites working
 */
public class PlotManager {

    private final ProShield plugin;
    private final MessagesUtil messages;

    /** Primary storage by unique plot ID */
    private final Map<UUID, Plot> plotsById = new HashMap<>();
    /** Secondary index from world:cx:cz -> plotId */
    private final Map<String, UUID> byChunk = new HashMap<>();
    /** Optional reverse index for owner -> plot IDs (supports multi-plot owners) */
    private final Map<UUID, Set<UUID>> plotsByOwner = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
    }

    /* ======================
     * Basic indexing helpers
     * ====================== */
    private String key(String worldName, int cx, int cz) {
        return worldName + ":" + cx + ":" + cz;
    }

    /* =====================
     * Lookup / read methods
     * ===================== */

    /** Get a plot at a given Location (null if chunk is unclaimed). */
    public Plot getPlot(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        Chunk c = loc.getChunk();
        String k = key(loc.getWorld().getName(), c.getX(), c.getZ());
        UUID id = byChunk.get(k);
        return (id == null) ? null : plotsById.get(id);
    }

    /** Get a plot by its unique ID (null if not found). */
    public Plot getPlotById(UUID plotId) {
        if (plotId == null) return null;
        return plotsById.get(plotId);
    }

    /** Convenience: find first plot owned by a specific player (may be null). */
    public Plot getAnyPlotOwnedBy(UUID ownerId) {
        Set<UUID> ids = plotsByOwner.get(ownerId);
        if (ids == null || ids.isEmpty()) return null;
        for (UUID id : ids) {
            Plot p = plotsById.get(id);
            if (p != null) return p;
        }
        return null;
    }

    /** Resolve a player's last known name (best-effort). */
    public String getPlayerName(UUID playerId) {
        if (playerId == null) return "unknown";
        OfflinePlayer off = Bukkit.getOfflinePlayer(playerId);
        return (off != null && off.getName() != null) ? off.getName() : playerId.toString();
    }

    /* =========================
     * Create / remove / mutate
     * ========================= */

    public Plot createPlot(UUID ownerId, Location loc) {
        if (ownerId == null || loc == null || loc.getWorld() == null) return null;

        Chunk c = loc.getChunk();
        String world = loc.getWorld().getName();
        int cx = c.getX();
        int cz = c.getZ();
        String k = key(world, cx, cz);

        if (byChunk.containsKey(k)) {
            return null; // already claimed
        }

        Plot plot = new Plot(ownerId, world, cx, cz);

        // Apply default flags from config
        if (plugin.getConfig().isConfigurationSection("claims.default-flags")) {
            for (String flag : plugin.getConfig().getConfigurationSection("claims.default-flags").getKeys(false)) {
                boolean state = plugin.getConfig().getBoolean("claims.default-flags." + flag, false);
                plot.setFlag(flag, state);
            }
        }

        plotsById.put(plot.getId(), plot);
        byChunk.put(k, plot.getId());
        plotsByOwner.computeIfAbsent(ownerId, _x -> new HashSet<>()).add(plot.getId());
        return plot;
    }

    public boolean removePlot(Location loc) {
        Plot p = getPlot(loc);
        if (p == null) return false;
        return removePlot(p);
    }

    public boolean removePlot(UUID id) {
        if (id == null) return false;
        Plot p = getPlotById(id);
        return removePlot(p);
    }

    public boolean removePlot(Plot plot) {
        if (plot == null) return false;

        UUID id = plot.getId();
        Plot removed = plotsById.remove(id);
        if (removed == null) return false;

        String k = key(plot.getWorldName(), plot.getX(), plot.getZ());
        byChunk.remove(k);

        Set<UUID> owned = plotsByOwner.get(plot.getOwner());
        if (owned != null) {
            owned.remove(id);
            if (owned.isEmpty()) plotsByOwner.remove(plot.getOwner());
        }
        return true;
    }

    public boolean expandClaim(UUID ownerId, int extraRadius) {
        if (ownerId == null || extraRadius <= 0) return false;
        Plot p = getAnyPlotOwnedBy(ownerId);
        if (p == null) return false;
        p.expand(extraRadius);
        return true;
    }

    public boolean transferOwnership(UUID plotId, UUID newOwnerId) {
        Plot p = getPlotById(plotId);
        if (p == null || newOwnerId == null) return false;

        UUID oldOwner = p.getOwner();
        if (Objects.equals(oldOwner, newOwnerId)) return true;

        if (oldOwner != null) {
            Set<UUID> set = plotsByOwner.get(oldOwner);
            if (set != null) {
                set.remove(p.getId());
                if (set.isEmpty()) plotsByOwner.remove(oldOwner);
            }
        }
        p.setOwner(newOwnerId);
        plotsByOwner.computeIfAbsent(newOwnerId, _x -> new HashSet<>()).add(p.getId());
        return true;
    }

    /* ==================
     * Command helpers
     * ================== */

    /** Called by /claim */
    public void claimPlot(Player player) {
        Plot created = createPlot(player.getUniqueId(), player.getLocation());
        if (created == null) {
            messages.send(player, "&cThis land is already claimed.");
        } else {
            messages.send(player, "&aLand claimed successfully!");
        }
    }

    /** Called by /unclaim */
    public void unclaimPlot(Player player) {
        Plot plot = getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "&cThis land is not claimed.");
            return;
        }
        if (!plot.isOwner(player.getUniqueId())) {
            messages.send(player, "&cYou are not the owner of this claim.");
            return;
        }
        removePlot(plot);
        messages.send(player, "&cYour claim has been unclaimed.");
    }

    /** Called by /proshield info */
    public void sendClaimInfo(Player player) {
        Plot plot = getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "&eThis chunk is unclaimed.");
            return;
        }

        messages.send(player, "&6--- Claim Info ---");
        messages.send(player, "&7Owner: &f" + getPlayerName(plot.getOwner()));

        if (plot.getTrusted().isEmpty()) {
            messages.send(player, "&7Trusted: &cNone");
        } else {
            messages.send(player, "&7Trusted:");
            plot.getTrusted().forEach((uuid, role) ->
                messages.send(player, " &8- &f" + getPlayerName(uuid) + " &7(" + role + ")")
            );
        }

        messages.send(player, "&7Flags:");
        plot.getFlags().forEach((flag, state) ->
            messages.send(player, " &8- &f" + flag + ": " + (state ? "&aON" : "&cOFF"))
        );
    }

    /* ==================
     * Persistence hooks
     * ================== */

    public void loadAll() {
        // TODO: Load plotsById, byChunk, plotsByOwner from disk.
    }

    public void saveAll() {
        // TODO: Save plotsById, byChunk, plotsByOwner to disk.
    }

    /* ===============
     * Debug utilities
     * =============== */

    public int getTotalPlots() {
        return plotsById.size();
    }
}
