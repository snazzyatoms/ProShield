// path: src/main/java/com/snazzyatoms/proshield/plots/PlayerJoinListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerJoinListener implements Listener {

    private final ProShield plugin;

    public PlayerJoinListener(ProShield plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Only give compass if enabled in config
        if (!plugin.getConfig().getBoolean("autogive.compass-on-join", true)) return;

        // Only if they have correct perms
        if (!(player.hasPermission("proshield.compass") || player.hasPermission("proshield.admin"))) {
            return;
        }

        // Avoid duplicates: check if they already have one
        boolean alreadyHas = player.getInventory().containsAtLeast(GUIManager.createAdminCompass(), 1);
        if (alreadyHas) return;

        ItemStack compass = GUIManager.createAdminCompass();

        // Try adding to inventory
        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(compass);

        // If inventory full, drop at player’s feet
        if (!leftovers.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), compass);
            plugin.getLogger().info("Dropped ProShield compass at " + player.getName() + "'s feet (inventory full).");
        }
    }
}
