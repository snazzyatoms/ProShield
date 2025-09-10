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

/**
 * ProShieldCommand - Handles all /proshield commands.
 * Commands either run directly or open GUIs (player/admin).
 */
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
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Players only.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            gui.openMain(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "claim":
                plots.claimChunk(player);
                break;

            case "unclaim":
                plots.unclaimChunk(player);
                break;

            case "info":
                plots.showClaimInfo(player);
                break;

            case "trust":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /proshield trust <player> [role]");
                    return true;
                }
                String targetTrust = args[1];
                String role = args.length >= 3 ? args[2] : "member";
                plots.trustPlayer(player, targetTrust, role);
                break;

            case "untrust":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /proshield untrust <player>");
                    return true;
                }
                String targetUntrust = args[1];
                plots.untrustPlayer(player, targetUntrust);
                break;

            case "trusted":
                plots.listTrusted(player);
                break;

            case "transfer":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /proshield transfer <player>");
                    return true;
                }
                String newOwner = args[1];
                plots.transferOwnership(player, newOwner);
                break;

            case "flags":
                gui.openPlayerFlags(player);
                break;

            case "roles":
                gui.openPlayerRoles(player);
                break;

            case "gui":
            case "menu":
                gui.openMain(player);
                break;

            case "compass":
                gui.giveCompass(player, player.isOp());
                break;

            case "bypass":
                if (!player.hasPermission("proshield.bypass")) {
                    player.sendMessage(ChatColor.RED + "You lack permission: proshield.bypass");
                    return true;
                }
                plots.toggleBypass(player);
                break;

            case "reload":
                if (!player.hasPermission("proshield.admin.reload")) {
                    player.sendMessage(ChatColor.RED + "You lack permission: proshield.admin.reload");
                    return true;
                }
                plugin.reloadAllConfigs();
                player.sendMessage(ChatColor.GREEN + "ProShield configuration reloaded.");
                break;

            case "purgeexpired":
                if (!player.hasPermission("proshield.admin.expired.purge")) {
                    player.sendMessage(ChatColor.RED + "You lack permission: proshield.admin.expired.purge");
                    return true;
                }
                int days = args.length >= 2 ? Integer.parseInt(args[1]) : plugin.getConfig().getInt("expiry.days", 30);
                boolean dryrun = args.length >= 3 && args[2].equalsIgnoreCase("dryrun");
                plots.purgeExpiredClaims(player, days, dryrun);
                break;

            case "debug":
                if (!player.hasPermission("proshield.admin.debug")) {
                    player.sendMessage(ChatColor.RED + "You lack permission: proshield.admin.debug");
                    return true;
                }
                plots.toggleDebug(player);
                break;

            default:
                player.sendMessage(ChatColor.YELLOW + "Unknown subcommand. Use /proshield for menu.");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return null;

        List<String> subs = Arrays.asList(
                "claim", "unclaim", "info", "trust", "untrust",
                "trusted", "transfer", "flags", "roles", "menu",
                "compass", "bypass", "reload", "purgeexpired", "debug"
        );

        if (args.length == 1) {
            List<String> matches = new ArrayList<>();
            for (String sub : subs) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    matches.add(sub);
                }
            }
            return matches;
        }

        return null;
    }
}
