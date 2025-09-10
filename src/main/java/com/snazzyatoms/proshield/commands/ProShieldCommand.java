package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProShieldCommand implements CommandExecutor, TabCompleter {

    private final ProShield plugin;
    private final PlotManager plots;
    private final GUIManager gui;

    public ProShieldCommand(ProShield plugin, PlotManager plots, GUIManager gui) {
        this.plugin = plugin;
        this.plots = plots;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                boolean isAdmin = player.isOp() || player.hasPermission("proshield.admin");
                gui.openMain(player, isAdmin);
                return true;
            }
            sender.sendMessage(ChatColor.RED + "This command is player-only.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "claim" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                plots.claimChunk(player);
                return true;
            }
            case "unclaim" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                plots.unclaimChunk(player);
                return true;
            }
            case "info" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                plots.showClaimInfo(player);
                return true;
            }
            case "compass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                boolean isAdmin = player.isOp() || player.hasPermission("proshield.admin");
                ItemStack compass = gui.createCompass(isAdmin);
                player.getInventory().addItem(compass);
                player.sendMessage(ChatColor.GREEN + "You have received the " + (isAdmin ? "Admin " : "") + "ProShield Compass!");
                return true;
            }
            case "bypass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                if (!player.hasPermission("proshield.bypass")) {
                    player.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                plugin.toggleBypass(player);
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("proshield.admin.reload")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                plugin.reloadConfig();
                gui.onConfigReload();
                sender.sendMessage(ChatColor.GREEN + "ProShield configuration reloaded.");
                return true;
            }
            case "purgeexpired" -> {
                if (!sender.hasPermission("proshield.admin.expired.purge")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                int days = args.length >= 2 ? Integer.parseInt(args[1]) : plugin.getConfig().getInt("expiry.days", 30);
                boolean dryRun = args.length >= 3 && args[2].equalsIgnoreCase("dryrun");
                int removed = plots.purgeExpiredClaims(days, dryRun);
                sender.sendMessage(ChatColor.YELLOW + "Expired claims " + (dryRun ? "that would be removed: " : "removed: ") + removed);
                return true;
            }
            case "debug" -> {
                if (!sender.hasPermission("proshield.admin.debug")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /proshield debug <on|off>");
                    return true;
                }
                boolean enable = args[1].equalsIgnoreCase("on");
                plugin.setDebug(enable);
                sender.sendMessage(ChatColor.GREEN + "Debug mode " + (enable ? "enabled" : "disabled"));
                return true;
            }
            default -> {
                sender.sendMessage(ChatColor.YELLOW + "Unknown command. Use /proshield for help.");
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("claim", "unclaim", "info", "compass", "bypass", "reload", "purgeexpired", "debug");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            return Arrays.asList("on", "off");
        }
        if (args[0].equalsIgnoreCase("purgeexpired") && args.length == 2) {
            return Collections.singletonList("<days>");
        }
        return new ArrayList<>();
    }
}
