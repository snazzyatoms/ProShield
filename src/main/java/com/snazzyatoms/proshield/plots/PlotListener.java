package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PlotListener
 * - Handles entering/leaving claims with clean messages
 * - Shows proper owner usernames (never UUIDs)
 * - No wilderness spam
 * - Only owners/trusted players are flagged as "protected"
 */
public class PlotListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    // Cache: Player UUID → Last known plot ID (or null if wilderness)
    private final Map<UUID, UUID> lastPlotCache = new HashMap<>();

    public PlotListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        // Only fire on chunk changes (avoid spam for small steps)
        if (event.getFrom().getChunk().equals(event.getTo().getChunk())) {
            return;
        }

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        Plot to = plotManager.getPlotAt(event.getTo());
        Plot from = plotManager.getPlotAt(event.getFrom());

        UUID lastPlotId = lastPlotCache.get(playerId);
        UUID newPlotId = (to != null ? to.getId() : null);

        // No change → do nothing
        if ((lastPlotId == null && newPlotId == null) ||
            (lastPlotId != null && lastPlotId.equals(newPlotId))) {
            return;
        }

        // Update cache
        lastPlotCache.put(playerId, newPlotId);

        // Handle leaving old plot
        if (from != null && (to == null || !from.equals(to))) {
            String ownerName = resolveName(from.getOwner());
            if (from.getOwner().equals(playerId)) {
                messages.send(player, "&cYou left your claim.");
            } else {
                messages.send(player, "&cYou left " + ownerName + "'s claim.");
            }
        }

        // Handle entering new plot
        if (to != null && (from == null || !from.equals(to))) {
            String ownerName = resolveName(to.getOwner());
            if (to.getOwner().equals(playerId)) {
                messages.send(player, "&aYou entered your claim.");
            } else {
                messages.send(player, "&aYou entered " + ownerName + "'s claim.");
            }
        }

        // Do NOT send wilderness message anymore (suppressed)
    }

    /**
     * Helper: Get player name from UUID, or fallback short UUID.
     */
    private String resolveName(UUID uuid) {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(uuid);
        return owner != null && owner.getName() != null
                ? owner.getName()
                : uuid.toString().substring(0, 8);
    }

    /**
     * Check if a player is protected inside a claim (owner or trusted).
     */
    public boolean isProtected(Player player, Plot plot) {
        if (plot == null) return false;
        UUID playerId = player.getUniqueId();
        if (plot.getOwner().equals(playerId)) return true;
        return plot.getTrusted().containsKey(playerId);
    }
}
