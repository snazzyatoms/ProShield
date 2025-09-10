package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class InteractionProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    public InteractionProtectionListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return; // avoid double-firing

        Player player = event.getPlayer();
        if (player.hasPermission("proshield.bypass")) return;

        if (event.getClickedBlock() == null) return;

        Claim claim = plots.getClaimAt(event.getClickedBlock().getLocation());
        if (claim == null) return;

        // Owners & trusted (Container or above) can interact
        if (!claim.hasContainerAccess(player.getUniqueId())) {
            Material blockType = event.getClickedBlock().getType();
            player.sendMessage(plugin.getPrefix() + "§cYou cannot interact with " + blockType.name().toLowerCase() + " here!");
            event.setCancelled(true);
            return;
        }

        // Prevent using flint & steel, buckets, etc. unless allowed
        ItemStack item = event.getItem();
        if (item != null) {
            switch (item.getType()) {
                case FLINT_AND_STEEL:
                case BUCKET:
                case LAVA_BUCKET:
                case WATER_BUCKET:
                    if (!claim.hasBuildAccess(player.getUniqueId())) {
                        player.sendMessage(plugin.getPrefix() + "§cYou cannot use " + item.getType().name().toLowerCase() + " in this claim!");
                        event.setCancelled(true);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
