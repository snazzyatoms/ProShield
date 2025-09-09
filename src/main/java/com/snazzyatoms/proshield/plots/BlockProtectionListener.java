// path: src/main/java/com/snazzyatoms/proshield/plots/PvpProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Cancels PvP inside claimed chunks when protection.pvp-in-claims = false.
 * Exceptions:
 *  - Attacker or victim has "proshield.bypass"
 *  - (Optional) If protection.pvp.allow-trusted = true AND
 *    BOTH attacker & victim are owner/trusted in the victim's claim, allow PvP.
 *
 * Config keys used (with safe defaults):
 *   protection.pvp-in-claims: false
 *   protection.pvp.allow-trusted: false
 *   messages.pvp-denied: "&cPvP is disabled in this claim!"
 */
public class PvpProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    private boolean pvpInClaims;        // if false, we block PvP in claims
    private boolean allowTrustedPvp;    // optional exception
    private String  denyMessage;        // message sent to attacker when blocked

    public PvpProtectionListener(PlotManager plots) {
        this.plugin = ProShield.getInstance();
        this.plots = plots;
        reloadPvpFlag();
    }

    /** Called by /proshield reload */
    public final void reloadPvpFlag() {
        this.pvpInClaims     = plugin.getConfig().getBoolean("protection.pvp-in-claims", false);
        this.allowTrustedPvp = plugin.getConfig().getBoolean("protection.pvp.allow-trusted", false);
        this.denyMessage     = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.pvp-denied", "&cPvP is disabled in this claim!"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        // We only care about Player -> Player damage.
        Player victim = asPlayer(e.getEntity());
        if (victim == null) return;

        Player attacker = getAttackingPlayer(e.getDamager());
        if (attacker == null) return;

        // Bypass check first.
        if (attacker.hasPermission("proshield.bypass") || victim.hasPermission("proshield.bypass")) return;

        // If config allows PvP in claims, nothing to do.
        if (pvpInClaims) return;

        // Evaluate the claim at the victim's location.
        Location where = victim.getLocation();
        if (!plots.isClaimed(where)) return; // only restrict inside claims

        // Optional: allow trusted-vs-trusted PvP inside SAME claim.
        if (allowTrustedPvp) {
            boolean atkOk = plots.isTrustedOrOwner(attacker.getUniqueId(), where);
            boolean vicOk = plots.isTrustedOrOwner(victim.getUniqueId(),   where);
            if (atkOk && vicOk) return; // both are trusted/owner in the claim → allow
        }

        // Otherwise block the hit.
        e.setCancelled(true);
        if (!denyMessage.isEmpty()) attacker.sendMessage(denyMessage);
    }

    private Player asPlayer(Entity ent) {
        return (ent instanceof Player) ? (Player) ent : null;
    }

    private Player getAttackingPlayer(Entity damager) {
        if (damager instanceof Player) return (Player) damager;
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Player) {
            return (Player) proj.getShooter();
        }
        return null;
    }
}
