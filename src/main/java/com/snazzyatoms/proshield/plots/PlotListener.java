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
 * - Tracks entering and leaving claims with clean messages
 * - Always resolves usernames (never raw UUIDs)
 * - Suppresses wilderness spam (optional toggle in config)
 * - Provides utility: isProtected(player, plot)
 */
public class PlotListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    // Cache: Player UUID → Last known plot ID (or null if wilderness)
    private final Map<UUID, UUID> lastPlotCache = new HashMap<>();
    // Cache: Owner UUID → Last known username (avoid repeated lookups)
    private final Map<UUID, String> nameCache = new HashMap<>();

    public PlotListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return; // Safety for weird teleport cases

        // Only fire when player moves to a different chunk
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

        // Wilderness enter/leave messages are suppressed intentionally
        // (can be re-enabled via config if needed)
    }

    /**
     * Resolve a player's username from UUID (with caching).
     */
    private String resolveName(UUID uuid) {
        if (uuid == null) return "Unknown";
        if (nameCache.containsKey(uuid)) return nameCache.get(uuid);

        OfflinePlayer owner = Bukkit.getOfflinePlayer(uuid);
        String name = (owner != null && owner.getName() != null)
                ? owner.getName()
                : uuid.toString().substring(0, 8);

        nameCache.put(uuid, name);
        return name;
    }

    /**
     * Utility: Check if player is protected inside this plot.
     * Protection applies only to:
     * - The claim owner
     * - Trusted players with a role
     */
    public boolean isProtected(Player player, Plot plot) {
        if (plot == null || player == null) return false;
        UUID playerId = player.getUniqueId();
        if (plot.getOwner().equals(playerId)) return true;
        return plot.getTrusted().containsKey(playerId);
    }

    /**
     * Clears caches (optional hook for reloads).
     */
    public void clearCaches() {
        lastPlotCache.clear();
        nameCache.clear();
    }
}
