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

import java.util.ArrayList;
import java.util.Arrays;
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
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Players only.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            gui.openMain(player, player.isOp());
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "claim":
                plots.claimChunk(player);
                return true;

            case "unclaim":
                plots.unclaimChunk(player);
                return true;

            case "info":
                plots.showClaimInfo(player);
                return true;

            case "trust":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /proshield trust <player> [role]");
                    return true;
                }
                String trustTarget = args[1];
                String role = args.length >= 3 ? args[2] : "Member";
                plots.trustPlayer(player, trustTarget, role);
                return true;

            case "untrust":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /proshield untrust <player>");
                    return true;
                }
                plots.untrustPlayer(player, args[1]);
                return true;

            case "trusted":
                plots.listTrustedPlayers(player);
                return true;

            case "transfer":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /proshield transfer <player>");
                    return true;
                }
                plots.transferClaim(player, args[1]);
                return true;

            case "preview":
                plots.previewClaim(player);
                return true;

            case "compass":
                gui.giveCompass(player, player.isOp());
                return true;

            case "reload":
                if (!player.hasPermission("proshield.admin.reload")) {
                    player.sendMessage(ChatColor.RED + "You lack permission.");
                    return true;
                }
                plugin.reloadConfig();
                player.sendMessage(ChatColor.GREEN + "ProShield reloaded!");
                return true;

            case "purgeexpired":
                if (!player.hasPermission("proshield.admin.expired.purge")) {
                    player.sendMessage(ChatColor.RED + "You lack permission.");
                    return true;
                }
                int days = args.length >= 2 ? Integer.parseInt(args[1]) : plugin.getConfig().getInt("expiry.days", 30);
                boolean dryRun = args.length >= 3 && args[2].equalsIgnoreCase("dryrun");
                plots.purgeExpiredClaims(player, days, dryRun);
                return true;

            case "debug":
                if (!player.hasPermission("proshield.admin.debug")) {
                    player.sendMessage(ChatColor.RED + "You lack permission.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /proshield debug <on|off>");
                    return true;
                }
                boolean enable = args[1].equalsIgnoreCase("on");
                plugin.getConfig().set("proshield.debug", enable);
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "Debug " + (enable ? "enabled" : "disabled"));
                return true;

            default:
                player.sendMessage(ChatColor.YELLOW + "Unknown command. Use /proshield for help.");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("claim", "unclaim", "info", "trust", "untrust", "trusted",
                    "transfer", "preview", "compass", "reload", "purgeexpired", "debug");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("trust")) {
            List<String> names = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> names.add(p.getName()));
            return names;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("untrust")) {
            return plots.getTrustedPlayerNames((Player) sender);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("transfer")) {
            List<String> names = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> names.add(p.getName()));
            return names;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            return Arrays.asList("on", "off");
        }
        return null;
    }
}
