package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ProShieldCommand implements CommandExecutor, TabCompleter {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public ProShieldCommand(ProShield plugin, GUIManager guiManager, PlotManager plotManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.plotManager = plotManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "help" -> sendHelp(sender);

            case "claim" -> {
                if (sender instanceof Player player) {
                    plotManager.claimPlot(player);
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                }
            }

            case "unclaim" -> {
                if (sender instanceof Player player) {
                    plotManager.unclaimPlot(player);
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                }
            }

            case "info" -> {
                if (sender instanceof Player player) {
                    plotManager.sendClaimInfo(player);
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                }
            }

            case "compass" -> {
                if (sender instanceof Player player) {
                    guiManager.openMenu(player, "main");
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                }
            }

            case "reload" -> {
                if (sender.hasPermission("proshield.admin.reload")) {
                    plugin.reloadConfig();
                    sender.sendMessage(ChatColor.GREEN + "ProShield configuration reloaded.");
                } else {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                }
            }

            case "debug" -> {
                if (sender.hasPermission("proshield.admin")) {
                    plugin.toggleDebug();
                    sender.sendMessage(ChatColor.YELLOW + "Debug mode: " + plugin.isDebugEnabled());
                } else {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                }
            }

            case "bypass" -> {
                if (sender instanceof Player player) {
                    if (plugin.isBypassing(player.getUniqueId())) {
                        plugin.getBypassing().remove(player.getUniqueId());
                        sender.sendMessage(ChatColor.RED + "Bypass disabled.");
                    } else {
                        plugin.getBypassing().add(player.getUniqueId());
                        sender.sendMessage(ChatColor.GREEN + "Bypass enabled.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                }
            }

            case "admin" -> {
                if (sender instanceof Player player) {
                    if (sender.hasPermission("proshield.admin")) {
                        guiManager.openMenu(player, "admin-expansions");
                    } else {
                        sender.sendMessage(ChatColor.RED + "No permission.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                }
            }

            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        List<String> lines = sender.hasPermission("proshield.admin")
                ? plugin.getConfig().getStringList("help.admin")
                : plugin.getConfig().getStringList("help.player");

        for (String line : lines) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("help");
            completions.add("claim");
            completions.add("unclaim");
            completions.add("info");
            completions.add("compass");
            completions.add("reload");
            completions.add("debug");
            completions.add("bypass");
            completions.add("admin");
        }
        return completions;
    }
}
