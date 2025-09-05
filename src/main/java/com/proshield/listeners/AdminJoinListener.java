package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class AdminJoinListener implements Listener {

    private final ProShield plugin;

    public AdminJoinListener(ProShield plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Give compass to OPs for menu access, even if they don’t own plots
        if (player.isOp()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.getInventory().contains(Material.COMPASS)) {
                    player.getInventory().addItem(new ItemStack(Material.COMPASS));
                }
            }, 20L); // delay 1 second so join completes
        }
    }
}
