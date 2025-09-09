package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerJoinListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;
    private final GUIManager gui;

    public PlayerJoinListener(ProShield plugin, PlotManager plots, GUIManager gui) {
        this.plugin = plugin;
        this.plots = plots;
        this.gui = gui;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player p = event.getPlayer();

        if (!plugin.getConfig().getBoolean("autogive.compass-on-join", true)) {
            return;
        }

        final boolean isAdmin = p.isOp()
                || p.hasPermission("proshield.admin")
                || p.hasPermission("proshield.admin.gui");

        // Admins get Admin Compass; others need proshield.compass to get Player Compass
        if (isAdmin) {
            if (!hasCompass(p, gui.getAdminCompassName())) {
                giveCompassRespectingConfig(p, gui.createAdminCompass());
            }
        } else if (p.hasPermission("proshield.compass")) {
            if (!hasCompass(p, gui.getPlayerCompassName())) {
                giveCompassRespectingConfig(p, gui.createPlayerCompass());
            }
        }
    }

    private boolean hasCompass(Player p, String displayName) {
        for (ItemStack it : p.getInventory().getContents()) {
            if (it == null || it.getType() != Material.COMPASS) continue;
            ItemMeta meta = it.getItemMeta();
            if (meta != null && meta.hasDisplayName() && displayName.equals(meta.getDisplayName())) {
                return true;
            }
        }
        return false;
    }

    private void giveCompassRespectingConfig(Player p, ItemStack compass) {
        boolean dropIfFull = plugin.getConfig().getBoolean("compass.drop-if-full", true);
        if (p.getInventory().firstEmpty() == -1) {
            if (dropIfFull) {
                p.getWorld().dropItemNaturally(p.getLocation(), compass);
            } else {
                p.sendMessage(plugin.msg("&eYour inventory is full. Use &b/proshield compass &eto get it later."));
            }
        } else {
            p.getInventory().addItem(compass);
        }
    }
}
