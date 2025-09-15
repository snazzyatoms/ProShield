package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class ProtectionListener implements Listener {

    private final ProShield plugin;

    public ProtectionListener(ProShield plugin) {
        this.plugin = plugin;
        // periodic hostile despawn inside safe zones stays as you already wired elsewhere
    }

    /* =========
     * Helpers
     * ========= */
    private boolean isBypassing(Player p) {
        return p != null && plugin.getBypassing().contains(p.getUniqueId());
    }

    private boolean worldCtrl(World world, String key, boolean def) {
        FileConfiguration cfg = plugin.getConfig();
        String base = "protection.world-controls";
        if (!cfg.getBoolean(base + ".enabled", true)) return def;

        String w = world.getName();
        String wPath = base + ".worlds." + w + "." + key;
        if (cfg.isSet(wPath)) return cfg.getBoolean(wPath);

        return cfg.getBoolean(base + ".defaults." + key, def);
    }

    private boolean isHostile(Entity e) {
        return e instanceof Monster || e instanceof Slime || e instanceof Phantom || e instanceof Shulker || e instanceof MagmaCube;
    }

    /* ============================
     * WORLD CONTROLS ENFORCEMENT
     * ============================ */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (isBypassing(e.getPlayer())) return;
        if (!worldCtrl(e.getBlock().getWorld(), "block-break", true)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "Block breaking is disabled in this world.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (isBypassing(e.getPlayer())) return;
        if (!worldCtrl(e.getBlock().getWorld(), "block-place", true)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "Block placing is disabled in this world.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (isBypassing(e.getPlayer())) return;
        if (!worldCtrl(e.getBlockClicked().getWorld(), "bucket-use", true)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "Bucket use is disabled in this world.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (isBypassing(e.getPlayer())) return;
        if (!worldCtrl(e.getBlockClicked().getWorld(), "bucket-use", true)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "Bucket use is disabled in this world.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent e) {
        BlockIgniteEvent.IgniteCause cause = e.getCause();
        World w = e.getBlock().getWorld();

        switch (cause) {
            case FLINT_AND_STEEL -> { if (!worldCtrl(w, "ignite-flint", true)) e.setCancelled(true); }
            case LAVA -> { if (!worldCtrl(w, "ignite-lava", true)) e.setCancelled(true); }
            case LIGHTNING -> { if (!worldCtrl(w, "ignite-lightning", true)) e.setCancelled(true); }
            default -> {}
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBurn(BlockBurnEvent e) {
        if (!worldCtrl(e.getBlock().getWorld(), "fire-burn", true)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent e) {
        if (e.getSource() != null && e.getSource().getType() == org.bukkit.Material.FIRE) {
            if (!worldCtrl(e.getBlock().getWorld(), "fire-spread", true)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        if (!worldCtrl(e.getLocation().getWorld(), "explosions", true)) {
            e.blockList().clear(); // no block damage
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent e) {
        if (!worldCtrl(e.getLocation().getWorld(), "mob-spawn", true)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        if (isBypassing(victim)) return;

        Entity damager = e.getDamager();
        if (damager instanceof Player) return; // handled by claim PvP
        if (!worldCtrl(victim.getWorld(), "mob-damage", true)) {
            e.setCancelled(true);
        }
    }

    /* ============================
     * CLAIM FLAG ENFORCEMENT (as before)
     * ============================ */

    // PvP in claims
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPvpDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (isBypassing(attacker) || isBypassing(victim)) return;

        Plot plot = plugin.getPlotManager().getPlot(victim.getLocation());
        if (plot != null && !plot.getFlag("pvp",
                plugin.getConfig().getBoolean("claims.default-flags.pvp", false))) {
            e.setCancelled(true);
            plugin.getMessagesUtil().send(attacker, "&cPVP is disabled in this claim.");
        }
    }

    // Explosions in claims (still allow world control to clear too)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClaimExplosion(EntityExplodeEvent e, boolean dummy) {} // no-op, covered by world + below

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClaimExplosion2(EntityExplodeEvent e) {
        Plot plot = plugin.getPlotManager().getPlot(e.getLocation());
        if (plot != null && !plot.getFlag("explosions",
                plugin.getConfig().getBoolean("claims.default-flags.explosions", false))) {
            e.blockList().clear();
        }
    }

    // Buckets in claims
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClaimBucket(PlayerBucketEmptyEvent e) {
        if (isBypassing(e.getPlayer())) return;
        Plot plot = plugin.getPlotManager().getPlot(e.getBlockClicked().getLocation());
        if (plot != null && !plot.getFlag("buckets",
                plugin.getConfig().getBoolean("claims.default-flags.buckets", false))) {
            e.setCancelled(true);
            plugin.getMessagesUtil().send(e.getPlayer(), "&cBuckets are disabled in this claim.");
        }
    }

    // Containers in claims
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onContainerInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getClickedBlock() == null) return;
        if (!(e.getClickedBlock().getState() instanceof org.bukkit.block.Container)) return;

        Player p = e.getPlayer();
        if (isBypassing(p)) return;

        Plot plot = plugin.getPlotManager().getPlot(e.getClickedBlock().getLocation());
        if (plot != null && !plot.getFlag("containers",
                plugin.getConfig().getBoolean("claims.default-flags.containers", true))) {
            e.setCancelled(true);
            plugin.getMessagesUtil().send(p, "&cYou cannot open containers in this claim.");
        }
    }

    // Item frames / armor stands in claims
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFrameBreak(HangingBreakByEntityEvent e) {
        if (!(e.getRemover() instanceof Player p)) return;
        if (isBypassing(p)) return;

        Plot plot = plugin.getPlotManager().getPlot(e.getEntity().getLocation());
        if (plot != null && e.getEntity() instanceof ItemFrame &&
                !plot.getFlag("item-frames",
                        plugin.getConfig().getBoolean("claims.default-flags.item-frames", true))) {
            e.setCancelled(true);
            plugin.getMessagesUtil().send(p, "&cItem frames are protected.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArmorStandInteract(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof ArmorStand)) return;
        Player p = e.getPlayer();
        if (isBypassing(p)) return;

        Plot plot = plugin.getPlotManager().getPlot(e.getRightClicked().getLocation());
        if (plot != null && !plot.getFlag("armor-stands",
                plugin.getConfig().getBoolean("claims.default-flags.armor-stands", true))) {
            e.setCancelled(true);
            plugin.getMessagesUtil().send(p, "&cArmor stands are protected.");
        }
    }

    // Pets protected in claims
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPetDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Tameable)) return;
        Entity damager = e.getDamager();
        Player p = (damager instanceof Player) ? (Player) damager : null;
        if (p != null && isBypassing(p)) return;

        Plot plot = plugin.getPlotManager().getPlot(e.getEntity().getLocation());
        if (plot != null && !plot.getFlag("pets",
                plugin.getConfig().getBoolean("claims.default-flags.pets", true))) {
            e.setCancelled(true);
            if (p != null) plugin.getMessagesUtil().send(p, "&cPets are protected in this claim.");
        }
    }
}
