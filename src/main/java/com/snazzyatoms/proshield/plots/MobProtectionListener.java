// src/main/java/com/snazzyatoms/proshield/plots/MobProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.List;

public class MobProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public MobProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        startProtectionTasks(); // repel + optional despawn
    }

    /* ----------------------------------------------------
     * Helpers: read claim flags with config fallbacks
     * ---------------------------------------------------- */
    private boolean flag(Plot plot, String key, boolean defCfg) {
        if (plot == null) return defCfg;
        return plot.getFlag(key, defCfg);
    }

    private boolean cfg(String path, boolean defVal) {
        return plugin.getConfig().getBoolean(path, defVal);
    }

    private double cfgd(String path, double defVal) {
        return plugin.getConfig().getDouble(path, defVal);
    }

    private String cfgs(String path, String defVal) {
        String v = plugin.getConfig().getString(path);
        return (v == null || v.isBlank()) ? defVal : v;
    }

    private boolean isHostile(Entity e) {
        return e instanceof Monster || e instanceof Slime || e instanceof Phantom;
    }

    private boolean isTamedPet(Entity e) {
        if (e instanceof Tameable tame) {
            return tame.isTamed();
        }
        return false;
    }

    private boolean isInsideClaim(Location loc) {
        return plotManager.getPlot(loc) != null;
    }

    /* ----------------------------------------------------
     * Spawn control: cancel hostile spawns in safe zones
     * ---------------------------------------------------- */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!isHostile(event.getEntity())) return;
        Plot plot = plotManager.getPlot(event.getLocation());
        boolean defSafe = cfg("claims.default-flags.safezone", true);
        if (plot != null && flag(plot, "safezone", defSafe)) {
            event.setCancelled(true);
        }
    }

    /* ----------------------------------------------------
     * Targeting control: hostiles cannot target players in safe zones
     * ---------------------------------------------------- */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;
        Plot plot = plotManager.getPlot(player.getLocation());
        boolean defSafe = cfg("claims.default-flags.safezone", true);
        if (plot != null && flag(plot, "safezone", defSafe)) {
            if (isHostile(event.getEntity())) {
                event.setCancelled(true);
            }
        }
    }

    /* ----------------------------------------------------
     * Damage control
     * - Cancel hostile damage to players in safe zones
     * - Cancel PvP if pvp=false or safezone=true
     * - Protect tamed pets in claims
     * ---------------------------------------------------- */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        Plot plot = plotManager.getPlot(victim.getLocation());
        boolean defSafe = cfg("claims.default-flags.safezone", true);
        boolean safezone = plot != null && flag(plot, "safezone", defSafe);

        // PVP toggle (player vs player)
        if (victim instanceof Player && getDamagingPlayer(event) instanceof Player) {
            boolean pvpAllowed = plot == null ? cfg("claims.default-flags.pvp", false) : plot.getFlag("pvp", cfg("claims.default-flags.pvp", false));
            if (safezone || !pvpAllowed) {
                event.setCancelled(true);
                return;
            }
        }

        // Hostile → Player in safezone
        if (victim instanceof Player && safezone) {
            if (isHostileDamager(event.getDamager())) {
                event.setCancelled(true);
                return;
            }
        }

        // Protect tamed pets in claims (any damage)
        if (isTamedPet(victim) && plot != null) {
            // Optional: allow owner to damage their own pet? Keeping fully protected:
            event.setCancelled(true);
            return;
        }

        // Lightning protection for players/animals in claims if configured
        if ((victim instanceof Player || victim instanceof Animals) && event.getDamager() instanceof LightningStrike && plot != null) {
            boolean lightning = plot.getFlag("ignite-lightning", cfg("claims.default-flags.ignite-lightning", false));
            if (!lightning) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isHostileDamager(Entity damager) {
        if (isHostile(damager)) return true;

        if (damager instanceof Projectile proj) {
            ProjectileSource src = proj.getShooter();
            if (src instanceof Entity shooter && isHostile(shooter)) return true;
        }

        if (damager instanceof AreaEffectCloud cloud) {
            Entity source = cloud.getSource();
            return source != null && isHostile(source);
        }
        return false;
    }

    private Player getDamagingPlayer(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) return p;
        return null;
    }

    /* ----------------------------------------------------
     * Fishing hook / hanging break grief prevention in claims
     * ---------------------------------------------------- */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFish(PlayerFishEvent event) {
        if (event.getHook() == null) return;
        Location loc = event.getHook().getLocation();
        Plot plot = plotManager.getPlot(loc);
        if (plot != null && !plot.getFlag("container-access", cfg("claims.default-flags.container-access", true))) {
            // Using fishing rod to pull entities/armor stands/item frames, etc — block when container-access=false
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player remover)) return;
        Plot plot = plotManager.getPlot(event.getEntity().getLocation());
        if (plot == null) return;

        if (!plot.isTrusted(remover.getUniqueId()) && !remover.hasPermission("proshield.admin")) {
            // Respect generic build toggle: block-break=false prevents griefing hanging entities
            boolean canBreak = plot.getFlag("block-break", cfg("claims.default-flags.block-break", false));
            if (!canBreak) {
                event.setCancelled(true);
            }
        }
    }

    /* ----------------------------------------------------
     * Explosion protection (TNT/Creeper/Ghast/Block)
     * ---------------------------------------------------- */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        Location loc = event.getLocation();
        Plot plot = plotManager.getPlot(loc);
        boolean def = cfg("claims.default-flags.explosions", false);
        if (plot != null && !flag(plot, "explosions", def)) {
            // Cancel block damage entirely within claim
            event.blockList().clear();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        Location loc = event.getBlock().getLocation();
        Plot plot = plotManager.getPlot(loc);
        boolean def = cfg("claims.default-flags.explosions", false);
        if (plot != null && !flag(plot, "explosions", def)) {
            event.blockList().clear();
        }
    }

    /* ----------------------------------------------------
     * Fire protection (ignite/burn/spread)
     * ---------------------------------------------------- */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onIgnite(BlockIgniteEvent event) {
        Plot plot = plotManager.getPlot(event.getBlock().getLocation());
        if (plot == null) return;

        switch (event.getCause()) {
            case FLINT_AND_STEEL -> {
                if (!flag(plot, "ignite-flint", cfg("claims.default-flags.ignite-flint", false))) {
                    event.setCancelled(true);
                }
            }
            case LAVA -> {
                if (!flag(plot, "ignite-lava", cfg("claims.default-flags.ignite-lava", false))) {
                    event.setCancelled(true);
                }
            }
            case LIGHTNING -> {
                if (!flag(plot, "ignite-lightning", cfg("claims.default-flags.ignite-lightning", false))) {
                    event.setCancelled(true);
                }
            }
            default -> {
                // For other causes, respect fire-spread
                if (!flag(plot, "fire-spread", cfg("claims.default-flags.fire-spread", false))) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBurn(BlockBurnEvent event) {
        Plot plot = plotManager.getPlot(event.getBlock().getLocation());
        if (plot != null && !flag(plot, "fire-burn", cfg("claims.default-flags.fire-burn", false))) {
            event.setCancelled(true);
        }
    }

    // NOTE: Fire spread in modern APIs is largely represented by ignite/spread logic; BlockSpreadEvent (for mushrooms/vines/etc.)
    // would be overkill here. Ignite + Burn cover grief cases.

    /* ----------------------------------------------------
     * Fluid grief protection (water/lava flow into claims)
     * ---------------------------------------------------- */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockFromTo(BlockFromToEvent event) {
        Material type = event.getBlock().getType();
        if (type != Material.WATER && type != Material.KELP && type != Material.SEAGRASS &&
            type != Material.LAVA && type != Material.MAGMA_BLOCK && type != Material.POWDER_SNOW) {
            // Only gate common grief fluids; widen if you like
            if (type != Material.WATER && type != Material.LAVA) return;
        }

        Location to = event.getToBlock().getLocation();
        Plot toPlot = plotManager.getPlot(to);
        if (toPlot == null) return;

        boolean isLava = type == Material.LAVA;
        if (isLava && !flag(toPlot, "lava-flow", cfg("claims.default-flags.lava-flow", false))) {
            event.setCancelled(true);
            return;
        }
        boolean isWater = type == Material.WATER;
        if (isWater && !flag(toPlot, "water-flow", cfg("claims.default-flags.water-flow", false))) {
            event.setCancelled(true);
        }
    }

    /* ----------------------------------------------------
     * Periodic protection task:
     * - Repel hostiles near claim players
     * - Optional despawn hostiles within claimed chunks
     * ---------------------------------------------------- */
    private void startProtectionTasks() {
        new BukkitRunnable() {
            @Override
            public void run() {
                FileConfiguration cfg = plugin.getConfig();

                // Repel hostiles around players in claims
                if (cfg.getBoolean("protection.mobs.border-repel.enabled", true)) {
                    double radius = cfgd("protection.mobs.border-repel.radius", 3.0);
                    double pushX = cfgd("protection.mobs.border-repel.horizontal-push", 0.7);
                    double pushY = cfgd("protection.mobs.border-repel.vertical-push", 0.25);
                    boolean playSound = cfg("protection.mobs.border-repel.play-sound", true);
                    String soundName = cfgs("protection.mobs.border-repel.sound-type", "ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR");
                    Sound sound = null;
                    try { sound = Sound.valueOf(soundName); } catch (Throwable ignored) {}

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Plot plot = plotManager.getPlot(player.getLocation());
                        if (plot == null) continue;

                        boolean repelEnabled = plot.getFlag("repel-hostiles", cfg("claims.default-flags.repel-hostiles", true));
                        if (!repelEnabled) continue;

                        List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
                        for (Entity e : nearby) {
                            if (!isHostile(e)) continue;
                            Vector dir = e.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                            dir.setY(0.25);
                            e.setVelocity(dir.multiply(pushX).setY(pushY));
                            if (playSound && sound != null) {
                                player.playSound(player.getLocation(), sound, 0.5f, 1.0f);
                            }
                        }
                    }
                }

                // Optional despawn of hostiles inside claims
                if (cfg.getBoolean("protection.mobs.auto-despawn.enabled", false)) {
                    int radiusChunks = cfg.getInt("protection.mobs.auto-despawn.radius-chunks", 0); // 0 = only current chunk
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Plot center = plotManager.getPlot(player.getLocation());
                        if (center == null) continue;

                        if (!center.getFlag("despawn-hostiles", cfg("claims.default-flags.despawn-hostiles", true))) {
                            continue;
                        }

                        // Despawn within the claim chunk (and optionally neighbors)
                        Location base = player.getLocation();
                        Chunk c = base.getChunk();
                        int r = Math.max(0, radiusChunks);
                        for (int dx = -r; dx <= r; dx++) {
                            for (int dz = -r; dz <= r; dz++) {
                                Chunk chk = base.getWorld().getChunkAt(c.getX() + dx, c.getZ() + dz);
                                // Only act if the whole chunk is part of the same claim (simple check: center)
                                if (plotManager.getPlot(chk.getBlock(8, base.getWorld().getMaxHeight() / 2, 8).getLocation()) == null) {
                                    continue;
                                }
                                for (Entity e : chk.getEntities()) {
                                    if (isHostile(e)) {
                                        e.remove();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // run every second
    }
}
