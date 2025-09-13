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

import java.util.Set;
import java.util.UUID;

public class PlotListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    // Neutral mobs that can become hostile
    private static final Set<Class<? extends Entity>> AGGRESSIVE_NEUTRALS = Set.of(
            Wolf.class,
            Bee.class,
            Enderman.class,
            PigZombie.class // aka Zombified Piglin
    );

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
        Entity e = event.getEntity();

        boolean hostile = e instanceof Monster || AGGRESSIVE_NEUTRALS.contains(e.getClass());
        if (!hostile) return;

        Location loc = e.getLocation();
        Plot plot = plotManager.getPlotAt(loc);
        if (plot != null) {
            event.setCancelled(true);
            if (plugin.isDebugEnabled()) {
                plugin.getLogger().info("[Debug] Cancelled spawn of " + e.getType() +
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
                if (isHostile(damager)) {
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
                    if (isHostile(damager)) {
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
        if (!(event.getTarget() instanceof Player target)) return;
        if (!isHostile(event.getEntity())) return;

        Plot plot = plotManager.getPlotAt(target.getLocation());
        if (plot == null) return;

        UUID id = target.getUniqueId();

        // Safe if owner or trusted
        if (target.isOp() || plot.isOwner(id) || roleManager.isTrusted(plot.getId(), target.getName())) {
            event.setCancelled(true);
            if (plugin.isDebugEnabled()) {
                plugin.getLogger().info("[Debug] Cancelled targeting of " + target.getName() +
                        " inside claim " + plot.getDisplayNameSafe());
            }
        }
    }

    /* ======================================================
     * HELPERS
     * ====================================================== */
    private boolean isHostile(Entity e) {
        if (e == null) return false;
        return e instanceof Monster ||
               AGGRESSIVE_NEUTRALS.stream().anyMatch(clazz -> clazz.isInstance(e)) ||
               (e instanceof Projectile proj && proj.getShooter() instanceof Monster);
    }

    /* ======================================================
     * OPTIONAL: BORDER EFFECTS (particles, repel, etc.)
     * ====================================================== */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Placeholder: could add claim border effects later
    }
}
