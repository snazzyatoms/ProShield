package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Handles PvP inside claims and in wilderness.
 * - Uses global config for default rules
 * - Players can toggle PvP per-claim
 * - Role-based checks prevent griefing
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
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();

        if (!(damager instanceof Player attacker)) return;
        if (!(victim instanceof Player target)) return;
        if (attacker.hasPermission("proshield.bypass")) return; // admins bypass

        FileConfiguration config = plugin.getConfig();

        Chunk chunk = target.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        // === Wilderness PvP ===
        if (plot == null) {
            boolean allowWildernessPvP = config.getBoolean("protection.wilderness.allow-pvp", true);
            if (!allowWildernessPvP) {
                event.setCancelled(true);
                attacker.sendMessage(plugin.getPrefix() + "§cPvP is disabled in the wilderness.");
            }
            return;
        }

        // === Inside a claim ===
        boolean allowClaimPvP = plot.getSettings().isPvpEnabled(); // per-claim setting
        if (!allowClaimPvP) {
            event.setCancelled(true);
            attacker.sendMessage(plugin.getPrefix() + "§cPvP is disabled in this claim.");
            return;
        }

        // === Role checks ===
        ClaimRole attackerRole = roleManager.getRole(plot, attacker);
        ClaimRole victimRole = roleManager.getRole(plot, target);

        // Example rule: Owners & Co-Owners can always fight each other if claim PvP is enabled
        if (roleManager.isOwnerOrCoOwner(attackerRole) && roleManager.isOwnerOrCoOwner(victimRole)) {
            return; // allow
        }

        // Default: follow config
        boolean globalClaimPvP = config.getBoolean("protection.pvp-in-claims", false);
        if (!globalClaimPvP) {
            event.setCancelled(true);
            attacker.sendMessage(plugin.getPrefix() + "§cPvP is not allowed in this claim.");
        }
    }
}
