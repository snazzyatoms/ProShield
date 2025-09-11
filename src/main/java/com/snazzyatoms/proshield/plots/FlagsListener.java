package com.snazzyatoms.proshield.plots;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.function.Consumer;

/**
 * Handles claim flag GUI toggles.
 *
 * ✅ Preserves flag toggling logic
 * ✅ Matches PlotSettings flags
 * ✅ Saves async after update
 */
public class FlagsListener implements Listener {

    private final PlotManager plotManager;

    public FlagsListener(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlagMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName());
        Location loc = player.getLocation();
        Plot plot = plotManager.getPlot(loc);
        if (plot == null) return;

        PlotSettings settings = plot.getSettings();

        switch (name.toLowerCase()) {
            case "explosions"    -> toggleFlag(player, item, settings.isExplosionsAllowed(), settings::setExplosionsAllowed);
            case "buckets"       -> toggleFlag(player, item, settings.isBucketsAllowed(), settings::setBucketsAllowed);
            case "item frames"   -> toggleFlag(player, item, settings.isItemFramesAllowed(), settings::setItemFramesAllowed);
            case "armor stands"  -> toggleFlag(player, item, settings.isArmorStandsAllowed(), settings::setArmorStandsAllowed);
            case "animals"       -> toggleFlag(player, item, settings.isAnimalAccessAllowed(), settings::setAnimalAccessAllowed);
            case "pets"          -> toggleFlag(player, item, settings.isPetAccessAllowed(), settings::setPetAccessAllowed);
            case "containers"    -> toggleFlag(player, item, settings.isContainersAllowed(), settings::setContainersAllowed);
            case "vehicles"      -> toggleFlag(player, item, settings.isVehiclesAllowed(), settings::setVehiclesAllowed);
            case "fire"          -> toggleFlag(player, item, settings.isFireAllowed(), settings::setFireAllowed);
            case "redstone"      -> toggleFlag(player, item, settings.isRedstoneAllowed(), settings::setRedstoneAllowed);
            case "entity griefing" -> toggleFlag(player, item, settings.isEntityGriefingAllowed(), settings::setEntityGriefingAllowed);
            case "pvp"           -> toggleFlag(player, item, settings.isPvpEnabled(), settings::setPvpEnabled);
            case "mob repel"     -> toggleFlag(player, item, settings.isMobRepelEnabled(), settings::setMobRepelEnabled);
            case "mob despawn"   -> toggleFlag(player, item, settings.isMobDespawnInsideEnabled(), settings::setMobDespawnInsideEnabled);
            case "keep items"    -> toggleFlag(player, item, settings.isKeepItemsEnabled(), settings::setKeepItemsEnabled);
            default -> { /* ignore unknown flag */ }
        }

        plotManager.saveAsync(plot); // persist changes
    }

    private void toggleFlag(Player player, ItemStack item, boolean current, Consumer<Boolean> setter) {
        boolean newState = !current;
        setter.accept(newState);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(Collections.singletonList(
                    ChatColor.GRAY + "Now: " + (newState ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED")
            ));
            item.setItemMeta(meta);
            player.sendMessage(ChatColor.YELLOW + "Flag updated: " + ChatColor.AQUA + meta.getDisplayName());
        }
    }
}
