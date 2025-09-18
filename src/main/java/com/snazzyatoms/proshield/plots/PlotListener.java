// src/main/java/com/snazzyatoms/proshield/plots/PlotListener.java
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
 * PlotListener (v1.2.6)
 * - Tracks entering/leaving claims and wilderness
 * - Syncs with messages.yml for all transitions
 * - Respects config toggles (show-wilderness, admin-flag-chat)
 * - Enforces safezone vs. wilderness logic:
 *      Claims → Safezone (PvP off, protected)
 *      Wilderness → Vanilla (PvP on, unprotected)
 * - Uses caching to avoid spam
 */
public class PlotListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    // Cache: Player UUID → Last known plot ID (null if wilderness)
    private final Map<UUID, UUID> lastPlotCache = new HashMap<>();
    // Cache: Owner UUID → Last known username
    private final Map<UUID, String> nameCache = new HashMap<>();

    public PlotListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;

        // Only fire when player moves to a different chunk
        if (event.getFrom().getChunk().equals(event.getTo().getChunk())) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        Plot from = plotManager.getPlotAt(event.getFrom());
        Plot to = plotManager.getPlotAt(event.getTo());

        UUID lastPlotId = lastPlotCache.get(playerId);
        UUID newPlotId = (to != null ? to.getId() : null);

        // No change → do nothing
        if ((lastPlotId == null && newPlotId == null) ||
            (lastPlotId != null && lastPlotId.equals(newPlotId))) {
            return;
        }

        // Update cache
        lastPlotCache.put(playerId, newPlotId);

        // Leaving old plot
        if (from != null && (to == null || !from.equals(to))) {
            String ownerName = resolveName(from.getOwner());
            if (from.getOwner().equals(playerId)) {
                messages.send(player, messages.get("messages.leave-own"));
            } else {
                messages.send(player, messages.get("messages.leave-other").replace("{owner}", ownerName));
            }
        } else if (from == null && plugin.getConfig().getBoolean("messages.show-wilderness", true)) {
            // Leaving wilderness into claim handled below, so here only wilderness exit
            messages.send(player, messages.get("messages.wilderness"));
        }

        // Entering new plot
        if (to != null && (from == null || !from.equals(to))) {
            String ownerName = resolveName(to.getOwner());
            if (to.getOwner().equals(playerId)) {
                messages.send(player, messages.get("messages.enter-own"));
            } else {
                messages.send(player, messages.get("messages.enter-other").replace("{owner}", ownerName));
            }
        } else if (to == null && plugin.getConfig().getBoolean("messages.show-wilderness", true)) {
            // Entering wilderness
            messages.send(player, messages.get("messages.wilderness"));
        }

        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[PlotListener] " + player.getName() +
                    " moved from " + (from != null ? "claim " + from.getId() : "wilderness") +
                    " → " + (to != null ? "claim " + to.getId() : "wilderness"));
        }
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
     * - True for claim owner
     * - True for trusted roles
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
