package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class ItemProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    public ItemProtectionListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        Claim claim = plots.getClaimAt(item.getLocation());

        if (claim == null) return;

        // Check config: keep items inside claims
        boolean keepEnabled = plugin.getConfig().getBoolean("claims.keep-items.enabled", false);

        if (keepEnabled) {
            int despawn = plugin.getConfig().getInt("claims.keep-items.despawn-seconds", 900);
            item.setTicksLived(1); // reset life
            item.setUnlimitedLifetime(true);
            item.setPickupDelay(0);
            item.setCustomName(ChatColor.YELLOW + "[Claim-Protected]");
            item.setCustomNameVisible(false);

            // Custom despawn override
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!item.isDead() && item.isValid()) {
                    item.remove();
                }
            }, despawn * 20L);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();

        Claim claim = plots.getClaimAt(item.getLocation());
        if (claim == null) return;

        if (plots.isBypassing(player)) return; // Admin bypass

        ClaimRole role = claim.getRole(player.getUniqueId());

        if (!role.canPickupItems()) {
            event.setCancelled(true);
            player.sendMessage(plugin.prefix() + ChatColor.RED + "You cannot pick up items inside this claim!");
        }
    }
}
