package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getConfig().getBoolean("autogive.compass-on-join", true)) return;

        boolean admin = p.isOp() || p.hasPermission("proshield.admin.gui");
        // ensure exactly one compass if missing
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            boolean has = p.getInventory().containsAtLeast(gui.createPlayerCompass(), 1)
                       || p.getInventory().containsAtLeast(gui.createAdminCompass(), 1);
            if (!has) gui.giveCompass(p, admin);
        }, 10L);
    }
}
