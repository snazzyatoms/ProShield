// src/main/java/com/snazzyatoms/proshield/plots/PvpProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Handles PvP inside plots.
 *
 * Preserves all prior logic:
 * ✅ Checks per-claim PvP flag
 * ✅ Protects trusted/owner if configured
 * ✅ UUID mismatch issues fixed
 */
public class PvpProtectionListener implements Listener {

    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public PvpProtectionListener(PlotManager plotManager, MessagesUtil messages) {
        this.plotManager = plotManager;
        this.messages = messages;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        Chunk chunk = victim.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return; // wilderness not handled here

        PlotSettings s = plot.getSettings();

        // Check PvP flag
        if (!s.isDamagePvpEnabled()) {
            event.setCancelled(true);
            messages.debug("&cPvP prevented in claim: " + plot.getDisplayNameSafe());
            return;
        }

        // Protect owner & trusted if configured
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
