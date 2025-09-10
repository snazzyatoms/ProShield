package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class ItemProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    public ItemProtectionListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        Claim claim = plots.getClaimAt(event.getItem().getLocation());
        if (claim == null) return;

        if (plots.isBypassing(player)) return;

        if (!claim.canPickupItems(player)) {
            event.setCancelled(true);
            player.sendMessage(plugin.prefix() + ChatColor.RED + "You cannot pick up items here.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemFrameBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player player)) return;

        Claim claim = plots.getClaimAt(event.getEntity().getLocation());
        if (claim == null) return;

        if (plots.isBypassing(player)) return;

        if (event.getEntity() instanceof ItemFrame && !claim.canInteract(player, "item-frames")) {
            event.setCancelled(true);
            player.sendMessage(plugin.prefix() + ChatColor.RED + "You cannot break item frames here.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemFramePlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();

        Claim claim = plots.getClaimAt(event.getEntity().getLocation());
        if (claim == null) return;

        if (plots.isBypassing(player)) return;

        if (event.getEntity() instanceof ItemFrame && !claim.canInteract(player, "item-frames")) {
            event.setCancelled(true);
            player.sendMessage(plugin.prefix() + ChatColor.RED + "You cannot place item frames here.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStand(PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();

        Claim claim = plots.getClaimAt(event.getRightClicked().getLocation());
        if (claim == null) return;

        if (plots.isBypassing(player)) return;

        if (event.getRightClicked() instanceof ArmorStand && !claim.canInteract(player, "armor-stands")) {
            event.setCancelled(true);
            player.sendMessage(plugin.prefix() + ChatColor.RED + "You cannot interact with armor stands here.");
        }
    }
}
