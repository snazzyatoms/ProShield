package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Players only.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            gui.openPlayerMain(player);
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
                if (args.length >= 2) {
                    String role = args.length >= 3 ? args[2] : "member";
                    plots.trustPlayer(player, args[1], role);
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: /proshield trust <player> [role]");
                }
                return true;

            case "untrust":
                if (args.length >= 2) {
                    plots.untrustPlayer(player, args[1]);
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: /proshield untrust <player>");
                }
                return true;

            case "trusted":
                plots.listTrusted(player);
                return true;

            case "compass":
                gui.openPlayerMain(player);
                return true;

            case "reload":
                plugin.reloadAllConfigs();
                player.sendMessage(ChatColor.GREEN + "ProShield reloaded!");
                return true;

            case "purgeexpired":
                plots.purgeExpiredClaims(player, args);
                return true;

            case "debug":
                plugin.toggleDebug();
                player.sendMessage(ChatColor.YELLOW + "Debug mode toggled.");
                return true;

            case "toggle":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /proshield toggle <option>");
                    return true;
                }
                return handleToggle(player, args[1].toLowerCase());

            case "set":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /proshield set <option> <value>");
                    return true;
                }
                return handleSet(player, args);

            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /proshield help.");
                return true;
        }
    }

    private boolean handleToggle(Player player, String option) {
        switch (option) {
            case "fire":
            case "explosions":
            case "entitygrief":
            case "interactions":
            case "pvp":
            case "keepitems":
            case "compassdrop":
            case "spawnguard":
                plugin.toggleConfig("protection." + option);
                player.sendMessage(ChatColor.YELLOW + "Toggled " + option);
                return true;

            // NEW mob toggles
            case "mobspawn":
                plugin.toggleConfig("protection.mobs.block-spawn");
                player.sendMessage(ChatColor.YELLOW + "Toggled mob spawning");
                return true;

            case "mobdespawn":
                plugin.toggleConfig("protection.mobs.despawn-in-claims");
                player.sendMessage(ChatColor.YELLOW + "Toggled mob despawn in claims");
                return true;

            case "repel":
                plugin.toggleConfig("protection.mobs.border-repel.enabled");
                player.sendMessage(ChatColor.YELLOW + "Toggled mob repel");
                return true;

            default:
                player.sendMessage(ChatColor.RED + "Unknown toggle option.");
                return false;
        }
    }

    private boolean handleSet(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /proshield set <option> <value>");
            return false;
        }

        String option = args[1].toLowerCase();
        String valueStr = args[2];

        try {
            double value = Double.parseDouble(valueStr);
            switch (option) {
                case "repelradius":
                    plugin.getConfig().set("protection.mobs.border-repel.radius", value);
                    plugin.saveConfig();
                    player.sendMessage(ChatColor.GREEN + "Set repel radius to " + value);
                    return true;

                case "repelforce":
                    if (args.length < 4) {
                        player.sendMessage(ChatColor.RED + "Usage: /proshield set repelforce <horizontal> <vertical>");
                        return false;
                    }
                    double h = Double.parseDouble(valueStr);
                    double v = Double.parseDouble(args[3]);
                    plugin.getConfig().set("protection.mobs.border-repel.horizontal-push", h);
                    plugin.getConfig().set("protection.mobs.border-repel.vertical-push", v);
                    plugin.saveConfig();
                    player.sendMessage(ChatColor.GREEN + "Set repel force H=" + h + " V=" + v);
                    return true;

                default:
                    player.sendMessage(ChatColor.RED + "Unknown set option.");
                    return false;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid number: " + valueStr);
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("claim", "unclaim", "info", "trust", "untrust", "trusted",
                    "compass", "reload", "purgeexpired", "debug", "toggle", "set");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            return Arrays.asList("fire", "explosions", "entitygrief", "interactions", "pvp",
                    "keepitems", "compassdrop", "spawnguard", "mobspawn", "mobdespawn", "repel");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return Arrays.asList("repelradius", "repelforce");
        }
        return Collections.emptyList();
    }
}
