package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;  // ✅ updated import

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ProShieldCommand implements CommandExecutor {
    private final ProShield plugin;
    private final PlotManager plotManager;

    public ProShieldCommand(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /proshield <compass|reload>");
            return true;
        }

        if (args[0].equalsIgnoreCase("compass")) {
            if (player.hasPermission("proshield.compass") || player.isOp()) {
                ItemStack compass = new ItemStack(Material.COMPASS);
                ItemMeta meta = compass.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + "ProShield Admin Compass");
                compass.setItemMeta(meta);
                player.getInventory().addItem(compass);
                player.sendMessage(ChatColor.GREEN + "You received the ProShield Admin Compass!");
            } else {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            player.sendMessage(ChatColor.GREEN + "ProShield configuration reloaded.");
            return true;
        }

        return false;
    }
}
