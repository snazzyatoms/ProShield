package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles /proshield and related commands.
 * - Opens GUI
 * - Handles compass distribution
 * - Admin reload/debug/purge
 * - Feedback now goes through MessagesUtil for consistency
 */
public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public ProShieldCommand(ProShield plugin, GUIManager guiManager, PlotManager plotManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "general.players-only");
            return true;
        }

        if (args.length == 0) {
            guiManager.openMain(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "compass" -> {
                if (!player.hasPermission("proshield.compass")) {
                    messages.send(player, "general.no-permission");
                    return true;
                }
                guiManager.giveCompass(player, true);
                messages.send(player, "compass.given");
            }
            case "reload" -> {
                if (!player.hasPermission("proshield.admin.reload")) {
                    messages.send(player, "general.no-permission");
                    return true;
                }
                plugin.reloadConfig();
                plugin.getPlotManager().reloadFromConfig();
                messages.send(player, "admin.reloaded");
            }
            case "bypass" -> {
                if (!player.hasPermission("proshield.bypass")) {
                    messages.send(player, "general.no-permission");
                    return true;
                }
                plugin.toggleBypass(player);
                boolean bypass = plugin.isBypassing(player);
                messages.send(player, bypass ? "admin.bypass-on" : "admin.bypass-off");
            }
            case "purgeexpired" -> {
                if (!player.hasPermission("proshield.admin.expired.purge")) {
                    messages.send(player, "general.no-permission");
                    return true;
                }
                int days = args.length > 1 ? Integer.parseInt(args[1]) : 30;
                boolean dryRun = args.length > 2 && args[2].equalsIgnoreCase("dryrun");
                int removed = plotManager.purgeExpired(days, dryRun);
                messages.send(player, "admin.purge-expired",
                        "%count%", String.valueOf(removed),
                        "%days%", String.valueOf(days),
                        "%dryrun%", String.valueOf(dryRun));
            }
            case "debug" -> {
                if (!player.hasPermission("proshield.admin.debug")) {
                    messages.send(player, "general.no-permission");
                    return true;
                }
                boolean enabled = plugin.toggleDebug();
                messages.send(player, enabled ? "admin.debug-on" : "admin.debug-off");
            }
            default -> {
                messages.send(player, "general.unknown-command");
            }
        }
        return true;
    }
}
