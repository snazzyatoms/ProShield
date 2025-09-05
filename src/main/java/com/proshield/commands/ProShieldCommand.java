package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.managers.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class ProShieldCommand implements CommandExecutor {

    private final GUIManager guiManager;

    public ProShieldCommand(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GREEN + "⚔ ProShield v" + Bukkit.getPluginManager().getPlugin("ProShield").getDescription().getVersion());
            sender.sendMessage(ChatColor.YELLOW + "Available commands:");
            sender.sendMessage(ChatColor.AQUA + "/proshield compass" + ChatColor.GRAY + " - Get the admin claim compass.");
            sender.sendMessage(ChatColor.AQUA + "/proshield reload" + ChatColor.GRAY + " - Reload configuration.");
            return true;
        }

        if (args[0].equalsIgnoreCase("compass")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("proshield.compass")) {
                sender.sendMessage(ChatColor.RED + "You don’t have permission to use this command.");
                return true;
            }

            // ✅ Create the ProShield Compass
            ItemStack compass = new ItemStack(Material.COMPASS, 1);
            ItemMeta meta = compass.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + "ProShield Admin Compass");
                meta.setLore(Collections.singletonList(ChatColor.GRAY + "Right-click to open Claim Manager"));
                compass.setItemMeta(meta);
            }

            player.getInventory().addItem(compass);
            player.sendMessage(ChatColor.GREEN + "✅ You have received the ProShield Admin Compass!");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("proshield.reload")) {
                sender.sendMessage(ChatColor.RED + "You don’t have permission to reload ProShield.");
                return true;
            }

            Bukkit.getPluginManager().getPlugin("ProShield").reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "✅ ProShield configuration reloaded.");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /proshield for help.");
        return true;
    }
}
