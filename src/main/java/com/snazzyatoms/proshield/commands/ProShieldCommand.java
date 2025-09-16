package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ProShieldCommand implements CommandExecutor, TabCompleter {

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
            sender.sendMessage("This command is only for players.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            // If no arguments, open the main menu
            if (args.length == 0) {
                guiManager.openMain(player);
                return true;
            }

            // Otherwise, show help
            messages.send(player, "&bProShield Commands:");
            messages.send(player, "&f/ps claim &7- Claim your current chunk.");
            messages.send(player, "&f/ps unclaim &7- Unclaim your chunk.");
            messages.send(player, "&f/ps info &7- Info about your claim.");
            messages.send(player, "&f/ps compass &7- Get a ProShield compass.");
            messages.send(player, "&f/ps admin &7- Open Admin Tools (if admin).");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "claim":
                plotManager.claimPlot(player);
                break;
            case "unclaim":
                plotManager.unclaimPlot(player);
                break;
            case "info":
                plotManager.sendClaimInfo(player);
                break;
            case "compass":
                plugin.getCompassManager().giveCompass(player);
                break;
            case "reload":
                if (player.hasPermission("proshield.admin")) {
                    plugin.reloadConfig();
                    plugin.loadMessagesConfig();
                    messages.send(player, "&aConfigs reloaded.");
                } else {
                    messages.send(player, "&cNo permission.");
                }
                break;
            case "debug":
                if (player.hasPermission("proshield.admin")) {
                    plugin.toggleDebug();
                    messages.send(player, "&eDebug mode: " + (plugin.isDebugEnabled() ? "&aENABLED" : "&cDISABLED"));
                } else {
                    messages.send(player, "&cNo permission.");
                }
                break;
            case "bypass":
                if (player.hasPermission("proshield.admin")) {
                    if (plugin.isBypassing(player.getUniqueId())) {
                        plugin.getBypassing().remove(player.getUniqueId());
                        messages.send(player, "&cBypass disabled.");
                    } else {
                        plugin.getBypassing().add(player.getUniqueId());
                        messages.send(player, "&aBypass enabled.");
                    }
                } else {
                    messages.send(player, "&cNo permission.");
                }
                break;
            case "admin":
                if (player.hasPermission("proshield.admin")) {
                    guiManager.openAdminTools(player);
                } else {
                    messages.send(player, "&cNo permission.");
                }
                break;
            default:
                // Any unknown command → open main GUI
                guiManager.openMain(player);
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.add("claim");
            options.add("unclaim");
            options.add("info");
            options.add("compass");
            if (sender.hasPermission("proshield.admin")) {
                options.add("reload");
                options.add("debug");
                options.add("bypass");
                options.add("admin");
            }
        }
        return options;
    }
}
