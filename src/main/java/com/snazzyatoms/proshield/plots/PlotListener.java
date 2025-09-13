// src/main/java/com/snazzyatoms/proshield/plots/PlotListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    // Track last known plot for each player (to detect entering/leaving)
    private final Map<UUID, Plot> lastPlot = new HashMap<>();

    public PlotListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = messages;
    }

    /* =====================================================
     * SAFEZONE LOGIC
     * ===================================================== */
    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        Plot plot = plotManager.getPlot(event.getLocation());
        if (plot != null && plot.getFlag("safezone", plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
            if (event.getEntity() instanceof Monster) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot != null && plot.getFlag("safezone", plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
            if (event.getDamager() instanceof Monster) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;

        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot != null && plot.getFlag("safezone", plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
            if (event.getEntity() instanceof Monster) {
                event.setCancelled(true);
            }
        }
    }

    /* =====================================================
     * CLAIM ENTER/EXIT MESSAGES
     * ===================================================== */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Only check if player changed chunk (performance optimization)
        if (event.getFrom().getChunk().equals(event.getTo().getChunk())) {
            return;
        }

        Plot fromPlot = lastPlot.get(player.getUniqueId());
        Plot toPlot = plotManager.getPlot(event.getTo());

        if (fromPlot == toPlot) return; // Still in same plot

        // Leaving old claim
        if (fromPlot != null) {
            sendLeaveMessage(player, fromPlot);
        }

        // Entering new claim
        if (toPlot != null) {
            sendEnterMessage(player, toPlot);
        } else {
            // Wilderness (if enabled in config)
            if (plugin.getConfig().getBoolean("messages.show-wilderness", true)) {
                messages.send(player, "messages.wilderness");
            }
        }

        // Update last known plot
        lastPlot.put(player.getUniqueId(), toPlot);
    }

    private void sendEnterMessage(Player player, Plot plot) {
        UUID ownerId = plot.getOwner();
        if (ownerId == null) return;

        String ownerName = resolveName(ownerId);

        if (plot.isOwner(player.getUniqueId())) {
            messages.send(player, "messages.enter-own");
        } else {
            messages.send(player, "messages.enter-other", "{owner}", ownerName);
        }
    }

    private void sendLeaveMessage(Player player, Plot plot) {
        UUID ownerId = plot.getOwner();
        if (ownerId == null) return;

        String ownerName = resolveName(ownerId);

        if (plot.isOwner(player.getUniqueId())) {
            messages.send(player, "messages.leave-own");
        } else {
            messages.send(player, "messages.leave-other", "{owner}", ownerName);
        }
    }

    private String resolveName(UUID uuid) {
        OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
        return (offline != null && offline.getName() != null) ? offline.getName() : "Unknown";
    }
}
