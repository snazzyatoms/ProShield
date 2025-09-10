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

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "ProShield Commands:");
        sender.sendMessage(ChatColor.GRAY + "/proshield claim " + ChatColor.WHITE + "- Claim current chunk");
        sender.sendMessage(ChatColor.GRAY + "/proshield unclaim " + ChatColor.WHITE + "- Unclaim current chunk");
        sender.sendMessage(ChatColor.GRAY + "/proshield info " + ChatColor.WHITE + "- Show claim info");
        sender.sendMessage(ChatColor.GRAY + "/proshield trust <player> [role] " + ChatColor.WHITE + "- Trust player with role");
        sender.sendMessage(ChatColor.GRAY + "/proshield untrust <player> " + ChatColor.WHITE + "- Remove trust");
        sender.sendMessage(ChatColor.GRAY + "/proshield transfer <player> " + ChatColor.WHITE + "- Transfer ownership");
        sender.sendMessage(ChatColor.GRAY + "/proshield preview " + ChatColor.WHITE + "- Preview claim borders");
        sender.sendMessage(ChatColor.GRAY + "/proshield compass " + ChatColor.WHITE + "- Get ProShield compass");
        sender.sendMessage(ChatColor.GRAY + "/proshield reload " + ChatColor.WHITE + "- Reload configuration (admin)");
        sender.sendMessage(ChatColor.GRAY + "/proshield purgeexpired <days> [dryrun] " + ChatColor.WHITE + "- Purge expired claims (admin)");
        sender.sendMessage(ChatColor.GRAY + "/proshield bypass " + ChatColor.WHITE + "- Toggle bypass mode (admin)");
        sender.sendMessage(ChatColor.GRAY + "/proshield debug <on|off> " + ChatColor.WHITE + "- Toggle debug logging (admin)");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                gui.openMain(player);
            } else {
                sendUsage(sender);
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "claim" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can claim land.");
                    return true;
                }
                plots.claimChunk(player);
                return true;
            }

            case "unclaim" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can unclaim land.");
                    return true;
                }
                plots.unclaimChunk(player);
                return true;
            }

            case "info" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can use this.");
                    return true;
                }
                plots.showClaimInfo(player);
                return true;
            }

            case "trust" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can trust others.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /proshield trust <player> [role]");
                    return true;
                }
                String target = args[1];
                String role = args.length > 2 ? args[2] : "member";
                plots.trustPlayer(player, target, role);
                return true;
            }

            case "untrust" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can untrust others.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /proshield untrust <player>");
                    return true;
                }
                String target = args[1];
                plots.untrustPlayer(player, target);
                return true;
            }

            case "transfer" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can transfer claims.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /proshield transfer <player>");
                    return true;
                }
                String target = args[1];
                plots.transferClaim(player, target);
                return true;
            }

            case "preview" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can preview claims.");
                    return true;
                }
                plots.previewClaim(player);
                return true;
            }

            case "compass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can get a compass.");
                    return true;
                }
                gui.giveCompass(player, player.isOp());
                return true;
            }

            case "reload" -> {
                if (!sender.hasPermission("proshield.admin.reload")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                plugin.reloadAllConfigs();
                sender.sendMessage(ChatColor.GREEN + "ProShield reloaded.");
                return true;
            }

            case "purgeexpired" -> {
                if (!sender.hasPermission("proshield.admin.expired.purge")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                int days = args.length > 1 ? Integer.parseInt(args[1]) : 30;
                boolean dryRun = args.length > 2 && args[2].equalsIgnoreCase("dryrun");
                plots.purgeExpiredClaims(sender, days, dryRun);
                return true;
            }

            case "bypass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can toggle bypass.");
                    return true;
                }
                if (!player.hasPermission("proshield.bypass")) {
                    player.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                plots.toggleBypass(player);
                return true;
            }

            case "debug" -> {
                if (!sender.hasPermission("proshield.admin.debug")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /proshield debug <on|off>");
                    return true;
                }
                boolean enable = args[1].equalsIgnoreCase("on");
                plugin.getConfig().set("proshield.debug", enable);
                sender.sendMessage(ChatColor.YELLOW + "Debug mode set to: " + enable);
                return true;
            }

            default -> sendUsage(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("claim", "unclaim", "info", "trust", "untrust",
                    "transfer", "preview", "compass", "reload", "purgeexpired", "bypass", "debug");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("trust")) {
            List<String> online = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> online.add(p.getName()));
            return online;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("transfer")) {
            List<String> online = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> online.add(p.getName()));
            return online;
        }
        return Collections.emptyList();
    }
}
