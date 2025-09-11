// src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
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
 * Main /proshield command executor
 *
 * ✅ Preserves prior logic
 * ✅ Fixed reload, cache clear, compass, and debug toggle
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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            messages.send(sender, "help.header", "&7Use &a/proshield help &7for commands.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("proshield.admin.reload")) {
                    messages.send(sender, "no-permission", "&cYou lack permission.");
                    return true;
                }

                plugin.reloadConfig();
                messages.reload();

                // Reload claim configs
                plotManager.reloadFromConfig();

                // Clear GUI cache if available
                if (guiManager != null) {
                    guiManager.clearCache();
                }

                messages.broadcastConsole("messages.reloaded", Bukkit.getConsoleSender());
                messages.send(sender, "reloaded", "&aProShield reloaded.");
                return true;
            }

            case "bypass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can toggle bypass.");
                    return true;
                }
                if (!sender.hasPermission("proshield.bypass")) {
                    messages.send(sender, "no-permission", "&cYou lack permission.");
                    return true;
                }
                boolean newState = plugin.toggleBypass(player);
                messages.send(player, newState ? "admin.bypass-on" : "admin.bypass-off");
                return true;
            }

            case "debug" -> {
                if (!sender.hasPermission("proshield.admin.debug")) {
                    messages.send(sender, "no-permission", "&cYou lack permission.");
                    return true;
                }
                boolean newDebug = plugin.toggleDebug();
                sender.sendMessage("§eProShield debug: " + (newDebug ? "§aON" : "§cOFF"));
                return true;
            }

            case "compass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can get the compass.");
                    return true;
                }
                if (!sender.hasPermission("proshield.compass")) {
                    messages.send(sender, "no-permission", "&cYou lack permission.");
                    return true;
                }

                // Gracefully handle giveCompass() missing
                try {
                    guiManager.giveCompass(player, true);
                    messages.send(player, "compass.given", "&aYou received a ProShield compass.");
                } catch (NoSuchMethodError ignored) {
                    player.sendMessage("§cCompass feature is not available in this build.");
                }
                return true;
            }

            default -> {
                messages.send(sender, "help.unknown", "&cUnknown subcommand.");
                return true;
            }
        }
    }
}
