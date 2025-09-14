package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class ChatListener implements Listener {

    private final ProShield plugin;

    public ChatListener(ProShield plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // --- DENY REASONS ---
        if (GUIManager.isAwaitingReason(player)) {
            event.setCancelled(true);
            String reason = event.getMessage();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                GUIManager.provideManualReason(player, reason, plugin);
            });
            return;
        }

        // --- TRUSTED PLAYERS (Add/Remove) ---
        if (GUIManager.isAwaitingRoleAction(player)) {
            event.setCancelled(true);
            String action = GUIManager.getRoleAction(player);
            String targetName = event.getMessage();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                Player target = Bukkit.getPlayerExact(targetName);
                if (target == null) {
                    plugin.getMessagesUtil().send(player, "&cPlayer not found: " + targetName);
                    GUIManager.cancelAwaiting(player);
                    return;
                }

                // Get claim at player location
                Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
                if (plot == null) {
                    plugin.getMessagesUtil().send(player, "&cYou must stand inside a claim.");
                    GUIManager.cancelAwaiting(player);
                    return;
                }

                UUID targetId = target.getUniqueId();

                // Check permissions: owner or bypass/admin
                boolean isOwner = plot.isOwner(player.getUniqueId());
                boolean isAdmin = player.hasPermission("proshield.admin") || plugin.isBypassing(player.getUniqueId());

                if (!isOwner && !isAdmin) {
                    plugin.getMessagesUtil().send(player, "&cYou don't have permission to manage trusted players here.");
                    GUIManager.cancelAwaiting(player);
                    return;
                }

                switch (action) {
                    case "add" -> {
                        plot.addTrusted(targetId);
                        plugin.getMessagesUtil().send(player, "&aTrusted player: " + targetName);
                    }
                    case "remove" -> {
                        if (plot.getTrusted().remove(targetId)) {
                            plugin.getMessagesUtil().send(player, "&cUntrusted player: " + targetName);
                        } else {
                            plugin.getMessagesUtil().send(player, "&7That player was not trusted.");
                        }
                    }
                }

                GUIManager.cancelAwaiting(player);
            });
        }
    }
}
