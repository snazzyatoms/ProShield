package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class PlotManager {
    private final ProShield plugin;
    private final Map<UUID, Plot> plots = new HashMap<>();
    private final Map<String, UUID> playerNames = new HashMap<>();
    private final Map<UUID, Integer> claimRadii = new HashMap<>(); // ✅ tracks custom expansion radius per player

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    public Plot getPlot(Location loc) {
        if (loc == null) return null;
        Chunk chunk = loc.getChunk();
        UUID id = new UUID(chunk.getX(), chunk.getZ());
        return plots.get(id);
    }

    public Plot getPlot(UUID id) {
        return plots.get(id);
    }

    public void createPlot(Player owner, Location loc) {
        Chunk chunk = loc.getChunk();
        UUID id = new UUID(chunk.getX(), chunk.getZ());
        Plot plot = new Plot(id, owner.getUniqueId());
        plots.put(id, plot);
        playerNames.put(owner.getName(), owner.getUniqueId());

        // Set default radius if no expansion yet
        int defaultRadius = plugin.getConfig().getInt("claims.default-radius", 50);
        claimRadii.put(owner.getUniqueId(), defaultRadius);
    }

    public void removePlot(Location loc) {
        Chunk chunk = loc.getChunk();
        UUID id = new UUID(chunk.getX(), chunk.getZ());
        plots.remove(id);
    }

    public void saveAll() {
        // TODO: persist to disk (future feature)
    }

    public String getPlayerName(UUID id) {
        for (Map.Entry<String, UUID> entry : playerNames.entrySet()) {
            if (entry.getValue().equals(id)) return entry.getKey();
        }
        return "Unknown";
    }

    // ✅ Expansion support
    public void expandClaim(UUID playerId, int extraRadius) {
        int current = claimRadii.getOrDefault(playerId,
                plugin.getConfig().getInt("claims.default-radius", 50));
        int newRadius = current + extraRadius;
        claimRadii.put(playerId, newRadius);

        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null) {
            player.sendMessage(plugin.getMessagesUtil().color(
                    "&aYour claim radius has been expanded to &e" + newRadius + " &ablocks."));
        }
    }

    public int getClaimRadius(UUID playerId) {
        return claimRadii.getOrDefault(playerId,
                plugin.getConfig().getInt("claims.default-radius", 50));
    }
}
