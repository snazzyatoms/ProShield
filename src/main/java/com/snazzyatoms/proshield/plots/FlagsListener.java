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

import java.util.Arrays;
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
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;

        String title = ChatColor.stripColor(e.getView().getTitle()).toLowerCase();
        if (!title.contains("flags")) return; // ensures only in Flags menu

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
            case "explosions" -> toggleFlag(player, item, "Toggle TNT & creeper damage", settings.isExplosionsAllowed(), settings::setExplosionsAllowed);
            case "buckets" -> toggleFlag(player, item, "Control bucket usage", settings.isBucketAllowed(), settings::setBucketAllowed);
            case "item frames" -> toggleFlag(player, item, "Protect item frames", settings.isItemFramesAllowed(), settings::setItemFramesAllowed);
            case "armor stands" -> toggleFlag(player, item, "Protect armor stands", settings.isArmorStandsAllowed(), settings::setArmorStandsAllowed);
            case "animals" -> toggleFlag(player, item, "Allow/deny animal access", settings.isAnimalAccessAllowed(), settings::setAnimalAccessAllowed);
            case "pets" -> toggleFlag(player, item, "Protect tamed pets", settings.isPetAccessAllowed(), settings::setPetAccessAllowed);
            case "containers" -> toggleFlag(player, item, "Protect chests/furnaces/etc", settings.isContainersAllowed(), settings::setContainersAllowed);
            case "vehicles" -> toggleFlag(player, item, "Protect boats & minecarts", settings.isVehiclesAllowed(), settings::setVehiclesAllowed);
            case "fire" -> toggleFlag(player, item, "Enable or block fire spread", settings.isFireAllowed(), settings::setFireAllowed);
            case "redstone" -> toggleFlag(player, item, "Toggle redstone use", settings.isRedstoneAllowed(), settings::setRedstoneAllowed);
            case "entity griefing" -> toggleFlag(player, item, "Prevent mob griefing", settings.isEntityGriefingAllowed(), settings::setEntityGriefingAllowed);
            case "pvp" -> toggleFlag(player, item, "Enable or disable PvP", settings.isPvpEnabled(), settings::setPvpEnabled);
            case "mob repel" -> toggleFlag(player, item, "Push mobs away", settings.isMobRepelEnabled(), settings::setMobRepelEnabled);
            case "mob despawn" -> toggleFlag(player, item, "Auto-despawn mobs inside", settings.isMobDespawnInsideEnabled(), settings::setMobDespawnInsideEnabled);
            case "keep items" -> toggleFlag(player, item, "Keep items on death", settings.isKeepItemsEnabled(), settings::setKeepItemsEnabled);
            case "back" -> {
                if (player.hasPermission("proshield.admin")) gui.openAdminMain(player);
                else gui.openMain(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                return;
            }
            default -> changed = false;
        }

        if (changed) plots.saveAsync(plot);
    }

    /* -------------------------------------------------------
     * Toggle flag + instantly update lore
     * ------------------------------------------------------- */
    private void toggleFlag(Player player, ItemStack item, String description, boolean current, Consumer<Boolean> setter) {
        boolean newState = !current;
        setter.accept(newState);

        // update lore on clicked item
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + description,
                    newState ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"
            ));
            item.setItemMeta(meta);
        }

        // sound feedback
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, newState ? 1.2f : 0.8f);

        // optional admin debug
        if (player.hasPermission("proshield.admin")
                && plugin.getConfig().getBoolean("messages.admin-flag-chat", true)) {
            player.sendMessage(ChatColor.YELLOW + "Flag updated: " +
                    (newState ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"));
        }
    }
}
