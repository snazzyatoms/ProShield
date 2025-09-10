package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles /proshield and subcommands.
 * v1.2.5
 * - Integrated MessagesUtil
 * - Preserved reload, bypass, purge, debug, compass
 * - Extended consistency & feedback
 */
public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final GUIManager guiManager;
    private final MessagesUtil messages;

    public ProShieldCommand(ProShield plugin, PlotManager plotManager, GUIManager guiManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.guiManager = guiManager;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            messages.send(sender, "commands.help.header");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload" -> {
                if (!sender.hasPermission("proshield.admin.reload")) {
                    messages.send(sender, "no-permission");
                    return true;
                }
                plugin.reloadConfig();
                plotManager.reloadFromConfig();
                guiManager.clearCache();
                messages.send(sender, "commands.reload.success");
                return true;
            }

            case "bypass" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, "player-only");
                    return true;
                }
                if (!player.hasPermission("proshield.bypass")) {
                    messages.send(player, "no-permission");
                    return true;
                }
                plugin.toggleBypass(player);
                boolean bypassing = plugin.isBypassing(player);
                messages.send(player, bypassing ? "commands.bypass.on" : "commands.bypass.off");
                return true;
            }

            case "purgeexpired" -> {
                if (!sender.hasPermission("proshield.admin.expired.purge")) {
                    messages.send(sender, "no-permission");
                    return true;
                }
                if (args.length < 2) {
                    messages.send(sender, "commands.purgeexpired.usage");
                    return true;
                }
                try {
                    int days = Integer.parseInt(args[1]);
                    boolean dryRun = args.length > 2 && args[2].equalsIgnoreCase("dryrun");
                    int purged = plotManager.purgeExpired(days, dryRun);
                    if (dryRun) {
                        messages.send(sender, "commands.purgeexpired.dryrun", "%count%", String.valueOf(purged));
                    } else {
                        messages.send(sender, "commands.purgeexpired.done", "%count%", String.valueOf(purged));
                    }
                } catch (NumberFormatException e) {
                    messages.send(sender, "commands.purgeexpired.invalid");
                }
                return true;
            }

            case "debug" -> {
                if (!sender.hasPermission("proshield.admin.debug")) {
                    messages.send(sender, "no-permission");
                    return true;
                }
                boolean enabled = plugin.toggleDebug();
                messages.send(sender, enabled ? "commands.debug.on" : "commands.debug.off");
                return true;
            }

            case "compass" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, "player-only");
                    return true;
                }
                if (!player.hasPermission("proshield.compass")) {
                    messages.send(player, "no-permission");
                    return true;
                }
                boolean dropIfFull = plugin.getConfig().getBoolean("compass.drop-if-full", true);
                guiManager.giveCompass(player, dropIfFull);
                messages.send(player, "compass.given");
                return true;
            }

            default -> {
                messages.send(sender, "commands.unknown", "%cmd%", sub);
                return true;
            }
        }
    }
}
