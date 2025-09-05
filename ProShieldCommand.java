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
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§eUsage: /proshield claim [radius]");
            return true;
        }

        if (args[0].equalsIgnoreCase("claim")) {
            int radius = -1;
            if (args.length > 1) {
                try {
                    radius = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cRadius must be a number.");
                    return true;
                }
            }
            plotManager.claimPlot(player, radius);
            return true;
        }

        player.sendMessage("§eUnknown subcommand. Usage: /proshield claim [radius]");
        return true;
    }
}
