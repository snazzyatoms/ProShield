// src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ProShieldCommand (v1.2.6 FINAL)
 *
 * - Handles /proshield command and its subcommands
 * - Unified compass handling with CompassManager + GUIManager
 * - Admin commands: reload, debug, bypass
 */
public class ProShieldCommand implements CommandExecutor, TabCompleter {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final CompassManager compassManager;

    public ProShieldCommand(ProShield plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
        this.compassManager = new CompassManager(plugin, guiManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                if (player.hasPermission("proshield.player.access")) {
                    guiManager.openMainMenu(player);
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use ProShield.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            }
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "help" -> {
                sender.sendMessage(ChatColor.AQUA + "=== ProShield Help ===");
                sender.sendMessage(ChatColor.GRAY + "/proshield help - Show this help menu");
                sender.sendMessage(ChatColor.GRAY + "/proshield compass - Get or open the ProShield Compass");
                sender.sendMessage(ChatColor.GRAY + "/proshield admin - Open the Admin Tools GUI");
                sender.sendMessage(ChatColor.GRAY + "/proshield reload - Reload plugin configs");
                sender.sendMessage(ChatColor.GRAY + "/proshield debug - Toggle debug logging");
                sender.sendMessage(ChatColor.GRAY + "/proshield bypass - Toggle admin bypass mode");
            }

            case "compass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }
                if (!player.hasPermission("proshield.compass")) {
                    player.sendMessage(ChatColor.RED + "You don’t have permission to use this.");
                    return true;
                }

                if (compassManager.hasCompass(player)) {
                    guiManager.openMainMenu(player);
                } else {
                    compassManager.giveCompass(player);
                    player.sendMessage(ChatColor.AQUA + plugin.getMessagesUtil()
                            .getOrDefault("compass.command-success", "A ProShield Compass has been given to you."));
                }
            }

            case "admin" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }
                if (!player.hasPermission("proshield.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don’t have permission to open Admin Tools.");
                    return true;
                }
                guiManager.openAdmin(player);
            }

            case "reload" -> {
                if (!sender.hasPermission("proshield.admin.reload")) {
                    sender.sendMessage(ChatColor.RED + "You don’t have permission to reload ProShield.");
                    return true;
                }
                plugin.reloadConfig();
                plugin.loadMessagesConfig();
                sender.sendMessage(ChatColor.GREEN + "ProShield configuration reloaded.");
            }

            case "debug" -> {
                if (!sender.hasPermission("proshield.admin.debug")) {
                    sender.sendMessage(ChatColor.RED + "You don’t have permission to toggle debug.");
                    return true;
                }
                plugin.toggleDebug();
                sender.sendMessage(plugin.isDebugEnabled()
                        ? ChatColor.YELLOW + "Debug mode ENABLED"
                        : ChatColor.RED + "Debug mode DISABLED");
            }

            case "bypass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can toggle bypass.");
                    return true;
                }
                if (!player.hasPermission("proshield.admin.bypass")) {
                    player.sendMessage(ChatColor.RED + "You don’t have permission to toggle bypass.");
                    return true;
                }
                boolean now = plugin.toggleBypass(player.getUniqueId());
                player.sendMessage(now
                        ? ChatColor.GREEN + "Bypass enabled."
                        : ChatColor.RED + "Bypass disabled.");
            }

            default -> {
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /proshield help");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("help", "compass", "admin", "reload", "debug", "bypass");
        }
        return new ArrayList<>();
    }
}
