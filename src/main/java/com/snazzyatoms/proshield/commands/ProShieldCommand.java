package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§bProShield §7→ Use /proshield [reload|debug|compass|flag]");
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
                messages.reload();
                sender.sendMessage("§aProShield configuration reloaded.");
            }
            case "debug" -> {
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, "error.no-permission");
                    return true;
                }
                plugin.toggleDebug();
                sender.sendMessage("§eDebug mode: " + (plugin.isDebugEnabled() ? "§aENABLED" : "§cDISABLED"));
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
                compassManager.giveCompass(player, true);
                sender.sendMessage("§aProShield compass given!");
            }
            case "flag" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, "error.player-only");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /proshield flag <flag>");
                    return true;
                }
                String flag = args[1].toLowerCase();
                // TODO: hook into your flag system
                sender.sendMessage("§eToggled flag: §b" + flag);
            }
            default -> sender.sendMessage("§cUnknown subcommand. Use /proshield [reload|debug|compass|flag]");
        }
        return true;
    }
}
