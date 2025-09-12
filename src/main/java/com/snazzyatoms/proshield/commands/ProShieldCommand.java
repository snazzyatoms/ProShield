package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProShieldCommand implements CommandExecutor, TabCompleter {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final GUIManager guiManager;
    private final CompassManager compassManager;

    public ProShieldCommand(ProShield plugin, PlotManager plotManager,
                            GUIManager guiManager, CompassManager compassManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.guiManager = guiManager;
        this.compassManager = compassManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§bProShield §7v" + plugin.getDescription().getVersion());
            sender.sendMessage("§7Use §e/proshield help §7for commands.");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> {
                if (!sender.hasPermission("proshield.admin.reload")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }
                plugin.reloadConfig();
                plugin.getMessagesUtil().reload();
                sender.sendMessage("§aProShield configuration reloaded.");
            }
            case "debug" -> {
                plugin.toggleDebug();
                sender.sendMessage("§eDebug mode: " + (plugin.isDebugEnabled() ? "§aON" : "§cOFF"));
            }
            case "compass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cPlayers only.");
                    return true;
                }
                compassManager.giveCompass(player, player.isOp());
                sender.sendMessage("§aCompass given.");
            }
            case "flags" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cPlayers only.");
                    return true;
                }
                guiManager.openMenu(player, "flags");
            }
            default -> {
                sender.sendMessage("§cUnknown subcommand. Use §e/proshield help");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = Arrays.asList("reload", "debug", "compass", "flags");
            String current = args[0].toLowerCase();
            List<String> matches = new ArrayList<>();
            for (String opt : options) {
                if (opt.startsWith(current)) matches.add(opt);
            }
            return matches;
        }
        return List.of();
    }
}
