package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Handles PvP inside claims:
 * - Defaults to config.yml (protection.pvp-in-claims)
 * - Claim owners can override PvP per-claim
 * - Role manager decides who can fight inside claims
 */
public class PvpProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    public PvpProtectionListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        Chunk chunk = victim.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        // ✅ Wilderness → allowed unless globally disabled
        if (plot == null) {
            if (!plugin.getConfig().getBoolean("protection.pvp-in-wilderness", true)) {
                event.setCancelled(true);
                attacker.sendMessage(plugin.getPrefix() + "§cPvP is disabled in the wilderness.");
            }
            return;
        }

        // ✅ Claim → check roles + claim PvP toggle
        boolean claimPvP = plot.getSettings().isPvpAllowed();

        if (!claimPvP) {
            event.setCancelled(true);
            attacker.sendMessage(plugin.getPrefix() + "§cPvP is disabled in this claim.");
            return;
        }

        ClaimRole attackerRole = roleManager.getRole(plot, attacker);
        ClaimRole victimRole = roleManager.getRole(plot, victim);

        if (!roleManager.canPvp(attackerRole) || !roleManager.canPvp(victimRole)) {
            event.setCancelled(true);
            attacker.sendMessage(plugin.getPrefix() + "§cYou cannot attack players here.");
        }
    }
}
