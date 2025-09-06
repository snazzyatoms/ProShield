// path: src/main/java/com/snazzyatoms/proshield/plots/BlockProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
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

public class BlockProtectionListener implements Listener {

    private final PlotManager plotManager;

    public BlockProtectionListener(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    private boolean isBypassing(Player p) {
        return p.hasMetadata("proshield_bypass");
    }

    private boolean denyIfNeeded(Player p, Location loc, String msg) {
        // Allow admins in bypass mode
        if (isBypassing(p)) return false;

        // If not claimed, allow
        if (!plotManager.isClaimed(loc)) return false;

        // Owner is allowed
        if (plotManager.isOwner(p.getUniqueId(), loc)) return false;

        // Otherwise deny
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

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;

        final Block clicked = e.getClickedBlock();
        if (clicked == null) return;

        // Toggle via config: protection.containers (default true)
        boolean protectContainers = ProShield.getInstance().getConfig().getBoolean("protection.containers", true);
        if (!protectContainers) return;

        BlockState state = clicked.getState();
        if (!(state instanceof Container)) return;

        if (denyIfNeeded(e.getPlayer(), clicked.getLocation(), "You cannot open containers here!")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        // pvp-in-claims (default false) -> if false, cancel PvP inside claims
        boolean allowPvPInClaims = ProShield.getInstance().getConfig().getBoolean("protection.pvp-in-claims", false);
        if (allowPvPInClaims) return;

        if (e.getDamager() instanceof Player damager && e.getEntity() instanceof Player) {
            if (isBypassing(damager)) return;
            Location loc = e.getEntity().getLocation();
            if (plotManager.isClaimed(loc)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        // mob-grief (default true) -> if true, prevent explosions from modifying claimed land
        boolean preventGrief = ProShield.getInstance().getConfig().getBoolean("protection.mob-grief", true);
        if (!preventGrief) return;

        EntityType type = e.getEntityType();
        if (type == EntityType.CREEPER || type == EntityType.PRIMED_TNT) {
            e.blockList().removeIf(block -> plotManager.isClaimed(block.getLocation()));
        }
    }
}
