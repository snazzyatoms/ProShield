// src/main/java/com/snazzyatoms/proshield/compass/CompassManager.java
package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class CompassManager implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public CompassManager(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        // Register this as a listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Creates the ProShield compass item.
     */
    private ItemStack createCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bProShield Compass");
            meta.setLore(Collections.singletonList("§7Right-click to open ProShield menu"));
            compass.setItemMeta(meta);
        }
        return compass;
    }

    /**
     * Checks whether the player already has a ProShield compass.
     */
    private boolean hasCompass(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.COMPASS) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName()
                        && "§bProShield Compass".equals(meta.getDisplayName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gives the ProShield compass to a player,
     * avoiding duplicates if they already have one.
     */
    public void giveCompass(Player player) {
        if (!hasCompass(player)) {
            player.getInventory().addItem(createCompass());
        }
    }

    /**
     * Gives the compass to all online players (e.g., on reload).
     */
    public void giveCompassToAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            giveCompass(player);
        }
    }

    /**
     * Handles compass distribution on player join.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        boolean giveOnJoin = plugin.getConfig().getBoolean("settings.give-compass-on-join", true);
        boolean autoReplace = plugin.getConfig().getBoolean("settings.compass-auto-replace", false);

        // If either "give on join" or "auto-replace" is enabled,
        // ensure the player has a compass (without duplicates).
        if (giveOnJoin || autoReplace) {
            giveCompass(player);
        }
    }
}
