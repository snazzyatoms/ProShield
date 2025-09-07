package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Enforces protection.pvp-in-claims
 * - If false: PvP is blocked inside claimed chunks (unless attacker has proshield.bypass).
 * - If true: PvP is allowed (listener passive).
 */
public class PvpProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private boolean pvpInClaims;

    public PvpProtectionListener(PlotManager plotManager) {
        this.plugin = ProShield.getInstance();
        this.plotManager = plotManager;
        reloadPvpFlag();
    }

    public final void reloadPvpFlag() {
        pvpInClaims = plugin.getConfig().getBoolean("protection.pvp-in-claims", false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (pvpInClaims) return; // allowed globally in claims
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof Player)) return;

        Player victim = (Player) e.getEntity();
        Player attacker = (Player) e.getDamager();

        if (attacker.hasPermission("proshield.bypass")) return;
        if (plotManager.isClaimed(victim.getLocation())) {
            e.setCancelled(true);
            attacker.sendMessage("§cPvP is disabled inside claimed chunks.");
        }
    }
}
