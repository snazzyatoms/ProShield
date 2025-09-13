package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final GUIManager guiManager;
    private final MessagesUtil messages;

    public ProShieldCommand(ProShield plugin, PlotManager plotManager, GUIManager guiManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.guiManager = guiManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Players only.");
            return true;
        }

        if (args.length == 0) {
            guiManager.openMenu(player, "main");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "help" -> {
                for (String line : plugin.getConfig().getStringList("help.player")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
                }
            }
            case "reload" -> {
                if (player.isOp() || player.hasPermission("proshield.admin.reload")) {
                    plugin.reloadConfig();
                    messages.send(player, "&aProShield config reloaded.");
                } else {
                    messages.send(player, "&cNo permission.");
                }
            }
            case "debug" -> {
                if (player.isOp()) {
                    plugin.toggleDebug();
                    messages.send(player, "&eDebug mode: " + plugin.isDebugEnabled());
                }
            }
            case "bypass" -> {
                // Placeholder: bypass handling will be reintroduced in v2.0
                messages.send(player, "&cBypass mode is not available in this version.");
            }
            case "compass" -> {
                // Placeholder: compass is now GUI-driven
                messages.send(player, "&eUse the ProShield Compass item to open the menu.");
            }
            default -> {
                messages.send(player, "&cUnknown subcommand.");
            }
        }
        return true;
    }
}
