// path: src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Claim> claims = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        reloadFromConfig();
    }

    public int getClaimCount() { return claims.size(); }

    public void reloadFromConfig() {
        claims.clear();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("claims");
        if (root == null) return;

        for (String key : root.getKeys(false)) {
            ConfigurationSection c = root.getConfigurationSection(key);
            if (c == null) continue;

            String world = c.getString("world");
            int cx = c.getInt("chunkX");
            int cz = c.getInt("chunkZ");
            String ownerStr = c.getString("owner");
            if (world == null || ownerStr == null) continue;

            UUID owner = UUID.fromString(ownerStr);
            claims.put(Claim.key(world, cx, cz), new Claim(owner, world, cx, cz));
        }
    }

    public void saveAll() {
        plugin.getConfig().set("claims", null); // wipe and rewrite
        for (Claim cl : claims.values()) {
            String path = "claims." + Claim.key(cl.getWorld(), cl.getChunkX(), cl.getChunkZ());
            plugin.getConfig().set(path + ".world", cl.getWorld());
            plugin.getConfig().set(path + ".chunkX", cl.getChunkX());
            plugin.getConfig().set(path + ".chunkZ", cl.getChunkZ());
            plugin.getConfig().set(path + ".owner", cl.getOwner().toString());
        }
        plugin.saveConfig();
    }

    /* ================= Core API ================= */

    public void createClaim(Player p) {
        Chunk chunk = p.getLocation().getChunk();
        String key = Claim.key(chunk);
        if (claims.containsKey(key)) {
            p.sendMessage(prefix(ChatColor.RED + "This chunk is already claimed."));
            return;
        }
        Claim cl = new Claim(p.getUniqueId(), chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        claims.put(key, cl);
        saveAll();
        p.sendMessage(prefix(ChatColor.GREEN + "Claim created for chunk " + ChatColor.YELLOW + chunk.getX() + "," + chunk.getZ()));
    }

    public void getClaimInfo(Player p) {
        Chunk chunk = p.getLocation().getChunk();
        Claim cl = claims.get(Claim.key(chunk));
        if (cl == null) {
            p.sendMessage(prefix(ChatColor.GRAY + "This chunk is unclaimed."));
            return;
        }
        String ownerName = plugin.getServer().getOfflinePlayer(cl.getOwner()).getName();
        p.sendMessage(prefix(ChatColor.AQUA + "Chunk " + chunk.getX() + "," + chunk.getZ() +
                " is owned by " + ChatColor.GOLD + (ownerName == null ? cl.getOwner() : ownerName)));
    }

    public void removeClaim(Player p) {
        Chunk chunk = p.getLocation().getChunk();
        String key = Claim.key(chunk);
        Claim cl = claims.get(key);
        if (cl == null) {
            p.sendMessage(prefix(ChatColor.RED + "No claim found in this chunk."));
            return;
        }
        boolean admin = p.isOp() || p.hasPermission("proshield.admin");
        if (!admin && !cl.getOwner().equals(p.getUniqueId())) {
            p.sendMessage(prefix(ChatColor.RED + "You do not own this claim."));
            return;
        }
        claims.remove(key);
        saveAll();
        p.sendMessage(prefix(ChatColor.GREEN + "Claim removed for chunk " + chunk.getX() + "," + chunk.getZ()));
    }

    /* ================= Query Helpers ================= */

    public boolean isClaimed(Location loc) {
        return claims.containsKey(Claim.key(loc.getWorld().getName(), loc.getChunk().getX(), loc.getChunk().getZ()));
    }

    public Claim getClaimAt(Location loc) {
        return claims.get(Claim.key(loc.getWorld().getName(), loc.getChunk().getX(), loc.getChunk().getZ()));
    }

    private String prefix(String msg) {
        return ChatColor.DARK_AQUA + "[ProShield] " + ChatColor.RESET + msg;
    }
}
