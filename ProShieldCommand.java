package com.snazzyatoms.proshield;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ProShieldCommand implements CommandExecutor {

    private final PlotManager plotManager;

    public ProShieldCommand(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        UUID plotId = player.getWorld().getUID(); 

        if (args.length == 0) {
            player.sendMessage("Usage: /proshield <protect|unprotect|check>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "protect":
                plotManager.protectPlot(plotId);
                player.sendMessage("Plot protected!");
                break;

            case "unprotect":
                plotManager.unprotectPlot(plotId);
                player.sendMessage("Plot unprotected!");
                break;

            case "check":
                if (plotManager.isProtected(plotId)) {
                    player.sendMessage("This plot is protected.");
                } else {
                    player.sendMessage("This plot is not protected.");
                }
                break;

            default:
                player.sendMessage("Usage: /proshield <protect|unprotect|check>");
                break;
        }
        return true;
    }
}
