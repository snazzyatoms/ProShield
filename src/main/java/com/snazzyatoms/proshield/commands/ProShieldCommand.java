// src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ProShieldCommand
 * - Root /proshield command
 * - Handles reload, debug, bypass, compass, admin menu
 *
 * Fixed for v1.2.5:
 *   • Removed invalid MessagesUtil#getConfigList calls
 *   • Uses plugin.getConfig().getStringList() for help sections
 *   • Updated /proshield admin to open "main" menu (admin entries hidden if no permission)
 */
public class ProShieldCommand implements CommandExecutor, TabCompleter {

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
            showHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload" -> {
                if (!sender.hasPermission("proshield.admin.reload")) {
                    messages.send(sender, "error.no-permission");
                    return true;
                }
                plugin.reloadConfig();
                messages.send(sender, "admin.reload");
            }
            case "debug" -> {
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, "error.no-permission");
                    return true;
                }
                plugin.toggleDebug();
                messages.send(sender, plugin.isDebugEnabled() ? "admin.debug-on" : "admin.debug-off");
            }
            case "bypass" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, "error.player-only");
                    return true;
                }
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, "error.no-permission");
                    return true;
                }
                UUID id = player.getUniqueId();
                boolean newState = plotManager.toggleBypass(id);
                messages.send(player, newState ? "admin.bypass-on" : "admin.bypass-off");
            }
            case "compass" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, "error.player-only");
                    return true;
                }
                if (!sender.hasPermission("proshield.compass")) {
                    messages.send(sender, "error.no-permission");
                    return true;
                }
                compassManager.giveCompass(player);
                messages.send(player, "compass.given");
            }
            case "admin" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, "error.player-only");
                    return true;
                }
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, "error.no-permission");
                    return true;
                }
                // Open unified main menu (admin items show only if permission is present)
                guiManager.openMenu(player, "main");
            }
            case "help" -> showHelp(sender);
            default -> messages.send(sender, "error.unknown-command");
        }
        return true;
    }

    private void showHelp(CommandSender sender) {
        List<String> lines = new ArrayList<>(plugin.getConfig().getStringList("help.player"));
        if (sender.hasPermission("proshield.admin")) {
            lines.addAll(plugin.getConfig().getStringList("help.admin"));
        }
        for (String line : lines) sender.sendMessage(line);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("proshield")) return Collections.emptyList();
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(List.of("help"));
            if (sender.hasPermission("proshield.admin.reload")) subs.add("reload");
            if (sender.hasPermission("proshield.admin")) {
                subs.addAll(Arrays.asList("debug", "bypass", "compass", "admin"));
            }
            return subs.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
