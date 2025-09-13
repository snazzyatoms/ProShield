// src/main/java/com/snazzyatoms/proshield/plots/PlotListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

public class PlotListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public PlotListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = messages;
    }

    /* ======================================================
     * ENTITY SPAWN CONTROL
     * ====================================================== */
    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)) return; // only hostile mobs

        Location loc = event.getLocation();
        Plot plot = plotManager.getPlotAt(loc);
        if (plot != null) {
            event.setCancelled(true);
            if (plugin.isDebugEnabled()) {
                plugin.getLogger().info("[Debug] Cancelled spawn of " + event.getEntityType() +
                        " in claim " + plot.getDisplayNameSafe());
            }
        }
    }

    /* ======================================================
     * DAMAGE PREVENTION (Players + Pets)
     * ====================================================== */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        Entity damager = event.getDamager();

        // Player in claim safe zone
        if (victim instanceof Player player) {
            Plot plot = plotManager.getPlotAt(player.getLocation());
            if (plot != null && !player.isOp()) {
                if (damager instanceof Monster || damager instanceof Projectile) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You are safe inside your claim.");
                }
            }
        }

        // Pets in claim safe zone
        if (victim instanceof Tameable pet && pet.isTamed()) {
            AnimalTamer owner = pet.getOwner();
            if (owner instanceof Player ownerPlayer) {
                Plot plot = plotManager.getPlotAt(pet.getLocation());
                if (plot != null && !ownerPlayer.isOp()) {
                    if (damager instanceof Monster || damager instanceof Projectile) {
                        event.setCancelled(true);
                        ownerPlayer.sendMessage(ChatColor.YELLOW + "Your pet is safe inside your claim.");
                    }
                }
            }
        }
    }

    /* ======================================================
     * TARGETING PREVENTION
     * ====================================================== */
    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        if (!(event.getTarget() instanceof Player target)) return;

        Plot plot = plotManager.getPlotAt(target.getLocation());
        if (plot == null) return;

        UUID id = target.getUniqueId();

        // Safe if owner or trusted
        if (plot.isOwner(id) || roleManager.isTrusted(plot.getId(), target.getName()) || target.isOp()) {
            event.setCancelled(true);
            if (plugin.isDebugEnabled()) {
                plugin.getLogger().info("[Debug] Cancelled targeting of " + target.getName() +
                        " inside claim " + plot.getDisplayNameSafe());
            }
        }
    }

    /* ======================================================
     * OPTIONAL: BORDER EFFECTS (particles, repel, etc.)
     * ====================================================== */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Placeholder: you can later add border previews or repel here
    }
}
