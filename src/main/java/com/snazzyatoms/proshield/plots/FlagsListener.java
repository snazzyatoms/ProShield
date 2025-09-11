package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

/**
 * FlagsListener
 *
 * ✅ Toggles claim flags when clicked in the Flags GUI.
 * ✅ Prevents icon movement (menu stays static).
 * ✅ Refreshes the GUI after toggling to reflect states.
 * ✅ Back button returns to Admin or Player parent menus.
 * ✅ Sound feedback + optional admin debug chat.
 */
public class FlagsListener implements Listener {

    private final PlotManager plots;
    private final ProShield plugin;
    private final GUIManager gui;

    public FlagsListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
        this.gui = plugin.getGuiManager();
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
        boolean changed = true;

        switch (name) {
            case "explosions" -> toggleFlag(player, settings.isExplosionsAllowed(), settings::setExplosionsAllowed);
            case "buckets" -> toggleFlag(player, settings.isBucketAllowed(), settings::setBucketAllowed);
            case "item frames" -> toggleFlag(player, settings.isItemFramesAllowed(), settings::setItemFramesAllowed);
            case "armor stands" -> toggleFlag(player, settings.isArmorStandsAllowed(), settings::setArmorStandsAllowed);
            case "animals" -> toggleFlag(player, settings.isAnimalAccessAllowed(), settings::setAnimalAccessAllowed);
            case "pets" -> toggleFlag(player, settings.isPetAccessAllowed(), settings::setPetAccessAllowed);
            case "containers" -> toggleFlag(player, settings.isContainersAllowed(), settings::setContainersAllowed);
            case "vehicles" -> toggleFlag(player, settings.isVehiclesAllowed(), settings::setVehiclesAllowed);
            case "fire" -> toggleFlag(player, settings.isFireAllowed(), settings::setFireAllowed);
            case "redstone" -> toggleFlag(player, settings.isRedstoneAllowed(), settings::setRedstoneAllowed);
            case "entity griefing" -> toggleFlag(player, settings.isEntityGriefingAllowed(), settings::setEntityGriefingAllowed);
            case "pvp" -> toggleFlag(player, settings.isPvpEnabled(), settings::setPvpEnabled);
            case "mob repel" -> toggleFlag(player, settings.isMobRepelEnabled(), settings::setMobRepelEnabled);
            case "mob despawn" -> toggleFlag(player, settings.isMobDespawnInsideEnabled(), settings::setMobDespawnInsideEnabled);
            case "keep items" -> toggleFlag(player, settings.isKeepItemsEnabled(), settings::setKeepItemsEnabled);
            case "back" -> {
                boolean fromAdmin = player.hasPermission("proshield.admin");
                if (fromAdmin) gui.openAdminMain(player); else gui.openMain(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                return; // stop here
            }
            default -> changed = false;
        }

        if (changed) {
            plots.saveAsync(plot);
            // ✅ Refresh GUI so states update
            boolean fromAdmin = player.hasPermission("proshield.admin");
            gui.openFlagsMenu(player, fromAdmin);
        }
    }

    /* -------------------------------------------------------
     * Toggle flag helper
     * ------------------------------------------------------- */
    private void toggleFlag(Player player, boolean current, Consumer<Boolean> setter) {
        boolean newState = !current;
        setter.accept(newState);

        // 🔊 Player feedback (sound only)
        player.playSound(player.getLocation(),
                Sound.UI_BUTTON_CLICK,
                1f,
                newState ? 1.2f : 0.8f);

        // 🛠️ Admin debug chat (if enabled in config)
        if (player.hasPermission("proshield.admin")
                && plugin.getConfig().getBoolean("messages.admin-flag-chat", true)) {
            player.sendMessage(ChatColor.YELLOW + "Flag updated: " +
                    (newState ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"));
        }
    }
}
