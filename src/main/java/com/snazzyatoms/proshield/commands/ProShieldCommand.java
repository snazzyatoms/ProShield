package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                messages.send(sender, "&cOnly players can use this command.");
                return true;
            }
            guiManager.openMenu(player, "main");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "claim" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, "&cOnly players can claim land.");
                    return true;
                }
                plotManager.claimPlot(player);
                return true;
            }
            case "unclaim" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, "&cOnly players can unclaim land.");
                    return true;
                }
                plotManager.unclaimPlot(player);
                return true;
            }
            case "info" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, "&cOnly players can check claim info.");
                    return true;
                }
                plotManager.sendClaimInfo(player);
                return true;
            }
            case "compass" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, "&cOnly players can use the compass.");
                    return true;
                }
                player.getInventory().addItem(plugin.getConfig().getItemStack("compass-item", null));
                messages.send(player, "&aYou received a ProShield Compass.");
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, "&cYou lack permission to reload.");
                    return true;
                }
                plugin.reloadConfig();
                messages.send(sender, "&aConfiguration reloaded.");
                return true;
            }
            case "debug" -> {
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, "&cYou lack permission to toggle debug.");
                    return true;
                }
                plugin.toggleDebug();
                messages.send(sender, "&aDebug mode: " + (plugin.isDebugEnabled() ? "enabled" : "disabled"));
                return true;
            }
            case "bypass" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, "&cOnly players can toggle bypass.");
                    return true;
                }
                if (plugin.getBypassing().contains(player.getUniqueId())) {
                    plugin.getBypassing().remove(player.getUniqueId());
                    messages.send(player, "&cBypass disabled.");
                } else {
                    plugin.getBypassing().add(player.getUniqueId());
                    messages.send(player, "&aBypass enabled.");
                }
                return true;
            }
            case "help" -> {
                messages.sendHelp(sender, args.length > 1 && args[1].equalsIgnoreCase("admin"));
                return true;
            }
            case "admin" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, "&cOnly players can open the admin menu.");
                    return true;
                }
                if (!player.hasPermission("proshield.admin")) {
                    messages.send(player, "&cYou lack permission to open admin tools.");
                    return true;
                }
                guiManager.openMenu(player, "admin-expansions");
                return true;
            }
            default -> messages.send(sender, "&cUnknown subcommand. Use /proshield help");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("claim");
            completions.add("unclaim");
            completions.add("info");
            completions.add("compass");
            completions.add("help");

            if (sender.hasPermission("proshield.admin")) {
                completions.add("reload");
                completions.add("debug");
                completions.add("bypass");
                completions.add("admin");
            }
        }
        return completions;
    }
}
