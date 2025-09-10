package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Prevents claiming within spawn-protected radius.
 * - Uses config.yml spawn.block-claiming + spawn.radius
 * - Messages handled via MessagesUtil
 */
public class SpawnGuardListener implements Listener {

    private final ProShield plugin;
    private final MessagesUtil messages;

    public SpawnGuardListener(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler(ignoreCancelled = true)
    public void onClaimCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase();

        // Only intercept claim commands
        if (!msg.startsWith("/proshield claim") && !msg.startsWith("/ps claim") && !msg.startsWith("/shield claim")) {
            return;
        }

        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();

        if (!config.getBoolean("spawn.block-claiming", true)) return;

        Location spawn = player.getWorld().getSpawnLocation();
        Location playerLoc = player.getLocation();

        int radius = config.getInt("spawn.radius", 32);
        if (!isWithinSpawnProtection(spawn, playerLoc, radius)) return;

        event.setCancelled(true);
        messages.send(player, "spawn.claim-blocked", "%radius%", String.valueOf(radius));
    }

    private boolean isWithinSpawnProtection(Location spawn, Location loc, int radius) {
        if (!spawn.getWorld().equals(loc.getWorld())) return false;

        World world = spawn.getWorld();
        if (world == null) return false;

        return spawn.distanceSquared(loc) <= (radius * radius);
    }
}
