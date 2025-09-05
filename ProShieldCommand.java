package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.managers.PlotManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProShieldCommand implements CommandExecutor {

    private final PlotManager plotManager;

    public ProShieldCommand(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§eUsage: /proshield <claim|unclaim>");
            return true;
        }

        if (args[0].equalsIgnoreCase("claim")) {
            int radius = args.length > 1 ? Integer.parseInt(args[1]) : 0;
            plotManager.claimPlot(player, radius);
        } else if (args[0].equalsIgnoreCase("unclaim")) {
            if (plotManager.hasPlot(player)) {
                plotManager.unclaimPlot(player);
            } else {
                player.sendMessage("§cYou don't have a claimed plot.");
            }
        } else {
            player.sendMessage("§eUsage: /proshield <claim|unclaim>");
        }

        return true;
    }
}
