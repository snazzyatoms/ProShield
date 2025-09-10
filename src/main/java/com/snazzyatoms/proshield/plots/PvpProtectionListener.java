package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PvpProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    public PvpProtectionListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        // Admin bypass
        if (plots.isBypassing(attacker) || plots.isBypassing(victim)) return;

        // Get claim at victim's location
        Claim claim = plots.getClaimAt(victim.getLocation());
        if (claim == null) return; // PvP in wilderness handled by server

        // Check if PvP is enabled in this claim
        if (!claim.isPvpAllowed()) {
            event.setCancelled(true);
            attacker.sendMessage(plugin.prefix() + ChatColor.RED + "PvP is disabled in this claim.");
        }
    }
}
