package com.proshield.commands;

import com.proshield.ProShield;
import com.proshield.managers.GUIManager;
import org.bukkit.ChatColor;
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
            sender.sendMessage(ChatColor.RED + "Only players can use ProShield commands!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Open main GUI
            GUIManager guiManager = plugin.getGuiManager();
            guiManager.openPlayerGUI(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (player.hasPermission("proshield.admin")) {
                plugin.reloadConfig();
                player.sendMessage(ChatColor.GREEN + "[ProShield] Config reloaded successfully.");
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("debug")) {
            if (player.hasPermission("proshield.admin")) {
                player.sendMessage(ChatColor.YELLOW + "[ProShield] Debug mode command is not implemented yet.");
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            }
            return true;
        }

        player.sendMessage(ChatColor.RED + "Unknown command. Use /proshield or /proshield reload");
        return true;
    }
}
