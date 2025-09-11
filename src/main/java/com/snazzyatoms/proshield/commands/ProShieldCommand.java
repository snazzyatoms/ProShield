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

/**
 * /proshield command
 *
 * ✅ Admin + general utility command dispatcher.
 * ✅ Handles cache clearing, reload, compass distribution, debug toggle.
 * ✅ Uses CompassManager instance + GUICache instead of deprecated GUIManager calls.
 */
public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final CompassManager compassManager;
    private final MessagesUtil messages;

    public ProShieldCommand(ProShield plugin,
                            com.snazzyatoms.proshield.plots.PlotManager plotManager,
                            GUIManager guiManager,
                            CompassManager compassManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.compassManager = compassManager;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            messages.send(sender, "usage.proshield");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload" -> {
                if (!sender.hasPermission("proshield.admin.reload")) {
                    messages.send(sender, "error.no-permission");
                    return true;
                }
                plugin.reloadConfig();
                messages.send(sender, "prefix", "&aProShield configuration reloaded.");
            }

            case "clearcache" -> {
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, "error.no-permission");
                    return true;
                }
                plugin.getGuiCache().clearCache();
                messages.send(sender, "prefix", "&eAll GUI caches cleared.");
            }

            case "debug" -> {
                if (!sender.hasPermission("proshield.admin.debug")) {
                    messages.send(sender, "error.no-permission");
                    return true;
                }
                boolean debug = plugin.toggleDebug();
                messages.send(sender, "prefix", "&eDebug mode: " + (debug ? "&aON" : "&cOFF"));
            }

            case "compass" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, "error.players-only");
                    return true;
                }
                if (!player.hasPermission("proshield.compass")) {
                    messages.send(player, "error.no-permission");
                    return true;
                }
                compassManager.giveCompass(player, player.isOp()); // ✅ instance method
                messages.send(player, "prefix", "&aProShield compass has been given.");
            }

            default -> {
                messages.send(sender, "usage.proshield");
            }
        }

        return true;
    }
}
