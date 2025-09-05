package com.snazzyatoms.proshield.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.snazzyatoms.proshield.ProShield;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;

    public ProShieldCommand(ProShield plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "ProShield Commands:");
            sender.sendMessage(ChatColor.AQUA + "/proshield reload" + ChatColor.WHITE + " - Reloads the config");
            sender.sendMessage(ChatColor.AQUA + "/proshield info" + ChatColor.WHITE + " - Shows plugin info");
            sender.sendMessage(ChatColor.AQUA + "/proshield compass" + ChatColor.WHITE + " - Gives Admin Compass tool");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload":
                if (!sender.hasPermission("proshield.reload")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "ProShield configuration reloaded.");
                return true;

            case "info":
                if (!sender.hasPermission("proshield.info")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "ProShield v" + plugin.getDescription().getVersion());
                sender.sendMessage(ChatColor.GRAY + "Author: " + String.join(", ", plugin.getDescription().getAuthors()));
                sender.sendMessage(ChatColor.GRAY + "Website: " + plugin.getDescription().getWebsite());
                return true;

            case "compass":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }

                Player player = (Player) sender;
                if (!player.isOp() && !player.hasPermission("proshield.compass")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }

                ItemStack compass = new ItemStack(Material.COMPASS, 1);
                ItemMeta meta = compass.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GOLD + "Admin Compass");
                    compass.setItemMeta(meta);
                }

                player.getInventory().addItem(compass);
                player.sendMessage(ChatColor.GREEN + "You received the Admin Compass!");
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /proshield for help.");
                return true;
        }
    }
}
