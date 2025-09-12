package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProShieldCommand implements CommandExecutor, TabCompleter {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final GUIManager guiManager;
    private final CompassManager compassManager;

    public ProShieldCommand(ProShield plugin, PlotManager plotManager, GUIManager guiManager, CompassManager compassManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.guiManager = guiManager;
        this.compassManager = compassManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // existing logic (reload, debug, compass, help, etc.)
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("help");

            if (sender.hasPermission("proshield.admin.reload")) {
                suggestions.add("reload");
            }
            if (sender.hasPermission("proshield.admin")) {
                suggestions.add("debug");
                suggestions.add("compass");
                suggestions.add("flags");
            }
        }

        // filter partial matches
        String current = args[args.length - 1].toLowerCase(Locale.ROOT);
        suggestions.removeIf(s -> !s.toLowerCase(Locale.ROOT).startsWith(current));

        return suggestions;
    }
}
