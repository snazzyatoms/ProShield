package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class ProShieldCommand implements CommandExecutor, TabCompleter {

    private final ProShield plugin;
    private final PlotManager plots;
    private final GUIManager gui;

    public ProShieldCommand(ProShield plugin, PlotManager plots, GUIManager gui) {
        this.plugin = plugin;
        this.plots = plots;
        this.gui = gui;
    }

    private void msg(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r ") + msg));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            msg(sender, "&cOnly players can use ProShield commands.");
            return true;
        }

        String name = cmd.getName().toLowerCase(Locale.ROOT);

        switch (name) {
            case "proshield" -> openMain(player, true);
            case "claim" -> plots.claim(player);
            case "unclaim" -> plots.unclaim(player);
            case "info" -> plots.sendInfo(player);
            case "trust" -> {
                if (args.length < 1) {
                    msg(player, "&cUsage: /trust <player> [role]");
                    return true;
                }
                String target = args[0];
                String role = args.length > 1 ? args[1] : "member";
                plots.trust(player, target, role);
            }
            case "untrust" -> {
                if (args.length < 1) {
                    msg(player, "&cUsage: /untrust <player>");
                    return true;
                }
                plots.untrust(player, args[0]);
            }
            case "trusted" -> plots.listTrusted(player);
            case "roles" -> gui.openRolesMenu(player);
            case "transfer" -> {
                if (args.length < 1) {
                    msg(player, "&cUsage: /transfer <player>");
                    return true;
                }
                plots.transferClaim(player, args[0]);
            }
            case "preview" -> plots.previewClaim(player);
            case "compass" -> gui.giveCompass(player, player.isOp()); // admins get admin compass
            case "bypass" -> plots.toggleBypass(player, args);
            case "reload" -> {
                if (!player.hasPermission("proshield.admin.reload")) {
                    msg(player, "&cNo permission.");
                    return true;
                }
                plugin.reloadConfig();
                gui.onConfigReload();
                msg(player, "&aConfiguration reloaded.");
            }
            case "purgeexpired" -> plots.purgeExpired(player, args);
            case "debug" -> plots.toggleDebug(player, args);
            default -> msg(player, "&cUnknown command. Use /proshield for help.");
        }
        return true;
    }

    private void openMain(Player player, boolean isRoot) {
        gui.openMain(player);
        if (isRoot) {
            msg(player, "&7Opening ProShield menu...");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        String name = cmd.getName().toLowerCase(Locale.ROOT);

        if (!(sender instanceof Player)) return suggestions;

        switch (name) {
            case "trust", "untrust", "transfer" -> {
                if (args.length == 1) {
                    Bukkit.getOnlinePlayers().forEach(p -> suggestions.add(p.getName()));
                }
            }
            case "debug" -> {
                if (args.length == 1) suggestions.addAll(Arrays.asList("on", "off", "toggle"));
            }
            case "bypass" -> {
                if (args.length == 1) suggestions.addAll(Arrays.asList("on", "off", "toggle"));
            }
            case "purgeexpired" -> {
                if (args.length == 1) suggestions.add("<days>");
                else if (args.length == 2) suggestions.add("dryrun");
            }
        }

        return suggestions;
    }
}
