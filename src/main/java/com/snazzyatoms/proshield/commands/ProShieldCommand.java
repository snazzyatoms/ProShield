package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.GUI.GUIManager;
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

    public ProShieldCommand(ProShield plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "ProShield v" + plugin.getDescription().getVersion() +
                    " - /" + label + " <reload|info|compass>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("proshield.reload")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "ProShield config reloaded.");
                return true;
            }

            case "info" -> {
                if (!sender.hasPermission("proshield.info")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                sender.sendMessage(ChatColor.AQUA + "ProShield v" + plugin.getDescription().getVersion());
                sender.sendMessage(ChatColor.GRAY + "Author: " + String.join(", ", plugin.getDescription().getAuthors()));
                return true;
            }

            case "compass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Players only.");
                    return true;
                }
                if (!player.hasPermission("proshield.compass")) {
                    player.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                giveCompass(player, plugin.getConfig().getString("settings.compass-name", "&aProShield Admin Compass"));
                player.sendMessage(ChatColor.GREEN + "Compass given. Right-click to open Claim Management.");
                return true;
            }
        }
        sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /" + label + " <reload|info|compass>");
        return true;
    }

    private void giveCompass(Player player, String displayName) {
        ItemStack compass = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        compass.setItemMeta(meta);
        player.getInventory().addItem(compass);
    }
}
