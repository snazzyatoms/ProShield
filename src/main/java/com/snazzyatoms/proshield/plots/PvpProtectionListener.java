package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class PvpProtectionListener implements Listener {

    private final ProShield plugin = ProShield.getInstance();
    private final PlotManager plotManager;

    private boolean pvpInClaims;

    // >>> IMPORTANT: constructor takes PlotManager
    public PvpProtectionListener(PlotManager plotManager) {
        this.plotManager = plotManager;
        reloadPvpFlag();
    }

    public final void reloadPvpFlag() {
        pvpInClaims = plugin.getConfig().getBoolean("protection.pvp-in-claims", false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (pvpInClaims) return; // PvP allowed inside claims when true

        if (e.getEntity() instanceof Player victim && e.getDamager() instanceof Player damager) {
            if (damager.hasPermission("proshield.bypass")) return;
            Location loc = victim.getLocation();
            if (plotManager.isClaimed(loc)) {
                e.setCancelled(true);
                damager.sendMessage("§cPvP is disabled inside claims.");
            }
        }
    }
}
