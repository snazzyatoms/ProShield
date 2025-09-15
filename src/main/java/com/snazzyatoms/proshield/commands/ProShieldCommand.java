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
            case "help" -> {
                sendHelp(sender);
                return true;
            }
            case "claim" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }
                plotManager.claimPlot(player); // stubbed in PlotManager
                return true;
            }
            case "unclaim" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }
                plotManager.unclaimPlot(player); // stubbed in PlotManager
                return true;
            }
            case "info" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }
                plotManager.sendClaimInfo(player); // stubbed in PlotManager
                return true;
            }
            case "compass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }
                guiManager.openMenu(player, "main");
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("proshield.admin.reload")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "ProShield configuration reloaded.");

                // Give compass to all online players if enabled
                if (plugin.getConfig().getBoolean("settings.give-compass-on-join", true)) {
                    plugin.getCompassManager().giveCompassToAll();
                }
                return true;
            }
            case "debug" -> {
                if (!sender.hasPermission("proshield.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                plugin.toggleDebug();
                sender.sendMessage(ChatColor.YELLOW + "Debug mode: " + plugin.isDebugEnabled());
                return true;
            }
            case "bypass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }
                if (plugin.isBypassing(player.getUniqueId())) {
                    plugin.getBypassing().remove(player.getUniqueId());
                    sender.sendMessage(ChatColor.RED + "Bypass disabled.");
                } else {
                    plugin.getBypassing().add(player.getUniqueId());
                    sender.sendMessage(ChatColor.GREEN + "Bypass enabled.");
                }
                return true;
            }
            case "admin" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }
                if (!sender.hasPermission("proshield.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                guiManager.openMenu(player, "admin-expansions");
                return true;
            }
            default -> {
                sendHelp(sender);
                return true;
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        if (sender.hasPermission("proshield.admin")) {
            for (String line : plugin.getConfig().getStringList("help.admin")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
            }
        } else {
            for (String line : plugin.getConfig().getStringList("help.player")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
            }
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
