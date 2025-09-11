package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * PvpProtectionListener
 *
 * ✅ Constructor cleaned (PlotManager, MessagesUtil)
 * ✅ Uses per-claim settings (damagePvpEnabled + damageProtectOwnerAndTrusted)
 * ✅ Protects owners & trusted players if configured
 * ✅ Debug messages preserved
 */
public class PvpProtectionListener implements Listener {

    private final PlotManager plots;
    private final MessagesUtil messages;

    public PvpProtectionListener(PlotManager plots, MessagesUtil messages) {
        this.plots = plots;
        this.messages = messages;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        Chunk chunk = victim.getLocation().getChunk();
        Plot plot = plots.getPlot(chunk);
        if (plot == null) return; // wilderness handled separately by config

        PlotSettings s = plot.getSettings();

        // PvP disabled inside this claim
        if (!s.isDamagePvpEnabled()) {
            event.setCancelled(true);
            messages.debug("&cPvP prevented in claim: " + plot.getDisplayNameSafe());
            return;
        }

        // Protect trusted/owners if configured
        if (s.isDamageProtectOwnerAndTrusted()) {
            if (plot.isOwner(attacker.getUniqueId()) || plot.getTrusted().containsKey(attacker.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
            if (plot.isOwner(victim.getUniqueId()) || plot.getTrusted().containsKey(victim.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
