// src/main/java/com/snazzyatoms/proshield/compass/CompassManager.java
package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

/**
 * CompassManager (ProShield v1.2.6 FINAL + Synced)
 *
 * - Handles giving and managing the ProShield Compass for players
 * - Detects right-clicks to open GUI
 * - Registers itself as a listener automatically
 */
public class CompassManager implements Listener {

    private final ProShield plugin;
    public static final String COMPASS_NAME = "§bProShield Compass"; // 🔑 Single source of truth

    public CompassManager(ProShield plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Creates a correctly tagged ProShield Compass.
     */
    private ItemStack createCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(COMPASS_NAME);
            meta.setLore(Collections.singletonList("§7Right-click to open the ProShield menu"));
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
                    && COMPASS_NAME.equals(meta.getDisplayName())) {
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

    /**
     * Handles right-clicking the ProShield Compass to open GUI.
     */
    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.COMPASS) return;

        ItemMeta meta = event.getItem().getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        if (!COMPASS_NAME.equals(meta.getDisplayName())) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        event.setCancelled(true); // prevent compass pointing behavior

        Player player = event.getPlayer();
        if (player.hasPermission("proshield.player.access")) {
            plugin.getGuiManager().openMainMenu(player);
        } else {
            player.sendMessage(plugin.getMessagesUtil()
                .getOrDefault("messages.error.no-permission", "§cYou do not have permission to use the ProShield Compass."));
        }
    }
}
