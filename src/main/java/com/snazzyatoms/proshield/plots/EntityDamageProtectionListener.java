package com.snazzyatoms.proshield.plots;

import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class EntityDamageProtectionListener implements Listener {
    private final PlotManager plots;
    public EntityDamageProtectionListener(PlotManager plots) { this.plots = plots; }

    @EventHandler(ignoreCancelled = true)
    public void onFrameBreak(HangingBreakByEntityEvent e) {
        if (!(e.getEntity() instanceof ItemFrame)) return;
        if (plots.getClaim(e.getEntity().getLocation()).isEmpty()) return;
        Entity remover = e.getRemover();
        if (remover instanceof Player p) {
            if (!plots.isTrustedOrOwner(p.getUniqueId(), e.getEntity().getLocation())) e.setCancelled(true);
        } else if (remover instanceof Projectile) {
            e.setCancelled(true);
        } else {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        if (plots.getClaim(e.getRightClicked().getLocation()).isEmpty()) return;
        if (!plots.isTrustedOrOwner(e.getPlayer().getUniqueId(), e.getRightClicked().getLocation())) e.setCancelled(true);
    }
}
