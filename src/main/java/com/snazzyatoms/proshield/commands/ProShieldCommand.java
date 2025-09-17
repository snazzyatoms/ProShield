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

        // Default: open GUI for player
        if (args.length == 0) {
            if (sender instanceof Player player) {
                if (player.hasPermission("ProShield.access")) {
                    plugin.getGuiManager().openMain(player);
                } else {
                    player.sendMessage("§cYou do not have permission to use ProShield.");
                }
            } else {
                sender.sendMessage("§cOnly players can use this command.");
            }
            return true;
        }

        // Admin-only commands
        String sub = args[0].toLowerCase();
        if (!sender.hasPermission("proshield.admin")) {
            sender.sendMessage("§cYou don’t have permission to use admin commands.");
            return true;
        }

        switch (sub) {
            case "reload" -> {
                plugin.reloadConfig();
                plugin.loadMessagesConfig();
                sender.sendMessage("§aProShield configs reloaded.");
            }

            case "debug" -> {
                plugin.toggleDebug();
                sender.sendMessage("§eDebug mode: " + (plugin.isDebugEnabled() ? "§aENABLED" : "§cDISABLED"));
            }

            case "bypass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can use this command.");
                    return true;
                }
                UUID uuid = player.getUniqueId();
                boolean enabled = plugin.getBypassing().contains(uuid);
                if (enabled) {
                    plugin.getBypassing().remove(uuid);
                    player.sendMessage("§cBypass disabled.");
                } else {
                    plugin.getBypassing().add(uuid);
                    player.sendMessage("§aBypass enabled.");
                }
            }

            case "admin" -> {
                if (sender instanceof Player player) {
                    plugin.getGuiManager().openAdminTools(player);
                } else {
                    sender.sendMessage("§cOnly players can use this command.");
                }
            }

            default -> {
                sender.sendMessage("§cUnknown subcommand.");
                sender.sendMessage("§7Try: §f/proshield admin, reload, debug, bypass");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("proshield.admin")) {
            return List.of("admin", "reload", "debug", "bypass");
        }
        return Collections.emptyList();
    }
}
