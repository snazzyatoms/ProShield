package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Map;
import java.util.UUID;

public class ProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    public ProtectionListener(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.roleManager = plugin.getRoleManager();

        // Start periodic safezone mob cleanup
        long interval = 20L * 30; // every 30 seconds
        Bukkit.getScheduler().runTaskTimer(plugin, this::sweepSafezoneMobs, interval, interval);
    }

    /* ===========
     * Utilities
     * =========== */
    private Plot plotAt(Block block) {
        return (block == null) ? null : plotManager.getPlot(block.getLocation());
    }
    private Plot plotAt(Entity e) {
        return (e == null) ? null : plotManager.getPlot(e.getLocation());
    }
    private boolean isOwnerOrTrusted(Plot plot, Player p) {
        return plot != null && (plot.isOwner(p.getUniqueId()) || plot.isTrusted(p.getUniqueId()));
    }
    private boolean hasRoleOverride(UUID plotId, Player p, String key) {
        if (p == null || plotId == null) return false;
        if (plugin.getBypassing().contains(p.getUniqueId())) return true;
        Plot plot = plotManager.getPlotById(plotId);
        if (plot != null && plot.isOwner(p.getUniqueId())) return true;
        Map<String, Boolean> perms = roleManager.getPermissions(plotId, p.getName());
        Boolean v = perms.get(key);
        return v != null && v;
    }

    /* ========================
     * BLOCK BREAK / PLACE
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Plot plot = plotAt(event.getBlock());
        if (plot == null) return;
        Player p = event.getPlayer();
        if (hasRoleOverride(plot.getId(), p, "build")) return;
        if (!isOwnerOrTrusted(plot, p)) {
            event.setCancelled(true);
            plugin.getMessagesUtil().send(p, "&cYou cannot break blocks in this claim.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Plot plot = plotAt(event.getBlock());
        if (plot == null) return;
        Player p = event.getPlayer();
        if (hasRoleOverride(plot.getId(), p, "build")) return;
        if (!isOwnerOrTrusted(plot, p)) {
            event.setCancelled(true);
            plugin.getMessagesUtil().send(p, "&cYou cannot place blocks in this claim.");
        }
    }

    /* ========================
     * CONTAINERS / INTERACT
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player p)) return;
        Plot plot = plotManager.getPlot(p.getLocation());
        if (plot == null) return;

        if (hasRoleOverride(plot.getId(), p, "containers")) return;

        boolean allowContainers = plot.getFlag("containers",
                plugin.getConfig().getBoolean("claims.default-flags.containers", true));

        if (!allowContainers && !isOwnerOrTrusted(plot, p)) {
            event.setCancelled(true);
            plugin.getMessagesUtil().send(p, "&cContainers are protected here.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Plot plot = plotManager.getPlot(p.getLocation());
        if (plot == null) return;

        if (hasRoleOverride(plot.getId(), p, "interact")) return;

        boolean allowInteract = plot.getFlag("interact",
                plugin.getConfig().getBoolean("claims.default-flags.interact", true));

        if (!allowInteract && !isOwnerOrTrusted(plot, p)) {
            event.setCancelled(true);
            plugin.getMessagesUtil().send(p, "&cYou cannot interact here.");
        }
    }

    /* ========================
     * BUCKETS
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Plot plot = plotManager.getPlot(event.getBlockClicked().getLocation());
        if (plot == null) return;
        Player p = event.getPlayer();
        boolean allow = plot.getFlag("buckets",
                plugin.getConfig().getBoolean("claims.default-flags.buckets", false));
        if (!allow && !isOwnerOrTrusted(plot, p) && !hasRoleOverride(plot.getId(), p, "interact")) {
            event.setCancelled(true);
            plugin.getMessagesUtil().send(p, "&cBuckets are disabled in this claim.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Plot plot = plotManager.getPlot(event.getBlockClicked().getLocation());
        if (plot == null) return;
        Player p = event.getPlayer();
        boolean allow = plot.getFlag("buckets",
                plugin.getConfig().getBoolean("claims.default-flags.buckets", false));
        if (!allow && !isOwnerOrTrusted(plot, p) && !hasRoleOverride(plot.getId(), p, "interact")) {
            event.setCancelled(true);
            plugin.getMessagesUtil().send(p, "&cBuckets are disabled in this claim.");
        }
    }

    /* ========================
     * ITEM FRAMES / ARMOR STANDS
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player p)) return;
        Plot plot = plotAt(event.getEntity());
        if (plot == null) return;
        boolean protectFrames = plot.getFlag("item-frames",
                plugin.getConfig().getBoolean("claims.default-flags.item-frames", true));
        if (protectFrames && !isOwnerOrTrusted(plot, p) && !hasRoleOverride(plot.getId(), p, "interact")) {
            event.setCancelled(true);
            plugin.getMessagesUtil().send(p, "&cYou cannot remove that here.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onArmorStandInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        Player p = event.getPlayer();
        Plot plot = plotAt(event.getRightClicked());
        if (plot == null) return;
        boolean protectStands = plot.getFlag("armor-stands",
                plugin.getConfig().getBoolean("claims.default-flags.armor-stands", true));
        if (protectStands && !isOwnerOrTrusted(plot, p) && !hasRoleOverride(plot.getId(), p, "interact")) {
            event.setCancelled(true);
            plugin.getMessagesUtil().send(p, "&cYou cannot modify armor stands here.");
        }
    }

    /* ========================
     * VEHICLES
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onVehicleUse(PlayerInteractEntityEvent event) {
        Entity e = event.getRightClicked();
        if (!(e instanceof Boat || e instanceof Minecart)) return;
        Player p = event.getPlayer();
        Plot plot = plotAt(e);
        if (plot == null) return;

        boolean allowVehicles = plot.getFlag("vehicles",
                plugin.getConfig().getBoolean("claims.default-flags.vehicles", true));

        if (!allowVehicles && !isOwnerOrTrusted(plot, p) && !hasRoleOverride(plot.getId(), p, "interact")) {
            event.setCancelled(true);
            plugin.getMessagesUtil().send(p, "&cVehicles are protected here.");
        }
    }

    /* ========================
     * PVP / MOBS / PETS
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Player victim
        if (event.getEntity() instanceof Player victim) {
            Plot plot = plotManager.getPlot(victim.getLocation());
            if (plot != null) {
                if (event.getDamager() instanceof Player attacker) {
                    boolean pvp = plot.getFlag("pvp",
                            plugin.getConfig().getBoolean("claims.default-flags.pvp", false));
                    if (!pvp && !plugin.getBypassing().contains(attacker.getUniqueId())) {
                        event.setCancelled(true);
                        attacker.sendMessage(ChatColor.RED + "PVP is disabled in this claim!");
                    }
                } else if (event.getDamager() instanceof Monster) {
                    boolean safe = plot.getFlag("safezone",
                            plugin.getConfig().getBoolean("claims.default-flags.safezone", true));
                    if (safe) event.setCancelled(true);
                } else if (event.getDamager() instanceof Projectile proj) {
                    ProjectileSource shooter = proj.getShooter();
                    boolean safe = plot.getFlag("safezone",
                            plugin.getConfig().getBoolean("claims.default-flags.safezone", true));
                    if (safe && shooter instanceof Monster) event.setCancelled(true);
                }
            }
        }

        // Pet protection
        if (event.getEntity() instanceof Tameable tameable && tameable.isTamed()) {
            Plot plot = plotAt(event.getEntity());
            if (plot != null && plot.getFlag("pets",
                    plugin.getConfig().getBoolean("claims.default-flags.pets", true))) {
                AnimalTamer owner = tameable.getOwner();
                if (owner instanceof Player ownerPlayer) {
                    if (plot.isOwner(ownerPlayer.getUniqueId()) || plot.isTrusted(ownerPlayer.getUniqueId())) {
                        if (event.getDamager() instanceof Player attacker && !isOwnerOrTrusted(plot, attacker)) {
                            event.setCancelled(true);
                            attacker.sendMessage(ChatColor.RED + "You cannot harm pets here.");
                        }
                        if (event.getDamager() instanceof Monster) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    /* ========================
     * EXPLOSIONS
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        Plot plot = plotManager.getPlot(event.getLocation());
        if (plot == null) return;

        boolean explosions = plot.getFlag("explosions",
                plugin.getConfig().getBoolean("claims.default-flags.explosions", false));

        if (!explosions) {
            event.blockList().clear();
            if (event.getEntity() instanceof Creeper || event.getEntity() instanceof TNTPrimed) {
                event.getEntity().remove();
            }
        }
    }

    /* ========================
     * FIRE / IGNITE
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBurn(BlockBurnEvent event) {
        Plot plot = plotAt(event.getBlock());
        if (plot == null) return;
        boolean fire = plot.getFlag("fire",
                plugin.getConfig().getBoolean("claims.default-flags.fire", false));
        if (!fire) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockSpread(BlockSpreadEvent event) {
        if (event.getNewState() == null || event.getNewState().getType() != Material.FIRE) return;
        Plot plot = plotAt(event.getBlock());
        if (plot == null) return;
        boolean fire = plot.getFlag("fire",
                plugin.getConfig().getBoolean("claims.default-flags.fire", false));
        if (!fire) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Plot plot = plotAt(event.getBlock());
        if (plot == null) return;
        boolean ignite = plot.getFlag("ignite",
                plugin.getConfig().getBoolean("claims.default-flags.ignite", false));
        if (!ignite) event.setCancelled(true);
    }

    /* ========================
     * MOB SPAWN CONTROL (safezone)
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        Plot plot = plotAt(event.getLocation().getBlock());
        if (plot == null) return;
        boolean safe = plot.getFlag("safezone",
                plugin.getConfig().getBoolean("claims.default-flags.safezone", true));
        if (safe) {
            event.setCancelled(true);
        }
    }

    /* ========================
     * Periodic Sweep for Safezones
     * ======================== */
    private void sweepSafezoneMobs() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntitiesByClass(Monster.class)) {
                Plot plot = plotAt(e);
                if (plot != null) {
                    boolean safe = plot.getFlag("safezone",
                            plugin.getConfig().getBoolean("claims.default-flags.safezone", true));
                    if (safe) {
                        e.remove();
                    }
                }
            }
        }
    }
}
