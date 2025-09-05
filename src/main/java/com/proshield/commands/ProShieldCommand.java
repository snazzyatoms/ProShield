package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
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
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /proshield <reload|info|compass>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("proshield.reload")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission.");
                    return true;
                }
                plugin.getPlotManager().reloadFromDisk();
                sender.sendMessage(ChatColor.GREEN + "[ProShield] Config reloaded.");
                return true;

            case "info":
                if (!sender.hasPermission("proshield.info")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission.");
                    return true;
                }
                sender.sendMessage(ChatColor.AQUA + "ProShield v" + plugin.getDescription().getVersion());
                sender.sendMessage(ChatColor.GRAY + "Authors: " + String.join(", ", plugin.getDescription().getAuthors()));
                return true;

            case "compass":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Run this in-game.");
                    return true;
                }
                Player p = (Player) sender;
                if (!p.hasPermission("proshield.compass")) {
                    p.sendMessage(ChatColor.RED + "You do not have permission.");
                    return true;
                }
                ItemStack compass = new ItemStack(Material.COMPASS);
                ItemMeta meta = compass.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GOLD + "ProShield Admin Compass");
                    meta.setLore(Arrays.asList(
                            ChatColor.YELLOW + "Right-click to open Claim Manager",
                            ChatColor.GRAY + "Manage your land & protections"
                    ));
                    compass.setItemMeta(meta);
                }
                p.getInventory().addItem(compass);
                p.sendMessage(ChatColor.GREEN + "You received the ProShield Admin Compass.");
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
                return true;
        }
    }
}
