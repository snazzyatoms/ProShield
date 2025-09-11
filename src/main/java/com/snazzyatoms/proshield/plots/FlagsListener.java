// src/main/java/com/snazzyatoms/proshield/plots/FlagsListener.java
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
        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView().getTopInventory()) return;
        if (e.getCurrentItem() == null) return;

        String title = ChatColor.stripColor(e.getView().getTitle()).toLowerCase();
        if (!title.contains("flag")) return;

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
                if (player.hasPermission("proshield.admin")) gui.openAdminMain(player);
                else gui.openMain(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                return;
            }
            default -> changed = false;
        }

        if (changed) {
            plots.saveAsync(plot);
            boolean fromAdmin = player.hasPermission("proshield.admin");
            gui.openFlagsMenu(player, fromAdmin);
        }
    }

    private void toggleFlag(Player player, boolean current, Consumer<Boolean> setter) {
        boolean newState = !current;
        setter.accept(newState);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, newState ? 1.2f : 0.8f);

        if (player.hasPermission("proshield.admin")
                && plugin.getConfig().getBoolean("messages.admin-flag-chat", true)) {
            player.sendMessage(ChatColor.YELLOW + "Flag updated: " +
                    (newState ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"));
        }
    }
}
