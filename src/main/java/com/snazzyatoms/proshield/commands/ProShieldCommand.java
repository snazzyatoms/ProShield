package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.ProShieldReloadEvent;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
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
                    messages.send(sender, messages.get("error.player-only"));
                    return true;
                }
                plotManager.claimPlot(player);
                return true;
            }
            case "unclaim" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, messages.get("error.player-only"));
                    return true;
                }
                plotManager.unclaimPlot(player);
                return true;
            }
            case "info" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, messages.get("error.player-only"));
                    return true;
                }
                plotManager.sendClaimInfo(player);
                return true;
            }
            case "compass" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, messages.get("error.player-only"));
                    return true;
                }
                plugin.getCompassManager().giveCompass(player);
                messages.send(player, messages.get("compass.command-success"));
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, messages.get("error.no-permission"));
                    return true;
                }

                plugin.reloadConfig();

                // 🔔 Fire reload event so all listeners can resync
                Bukkit.getPluginManager().callEvent(new ProShieldReloadEvent(plugin));

                messages.send(sender, messages.get("messages.reloaded"));

                // After reload, re-give compasses if enabled in config
                boolean giveOnJoin = plugin.getConfig().getBoolean("settings.give-compass-on-join", true);
                boolean autoReplace = plugin.getConfig().getBoolean("settings.compass-auto-replace", false);

                if (giveOnJoin || autoReplace) {
                    plugin.getCompassManager().giveCompassToAll();
                }
                return true;
            }
            case "debug" -> {
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, messages.get("error.no-permission"));
                    return true;
                }
                plugin.toggleDebug();
                String status = plugin.isDebugEnabled()
                        ? messages.get("admin.debug-on")
                        : messages.get("admin.debug-off");
                messages.send(sender, status);
                return true;
            }
            case "bypass" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, messages.get("error.player-only"));
                    return true;
                }
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, messages.get("error.no-permission"));
                    return true;
                }
                if (plugin.isBypassing(player.getUniqueId())) {
                    plugin.getBypassing().remove(player.getUniqueId());
                    messages.send(player, messages.get("admin.bypass-off"));
                } else {
                    plugin.getBypassing().add(player.getUniqueId());
                    messages.send(player, messages.get("admin.bypass-on"));
                }
                return true;
            }
            case "admin" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, messages.get("error.player-only"));
                    return true;
                }
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, messages.get("error.no-permission"));
                    return true;
                }
                guiManager.openMenu(player, "admin-tools");
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
                messages.sendRaw(sender, line);
            }
        } else {
            for (String line : plugin.getConfig().getStringList("help.player")) {
                messages.sendRaw(sender, line);
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
