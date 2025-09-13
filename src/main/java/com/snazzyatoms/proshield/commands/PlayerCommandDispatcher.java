// src/main/java/com/snazzyatoms/proshield/commands/PlayerCommandDispatcher.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerCommandDispatcher implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public PlayerCommandDispatcher(ProShield plugin, PlotManager plotManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Players only.");
            return true;
        }

        switch (label.toLowerCase()) {
            case "claim" -> {
                plotManager.createPlot(player, player.getLocation());
                messages.send(player, "&aClaimed this chunk.");
            }
            case "unclaim" -> {
                plotManager.removePlot(player.getLocation());
                messages.send(player, "&cUnclaimed this chunk.");
            }
            default -> {
                messages.send(player, "&cUnknown player command.");
            }
        }
        return true;
    }
}
