package com.snazzyatoms.proshield.plots;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * FlagsListener
 *
 * ✅ Uses standalone PlotSettings (not inner class)
 * ✅ Toggles claim flags properly and persists them
 */
public class FlagsListener implements Listener {

    private final PlotManager plots;

    public FlagsListener(PlotManager plots) {
        this.plots = plots;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlagMenuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

        ItemStack item = e.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName());
        Location loc = player.getLocation();
        Plot plot = plots.getPlot(loc);
        if (plot == null) return;

        // ✅ Now always references the standalone PlotSettings
        PlotSettings settings = plot.getSettings();

        switch (name.toLowerCase()) {
            case "explosions" -> toggleFlag(player, item, settings.isExplosionsAllowed(), settings::setExplosionsAllowed);
            case "buckets" -> toggleFlag(player, item, settings.isBucketsAllowed(), settings::setBucketsAllowed);
            case "item frames" -> toggleFlag(player, item, settings.isItemFramesAllowed(), settings::setItemFramesAllowed);
            case "armor stands" -> toggleFlag(player, item, settings.isArmorStandsAllowed(), settings::setArmorStandsAllowed);
            case "animals" -> toggleFlag(player, item, settings.isAnimalAccessAllowed(), settings::setAnimalAccessAllowed);
            case "pets" -> toggleFlag(player, item, settings.isPetAccessAllowed(), settings::setPetAccessAllowed);
            case "containers" -> toggleFlag(player, item, settings.isContainersAllowed(), settings::setContainersAllowed);
            case "vehicles" -> toggleFlag(player, item, settings.isVehiclesAllowed(), settings::setVehiclesAllowed);
            case "fire" -> toggleFlag(player, item, settings.isFireAllowed(), settings::setFireAllowed);
            case "fire spread" -> toggleFlag(player, item, settings.isFireSpreadAllowed(), settings::setFireSpreadAllowed); // 🔥 added
            case "redstone" -> toggleFlag(player, item, settings.isRedstoneAllowed(), settings::setRedstoneAllowed);
            case "entity griefing" -> toggleFlag(player, item, settings.isEntityGriefingAllowed(), settings::setEntityGriefingAllowed);
            case "pvp" -> toggleFlag(player, item, settings.isPvpEnabled(), settings::setPvpEnabled);
            case "mob repel" -> toggleFlag(player, item, settings.isMobRepelEnabled(), settings::setMobRepelEnabled);
            case "mob despawn" -> toggleFlag(player, item, settings.isMobDespawnInsideEnabled(), settings::setMobDespawnInsideEnabled);
            case "keep items" -> toggleFlag(player, item, settings.isKeepItemsEnabled(), settings::setKeepItemsEnabled);
            default -> { /* ignore unknown flag */ }
        }

        plots.saveAsync(plot); // persist updated settings
    }

    /* -------------------------------------------------------
     * Helper to toggle a boolean flag and update item lore
     * ------------------------------------------------------- */
    private void toggleFlag(Player player, ItemStack item, boolean current, java.util.function.Consumer<Boolean> setter) {
        boolean newState = !current;
        setter.accept(newState);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(java.util.Collections.singletonList(
                    ChatColor.GRAY + "Now: " + (newState ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED")
            ));
            item.setItemMeta(meta);
        }

        player.sendMessage(ChatColor.YELLOW + "Flag updated: " + ChatColor.AQUA + meta.getDisplayName());
    }
}
