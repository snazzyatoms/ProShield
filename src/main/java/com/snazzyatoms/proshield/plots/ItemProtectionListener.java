// ===========================================
// ItemProtectionListener.java
// ===========================================
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class ItemProtectionListener implements Listener {
    private final PlotManager plots;

    public ItemProtectionListener(PlotManager plots) {
        this.plots = plots;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!plots.canInteract(p, e.getClickedBlock())) {
            e.setCancelled(true);
            p.sendMessage(ProShield.PREFIX + "You cannot interact here!");
        }
    }
}
