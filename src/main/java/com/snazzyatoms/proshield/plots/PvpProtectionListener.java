package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Blocks PvP inside claims based on config:
 * - protection.pvp-in-claims: false  => PvP is BLOCKED in claims (default/safe)
 * - protection.pvp-in-claims: true   => PvP allowed in claims
 *
 * Also respects bypass: players with "proshield.bypass" ignore protection.
 */
public class PvpProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;
    private boolean pvpInClaims;

    public PvpProtectionListener(PlotManager plots) {
        this.plugin = ProShield.getInstance();
        this.plots  = plots;
        reloadPvpFlag();
    }

    public void reloadPvpFlag() {
        this.pvpInClaims = plugin.getConfig().getBoolean("protection.pvp-in-claims", false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        // Bypass players ignore protection
        if (attacker.hasPermission("proshield.bypass") || victim.hasPermission("proshield.bypass")) return;

        // Only evaluate inside claims
        var claim = plots.getClaim(victim.getLocation());
        if (claim.isEmpty()) return;

        // If PvP is disallowed inside claims => cancel
        if (!pvpInClaims) {
            event.setCancelled(true);

            // Gentle, non-spammy feedback (attacker only)
            attacker.sendMessage(prefix() + ChatColor.RED + "PvP is disabled inside claims.");
        }
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }
}
