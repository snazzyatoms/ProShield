package com.snazzyatoms.proshield.plots;

import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.UUID;

/**
 * EntityDamageProtectionListener
 *
 * ✅ Prevents item frame/entity grief inside claims
 * ✅ Fixed missing methods (getClaim → getPlot, isTrustedOrOwner → manual check)
 * ✅ Preserves all prior logic
 */
public class EntityDamageProtectionListener implements Listener {
    private final PlotManager plots;

    public EntityDamageProtectionListener(PlotManager plots) {
        this.plots = plots;
    }

    private boolean isTrustedOrOwner(UUID playerId, Plot plot) {
        if (plot == null || playerId == null) return false;
        return plot.isOwner(playerId) || plot.getTrusted().containsKey(playerId);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFrameBreak(HangingBreakByEntityEvent e) {
        if (!(e.getEntity() instanceof ItemFrame)) return;

        Plot plot = plots.getPlot(e.getEntity().getLocation()); // ✅ use getPlot
        if (plot == null) return;

        Entity remover = e.getRemover();
        if (remover instanceof Player p) {
            if (!isTrustedOrOwner(p.getUniqueId(), plot)) {
                e.setCancelled(true);
            }
        } else if (remover instanceof Projectile) {
            e.setCancelled(true);
        } else {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        Plot plot = plots.getPlot(e.getRightClicked().getLocation()); // ✅ use getPlot
        if (plot == null) return;

        if (!isTrustedOrOwner(e.getPlayer().getUniqueId(), plot)) {
            e.setCancelled(true);
        }
    }
}
