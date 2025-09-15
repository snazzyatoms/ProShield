package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * CompassListener
 * ---------------
 * - Opens GUI when compass is right-clicked
 * - Schedules auto-despawn when ProShield Compass is dropped
 */
public class CompassListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public CompassListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() != Material.COMPASS) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = ChatColor.stripColor(meta.getDisplayName());
        if (displayName == null || !displayName.equalsIgnoreCase("ProShield Compass")) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        guiManager.openMenu(player, "main");
        event.setCancelled(true);
    }

    @EventHandler
    public void onCompassDrop(PlayerDropItemEvent event) {
        Item dropped = event.getItemDrop();
        ItemStack stack = dropped.getItemStack();

        if (stack.getType() != Material.COMPASS) return;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = ChatColor.stripColor(meta.getDisplayName());
        if (displayName == null || !displayName.equalsIgnoreCase("ProShield Compass")) return;

        // Read delay from config (default 600 ticks = 30s)
        int delayTicks = plugin.getConfig().getInt("settings.compass-despawn-delay", 600);

        if (delayTicks > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!dropped.isDead() && dropped.isValid()) {
                    dropped.remove();
                    Player player = event.getPlayer();
                    if (player != null && player.isOnline()) {
                        player.sendMessage(ChatColor.RED + "Your dropped ProShield Compass has despawned.");
                    }
                }
            }, delayTicks);
        }
    }
}
