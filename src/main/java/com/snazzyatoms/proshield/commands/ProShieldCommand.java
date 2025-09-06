// path: src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public ProShieldCommand(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /proshield <claim|unclaim>");
            return true;
        }

        Location loc = player.getLocation();

        switch (args[0].toLowerCase()) {
            case "claim":
                if (plotManager.createClaim(player.getUniqueId(), loc)) {
                    player.sendMessage(ChatColor.GREEN + "Chunk claimed successfully!");
                } else {
                    player.sendMessage(ChatColor.RED + "This chunk is already claimed.");
                }
                return true;

            case "unclaim":
                if (plotManager.removeClaim(player.getUniqueId(), loc)) {
                    player.sendMessage(ChatColor.GREEN + "Chunk unclaimed successfully!");
                } else {
                    player.sendMessage(ChatColor.RED + "You do not own this claim.");
                }
                return true;

            default:
                player.sendMessage(ChatColor.YELLOW + "Usage: /proshield <claim|unclaim>");
                return true;
        }
    }
}
