package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdminJoinListener implements Listener {

    private final ProShield plugin;

    public AdminJoinListener(ProShield plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        FileConfiguration cfg = plugin.getConfig();

        if (!cfg.getBoolean("settings.give-admin-compass-on-join", true)) return;
        if (!p.hasPermission("proshield.compass")) return;

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                cfg.getString("settings.compass-name", "&aProShield Admin Compass")));
        compass.setItemMeta(meta);

        if (!p.getInventory().contains(compass)) {
            p.getInventory().addItem(compass);
            p.sendMessage(ChatColor.GREEN + "Admin compass granted.");
        }
    }
}
