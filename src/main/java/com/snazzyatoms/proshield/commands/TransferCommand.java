// src/main/java/com/snazzyatoms/proshield/commands/TransferCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Handles /proshield transfer <player>
 *
 * Transfers claim ownership to another player.
 * Preserves prior logic and fixes:
 * ✅ Uses saveAsync(plot) instead of private savePlot()
 * ✅ Handles location → chunk properly
 */
public class TransferCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public TransferCommand(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            messages.send(player, "transfer-usage");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            messages.send(player, "player-not-found", args[0]);
            return true;
        }

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            messages.send(player, "not-in-claim");
            return true;
        }

        UUID playerId = player.getUniqueId();
        if (!plot.isOwner(playerId)) {
            messages.send(player, "not-owner");
            return true;
        }

        // Transfer ownership
        plot.setOwner(target.getUniqueId());
        plotManager.saveAsync(plot); // ✅ FIXED: use public saveAsync()

        messages.send(player, "transfer-success", target.getName());
        messages.send(target, "transfer-received", player.getName());
        return true;
    }
}
