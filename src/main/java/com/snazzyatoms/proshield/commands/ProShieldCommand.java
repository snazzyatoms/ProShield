package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ProShieldCommand implements CommandExecutor, TabCompleter {

    private final ProShield plugin;

    public ProShieldCommand(ProShield plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // If no args → open GUI main menu
        if (args.length == 0) {
            if (sender instanceof Player player) {
                plugin.getGuiManager().openMain(player);
            } else {
                sender.sendMessage("§cOnly players can use this command.");
            }
            return true;
        }

        // Keep admin/debug/bypass commands for staff
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("proshield.admin")) {
                    sender.sendMessage("§cYou don’t have permission.");
                    return true;
                }
                plugin.reloadConfig();
                plugin.loadMessagesConfig();
                sender.sendMessage("§aProShield configs reloaded.");
            }
            case "debug" -> {
                if (!sender.hasPermission("proshield.admin")) {
                    sender.sendMessage("§cYou don’t have permission.");
                    return true;
                }
                plugin.toggleDebug();
                sender.sendMessage("§eDebug mode: " + (plugin.isDebugEnabled() ? "§aENABLED" : "§cDISABLED"));
            }
            case "bypass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can use this command.");
                    return true;
                }
                if (!player.hasPermission("proshield.admin")) {
                    player.sendMessage("§cYou don’t have permission.");
                    return true;
                }
                if (plugin.isBypassing(player.getUniqueId())) {
                    plugin.getBypassing().remove(player.getUniqueId());
                    player.sendMessage("§cBypass disabled.");
                } else {
                    plugin.getBypassing().add(player.getUniqueId());
                    player.sendMessage("§aBypass enabled.");
                }
            }
            case "admin" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can use this command.");
                    return true;
                }
                if (!player.hasPermission("proshield.admin")) {
                    player.sendMessage("§cYou don’t have permission.");
                    return true;
                }
                plugin.getGuiManager().openAdminTools(player);
            }
            default -> sender.sendMessage("§cUnknown subcommand. Use /proshield admin, reload, debug, or bypass.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("admin", "reload", "debug", "bypass");
        }
        return Collections.emptyList();
    }
}
