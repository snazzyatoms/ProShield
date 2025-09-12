// src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final GUIManager guiManager;
    private final CompassManager compassManager;
    private final MessagesUtil messages;

    public ProShieldCommand(ProShield plugin,
                            PlotManager plotManager,
                            GUIManager guiManager,
                            CompassManager compassManager) {
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
            case "reload":   return handleReload(sender);
            case "debug":    return handleDebug(sender);
            case "compass":  return handleCompass(sender);
            case "flag":     return handleFlag(sender, args);
            default:
                sendHelp(sender);
                return true;
        }
    }

    /* -------------------------------------------------------
     * /proshield reload
     * ------------------------------------------------------- */
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("proshield.admin.reload")) {
            messages.send(sender, "error.no-permission");
            return true;
        }

        plugin.reloadConfig();
        messages.reload();
        messages.send(sender, "messages.reloaded");
        return true;
    }

    /* -------------------------------------------------------
     * /proshield debug
     * ------------------------------------------------------- */
    private boolean handleDebug(CommandSender sender) {
        if (!sender.hasPermission("proshield.admin")) {
            messages.send(sender, "error.no-permission");
            return true;
        }

        plugin.toggleDebug();
        messages.send(sender, plugin.isDebugEnabled() ? "admin.debug-on" : "admin.debug-off");
        return true;
    }

    /* -------------------------------------------------------
     * /proshield compass
     * ------------------------------------------------------- */
    private boolean handleCompass(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.player-only");
            return true;
        }

        if (!player.hasPermission("proshield.compass")) {
            messages.send(player, "error.no-permission");
            return true;
        }

        compassManager.giveCompass(player, true);
        messages.send(player, "compass.given");
        return true;
    }

    /* -------------------------------------------------------
     * /proshield flag <flag>
     * ------------------------------------------------------- */
    private boolean handleFlag(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.player-only");
            return true;
        }

        if (args.length < 2) {
            messages.send(player, "flags.usage");
            return true;
        }

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            messages.send(player, "error.no-claim");
            return true;
        }

        String flag = args[1].toLowerCase();
        boolean newState = plot.toggleFlag(flag);

        messages.send(player, "flags.toggle", Map.of(
                "flag", flag,
                "state", newState ? "ENABLED" : "DISABLED",
                "claim", plot.getDisplayNameSafe()
        ));
        return true;
    }

    /* -------------------------------------------------------
     * HELP
     * ------------------------------------------------------- */
    private void sendHelp(CommandSender sender) {
        messages.sendList(sender, "help.proshield");
    }
}
