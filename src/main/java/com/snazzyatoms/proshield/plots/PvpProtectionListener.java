package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PvpProtectionListener implements Listener {

    private final PlotManager plotManager;
    private boolean pvpAllowed;

    public PvpProtectionListener(PlotManager plotManager) {
        this.plotManager = plotManager;
        reloadPvpFlag();
    }

    public void reloadPvpFlag() {
        pvpAllowed = ProShield.getInstance().getConfig().getBoolean("protection.pvp-in-claims", false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPvp(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        if (!(e.getDamager() instanceof Player attacker)) return;

        Location loc = victim.getLocation();
        if (!plotManager.isClaimed(loc)) return;

        // if PvP is disabled in claims and attacker doesn’t bypass -> cancel
        if (!pvpAllowed && !attacker.hasPermission("proshield.bypass")) {
            e.setCancelled(true);
            attacker.sendMessage(ChatColor.RED + "PvP is disabled in claimed areas!");
        }
    }
}
