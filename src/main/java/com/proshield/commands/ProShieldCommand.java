package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.managers.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;

    public ProShieldCommand(ProShield plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "[ProShield] " + ChatColor.GRAY +
                    "Use /proshield <reload|info|compass>");
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {

            case "reload":
                if (!sender.hasPermission("proshield.reload")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "[ProShield] Configuration reloaded.");
                return true;

            case "info":
                if (!sender.hasPermission("proshield.info")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                sender.sendMessage(ChatColor.AQUA + "ProShield v" + plugin.getDescription().getVersion());
                sender.sendMessage(ChatColor.GRAY + "Author: " + String.join(", ", plugin.getDescription().getAuthors()));
                return true;

            case "compass":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used in-game.");
                    return true;
                }

                Player player = (Player) sender;
                if (!player.hasPermission("proshield.compass")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }

                // Create special Admin Compass
                ItemStack compass = new ItemStack(Material.COMPASS, 1);
                ItemMeta meta = compass.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GOLD + "ProShield Admin Compass");
                    meta.setLore(Arrays.asList(
                            ChatColor.YELLOW + "Right-click to open Claim Manager",
                            ChatColor.GRAY + "Manage your land and protections easily"
                    ));
                    compass.setItemMeta(meta);
                }

                // Give to player
                player.getInventory().addItem(compass);
                player.sendMessage(ChatColor.GREEN + "[ProShield] You have received the Admin Compass.");
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use: /proshield <reload|info|compass>");
                return true;
        }
    }
}
