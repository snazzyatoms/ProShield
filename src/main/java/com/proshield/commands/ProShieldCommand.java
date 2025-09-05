package com.proshield.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.proshield.managers.PlotManager;

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
            player.sendMessage(ChatColor.RED + "Usage: /proshield <add|remove>");
            return true;
        }

        if (args[0].equalsIgnoreCase("add")) {
            plotManager.addProtectedPlot(location);
            player.sendMessage(ChatColor.GREEN + "Plot protected at your location!");
            return true;
        }

        if (args[0].equalsIgnoreCase("remove")) {
            plotManager.removeProtectedPlot(location);
            player.sendMessage(ChatColor.YELLOW + "Plot unprotected at your location!");
            return true;
        }

        player.sendMessage(ChatColor.RED + "Usage: /proshield <add|remove>");
        return true;
    }
}
