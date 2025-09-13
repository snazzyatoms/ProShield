// src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
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
    private final GUIManager guiManager;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public ProShieldCommand(ProShield plugin, GUIManager guiManager, PlotManager plotManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.plotManager = plotManager;
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
                    plugin.setDebugEnabled(!plugin.isDebugEnabled());
                    messages.send(player, "&eDebug mode: " + plugin.isDebugEnabled());
                }
            }
            case "bypass" -> {
                if (player.isOp()) {
                    if (plugin.getBypassing().contains(player.getUniqueId())) {
                        plugin.getBypassing().remove(player.getUniqueId());
                        messages.send(player, "&cBypass disabled.");
                    } else {
                        plugin.getBypassing().add(player.getUniqueId());
                        messages.send(player, "&aBypass enabled.");
                    }
                }
            }
            case "compass" -> {
                plugin.getCompassManager().giveCompass(player);
            }
            default -> {
                messages.send(player, "&cUnknown subcommand.");
            }
        }
        return true;
    }
}
