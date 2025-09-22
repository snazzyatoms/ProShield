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
 * ProShieldCommand (v1.2.6 FINAL + Synced)
 *
 * - Handles /proshield command and its subcommands
 * - Unified compass handling with CompassManager + messages.yml
 * - Admin commands: reload, debug, bypass
 */
public class ProShieldCommand implements CommandExecutor, TabCompleter {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final CompassManager compassManager;

    public ProShieldCommand(ProShield plugin, CompassManager compassManager) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
        this.compassManager = compassManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                if (player.hasPermission("proshield.player.access")) {
                    guiManager.openMainMenu(player);
                } else {
                    sender.sendMessage(plugin.getMessagesUtil()
                            .getOrDefault("messages.error.no-permission", ChatColor.RED + "You do not have permission."));
                }
            } else {
                sender.sendMessage(plugin.getMessagesUtil()
                        .getOrDefault("messages.error.player-only", ChatColor.RED + "Only players can use this."));
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
                return true;
            }

            case "compass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.getMessagesUtil()
                            .getOrDefault("messages.error.player-only", ChatColor.RED + "Only players can use this."));
                    return true;
                }
                if (!player.hasPermission("proshield.compass")) {
                    player.sendMessage(plugin.getMessagesUtil()
                            .getOrDefault("messages.error.no-permission", ChatColor.RED + "You don’t have permission."));
                    return true;
                }

                if (compassManager.hasCompass(player)) {
                    player.sendMessage(plugin.getMessagesUtil()
                            .getOrDefault("messages.compass.already-have", ChatColor.YELLOW + "You already have a ProShield Compass."));
                    guiManager.openMainMenu(player);
                } else {
                    compassManager.giveCompass(player);
                    player.sendMessage(plugin.getMessagesUtil()
                            .getOrDefault("messages.compass.command-success", ChatColor.AQUA + "A ProShield Compass has been given to you."));
                }
                return true; // ✅ Make sure command is marked handled
            }

            case "admin" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.getMessagesUtil()
                            .getOrDefault("messages.error.player-only", ChatColor.RED + "Only players can use this."));
                    return true;
                }
                if (!player.hasPermission("proshield.admin")) {
                    sender.sendMessage(plugin.getMessagesUtil()
                            .getOrDefault("messages.error.no-permission", ChatColor.RED + "You don’t have permission."));
                    return true;
                }
                guiManager.openAdmin(player);
                return true;
            }

            case "reload" -> {
                if (!sender.hasPermission("proshield.admin.reload")) {
                    sender.sendMessage(plugin.getMessagesUtil()
                            .getOrDefault("messages.error.no-permission", ChatColor.RED + "You don’t have permission."));
                    return true;
                }
                plugin.reloadConfig();
                plugin.loadMessagesConfig();
                sender.sendMessage(plugin.getMessagesUtil()
                        .getOrDefault("messages.reloaded", ChatColor.GREEN + "ProShield configuration reloaded."));
                return true;
            }

            case "debug" -> {
                if (!sender.hasPermission("proshield.admin.debug")) {
                    sender.sendMessage(plugin.getMessagesUtil()
                            .getOrDefault("messages.error.no-permission", ChatColor.RED + "You don’t have permission."));
                    return true;
                }
                plugin.toggleDebug();
                sender.sendMessage(plugin.isDebugEnabled()
                        ? plugin.getMessagesUtil().getOrDefault("messages.admin.debug-on", ChatColor.YELLOW + "Debug mode ENABLED")
                        : plugin.getMessagesUtil().getOrDefault("messages.admin.debug-off", ChatColor.RED + "Debug mode DISABLED"));
                return true;
            }

            case "bypass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.getMessagesUtil()
                            .getOrDefault("messages.error.player-only", ChatColor.RED + "Only players can use this."));
                    return true;
                }
                if (!player.hasPermission("proshield.admin.bypass")) {
                    player.sendMessage(plugin.getMessagesUtil()
                            .getOrDefault("messages.error.no-permission", ChatColor.RED + "You don’t have permission."));
                    return true;
                }
                boolean now = plugin.toggleBypass(player.getUniqueId());
                player.sendMessage(now
                        ? plugin.getMessagesUtil().getOrDefault("messages.admin.bypass-on", ChatColor.GREEN + "Bypass enabled.")
                        : plugin.getMessagesUtil().getOrDefault("messages.admin.bypass-off", ChatColor.RED + "Bypass disabled."));
                return true;
            }

            default -> {
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /proshield help");
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("help", "compass", "admin", "reload", "debug", "bypass");
        }
        return new ArrayList<>();
    }
}
