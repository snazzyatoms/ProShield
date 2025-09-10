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

        Claim claim = plots.getClaimAt(victim.getLocation());
        if (claim == null) return;

        // Admin bypass ignores PvP rules
        if (plots.isBypassing(attacker) || plots.isBypassing(victim)) return;

        // Global config (default)
        boolean globalPvpAllowed = plugin.getConfig().getBoolean("protection.pvp-in-claims", false);

        // Per-claim setting (if defined, overrides global)
        Boolean claimPvp = claim.getFlag("pvp");
        boolean isAllowed = (claimPvp != null) ? claimPvp : globalPvpAllowed;

        if (!isAllowed) {
            event.setCancelled(true);
            attacker.sendMessage(plugin.prefix() + ChatColor.RED + "PvP is disabled in this claim.");
        }
    }
}
