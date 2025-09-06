package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;

public class BlockProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public BlockProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    private boolean denyIfNeeded(Player p, Location loc, String msg) {
        if (plugin.isBypassing(p.getUniqueId())) return false;
        if (plotManager.isTrustedOrOwner(p.getUniqueId(), loc)) return false;
        if (!plotManager.isClaimed(loc)) return false;
        p.sendMessage(ChatColor.RED + msg);
        return true;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (denyIfNeeded(e.getPlayer(), e.getBlock().getLocation(), "You cannot break blocks here!")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (denyIfNeeded(e.getPlayer(), e.getBlock().getLocation(), "You cannot place blocks here!")) {
            e.setCancelled(true);
        }
    }

    // Container protection
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return; // avoid double triggers
        Block b = e.getClickedBlock();
        if (b == null || !plugin.getConfig().getBoolean("protection.containers", true)) return;

        BlockState st = b.getState();
        if (!(st instanceof Container)) return;

        if (denyIfNeeded(e.getPlayer(), b.getLocation(), "You cannot open containers here!")) {
            e.setCancelled(true);
        }
    }

    // PVP toggle inside claims
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (!plugin.getConfig().getBoolean("protection.pvp-in-claims", false)) {
            if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
                Player damager = (Player) e.getDamager();
                Location loc = e.getEntity().getLocation();
                if (plotManager.isClaimed(loc) && !plugin.isBypassing(damager.getUniqueId())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    // Mob grief prevention (explosions)
    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if (!plugin.getConfig().getBoolean("protection.mob-grief", true)) return;
        if (e.getEntityType() == EntityType.CREEPER || e.getEntityType() == EntityType.TNT) {
            // if any block in explosion is within a claim, cancel damage to blocks in claims
            e.blockList().removeIf(block -> plotManager.isClaimed(block.getLocation()));
        }
    }
}
