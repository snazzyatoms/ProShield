package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages all plots (claimed chunks), persistence and lookups.
 * - Preserves prior behavior (YAML-backed, in-memory cache)
 * - Extended with helpers used across listeners/commands in 1.2.5
 */
public class PlotManager {

    private final ProShield plugin;
    // Key format: worldName + ":" + chunkX + "," + chunkZ
    private final Map<String, Plot> plots = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    public ProShield getPlugin() {
        return plugin;
    }

    /* =========================================================
     * Loading / Saving
     * ========================================================= */

    public void reloadFromConfig() {
        plots.clear();
        loadFromConfig(plugin.getConfig());
    }

    public void loadFromConfig(FileConfiguration config) {
        ConfigurationSection claimsSec = config.getConfigurationSection("claims");
        if (claimsSec == null) {
            // nothing yet; first run scenario
            return;
        }

        for (String key : claimsSec.getKeys(false)) {
            // key = world:x,z
            String[] worldAndCoords = key.split(":");
            if (worldAndCoords.length != 2) continue;

            String world = worldAndCoords[0];
            String[] coords = worldAndCoords[1].split(",");
            if (coords.length != 2) continue;

            int x, z;
            try {
                x = Integer.parseInt(coords[0]);
                z = Integer.parseInt(coords[1]);
            } catch (NumberFormatException nfe) {
                continue;
            }

            ConfigurationSection pSec = claimsSec.getConfigurationSection(key);
            if (pSec == null) continue;

            String ownerStr = pSec.getString("owner");
            if (ownerStr == null) continue;

            UUID owner;
            try {
                owner = UUID.fromString(ownerStr);
            } catch (IllegalArgumentException iae) {
                // legacy or name-based? try to resolve by name (best effort)
                OfflinePlayer legacy = Bukkit.getOfflinePlayer(ownerStr);
                owner = legacy.getUniqueId();
            }

            Plot plot = new Plot(owner, world, x, z);

            // Trusted players + roles
            ConfigurationSection trustedSec = pSec.getConfigurationSection("trusted");
            if (trustedSec != null) {
                for (String uuidOrName : trustedSec.getKeys(false)) {
                    ClaimRole role = ClaimRole.fromString(trustedSec.getString(uuidOrName, "VISITOR"));
                    UUID trustId = parseUuidOrResolve(uuidOrName);
                    if (trustId != null) {
                        plot.trustPlayer(trustId, role);
                    }
                }
            }

            // Flags (per-claim) via PlotSettings
            PlotSettings ps = plot.getSettings();
            ConfigurationSection flagsSec = pSec.getConfigurationSection("flags");
            if (flagsSec != null) {
                // Core booleans (only set if present to preserve defaults)
                setIfPresentBoolean(flagsSec, "pvp", ps::setPvpEnabled);
                setIfPresentBoolean(flagsSec, "keep-items", ps::setKeepItemsEnabled);

                setIfPresentBoolean(flagsSec, "damage.enabled", ps::setDamageEnabled);
                setIfPresentBoolean(flagsSec, "damage.pve", ps::setPveEnabled);

                setIfPresentBoolean(flagsSec, "entity-grief", ps::setEntityGriefingAllowed);
                setIfPresentBoolean(flagsSec, "item-frames", ps::setItemFramesAllowed);
                setIfPresentBoolean(flagsSec, "vehicles", ps::setVehiclesAllowed);
                setIfPresentBoolean(flagsSec, "buckets", ps::setBucketsAllowed);

                setIfPresentBoolean(flagsSec, "redstone", ps::setRedstoneAllowed);
                setIfPresentBoolean(flagsSec, "containers", ps::setContainersAllowed);
                setIfPresentBoolean(flagsSec, "animals", ps::setAnimalAccessAllowed);

                // Generic flags bag (for future)
                ConfigurationSection bag = flagsSec.getConfigurationSection("bag");
                if (bag != null) {
                    for (String fk : bag.getKeys(false)) {
                        ps.setFlag(fk, bag.getBoolean(fk, false));
                    }
                }
            }

            plots.put(key, plot);
        }
    }

    public void saveToConfig() {
        FileConfiguration config = plugin.getConfig();
        // wipe & rebuild section (safer than partial updates)
        config.set("claims", null);
        for (Plot p : plots.values()) {
            String key = key(p.getWorldName(), p.getChunkX(), p.getChunkZ());
            String base = "claims." + key;

            config.set(base + ".owner", p.getOwner().toString());

            // trusted
            String trustedPath = base + ".trusted";
            config.set(trustedPath, null);
            for (Map.Entry<UUID, ClaimRole> e : p.getTrustedPlayers().entrySet()) {
                config.set(trustedPath + "." + e.getKey().toString(), e.getValue().name());
            }

            // flags
            PlotSettings ps = p.getSettings();
            String flags = base + ".flags";
            config.set(flags + ".pvp", ps.isPvpEnabled());
            config.set(flags + ".keep-items", ps.isKeepItemsEnabled());

            config.set(flags + ".damage.enabled", ps.isDamageEnabled());
            config.set(flags + ".damage.pve", ps.isPveEnabled());

            config.set(flags + ".entity-grief", ps.isEntityGriefingAllowed());
            config.set(flags + ".item-frames", ps.isItemFramesAllowed());
            config.set(flags + ".vehicles", ps.isVehiclesAllowed());
            config.set(flags + ".buckets", ps.isBucketsAllowed());

            config.set(flags + ".redstone", ps.isRedstoneAllowed());
            config.set(flags + ".containers", ps.isContainersAllowed());
            config.set(flags + ".animals", ps.isAnimalAccessAllowed());

            // bag
            if (!ps.getFlags().isEmpty()) {
                for (Map.Entry<String, Boolean> f : ps.getFlags().entrySet()) {
                    config.set(flags + ".bag." + f.getKey(), f.getValue());
                }
            }
        }
        plugin.saveConfig();
    }

    private void setIfPresentBoolean(ConfigurationSection sec, String path, java.util.function.Consumer<Boolean> setter) {
        if (sec.isSet(path)) setter.accept(sec.getBoolean(path, false));
    }

    private UUID parseUuidOrResolve(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ignored) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(id);
            return op != null ? op.getUniqueId() : null;
        }
    }

    /* =========================================================
     * Lookups
     * ========================================================= */

    public Plot getPlot(Chunk chunk) {
        return plots.get(key(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
    }

    public Plot getClaim(Location loc) {
        Chunk c = loc.getChunk();
        return getPlot(c);
    }

    public boolean isClaimed(Location loc) {
        return getClaim(loc) != null;
    }

    public boolean isOwner(UUID playerId, Location loc) {
        Plot p = getClaim(loc);
        return p != null && p.isOwner(playerId);
    }

    public String getClaimName(Location loc) {
        Plot p = getClaim(loc);
        if (p == null) return "Wilderness";
        OfflinePlayer op = Bukkit.getOfflinePlayer(p.getOwner());
        String name = (op != null && op.getName() != null) ? op.getName() : p.getOwner().toString();
        return name + "'s Claim";
    }

    public boolean hasAnyClaim(UUID playerId) {
        for (Plot p : plots.values()) {
            if (p.getOwner().equals(playerId)) return true;
        }
        return false;
    }

    public List<Plot> getAllClaimsOf(UUID playerId) {
        return plots.values().stream()
                .filter(p -> p.getOwner().equals(playerId))
                .collect(Collectors.toList());
    }

    public Collection<Plot> getAllPlots() {
        return Collections.unmodifiableCollection(plots.values());
    }

    /* =========================================================
     * Mutations
     * ========================================================= */

    public boolean createClaim(UUID owner, Location loc) {
        Chunk c = loc.getChunk();
        String k = key(c.getWorld().getName(), c.getX(), c.getZ());
        if (plots.containsKey(k)) return false;
        Plot p = new Plot(owner, c.getWorld().getName(), c.getX(), c.getZ());
        plots.put(k, p);
        saveToConfig();
        return true;
    }

    public boolean removeClaim(Location loc) {
        Chunk c = loc.getChunk();
        String k = key(c.getWorld().getName(), c.getX(), c.getZ());
        if (!plots.containsKey(k)) return false;
        plots.remove(k);
        saveToConfig();
        return true;
    }

    /**
     * Transfer ownership of an existing plot. Because Plot.owner is immutable,
     * we re-create a Plot and migrate settings & trusted.
     */
    public boolean transferOwnership(Plot plot, UUID newOwner) {
        if (plot == null || newOwner == null) return false;

        // Remove old
        String oldKey = key(plot.getWorldName(), plot.getChunkX(), plot.getChunkZ());
        PlotSettings oldSettings = plot.getSettings();
        Map<UUID, ClaimRole> oldTrusted = new HashMap<>(plot.getTrustedPlayers());

        plots.remove(oldKey);

        // Create new
        Plot np = new Plot(newOwner, plot.getWorldName(), plot.getChunkX(), plot.getChunkZ());

        // Copy settings
        PlotSettings ns = np.getSettings();
        ns.copyFrom(oldSettings);

        // Copy trusted
        for (Map.Entry<UUID, ClaimRole> e : oldTrusted.entrySet()) {
            // if they were the new owner previously trusted, it's fine to keep or you may choose to clear
            np.trustPlayer(e.getKey(), e.getValue());
        }

        // Put new in the same key
        plots.put(oldKey, np);
        saveToConfig();
        return true;
    }

    public boolean transferOwnership(Plot plot, String targetNameOrUuid) {
        UUID target = parseUuidOrResolve(targetNameOrUuid);
        return target != null && transferOwnership(plot, target);
    }

    /* =========================================================
     * Expiry / Maintenance
     * ========================================================= */

    /**
     * Purge claims whose owner hasn't played for `days` days.
     * Returns number of candidates (dryRun=true) or number purged (dryRun=false).
     */
    public int purgeExpired(int days, boolean dryRun) {
        if (days <= 0) return 0;

        long cutoff = System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L);

        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, Plot> e : plots.entrySet()) {
            UUID owner = e.getValue().getOwner();
            OfflinePlayer op = Bukkit.getOfflinePlayer(owner);
            long last = (op != null) ? op.getLastPlayed() : 0L;
            // last == 0 means never joined (some hosts return 0); treat as expired
            if (last == 0L || last < cutoff) {
                toRemove.add(e.getKey());
            }
        }

        if (dryRun) {
            return toRemove.size();
        }

        for (String k : toRemove) {
            plots.remove(k);
        }
        if (!toRemove.isEmpty()) saveToConfig();
        return toRemove.size();
    }

    /* =========================================================
     * Helpers
     * ========================================================= */

    public static String key(String world, int x, int z) {
        return world + ":" + x + "," + z;
    }

    public static String key(Chunk c) {
        return key(c.getWorld().getName(), c.getX(), c.getZ());
    }

    public Optional<Plot> findByKey(String key) {
        return Optional.ofNullable(plots.get(key));
    }

    public Optional<Plot> findByLocation(Location loc) {
        return Optional.ofNullable(getClaim(loc));
    }

    public Optional<Plot> findByChunk(Chunk c) {
        return Optional.ofNullable(getPlot(c));
    }

    /* =========================================================
     * Utilities for commands/listeners to name owners safely
     * ========================================================= */

    public String ownerDisplayName(UUID owner) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(owner);
        return (op != null && op.getName() != null) ? op.getName() : owner.toString();
    }

    public String describe(Location loc) {
        World w = loc.getWorld();
        Chunk c = loc.getChunk();
        return (w != null ? w.getName() : "world") + "@" + c.getX() + "," + c.getZ();
    }
}
