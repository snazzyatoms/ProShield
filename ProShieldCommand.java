package com.snazzyatoms.proshield;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;

    public ProShieldCommand(ProShield plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("claim")) {
            int radius = 0;
            if (args.length > 1) {
                try {
                    radius = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid radius. Please use a number.");
                    return true;
                }
            }
            plugin.getPlotManager().claimPlot(player, radius);
            return true;
        }

        player.sendMessage("§eUsage: /proshield claim [radius]");
        return true;
    }
}
