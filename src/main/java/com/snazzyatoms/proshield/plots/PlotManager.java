// src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlotManager {

    private final ProShield plugin;
    // world -> x,z -> plot
    private final Map<String, Map<Long, Plot>> plots = new ConcurrentHashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadFromConfig();
    }

    private static long key(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    public Plot getPlot(Chunk chunk) {
        Map<Long, Plot> map = plots.get(chunk.getWorld().getName());
        if (map == null) return null;
        return map.get(key(chunk.getX(), chunk.getZ()));
    }

    public Plot getClaim(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        Map<Long, Plot> map = plots.get(loc.getWorld().getName());
        if (map == null) return null;
        return map.get(key(loc.getChunk().getX(), loc.getChunk().getZ()));
    }

    public Optional<Plot> getClaimOptional(Location loc) {
        return Optional.ofNullable(getClaim(loc));
    }

    public boolean isClaimed(Location loc) {
        return getClaim(loc) != null;
    }

    public boolean isOwner(UUID player, Location loc) {
        Plot p = getClaim(loc);
        return p != null && p.isOwner(player);
    }

    public boolean isTrustedOrOwner(UUID player, Location loc) {
        Plot p = getClaim(loc);
        return p != null && p.isTrustedOrOwner(player);
    }

    public boolean hasAnyClaim(UUID owner) {
        if (owner == null) return false;
        for (Map<Long, Plot> map : plots.values()) {
            for (Plot p : map.values()) {
                if (owner.equals(p.getOwner())) return true;
            }
        }
        return false;
    }

    public Plot createClaim(UUID owner, Location loc) {
        Chunk chunk = loc.getChunk();
        Plot p = new Plot(owner, chunk);
        plots.computeIfAbsent(chunk.getWorld().getName(), k -> new ConcurrentHashMap<>())
                .put(key(chunk.getX(), chunk.getZ()), p);
        saveAsync();
        return p;
    }

    public void unclaim(Location loc) {
        if (loc == null || loc.getWorld() == null) return;
        Map<Long, Plot> map = plots.get(loc.getWorld().getName());
        if (map == null) return;
        map.remove(key(loc.getChunk().getX(), loc.getChunk().getZ()));
        saveAsync();
    }

    public void transferOwnership(Plot plot, String targetName) {
        if (plot == null || targetName == null) return;
        UUID newOwner = plugin.getServer().getOfflinePlayer(targetName).getUniqueId();
        // Simplest: remove & re-add claim at same position under new owner
        Map<Long, Plot> map = plots.get(plot.getWorld());
        if (map == null) return;
        map.put(key(plot.getX(), plot.getZ()), new Plot(newOwner,
                plugin.getServer().getWorld(plot.getWorld()).getChunkAt(plot.getX(), plot.getZ())));
        saveAsync();
    }

    public void trust(Plot plot, UUID uuid, ClaimRole role) {
        if (plot == null || uuid == null || role == null) return;
        plot.addTrusted(uuid, role);
        saveAsync();
    }

    public void untrust(Plot plot, String name) {
        if (plot == null || name == null) return;
        plot.removeTrusted(name);
        saveAsync();
    }

    public void reloadFromConfig() {
        loadFromConfig();
    }

    private void loadFromConfig() {
        plots.clear();
        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection root = cfg.getConfigurationSection("claims");
        if (root == null) return;

        for (String world : root.getKeys(false)) {
            ConfigurationSection worldSec = root.getConfigurationSection(world);
            if (worldSec == null) continue;
            Map<Long, Plot> map = plots.computeIfAbsent(world, k -> new ConcurrentHashMap<>());
            for (String chunkKey : worldSec.getKeys(false)) {
                String[] parts = chunkKey.split(",");
                if (parts.length != 2) continue;
                int x = Integer.parseInt(parts[0]);
                int z = Integer.parseInt(parts[1]);
                ConfigurationSection sec = worldSec.getConfigurationSection(chunkKey);
                if (sec == null) continue;

                UUID owner = UUID.fromString(sec.getString("owner"));
                Plot p = new Plot(owner, plugin.getServer().getWorld(world).getChunkAt(x, z));
                p.setName(sec.getString("name", null));

                // trusted
                ConfigurationSection t = sec.getConfigurationSection("trusted");
                if (t != null) {
                    for (String uid : t.getKeys(false)) {
                        ClaimRole r = ClaimRole.fromString(t.getString(uid, "VISITOR"));
                        p.addTrusted(UUID.fromString(uid), r);
                    }
                }

                // settings
                ConfigurationSection s = sec.getConfigurationSection("settings");
                p.getSettings().loadFrom(s);

                map.put(key(x, z), p);
            }
        }
    }

    public void saveAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::saveNow);
    }

    private void saveNow() {
        FileConfiguration cfg = plugin.getConfig();
        cfg.set("claims", null); // wipe previous

        for (Map.Entry<String, Map<Long, Plot>> entry : plots.entrySet()) {
            String world = entry.getKey();
            for (Plot p : entry.getValue().values()) {
                String chunkKey = p.getX() + "," + p.getZ();
                String base = "claims." + world + "." + chunkKey + ".";
                cfg.set(base + "owner", p.getOwner().toString());
                cfg.set(base + "name", p.getName());

                // trusted
                Map<String, Object> trusted = new HashMap<>();
                for (Map.Entry<UUID, ClaimRole> e : p.getTrusted().entrySet()) {
                    trusted.put(e.getKey().toString(), e.getValue().name());
                }
                cfg.createSection(base + "trusted", trusted);

                // settings
                cfg.createSection(base + "settings");
                p.getSettings().saveTo(cfg.getConfigurationSection(base + "settings"));
            }
        }
        plugin.saveConfig();
    }
}
