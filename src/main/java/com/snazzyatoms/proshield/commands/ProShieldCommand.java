// src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final GUIManager guiManager;
    private final CompassManager compassManager;
    private final MessagesUtil messages;

    public ProShieldCommand(ProShield plugin, PlotManager plotManager, GUIManager guiManager, CompassManager compassManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.guiManager = guiManager;
        this.compassManager = compassManager;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> handleReload(sender);
            case "debug" -> handleDebug(sender);
            case "compass" -> handleCompass(sender);
            case "flags" -> handleFlags(sender);
            case "help" -> sendHelp(sender);
            default -> messages.send(sender, "error.unknown-command");
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("proshield.admin.reload")) {
            messages.send(sender, "error.no-permission");
            return;
        }
        plugin.reloadConfig();
        messages.send(sender, "messages.reloaded");
    }

    private void handleDebug(CommandSender sender) {
        if (!sender.hasPermission("proshield.admin")) {
            messages.send(sender, "error.no-permission");
            return;
        }
        plugin.toggleDebug();
        messages.send(sender, plugin.isDebugEnabled() ? "admin.debug-on" : "admin.debug-off");
    }

    private void handleCompass(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.player-only");
            return;
        }
        if (!player.hasPermission("proshield.compass")) {
            messages.send(sender, "error.no-permission");
            return;
        }
        compassManager.giveCompass(player, true);
        messages.send(player, "compass.given");
    }

    private void handleFlags(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.player-only");
            return;
        }
        if (!player.hasPermission("proshield.flags")) {
            messages.send(sender, "error.no-permission");
            return;
        }
        guiManager.openMenu(player, "flags");
    }

    private void sendHelp(CommandSender sender) {
        List<String> playerHelp = plugin.getConfig().getStringList("help.player");
        for (String line : playerHelp) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }

        if (sender.hasPermission("proshield.admin")) {
            List<String> adminHelp = plugin.getConfig().getStringList("help.admin");
            for (String line : adminHelp) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
            }
        }
    }
}
