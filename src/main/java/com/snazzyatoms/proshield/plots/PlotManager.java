package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlotManager — central access to plots.
 * Keeps previous behavior; adds helpers referenced across the codebase.
 */
public class PlotManager {

    private final ProShield plugin;

    // world -> (chunkKey -> Plot)
    private final Map<String, Map<Long, Plot>> plots = new ConcurrentHashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        reloadFromConfig();
    }

    /* -------- Key helpers -------- */

    private static long key(int x, int z) {
        return (((long) x) << 32) ^ (z & 0xffffffffL);
    }

    public Plot getPlot(Chunk chunk) {
        if (chunk == null) return null;
        Map<Long, Plot> map = plots.get(chunk.getWorld().getName());
        if (map == null) return null;
        return map.get(key(chunk.getX(), chunk.getZ()));
    }

    public Plot getPlot(String world, int chunkX, int chunkZ) {
        Map<Long, Plot> map = plots.get(world);
        if (map == null) return null;
        return map.get(key(chunkX, chunkZ));
    }

    public Plot getClaim(Location loc) {
        if (loc == null) return null;
        Map<Long, Plot> map = plots.get(loc.getWorld().getName());
        if (map == null) return null;
        Chunk c = loc.getChunk();
        return map.get(key(c.getX(), c.getZ()));
    }

    public boolean isClaimed(Location loc) {
        return getClaim(loc) != null;
    }

    public boolean isOwner(UUID uuid, Location loc) {
        Plot p = getClaim(loc);
        return p != null && p.isOwner(uuid);
    }

    public boolean isTrustedOrOwner(UUID uuid, Location loc) {
        Plot p = getClaim(loc);
        return p != null && p.isTrustedOrOwner(uuid);
    }

    public boolean hasAnyClaim(UUID owner) {
        for (Map<Long, Plot> map : plots.values()) {
            for (Plot p : map.values()) {
                if (p.isOwner(owner)) return true;
            }
        }
        return false;
    }

    public String getClaimName(Location loc) {
        Plot p = getClaim(loc);
        return (p == null ? null : p.getName());
    }

    /* -------- Create / Remove / Transfer -------- */

    public Plot createClaim(UUID owner, Location at) {
        if (owner == null || at == null || at.getWorld() == null) return null;
        Chunk c = at.getChunk();
        Plot p = new Plot(owner, c);
        plots.computeIfAbsent(c.getWorld().getName(), w -> new ConcurrentHashMap<>())
                .put(key(c.getX(), c.getZ()), p);
        saveAsync();
        return p;
    }

    public boolean unclaim(Location at) {
        if (at == null || at.getWorld() == null) return false;
        Chunk c = at.getChunk();
        Map<Long, Plot> map = plots.get(c.getWorld().getName());
        if (map == null) return false;
        Plot removed = map.remove(key(c.getX(), c.getZ()));
        if (removed != null) {
            saveAsync();
            return true;
        }
        return false;
    }

    public boolean transferOwnership(Plot plot, String newOwnerName) {
        if (plot == null || newOwnerName == null) return false;
        OfflinePlayer target = Bukkit.getOfflinePlayer(newOwnerName);
        if (target == null || target.getUniqueId() == null) return false;

        // Recreate a plot at same coords with new owner, preserve settings & trusted
        PlotSettings settings = plot.getSettings();
        Plot replacement = new Plot(target.getUniqueId(), plot.getWorldName(), plot.getChunkX(), plot.getChunkZ(), settings);

        // Carry over trusted map; demote previous owner to COOWNER if not the same
        replacement.getSettings().setPvpEnabled(settings.isPvpEnabled()); // (already same object; just explicit)
        for (Map.Entry<UUID, ClaimRole> e : plot.getTrusted().entrySet()) {
            replacement.addTrusted(e.getKey(), e.getValue());
        }
        if (!plot.getOwner().equals(target.getUniqueId())) {
            replacement.addTrusted(plot.getOwner(), ClaimRole.COOWNER);
        }

        // Put back
        Map<Long, Plot> map = plots.computeIfAbsent(plot.getWorldName(), w -> new ConcurrentHashMap<>());
        map.put(key(plot.getChunkX(), plot.getChunkZ()), replacement);
        saveAsync();
        return true;
    }

    /* -------- Trust helpers used by commands -------- */

    public boolean addTrusted(Plot plot, UUID uuid, ClaimRole role) {
        if (plot == null || uuid == null || role == null) return false;
        plot.addTrusted(uuid, role);
        saveAsync();
        return true;
    }

    public boolean removeTrusted(Plot plot, UUID uuid) {
        if (plot == null || uuid == null) return false;
        plot.removeTrusted(uuid);
        saveAsync();
        return true;
    }

    /* -------- Persistence (config.yml: claims: ...) -------- */

    public void reloadFromConfig() {
        plots.clear();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("claims");
        if (root == null) return;

        for (String world : root.getKeys(false)) {
            ConfigurationSection wSec = root.getConfigurationSection(world);
            if (wSec == null) continue;

            Map<Long, Plot> map = plots.computeIfAbsent(world, w -> new ConcurrentHashMap<>());

            for (String chunkKey : wSec.getKeys(false)) {
                ConfigurationSection cSec = wSec.getConfigurationSection(chunkKey);
                if (cSec == null) continue;

                int cx = cSec.getInt("x");
                int cz = cSec.getInt("z");
                UUID owner = UUID.fromString(cSec.getString("owner", "00000000-0000-0000-0000-000000000000"));
                String name = cSec.getString("name", null);

                PlotSettings settings = PlotSettings.fromConfig(cSec.getConfigurationSection("settings"));
                Plot p = new Plot(owner, world, cx, cz, settings);
                if (name != null) p.setName(name);

                // trusted
                ConfigurationSection tSec = cSec.getConfigurationSection("trusted");
                if (tSec != null) {
                    for (String id : tSec.getKeys(false)) {
                        try {
                            UUID uid = UUID.fromString(id);
                            String roleStr = tSec.getString(id, "VISITOR");
                            p.addTrusted(uid, com.snazzyatoms.proshield.roles.ClaimRole.fromString(roleStr));
                        } catch (IllegalArgumentException ignored) {}
                    }
                }

                map.put(key(cx, cz), p);
            }
        }
    }

    public void saveAsync() {
        new BukkitRunnable() {
            @Override public void run() { saveNow(); }
        }.runTaskAsynchronously(plugin);
    }

    public synchronized void saveNow() {
        // Serialize back to config.yml -> claims:
        plugin.reloadConfig(); // pull latest
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("claims");
        if (root == null) root = plugin.getConfig().createSection("claims");

        // wipe and rebuild the section for simplicity
        for (String k : new HashSet<>(root.getKeys(false))) {
            root.set(k, null);
        }

        for (Map.Entry<String, Map<Long, Plot>> worldEntry : plots.entrySet()) {
            String world = worldEntry.getKey();
            ConfigurationSection wSec = root.createSection(world);
            for (Plot p : worldEntry.getValue().values()) {
                String node = p.getChunkX() + "," + p.getChunkZ();
                ConfigurationSection cSec = wSec.createSection(node);
                cSec.set("x", p.getChunkX());
                cSec.set("z", p.getChunkZ());
                cSec.set("owner", p.getOwner().toString());
                cSec.set("name", p.getName());

                // settings
                ConfigurationSection sSec = cSec.createSection("settings");
                p.getSettings().toConfig(sSec);

                // trusted
                ConfigurationSection tSec = cSec.createSection("trusted");
                p.getTrusted().forEach((uuid, role) -> tSec.set(uuid.toString(), role.name()));
            }
        }

        plugin.saveConfig();
    }

    /* -------- Admin utilities -------- */

    /** Purge-expired stub: without last-activity timestamps, return 0; keep logic placeholder for future. */
    public int purgeExpired(int days, boolean dryRun) {
        // Placeholder: nothing to purge without an activity tracker
        return 0;
    }
}
