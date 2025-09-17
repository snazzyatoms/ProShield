package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PlayerCommandDispatcher implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    private static final String COMPASS_NAME = "&6ProShield Compass";

    public PlayerCommandDispatcher(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.messages = plugin.getMessagesUtil();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCompassClick(PlayerInteractEvent event) {
        // Only listen for main hand interactions
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = meta.getDisplayName();
        if (!displayName.contains("ProShield")) return;

        // Cancel default compass behavior
        event.setCancelled(true);

        // Open main GUI
        plugin.getGuiManager().openMain(player);
    }

    public void sendClaimInfo(Player player) {
        Plot plot = plotManager.getPlot(player.getLocation());

        if (plot == null) {
            messages.send(player, "&7You are in the wilderness.");
            return;
        }

        String ownerName = Bukkit.getOfflinePlayer(plot.getOwner()).getName();
        if (ownerName == null) ownerName = plot.getOwner().toString();

        messages.send(player, "&8&m--------------------------------------------------");
        messages.send(player, "&a&lClaim Info:");
        messages.send(player, "&7World: &f" + plot.getWorld());
        messages.send(player, "&7Chunk: &f" + plot.getX() + ", " + plot.getZ());
        messages.send(player, "&7Owner: &f" + ownerName);
        messages.send(player, "&7Radius: &f" + plot.getRadius() + " blocks");
        messages.send(player, "&7Flags:");
        for (Map.Entry<String, Boolean> entry : plot.getFlags().entrySet()) {
            messages.send(player, "  &8- &f" + entry.getKey() + ": " + (entry.getValue() ? "&aON" : "&cOFF"));
        }
        messages.send(player, "&8&m--------------------------------------------------");
    }

    public void giveCompass(Player player) {
        // Prevent giving duplicates
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.COMPASS && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && meta.getDisplayName().contains("ProShield")) {
                    return; // Already has compass
                }
            }
        }

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(messages.color(COMPASS_NAME));
            List<String> lore = Arrays.asList(
                    "&7Right-click to open the ProShield menu",
                    "&7Manage your claims, flags & trusted players"
            );
            meta.setLore(messages.colorList(lore));
            compass.setItemMeta(meta);
        }

        player.getInventory().addItem(compass);
    }
}
