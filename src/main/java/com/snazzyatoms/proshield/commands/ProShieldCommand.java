// src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * ProShieldCommand (v1.2.6 FINAL)
 *
 * - Handles /proshield main command and subcommands
 * - /proshield → opens main GUI (players only)
 * - /proshield compass → gives ProShield Compass (players only)
 * - /proshield reload → reloads configs (admin)
 * - /proshield debug → toggles debug logging (admin)
 * - /proshield bypass → toggles admin bypass (admin)
 */
public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final MessagesUtil messages;
    private final CompassManager compassManager;

    public ProShieldCommand(ProShield plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
        this.messages = plugin.getMessagesUtil();
        this.compassManager = new CompassManager(plugin, guiManager); // ensures compass handling
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, messages.get("messages.error.player-only"));
            return true;
        }

        if (args.length == 0) {
            // Default: open GUI
            guiManager.openMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "compass" -> {
                if (!sender.hasPermission("proshield.command.compass")) {
                    messages.send(sender, messages.get("messages.error.no-permission"));
                    return true;
                }
                if (compassManager.hasCompass(player)) {
                    messages.send(player, messages.get("messages.compass.already-have"));
                } else {
                    compassManager.giveCompass(player);
                    messages.send(player, messages.get("messages.compass.command-success"));
                }
            }

            case "reload" -> {
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, messages.get("messages.error.no-permission"));
                    return true;
                }
                plugin.reloadConfig();
                messages.send(sender, messages.get("messages.reloaded"));
            }

            case "debug" -> {
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, messages.get("messages.error.no-permission"));
                    return true;
                }
                plugin.setDebugEnabled(!plugin.isDebugEnabled());
                messages.send(sender, plugin.isDebugEnabled()
                        ? messages.get("messages.admin.debug-on")
                        : messages.get("messages.admin.debug-off"));
            }

            case "bypass" -> {
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, messages.get("messages.error.no-permission"));
                    return true;
                }
                boolean toggled = plugin.toggleBypass(player.getUniqueId());
                messages.send(sender, toggled
                        ? messages.get("messages.admin.bypass-on")
                        : messages.get("messages.admin.bypass-off"));
            }

            default -> {
                messages.send(sender, "&cUnknown subcommand. Try: compass, reload, debug, bypass.");
            }
        }

        return true;
    }
}
