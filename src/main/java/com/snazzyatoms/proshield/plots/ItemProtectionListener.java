package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class ItemProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    private boolean enabled;
    private int despawnSec;
    private boolean antiTheft;

    public ItemProtectionListener(PlotManager plots) {
        this.plugin = ProShield.getInstance();
        this.plots = plots;
        reloadFromConfig();
    }

    public void reloadFromConfig() {
        enabled   = plugin.getConfig().getBoolean("item-keep.enabled", false);
        despawnSec = Math.max(300, Math.min(900, plugin.getConfig().getInt("item-keep.despawn-seconds", 600)));
        antiTheft = plugin.getConfig().getBoolean("item-keep.anti-theft", true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (!enabled) return;
        Location loc = e.getPlayer().getLocation();
        if (!plots.isClaimed(loc)) return;

        Item item = e.getItemDrop();
        // Tag owner for anti-theft pickup
        item.setMetadata("ps_owner", new FixedMetadataValue(plugin, e.getPlayer().getUniqueId().toString()));
        // Extend despawn only inside claims (server-friendly)
        item.setTicksLived(Math.max(0, item.getTicksLived())); // no-op, but explicit
        item.setTicksLived(0);
        item.setPersistent(true);
        item.setUnlimitedLifetime(true);
        // We can't set despawn directly; we reschedule kill if needed in onSpawn
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        if (!enabled) return;
        if (!(e.getEntity() instanceof Item item)) return;
        if (!plots.isClaimed(item.getLocation())) return;

        // Reset life so it lives longer; Bukkit despawn is tick based
        item.setTicksLived(0);
        item.setUnlimitedLifetime(true);

        // Schedule a safe despawn at custom time
        int ticks = despawnSec * 20;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!item.isDead() && item.isValid()) {
                item.remove();
            }
        }, ticks);
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        if (!enabled || !antiTheft) return;
        if (!plots.isClaimed(e.getItem().getLocation())) return;

        var md = e.getItem().getMetadata("ps_owner");
        if (!md.isEmpty()) {
            String owner = md.get(0).asString();
            if (!e.getPlayer().getUniqueId().toString().equals(owner)) {
                // Allow if player is trusted or owner of this claim
                if (!plots.isTrustedOrOwner(e.getPlayer().getUniqueId(), e.getItem().getLocation())) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(prefix() + ChatColor.RED + "You cannot pick up items here.");
                }
            }
        }
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }
}
