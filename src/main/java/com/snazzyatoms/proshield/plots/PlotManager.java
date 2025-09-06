package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotManager {

    private final ProShield plugin;

    // key: world:x:z -> Claim
    private final Map<String, Claim> claims = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadFromConfig(plugin.getConfig());
    }

    private String key(World world, int x, int z) {
        return world.getName() + ":" + x + ":" + z;
    }

    private String key(Location loc) {
        return key(loc.getWorld(), loc.getBlockX(), loc.getBlockZ());
    }

    /* ------------ Public API used by GUI ------------ */

    public void createClaim(Player player) {
        Location loc = player.getLocation();
        String k = key(loc);
        if (claims.containsKey(k)) {
            player.sendMessage(ChatColor.RED + "This block is already claimed.");
            return;
        }
        Claim claim = new Claim(player.getUniqueId(), loc);
        claims.put(k, claim);
        player.sendMessage(ChatColor.GREEN + "Claim created at " + ChatColor.YELLOW +
                loc.getBlockX() + "," + loc.getBlockZ());
        // persist now
        persistOne(k, claim);
    }

    public void sendClaimInfo(Player player) {
        String k = key(player.getLocation());
        Claim claim = claims.get(k);
        if (claim == null) {
            player.sendMessage(ChatColor.YELLOW + "No claim here.");
            return;
        }
        player.sendMessage(ChatColor.AQUA + "Claim Info:");
        player.sendMessage(ChatColor.GRAY + "Owner: " + ChatColor.WHITE + claim.getOwner());
        player.sendMessage(ChatColor.GRAY + "At: " + ChatColor.WHITE +
                claim.getLocation().getBlockX() + "," + claim.getLocation().getBlockZ());
    }

    public void removeClaim(Player player) {
        String k = key(player.getLocation());
        Claim claim = claims.get(k);
        if (claim == null) {
            player.sendMessage(ChatColor.YELLOW + "No claim here to remove.");
            return;
        }
        // Only owner or admin can remove
        if (!player.hasPermission("proshield.admin") &&
            !player.getUniqueId().equals(claim.getOwner())) {
            player.sendMessage(ChatColor.RED + "You are not the owner of this claim.");
            return;
        }
        claims.remove(k);
        FileConfiguration cfg = plugin.getConfig();
        cfg.set("claims." + k, null);
        plugin.saveConfig();
        player.sendMessage(ChatColor.GREEN + "Claim removed.");
    }

    /* ------------ Integration helpers ------------ */

    public boolean isOwner(Player player, Location at) {
        Claim claim = claims.get(key(at));
        return claim != null && claim.getOwner().equals(player.getUniqueId());
    }

    public boolean isClaimed(Location at) {
        return claims.containsKey(key(at));
    }

    /* ------------ Config persistence ------------ */

    private void loadFromConfig(FileConfiguration cfg) {
        if (!cfg.isConfigurationSection("claims")) return;
        for (String k : cfg.getConfigurationSection("claims").getKeys(false)) {
            String ownerStr = cfg.getString("claims." + k);
            if (ownerStr == null) continue;

            String[] parts = k.split(":");
            if (parts.length != 3) continue;

            World world = plugin.getServer().getWorld(parts[0]);
            if (world == null) continue;

            int x = parseInt(parts[1]);
            int z = parseInt(parts[2]);

            Location loc = new Location(world, x, world.getHighestBlockYAt(x, z), z);
            claims.put(k, new Claim(UUID.fromString(ownerStr), loc));
        }
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    public void flushToConfig(FileConfiguration cfg) {
        cfg.set("claims", null);
        for (Map.Entry<String, Claim> e : claims.entrySet()) {
            cfg.set("claims." + e.getKey(), e.getValue().getOwner().toString());
        }
    }

    private void persistOne(String key, Claim claim) {
        FileConfiguration cfg = plugin.getConfig();
        cfg.set("claims." + key, claim.getOwner().toString());
        plugin.saveConfig();
    }
}
