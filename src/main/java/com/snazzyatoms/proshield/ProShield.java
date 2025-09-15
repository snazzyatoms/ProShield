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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            messages.send(sender, "&7Use &a/proshield help &7for commands.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, "&cYou do not have permission.");
                    return true;
                }
                plugin.reloadProShield();
                messages.send(sender, "&aProShield config reloaded.");
            }

            case "debug" -> {
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, "&cYou do not have permission.");
                    return true;
                }
                plugin.toggleDebug();
                messages.send(sender, "&eDebug mode: " + (plugin.isDebugEnabled() ? "&aON" : "&cOFF"));
            }

            case "bypass" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, "&cOnly players can use this command.");
                    return true;
                }
                if (!player.hasPermission("proshield.admin")) {
                    messages.send(player, "&cYou do not have permission.");
                    return true;
                }
                if (plugin.isBypassing(player.getUniqueId())) {
                    plugin.getBypassing().remove(player.getUniqueId());
                    messages.send(player, "&cBypass disabled.");
                } else {
                    plugin.getBypassing().add(player.getUniqueId());
                    messages.send(player, "&aBypass enabled.");
                }
            }

            default -> messages.send(sender, "&7Use &a/proshield help &7for commands.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("proshield.admin")) {
                options.add("reload");
                options.add("debug");
                options.add("bypass");
            }
        }
        return options;
    }
}
