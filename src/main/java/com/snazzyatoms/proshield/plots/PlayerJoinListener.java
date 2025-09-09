package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

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
        final Player p = e.getPlayer();

        // quick exit if autogive disabled
        if (!plugin.getConfig().getBoolean("autogive.compass-on-join", true)) return;

        // only eligible if they have compass/admin perms
        boolean eligible = p.hasPermission("proshield.compass") || p.hasPermission("proshield.admin");
        if (!eligible) return;

        // Run 1 tick later so inventory and perms are fully ready
        Bukkit.getScheduler().runTask(plugin, () -> giveCompassIfNeeded(p));
    }

    private void giveCompassIfNeeded(Player p) {
        // Already holding a ProShield compass?
        if (hasAnyProShieldCompass(p)) {
            if (plugin.isDebug()) plugin.getLogger().info("[Join] " + p.getName() + " already has a ProShield compass.");
            return;
        }

        // Choose admin or player compass based on perms
        ItemStack toGive = (p.hasPermission("proshield.admin") || p.hasPermission("proshield.admin.gui"))
                ? gui.createAdminCompass()
                : gui.createPlayerCompass();

        // Add or drop depending on config
        boolean dropIfFull = plugin.getConfig().getBoolean("compass.drop-if-full", true);
        var inv = p.getInventory();
        var leftover = inv.addItem(toGive);

        if (!leftover.isEmpty()) {
            if (dropIfFull) {
                // drop at feet
                p.getWorld().dropItemNaturally(p.getLocation(), toGive);
                p.sendMessage(color(plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "))
                        + "Your ProShield compass was dropped at your feet (inventory full).");
            } else {
                p.sendMessage(color(plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "))
                        + "Inventory full. Use /proshield compass to get the ProShield compass.");
            }
        } else if (plugin.isDebug()) {
            plugin.getLogger().info("[Join] Gave ProShield compass to " + p.getName());
        }
    }

    private boolean hasAnyProShieldCompass(Player p) {
        for (ItemStack it : p.getInventory().getContents()) {
            if (gui.isProShieldCompass(it)) return true;
        }
        return false;
    }

    private String color(String s) { return org.bukkit.ChatColor.translateAlternateColorCodes('&', s); }
}
