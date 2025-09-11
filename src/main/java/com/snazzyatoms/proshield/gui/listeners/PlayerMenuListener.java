package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerMenuListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;
    private final GUICache cache;
    private final PlotManager plots;
    private final MessagesUtil messages;

    public PlayerMenuListener(ProShield plugin, GUIManager gui, GUICache cache, PlotManager plots) {
        this.plugin = plugin;
        this.gui = gui;
        this.cache = cache;
        this.plots = plots;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

        String menu = cache.getOpenMenu(player.getUniqueId());
        if (menu == null || !menu.equals("player")) return;

        e.setCancelled(true); // prevent taking items
        ItemStack clicked = e.getCurrentItem();
        Material mat = clicked.getType();

        switch (mat) {
            case GRASS_BLOCK -> {
                Bukkit.dispatchCommand(player, "claim");
                player.closeInventory();
            }
            case BARRIER -> {
                Bukkit.dispatchCommand(player, "unclaim");
                player.closeInventory();
            }
            case PAPER -> {
                Bukkit.dispatchCommand(player, "info");
                player.closeInventory();
            }
            case PLAYER_HEAD -> {
                Bukkit.dispatchCommand(player, "trust");
                player.closeInventory();
            }
            case REDSTONE -> {
                Bukkit.dispatchCommand(player, "roles");
                player.closeInventory();
            }
            case LEVER -> {
                Plot plot = plots.getPlot(player.getLocation());
                if (plot != null) {
                    gui.openFlagsMenu(player, plot);
                } else {
                    messages.send(player, "error.not-in-claim");
                }
            }
            case COMPASS -> {
                Bukkit.dispatchCommand(player, "preview");
                player.closeInventory();
            }
            case CHEST -> {
                Bukkit.dispatchCommand(player, "transfer");
                player.closeInventory();
            }
            default -> { /* do nothing */ }
        }
    }
}
