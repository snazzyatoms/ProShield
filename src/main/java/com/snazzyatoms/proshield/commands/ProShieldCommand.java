package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.GUI.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;

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
    private final GUIManager guiManager;

    public ProShieldCommand(ProShield plugin, PlotManager plotManager, GUIManager guiManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("compass")) {
            if (player.isOp() || player.hasPermission("proshield.compass")) {
                ItemStack compass = new ItemStack(Material.COMPASS, 1);
                ItemMeta meta = compass.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + "ProShield Admin Compass");
                compass.setItemMeta(meta);

                player.getInventory().addItem(compass);
                player.sendMessage(ChatColor.YELLOW + "You have been given the ProShield Compass!");
            } else {
                player.sendMessage(ChatColor.RED + "You don’t have permission to use this!");
            }
            return true;
        }

        player.sendMessage(ChatColor.AQUA + "ProShield Commands: /proshield compass");
        return true;
    }
}
