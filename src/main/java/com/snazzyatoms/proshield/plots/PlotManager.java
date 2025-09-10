// path: src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages plot lookup, persistence, and defaults.
 * - Preserves prior behavior (owner/trust, basic flags)
 * - Extends with new per-claim flags (pvp/explosions/fire/mobGrief/keepItems/redstone/container/animals)
 * - Applies defaults from config.yml to newly created plots
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Plot> plots = new ConcurrentHashMap<>(); // key = world:x:z

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    // -------------------------
    // Key & Lookup
    // -------------------------
    public static String key(Chunk c) {
        return c.getWorld().getName() + ":" + c.getX() + ":" + c.getZ();
    }

    public Plot getPlot(Chunk chunk) {
        return plots.get(key(chunk));
    }

    public Plot getPlot(String world, int x, int z) {
        return plots.get(world + ":" + x + ":" + z);
    }

    public Plot getOrCreatePlot(Chunk chunk, UUID owner) {
        String k = key(chunk);
        Plot p = plots.get(k);
        if (p != null) return p;
        PlotSettings defaults = defaultsFromConfig();
        Plot created = new Plot(owner, chunk.getWorld().getName(), chunk.getX(), chunk.getZ(), defaults);
        plots.put(k, created);
        // Save immediately to disk
        savePlot(created);
        return created;
    }

    public Collection<Plot> allPlots() {
        return Collections.unmodifiableCollection(plots.values());
    }

    // -------------------------
    // Spawn guard / Claiming
    // -------------------------
    public boolean isInSpawnGuard(Location loc) {
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.getBoolean("spawn.block-claiming", true)) return false;
        int radius = Math.max(0, cfg.getInt("spawn.radius", 32));
        World w = loc.getWorld();
        if (w == null) return false;
        Location spawn = w.getSpawnLocation();
        return loc.getWorld().equals(spawn.getWorld()) && loc.distanceSquared(spawn) <= (long) radius * radius;
    }

    public boolean canClaimChunk(Chunk c, UUID requester, boolean adminBypass) {
        if (!adminBypass && isInSpawnGuard(c.getBlock(0, c.getWorld().getMinHeight(), 0).getLocation())) {
            return false;
        }
        return getPlot(c) == null; // no existing claim on this chunk
    }

    // -------------------------
    // Defaults from config.yml
    // -------------------------
    public PlotSettings defaultsFromConfig() {
        FileConfiguration cfg = plugin.getConfig();

        boolean defPvp = cfg.getBoolean("protection.pvp-in-claims", false);
        boolean defExpl = cfg.getBoolean("protection.explosions.enabled", true);
        boolean defFireSpread = cfg.getBoolean("protection.fire.spread", true);
        boolean defMobGrief = cfg.getBoolean("protection.entity-grief.enabled", true);
        boolean defKeepItems = cfg.getBoolean("claims.keep-items.enabled", false);

        boolean defRedstone = cfg.getBoolean("protection.interactions.categories.redstone", true)
                || cfg.getStringList("protection.interactions.categories").contains("redstone");
        boolean defContainer = cfg.getBoolean("protection.interactions.categories.container", true)
                || cfg.getStringList("protection.interactions.categories").contains("containers");
        boolean defAnimals = cfg.getBoolean("protection.entities.passive-animals", true);

        PlotSettings ps = new PlotSettings();
        // Preserve previous flags you already had:
        // (PlotSettings already contains these fields as per earlier work)
        ps.setPvpEnabled(defPvp);
        ps.setExplosionsAllowed(defExpl);
        ps.setFireSpreadAllowed(defFireSpread);
        ps.setMobGriefAllowed(defMobGrief);
        ps.setKeepItemsEnabled(defKeepItems);

        // Newly added flags (extended):
        ps.setRedstoneEnabled(defRedstone);
        ps.setContainerAccessEnabled(defContainer);
        ps.setAnimalInteractEnabled(defAnimals);

        return ps;
    }

    // -------------------------
    // Persistence
    // -------------------------
    public void loadAll() {
        plots.clear();
        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection root = cfg.getConfigurationSection("claims");
        if (root == null) return;

        for (String worldKey : root.getKeys(false)) {
            ConfigurationSection worldSec = root.getConfigurationSection(worldKey);
            if (worldSec == null) continue;

            for (String chunkKey : worldSec.getKeys(false)) {
                ConfigurationSection csec = worldSec.getConfigurationSection(chunkKey);
                if (csec == null) continue;

                try {
                    int x = csec.getInt("x");
                    int z = csec.getInt("z");
                    String world = worldKey;
                    UUID owner = csec.isString("owner") ? UUID.fromString(csec.getString("owner")) : null;

                    PlotSettings ps = new PlotSettings();
                    // Keep backward compat: use defaults if missing in config
                    PlotSettings def = defaultsFromConfig();

                    ps.setPvpEnabled(csec.getBoolean("flags.pvp", def.isPvpEnabled()));
                    ps.setExplosionsAllowed(csec.getBoolean("flags.explosions", def.isExplosionsAllowed()));
                    ps.setFireSpreadAllowed(csec.getBoolean("flags.fire-spread", def.isFireSpreadAllowed()));
                    ps.setMobGriefAllowed(csec.getBoolean("flags.mob-grief", def.isMobGriefAllowed()));
                    ps.setKeepItemsEnabled(csec.getBoolean("flags.keep-items", def.isKeepItemsEnabled()));

                    ps.setRedstoneEnabled(csec.getBoolean("flags.redstone", def.isRedstoneEnabled()));
                    ps.setContainerAccessEnabled(csec.getBoolean("flags.container-access", def.isContainerAccessEnabled()));
                    ps.setAnimalInteractEnabled(csec.getBoolean("flags.animal-interact", def.isAnimalInteractEnabled()));

                    Plot plot = new Plot(owner, world, x, z, ps);

                    // Trusted map (uuid -> roleName)
                    Map<String, Object> tm = csec.getConfigurationSection("trusted") != null
                            ? csec.getConfigurationSection("trusted").getValues(false)
                            : Collections.emptyMap();
                    if (!tm.isEmpty()) {
                        for (Map.Entry<String, Object> e : tm.entrySet()) {
                            try {
                                UUID uid = UUID.fromString(e.getKey());
                                String roleName = String.valueOf(e.getValue());
                                plot.getTrusted().put(uid, roleName);
                            } catch (IllegalArgumentException ignore) {}
                        }
                    }

                    plots.put(world + ":" + x + ":" + z, plot);
                } catch (Exception ex) {
                    plugin.getLogger().warning("[ProShield] Failed to load claim " + worldKey + "/" + chunkKey + ": " + ex.getMessage());
                }
            }
        }
        Bukkit.getLogger().info("[ProShield] Loaded " + plots.size() + " claims.");
    }

    public void saveAll() {
        // Save under claims.<world>.<x_z> ...
        FileConfiguration cfg = plugin.getConfig();
        cfg.set("claims", null); // clear runtime section, we rebuild it

        for (Plot p : plots.values()) {
            savePlot(p, false);
        }
        plugin.saveConfig();
    }

    public void savePlot(Plot p) {
        savePlot(p, true);
    }

    private void savePlot(Plot p, boolean saveNow) {
        FileConfiguration cfg = plugin.getConfig();
        String base = "claims." + p.getWorld() + "." + p.getX() + "_" + p.getZ();
        cfg.set(base + ".x", p.getX());
        cfg.set(base + ".z", p.getZ());
        cfg.set(base + ".owner", p.getOwner() != null ? p.getOwner().toString() : null);

        // flags
        cfg.set(base + ".flags.pvp", p.getSettings().isPvpEnabled());
        cfg.set(base + ".flags.explosions", p.getSettings().isExplosionsAllowed());
        cfg.set(base + ".flags.fire-spread", p.getSettings().isFireSpreadAllowed());
        cfg.set(base + ".flags.mob-grief", p.getSettings().isMobGriefAllowed());
        cfg.set(base + ".flags.keep-items", p.getSettings().isKeepItemsEnabled());

        cfg.set(base + ".flags.redstone", p.getSettings().isRedstoneEnabled());
        cfg.set(base + ".flags.container-access", p.getSettings().isContainerAccessEnabled());
        cfg.set(base + ".flags.animal-interact", p.getSettings().isAnimalInteractEnabled());

        // trusted
        String tBase = base + ".trusted";
        cfg.set(tBase, null);
        if (!p.getTrusted().isEmpty()) {
            for (Map.Entry<UUID, String> e : p.getTrusted().entrySet()) {
                cfg.set(tBase + "." + e.getKey(), e.getValue());
            }
        }

        if (saveNow) plugin.saveConfig();
    }

    public void deletePlot(Chunk c) {
        Plot p = plots.remove(key(c));
        if (p == null) return;
        FileConfiguration cfg = plugin.getConfig();
        String base = "claims." + p.getWorld() + "." + p.getX() + "_" + p.getZ();
        cfg.set(base, null);
        plugin.saveConfig();
    }

    // -------------------------
    // Trust helpers
    // -------------------------
    public boolean trust(Plot p, UUID playerId, String roleName) {
        if (p == null || playerId == null) return false;
        p.getTrusted().put(playerId, roleName);
        savePlot(p);
        return true;
    }

    public boolean untrust(Plot p, UUID playerId) {
        if (p == null || playerId == null) return false;
        boolean removed = p.getTrusted().remove(playerId) != null;
        if (removed) savePlot(p);
        return removed;
    }

    // -------------------------
    // Flag toggles (GUI/command call these)
    // -------------------------
    public void setPvp(Plot p, boolean enabled) { p.getSettings().setPvpEnabled(enabled); savePlot(p); }
    public void setExplosions(Plot p, boolean allow) { p.getSettings().setExplosionsAllowed(allow); savePlot(p); }
    public void setFireSpread(Plot p, boolean allow) { p.getSettings().setFireSpreadAllowed(allow); savePlot(p); }
    public void setMobGrief(Plot p, boolean allow) { p.getSettings().setMobGriefAllowed(allow); savePlot(p); }
    public void setKeepItems(Plot p, boolean enable) { p.getSettings().setKeepItemsEnabled(enable); savePlot(p); }
    public void setRedstone(Plot p, boolean enable) { p.getSettings().setRedstoneEnabled(enable); savePlot(p); }
    public void setContainerAccess(Plot p, boolean enable) { p.getSettings().setContainerAccessEnabled(enable); savePlot(p); }
    public void setAnimalInteract(Plot p, boolean enable) { p.getSettings().setAnimalInteractEnabled(enable); savePlot(p); }

    // -------------------------
    // Admin helpers
    // -------------------------
    public boolean transferOwner(Plot p, UUID newOwner) {
        if (p == null || newOwner == null) return false;
        p.setOwner(newOwner);
        savePlot(p);
        return true;
    }

    // For debug/metrics if needed
    public int countByOwner(UUID owner) {
        int n = 0;
        for (Plot p : plots.values()) {
            if (owner.equals(p.getOwner())) n++;
        }
        return n;
    }
}
