package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.function.Consumer;

/**
 * FlagsListener
 *
 * ✅ Toggles claim flags when clicked in the Flags GUI.
 * ✅ Prevents icon movement (menu stays static).
 * ✅ Sound feedback (players hear click / toggle sounds).
 * ✅ Admins optionally get a debug chat message (config-controlled).
 */
public class FlagsListener implements Listener {

    private final PlotManager plots;
    private final ProShield plugin;

    public FlagsListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlagMenuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

        // ✅ Prevent item movement
        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();
        Location loc = player.getLocation();
        Plot plot = plots.getPlot(loc);
        if (plot == null) return;

        PlotSettings settings = plot.getSettings();

        switch (name) {
            case "explosions" -> toggleFlag(player, item, settings.isExplosionsAllowed(), settings::setExplosionsAllowed);
            case "buckets" -> toggleFlag(player, item, settings.isBucketAllowed(), settings::setBucketAllowed);
            case "item frames" -> toggleFlag(player, item, settings.isItemFramesAllowed(), settings::setItemFramesAllowed);
            case "armor stands" -> toggleFlag(player, item, settings.isArmorStandsAllowed(), settings::setArmorStandsAllowed);
            case "animals" -> toggleFlag(player, item, settings.isAnimalAccessAllowed(), settings::setAnimalAccessAllowed);
            case "pets" -> toggleFlag(player, item, settings.isPetAccessAllowed(), settings::setPetAccessAllowed);
            case "containers" -> toggleFlag(player, item, settings.isContainersAllowed(), settings::setContainersAllowed);
            case "vehicles" -> toggleFlag(player, item, settings.isVehiclesAllowed(), settings::setVehiclesAllowed);
            case "fire" -> toggleFlag(player, item, settings.isFireAllowed(), settings::setFireAllowed);
            case "redstone" -> toggleFlag(player, item, settings.isRedstoneAllowed(), settings::setRedstoneAllowed);
            case "entity griefing" -> toggleFlag(player, item, settings.isEntityGriefingAllowed(), settings::setEntityGriefingAllowed);
            case "pvp" -> toggleFlag(player, item, settings.isPvpEnabled(), settings::setPvpEnabled);
            case "mob repel" -> toggleFlag(player, item, settings.isMobRepelEnabled(), settings::setMobRepelEnabled);
            case "mob despawn" -> toggleFlag(player, item, settings.isMobDespawnInsideEnabled(), settings::setMobDespawnInsideEnabled);
            case "keep items" -> toggleFlag(player, item, settings.isKeepItemsEnabled(), settings::setKeepItemsEnabled);
            case "back" -> {
                // handled by PlayerMenuListener/AdminMenuListener instead
                return;
            }
            default -> { return; }
        }

        // ✅ Save asynchronously
        plots.saveAsync(plot);
    }

    /* -------------------------------------------------------
     * Toggle flag helper
     * ------------------------------------------------------- */
    private void toggleFlag(Player player, ItemStack item, boolean current, Consumer<Boolean> setter) {
        boolean newState = !current;
        setter.accept(newState);

        // Update lore to reflect new state
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(Collections.singletonList(
                    ChatColor.GRAY + "Now: " + (newState ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED")
            ));
            item.setItemMeta(meta);
        }

        // 🔊 Player feedback (sound only)
        player.playSound(player.getLocation(),
                Sound.UI_BUTTON_CLICK,
                1f,
                newState ? 1.2f : 0.8f);

        // 🛠️ Admin debug chat (if enabled in config)
        if (player.hasPermission("proshield.admin")
                && plugin.getConfig().getBoolean("messages.admin-flag-chat", true)) {
            player.sendMessage(ChatColor.YELLOW + "Flag updated: " +
                    ChatColor.AQUA + meta.getDisplayName());
        }
    }
}
