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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class PlayerCommandDispatcher implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public PlayerCommandDispatcher(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.messages = plugin.getMessagesUtil();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /* =====================================================
     * Handle right-click with compass → open GUI
     * ===================================================== */
    @EventHandler
    public void onCompassClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = meta.getDisplayName();
        if (!name.contains("ProShield")) return;

        // Prevent default compass action
        event.setCancelled(true);

        // Open main GUI
        plugin.getGuiManager().openMain(player);
    }

    /* =====================================================
     * Utility → Show claim info in chat (optional fallback)
     * ===================================================== */
    public void sendClaimInfo(Player player) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "&7You are in the wilderness.");
            return;
        }

        String ownerName = Bukkit.getOfflinePlayer(plot.getOwner()).getName();
        if (ownerName == null) ownerName = plot.getOwner().toString();

        messages.send(player, "&aClaim Info:");
        messages.send(player, "&7World: &f" + plot.getWorld());
        messages.send(player, "&7Chunk: &f" + plot.getX() + ", " + plot.getZ());
        messages.send(player, "&7Owner: &f" + ownerName);
        messages.send(player, "&7Radius: &f" + plot.getRadius());
        messages.send(player, "&7Flags: &f" + Arrays.toString(plot.getFlags().entrySet().toArray()));
    }

    /* =====================================================
     * Give player a ProShield compass
     * ===================================================== */
    public void giveCompass(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(messages.color("&6ProShield Compass"));
            meta.setLore(Arrays.asList(
                    messages.color("&7Right-click to open the ProShield menu"),
                    messages.color("&7Manage your claims, flags & trusted players")
            ));
            compass.setItemMeta(meta);
        }
        player.getInventory().addItem(compass);
    }
}
