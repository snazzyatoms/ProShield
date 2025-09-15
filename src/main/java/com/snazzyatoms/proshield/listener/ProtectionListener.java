package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
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
    /** Role-aware check: explicit per-player overrides (build/interact/containers/unclaim) */
    private boolean hasRoleOverride(UUID plotId, Player p, String key) {
        if (p == null || plotId == null) return false;
        if (plugin.getBypassing().contains(p.getUniqueId())) return true;
        Plot plot = plotManager.getPlotById(plotId);
        if (plot != null && plot.isOwner(p.getUniqueId())) return true; // owners always allowed
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
        if (!plot.getFlag("item-frames",
                plugin.getConfig().getBoolean("claims.default-flags.item-frames", true))) {
            // If protection is ON (true), we require owner/trusted
            if (!isOwnerOrTrusted(plot, p) && !hasRoleOverride(plot.getId(), p, "interact")) {
                event.setCancelled(true);
                plugin.getMessagesUtil().send(p, "&cYou cannot remove that here.");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onArmorStandInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        Player p = event.getPlayer();
        Plot plot = plotAt(event.getRightClicked());
        if (plot == null) return;

        if (!plot.getFlag("armor-stands",
                plugin.getConfig().getBoolean("claims.default-flags.armor-stands", true))) {
            if (!isOwnerOrTrusted(plot, p) && !hasRoleOverride(plot.getId(), p, "interact")) {
                event.setCancelled(true);
                plugin.getMessagesUtil().send(p, "&cYou cannot modify armor stands here.");
            }
        }
    }

    /* ========================
     * VEHICLES (boats/minecarts)
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
     * PVP / MOB DAMAGE
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Protect players in claims
        if (event.getEntity() instanceof Player victim) {
            Plot plot = plotManager.getPlot(victim.getLocation());
            if (plot != null) {
                // PVP
                if (event.getDamager() instanceof Player attacker) {
                    boolean pvp = plot.getFlag("pvp", plugin.getConfig().getBoolean("claims.default-flags.pvp", false));
                    if (!pvp && !plugin.getBypassing().contains(attacker.getUniqueId())) {
                        event.setCancelled(true);
                        attacker.sendMessage(ChatColor.RED + "PVP is disabled in this claim!");
                    }
                }
                // Mob attack
                else if (event.getDamager() instanceof Monster) {
                    boolean safe = plot.getFlag("safezone", plugin.getConfig().getBoolean("claims.default-flags.safezone", true));
                    if (safe) {
                        event.setCancelled(true);
                    }
                }
                // Projectile by mobs
                else if (event.getDamager() instanceof Projectile proj) {
                    ProjectileSource shooter = proj.getShooter();
                    boolean safe = plot.getFlag("safezone", plugin.getConfig().getBoolean("claims.default-flags.safezone", true));
                    if (safe && shooter instanceof Monster) {
                        event.setCancelled(true);
                    }
                }
            }
        }

        // Protect item frames / armor stands against players if flags are ON (protection enabled)
        if (event.getEntity() instanceof ItemFrame || event.getEntity() instanceof ArmorStand) {
            Plot plot = plotAt(event.getEntity());
            if (plot == null) return;

            boolean protectFrames = plot.getFlag("item-frames",
                    plugin.getConfig().getBoolean("claims.default-flags.item-frames", true));
            boolean protectStands = plot.getFlag("armor-stands",
                    plugin.getConfig().getBoolean("claims.default-flags.armor-stands", true));

            if (event.getEntity() instanceof ItemFrame && protectFrames && event.getDamager() instanceof Player p) {
                if (!isOwnerOrTrusted(plot, p) && !hasRoleOverride(plot.getId(), p, "interact")) {
                    event.setCancelled(true);
                    plugin.getMessagesUtil().send(p, "&cItem frames are protected here.");
                }
            }
            if (event.getEntity() instanceof ArmorStand && protectStands && event.getDamager() instanceof Player p) {
                if (!isOwnerOrTrusted(plot, p) && !hasRoleOverride(plot.getId(), p, "interact")) {
                    event.setCancelled(true);
                    plugin.getMessagesUtil().send(p, "&cArmor stands are protected here.");
                }
            }
        }
    }

    /* ========================
     * EXPLOSIONS
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        Plot plot = plotAt(event.getLocation().getWorld().getBlockAt(event.getLocation()).getLocation().getBlock());
        if (plot == null) return;

        boolean explosions = plot.getFlag("explosions",
                plugin.getConfig().getBoolean("claims.default-flags.explosions", false));

        if (!explosions) {
            event.blockList().clear(); // no block damage
            // Extra: remove explosive entities inside claim
            if (event.getEntity() instanceof Creeper || event.getEntity() instanceof TNTPrimed) {
                event.getEntity().remove();
            }
        }
    }

    /* ========================
     * FIRE & IGNITE CONTROL
     * ======================== */

    // stop natural fire burn if fire flag is false
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBurn(BlockBurnEvent event) {
        Plot plot = plotAt(event.getBlock());
        if (plot == null) return;
        boolean fire = plot.getFlag("fire",
                plugin.getConfig().getBoolean("claims.default-flags.fire", false));
        if (!fire) {
            event.setCancelled(true);
        }
    }

    // stop fire spread if fire flag is false
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockSpread(BlockSpreadEvent event) {
        if (event.getNewState() == null || event.getNewState().getType() != Material.FIRE) return;
        Plot plot = plotAt(event.getBlock());
        if (plot == null) return;
        boolean fire = plot.getFlag("fire",
                plugin.getConfig().getBoolean("claims.default-flags.fire", false));
        if (!fire) {
            event.setCancelled(true);
        }
    }

    // stop ignition sources if ignite flag is false
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Plot plot = plotAt(event.getBlock());
        if (plot == null) return;
        boolean ignite = plot.getFlag("ignite",
                plugin.getConfig().getBoolean("claims.default-flags.ignite", false));
        if (!ignite) {
            event.setCancelled(true);
        }
    }
}
