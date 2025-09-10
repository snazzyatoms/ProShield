package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class ItemProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    public ItemProtectionListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    /**
     * Prevent unauthorized players from breaking item frames / paintings.
     */
    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player)) return;
        Player player = (Player) event.getRemover();

        if (player.hasPermission("proshield.bypass")) return;

        Claim claim = plots.getClaimAt(event.getEntity().getLocation());
        if (claim == null) return;

        if (!claim.hasBuildAccess(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§cYou cannot break item frames or decorations here!");
            event.setCancelled(true);
        }
    }

    /**
     * Prevent unauthorized players from placing item frames / paintings.
     */
    @EventHandler(ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("proshield.bypass")) return;

        Claim claim = plots.getClaimAt(event.getEntity().getLocation());
        if (claim == null) return;

        if (!claim.hasBuildAccess(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§cYou cannot place item frames or decorations here!");
            event.setCancelled(true);
        }
    }

    /**
     * Prevent unauthorized interaction with item frames & armor stands.
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("proshield.bypass")) return;

        if (!(event.getRightClicked() instanceof ItemFrame || event.getRightClicked() instanceof ArmorStand)) return;

        Claim claim = plots.getClaimAt(event.getRightClicked().getLocation());
        if (claim == null) return;

        if (!claim.hasContainerAccess(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§cYou cannot interact with " +
                    event.getRightClicked().getType().name().toLowerCase() + " here!");
            event.setCancelled(true);
        }
    }

    /**
     * Handles items spawned in claims (respects keep-items config).
     */
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        Claim claim = plots.getClaimAt(event.getLocation());
        if (claim == null) return;

        if (plugin.getConfig().getBoolean("claims.keep-items.enabled", false)) {
            int despawnSeconds = plugin.getConfig().getInt("claims.keep-items.despawn-seconds", 900);
            event.getEntity().setTicksLived(1);
            event.getEntity().setUnlimitedLifetime(true);
            event.getEntity().setPersistent(true);
            event.getEntity().setPickupDelay(0);
            event.getEntity().setTicksLived(0);
            event.getEntity().setCustomName("§e[Protected]");
            event.getEntity().setCustomNameVisible(false);
            event.getEntity().setRemoveWhenFarAway(false);
            event.getEntity().setTicksLived(-despawnSeconds * 20); // reset lifespan
        }
    }
}
