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

/**
 * Handles giving and managing the ProShield Compass for players.
 * Fully synchronized with CompassListener + GUIManager (v1.2.5).
 */
public class CompassManager implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public CompassManager(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Creates a correctly tagged ProShield Compass.
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
     * Checks if the player already has the official ProShield Compass.
     */
    public boolean hasCompass(Player player) {
        if (player == null) return false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != Material.COMPASS) continue;
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()
                    && "§bProShield Compass".equals(meta.getDisplayName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gives the ProShield Compass if the player doesn’t already have one.
     */
    public void giveCompass(Player player) {
        if (player == null) return;
        if (!hasCompass(player)) {
            player.getInventory().addItem(createCompass());
        }
    }

    /**
     * Force-replace a ProShield Compass (for despawn/auto-replace cases).
     */
    public void replaceCompass(Player player) {
        if (player == null) return;
        giveCompass(player);
    }

    /**
     * Distributes compasses to all players if config allows.
     */
    public void giveCompassToAll() {
        boolean giveOnJoin = plugin.getConfig().getBoolean("settings.give-compass-on-join", true);
        boolean autoReplace = plugin.getConfig().getBoolean("settings.compass-auto-replace", false);

        if (giveOnJoin || autoReplace) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                giveCompass(player);
            }
        }
    }

    /**
     * Handles compass distribution when players join.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        boolean giveOnJoin = plugin.getConfig().getBoolean("settings.give-compass-on-join", true);
        boolean autoReplace = plugin.getConfig().getBoolean("settings.compass-auto-replace", false);

        if (giveOnJoin || autoReplace) {
            giveCompass(event.getPlayer());
        }
    }
}
