package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.ChatColor;
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
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        // Config gates
        boolean giveOnJoin = plugin.getConfig().getBoolean("autogive.compass-on-join", true);
        if (!giveOnJoin) return;

        // Permission gate (OP or specific perms)
        if (!(p.isOp() || p.hasPermission("proshield.compass") || p.hasPermission("proshield.admin.gui"))) {
            return;
        }

        // Already has a ProShield compass?
        if (hasProShieldCompass(p)) return;

        boolean dropIfFull = plugin.getConfig().getBoolean("compass.drop-if-full", true);
        gui.giveCompass(p, dropIfFull);
    }

    private boolean hasProShieldCompass(Player p) {
        String wanted = ChatColor.AQUA + "ProShield Compass";
        for (ItemStack it : p.getInventory().getContents()) {
            if (it == null || it.getType() == Material.AIR) continue;
            if (it.getType() != Material.COMPASS) continue;
            ItemMeta meta = it.getItemMeta();
            if (meta != null && wanted.equals(meta.getDisplayName())) {
                return true;
            }
        }
        return false;
    }
}
