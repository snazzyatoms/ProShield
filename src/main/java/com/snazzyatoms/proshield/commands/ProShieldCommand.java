// path: src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
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

    private String p(String msg) {
        return ChatColor.DARK_AQUA + "[ProShield] " + ChatColor.RESET + msg;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(p(ChatColor.AQUA + "Usage: /proshield <compass|reload|help>"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "compass": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(p("Only players can receive the compass."));
                    return true;
                }
                Player player = (Player) sender;
                if (!player.isOp() && !player.hasPermission("proshield.compass")) {
                    player.sendMessage(p(ChatColor.RED + "You don't have permission (proshield.compass)."));
                    return true;
                }
                player.getInventory().addItem(GUIManager.createAdminCompass());
                player.sendMessage(p(ChatColor.GREEN + "Admin compass given."));
                return true;
            }
            case "reload": {
                if (!sender.isOp() && !sender.hasPermission("proshield.admin")) {
                    sender.sendMessage(p(ChatColor.RED + "You don't have permission (proshield.admin)."));
                    return true;
                }
                plugin.reloadConfig();
                plugin.getPlotManager().reloadFromConfig();
                sender.sendMessage(p(ChatColor.GREEN + "Configuration reloaded."));
                return true;
            }
            case "help":
            default: {
                sender.sendMessage(p(ChatColor.AQUA + "Commands:"));
                sender.sendMessage(ChatColor.GRAY + " - /proshield compass" + ChatColor.DARK_GRAY + " » " + ChatColor.WHITE + "Get the admin compass.");
                sender.sendMessage(ChatColor.GRAY + " - /proshield reload" + ChatColor.DARK_GRAY + " » " + ChatColor.WHITE + "Reload config/claims.");
                return true;
            }
        }
    }
}
