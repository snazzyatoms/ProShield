package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

        // --- EXPANSION DENY REASONS ---
        if (GUIManager.isAwaitingReason(player)) {
            event.setCancelled(true);
            String reason = event.getMessage();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                GUIManager.provideManualReason(player, reason, plugin);
            });
            return;
        }

        // --- TRUSTED PLAYERS (Add/Remove via chat) ---
        if (GUIManager.isAwaitingRoleAction(player)) {
            event.setCancelled(true);
            String action = GUIManager.getRoleAction(player);
            String targetName = event.getMessage().trim();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
                if (plot == null) {
                    plugin.getMessagesUtil().send(player, "&cYou must stand inside a claim.");
                    GUIManager.cancelAwaiting(player);
                    return;
                }

                // Permissions: must be owner or admin/bypass
                boolean isOwner = plot.isOwner(player.getUniqueId());
                boolean isAdmin = player.hasPermission("proshield.admin") || plugin.isBypassing(player.getUniqueId());
                if (!isOwner && !isAdmin) {
                    plugin.getMessagesUtil().send(player, "&cYou don't have permission to manage trusted players here.");
                    GUIManager.cancelAwaiting(player);
                    return;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                UUID targetId = target.getUniqueId();

                switch (action.toLowerCase()) {
                    case "add" -> {
                        // Default role = trusted
                        boolean ok = plugin.getRoleManager().trustPlayer(plot, target.getName(), "trusted");
                        if (ok) {
                            plugin.getMessagesUtil().send(player, "&aTrusted &e" + targetName + " &aas &6trusted&a.");
                        } else {
                            plugin.getMessagesUtil().send(player, "&7" + targetName + " is already trusted.");
                        }
                    }
                    case "remove" -> {
                        boolean removed = plugin.getRoleManager().untrustPlayer(plot, target.getName());
                        if (removed) {
                            plugin.getMessagesUtil().send(player, "&cUntrusted &e" + targetName + "&c.");
                        } else {
                            plugin.getMessagesUtil().send(player, "&7" + targetName + " was not trusted.");
                        }
                    }
                }

                GUIManager.cancelAwaiting(player);
            });
        }
    }
}
