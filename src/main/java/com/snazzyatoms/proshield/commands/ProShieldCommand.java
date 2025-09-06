package com.snazzytom.proshield.commands;

import com.snazzytom.proshield.plots.PlotManager;
import org.bukkit.Location;
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
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        Location location = player.getLocation();

        if (args.length == 0) {
            player.sendMessage("Usage: /proshield <create|remove>");
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            plotManager.createClaim(player, location); // ✅ fixed
            player.sendMessage("Plot created at your location!");
        } else if (args[0].equalsIgnoreCase("remove")) {
            plotManager.removeClaim(player, location); // ✅ fixed
            player.sendMessage("Plot removed at your location!");
        } else {
            player.sendMessage("Usage: /proshield <create|remove>");
        }

        return true;
    }
}
