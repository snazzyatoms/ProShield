package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class ProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    public ProtectionListener(ProShield plugin) {
        this.plugin = plugin;
        this.plots = plugin.getPlotManager();
    }

    /* =========================
     * Block break / place
     * ========================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE && p.hasPermission("proshield.admin")) return;

        Plot plot = plots.getPlot(e.getBlock().getLocation());
        if (plot == null) return;

        if (plot.isAdminClaim()) {
            // Admin/safe zones: only admins can edit
            if (!p.hasPermission("proshield.admin")) {
                e.setCancelled(true);
            }
            return;
        }

        if (!plot.getFlag("block-break", false) && !plot.isTrusted(p.getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE && p.hasPermission("proshield.admin")) return;

        Plot plot = plots.getPlot(e.getBlock().getLocation());
        if (plot == null) return;

        if (plot.isAdminClaim()) {
            if (!p.hasPermission("proshield.admin")) {
                e.setCancelled(true);
            }
            return;
        }

        if (!plot.getFlag("block-place", false) && !plot.isTrusted(p.getUniqueId())) {
            e.setCancelled(true);
        }
    }

    /* =========================
     * Interactions (containers / flint)
     * ========================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;

        if (e.getClickedBlock() != null) {
            Plot plot = plots.getPlot(e.getClickedBlock().getLocation());
            if (plot == null) return;

            Player p = e.getPlayer();

            // Flint & Steel lighting
            if (p.getInventory().getItemInMainHand().getType() == Material.FLINT_AND_STEEL) {
                if (!plot.getFlag("ignite-flint", false) && !p.hasPermission("proshield.admin")) {
                    e.setCancelled(true);
                    return;
                }
            }

            // Container access (chests, furnaces, etc.)
            Material type = e.getClickedBlock().getType();
            boolean isContainer =
                    type.name().contains("CHEST") || type.name().contains("BARREL") ||
                    type.name().contains("FURNACE") || type.name().contains("BLAST_FURNACE") ||
                    type.name().contains("SMOKER") || type.name().contains("HOPPER") ||
                    type.name().contains("SHULKER_BOX") || type.name().contains("DISPENSER") ||
                    type.name().contains("DROPPER");

            if (isContainer && !plot.getFlag("container-access", true) && !plot.isTrusted(p.getUniqueId())
                    && !p.hasPermission("proshield.admin")) {
                e.setCancelled(true);
            }
        }
    }

    /* =========================
     * Fire protection
     * ========================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent e) {
        Plot plot = plots.getPlot(e.getBlock().getLocation());
        if (plot == null) return;

        switch (e.getCause()) {
            case FLINT_AND_STEEL -> {
                if (!plot.getFlag("ignite-flint", false)) e.setCancelled(true);
            }
            case LAVA -> {
                if (!plot.getFlag("ignite-lava", false)) e.setCancelled(true);
            }
            case LIGHTNING -> {
                if (!plot.getFlag("ignite-lightning", false)) e.setCancelled(true);
            }
            default -> { /* other causes not controlled here */ }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBurn(BlockBurnEvent e) {
        Plot plot = plots.getPlot(e.getBlock().getLocation());
        if (plot != null && !plot.getFlag("fire-burn", false)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent e) {
        // Fire/lava/water spread into claim
        Plot plot = plots.getPlot(e.getBlock().getLocation());
        if (plot == null) return;

        Material src = e.getSource().getType();
        if ((src == Material.FIRE || src == Material.SOUL_FIRE) && !plot.getFlag("fire-spread", false)) {
            e.setCancelled(true);
        }
    }

    /* =========================
     * Liquid grief (flow / buckets)
     * ========================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFromTo(BlockFromToEvent e) {
        Plot to = plots.getPlot(e.getToBlock().getLocation());
        if (to == null) return;

        Material type = e.getBlock().getType();
        if (type == Material.WATER || type == Material.KELP || type == Material.SEAGRASS) {
            if (!to.getFlag("water-flow", false)) e.setCancelled(true);
        }
        if (type == Material.LAVA || type == Material.MAGMA_BLOCK) {
            if (!to.getFlag("lava-flow", false)) e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Plot plot = plots.getPlot(e.getBlock().getLocation());
        if (plot == null) return;

        if (!plot.getFlag("bucket-empty", false) && !plot.isTrusted(e.getPlayer().getUniqueId())
                && !e.getPlayer().hasPermission("proshield.admin")) {
            e.setCancelled(true);
        }
    }

    /* =========================
     * Explosions (TNT/Creeper/etc.)
     * ========================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        if (e.getLocation() == null) return;
        Plot plot = plots.getPlot(e.getLocation());
        if (plot == null) return;

        if (!plot.getFlag("explosions", false)) {
            e.blockList().clear();
            e.setCancelled(true);
        }
    }

    /* =========================
     * Mob damage & PvP
     * ========================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        Entity victim = e.getEntity();
        Plot plot = plots.getPlot(victim.getLocation());
        if (plot == null) return;

        // PvP
        if (victim instanceof Player vPlayer) {
            Entity damager = e.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Entity shooter
                    ? shooter : e.getDamager();

            if (damager instanceof Player dPlayer) {
                // Admin/safe zone blocks PvP unless admin
                if (plot.isAdminClaim() && !dPlayer.hasPermission("proshield.admin")) {
                    e.setCancelled(true);
                    return;
                }
                // Plot PvP flag
                if (!plot.getFlag("pvp", true)) {
                    e.setCancelled(true);
                    return;
                }
            } else if (damager instanceof Monster || damager instanceof Explosive) {
                // Mob → player damage inside claims
                if (!plot.getFlag("mob-damage", false)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        // Pet protection: tamed entities protected from other players
        if (plot.getFlag("pet-protect", true) && victim instanceof Tameable tame && tame.isTamed()) {
            Entity damager = e.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Entity shooter
                    ? shooter : e.getDamager();

            if (damager instanceof Player dPlayer) {
                // owner or trusted can damage; others cannot
                if (tame.getOwner() instanceof AnimalTamer owner) {
                    if (owner instanceof Player ownerPlayer) {
                        if (!ownerPlayer.getUniqueId().equals(dPlayer.getUniqueId())
                                && !plot.isTrusted(dPlayer.getUniqueId())
                                && !dPlayer.hasPermission("proshield.admin")) {
                            e.setCancelled(true);
                            return;
                        }
                    } else {
                        // non-player owner → block by default
                        if (!dPlayer.hasPermission("proshield.admin")) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                } else {
                    // tamed with unknown owner → block by default
                    if (!dPlayer.hasPermission("proshield.admin")) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }

        // Animal protection: passive/farm animals protected from other players
        if (plot.getFlag("animal-protect", true) &&
                (victim instanceof Animals || victim instanceof Villager || victim instanceof Golem)) {
            Entity damager = e.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Entity shooter
                    ? shooter : e.getDamager();

            if (damager instanceof Player dPlayer) {
                if (!plot.isTrusted(dPlayer.getUniqueId()) && !dPlayer.hasPermission("proshield.admin")) {
                    e.setCancelled(true);
                }
            }
        }
    }

    // Also catch generic mob damage to players (fall-throughs)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Plot plot = plots.getPlot(e.getEntity().getLocation());
        if (plot == null) return;

        // Lava/Lightning damage control (environmental)
        if (e.getCause() == EntityDamageEvent.DamageCause.LAVA && !plot.getFlag("lava-flow", false)) {
            e.setCancelled(true);
            return;
        }
        if (e.getCause() == EntityDamageEvent.DamageCause.LIGHTNING && !plot.getFlag("ignite-lightning", false)) {
            e.setCancelled(true);
            return;
        }
    }

    /* =========================
     * Hostile targeting & spawns
     * ========================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTarget(EntityTargetEvent e) {
        if (!(e.getTarget() instanceof Player)) return;
        Plot plot = plots.getPlot(e.getTarget().getLocation());
        if (plot == null) return;

        if (!plot.getFlag("hostile-aggro", false) && e.getEntity() instanceof Monster) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent e) {
        Plot plot = plots.getPlot(e.getLocation());
        if (plot == null) return;
        if (!plot.getFlag("mob-spawn", false) && e.getEntity() instanceof Monster) {
            e.setCancelled(true);
        }
    }
}
