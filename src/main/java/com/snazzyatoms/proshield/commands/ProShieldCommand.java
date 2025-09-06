// path: src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final GUIManager guiManager;

    public ProShieldCommand(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.guiManager = plugin.getGuiManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can run ProShield commands.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "compass":
                if (!player.hasPermission("proshield.compass")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use the compass.");
                    return true;
                }
                ItemStack compass = GUIManager.createAdminCompass();
                player.getInventory().addItem(compass);
                player.sendMessage(ChatColor.GREEN + "You have been given the ProShield Compass.");
                break;

            case "create":
                if (!player.hasPermission("proshield.create")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to create claims.");
                    return true;
                }
                boolean created = plotManager.createClaim(player);
                if (created) {
                    player.sendMessage(ChatColor.GREEN + "Claim created successfully!");
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to create claim. Maybe this area is already claimed?");
                }
                break;

            case "info":
                if (!player.hasPermission("proshield.info")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to view claim info.");
                    return true;
                }
                String info = plotManager.getClaimInfo(player.getLocation());
                if (info != null) {
                    player.sendMessage(ChatColor.YELLOW + "Claim Info: " + ChatColor.AQUA + info);
                } else {
                    player.sendMessage(ChatColor.RED + "No claim exists at this location.");
                }
                break;

            case "remove":
                if (!player.hasPermission("proshield.remove")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to remove claims.");
                    return true;
                }
                boolean removed = plotManager.removeClaim(player);
                if (removed) {
                    player.sendMessage(ChatColor.GREEN + "Claim removed successfully.");
                } else {
                    player.sendMessage(ChatColor.RED + "No claim found here to remove.");
                }
                break;

            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "==== ProShield Commands ====");
        player.sendMessage(ChatColor.YELLOW + "/proshield compass " + ChatColor.GRAY + "- Get the ProShield Compass");
        player.sendMessage(ChatColor.YELLOW + "/proshield create " + ChatColor.GRAY + "- Create a new claim");
        player.sendMessage(ChatColor.YELLOW + "/proshield info " + ChatColor.GRAY + "- Get info about the claim here");
        player.sendMessage(ChatColor.YELLOW + "/proshield remove " + ChatColor.GRAY + "- Remove your claim here");
    }
}
