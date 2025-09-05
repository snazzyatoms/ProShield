package com.snazzyatoms.proshield;

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
            player.sendMessage("§eProShield is active! Use /proshield help for commands.");
            return true;
        }

        if (args[0].equalsIgnoreCase("help")) {
            player.sendMessage("§a--- ProShield Commands ---");
            player.sendMessage("§e/proshield help §7- Show this help message");
            // Later: add more subcommands here
            return true;
        }

        player.sendMessage("§cUnknown subcommand. Try /proshield help.");
        return true;
    }
}
