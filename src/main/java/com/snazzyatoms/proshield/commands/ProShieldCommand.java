// src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ProShieldCommand implements CommandExecutor, TabCompleter {

    private final ProShield plugin;
    private final CompassManager compassManager;

    public ProShieldCommand(ProShield plugin, CompassManager compassManager) {
        this.plugin = plugin;
        this.compassManager = compassManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Default behavior: show info
            sender.sendMessage(ChatColor.YELLOW + "ProShield v" + plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.GRAY + "Usage: /" + label + " <reload|debug|bypass>");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> {
                if (!sender.hasPermission("proshield.admin.reload")) {
                    sender.sendMessage(ChatColor.RED + "You don’t have permission to reload ProShield.");
                    return true;
                }
                long start = System.currentTimeMillis();
                plugin.reloadAll(); // ✅ reload config + language manager + messages
                long took = System.currentTimeMillis() - start;
                sender.sendMessage(ChatColor.GREEN + "[ProShield] Reloaded configuration & languages in " + took + "ms.");
            }

            case "debug" -> {
                if (!sender.hasPermission("proshield.admin.debug")) {
                    sender.sendMessage(ChatColor.RED + "You don’t have permission to toggle debug.");
                    return true;
                }
                plugin.toggleDebug();
                sender.sendMessage(ChatColor.AQUA + "Debug mode: " +
                        (plugin.isDebugEnabled() ? "enabled" : "disabled"));
            }

            case "bypass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use bypass.");
                    return true;
                }
                if (!player.hasPermission("proshield.admin.bypass")) {
                    player.sendMessage(ChatColor.RED + "You don’t have permission to bypass claims.");
                    return true;
                }
                boolean nowBypassing = plugin.toggleBypass(player.getUniqueId());
                player.sendMessage(ChatColor.YELLOW + "Bypass " + (nowBypassing ? "enabled" : "disabled") + ".");
            }

            default -> sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /" + label + " <reload|debug|bypass>");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("proshield.admin.reload")) list.add("reload");
            if (sender.hasPermission("proshield.admin.debug")) list.add("debug");
            if (sender.hasPermission("proshield.admin.bypass")) list.add("bypass");
        }
        return list;
    }
}
