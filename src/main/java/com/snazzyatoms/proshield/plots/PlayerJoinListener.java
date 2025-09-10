package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerJoinListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;

    public PlayerJoinListener(ProShield plugin, GUIManager gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        boolean giveOnJoin = plugin.getConfig().getBoolean("autogive.compass-on-join", true);
        if (!giveOnJoin) return;

        // Check if player already has any ProShield compass
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && gui.isProShieldCompass(item)) {
                return; // Already has compass
            }
        }

        // Admins get Admin Compass, players get Player Compass
        if (player.isOp() || player.hasPermission("proshield.admin")) {
            gui.giveCompass(player, true);
            player.sendMessage(ChatColor.AQUA + "[ProShield] " + ChatColor.GREEN + "You have received the Admin ProShield Compass.");
        } else if (player.hasPermission("proshield.use") || player.hasPermission("proshield.compass")) {
            gui.giveCompass(player, false);
            player.sendMessage(ChatColor.AQUA + "[ProShield] " + ChatColor.GREEN + "You have received the ProShield Compass.");
        } else {
            // No permissions = no compass
            if (plugin.getConfig().getBoolean("proshield.debug", false)) {
                Bukkit.getLogger().info("[ProShield] Skipped giving compass to " + player.getName() + " (no permission).");
            }
        }
    }
}
