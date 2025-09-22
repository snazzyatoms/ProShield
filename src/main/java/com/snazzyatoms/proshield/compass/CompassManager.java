package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Handles creating and giving the ProShield Compass.
 * Reads display-name + lore from messages.yml.
 */
public class CompassManager implements Listener {

    private final ProShield plugin;
    private final MessagesUtil messages;

    public CompassManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /** Create a correctly tagged ProShield Compass from messages.yml */
    public ItemStack createCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            String name = messages.getOrDefault("messages.compass.display-name", "&bProShield Compass");
            List<String> lore = messages.getList("messages.compass.lore");
            meta.setDisplayName(name);
            if (!lore.isEmpty()) meta.setLore(lore);
            compass.setItemMeta(meta);
        }
        return compass;
    }

    /** Check if a player already has the official compass */
    public boolean hasCompass(Player player) {
        if (player == null) return false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != Material.COMPASS) continue;
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String dn = messages.color(messages.getOrDefault("messages.compass.display-name", "&bProShield Compass"));
                if (dn.equals(meta.getDisplayName())) return true;
            }
        }
        return false;
    }

    /** Give the compass if player doesn’t already have one */
    public void giveCompass(Player player) {
        if (player == null) return;
        if (!hasCompass(player)) {
            player.getInventory().addItem(createCompass());
        }
    }

    /** Force-replace compass (auto-replace cases) */
    public void replaceCompass(Player player) {
        if (player == null) return;
        giveCompass(player);
    }

    /** Give compass on join if enabled */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        boolean giveOnJoin = plugin.getConfig().getBoolean("settings.give-compass-on-join", true);
        if (giveOnJoin) {
            giveCompass(event.getPlayer());
        }
    }
}
