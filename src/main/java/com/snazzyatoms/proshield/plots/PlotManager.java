// path: src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles all claim storage and lookup logic for ProShield.
 * - Stores claims in-memory (map) and persists them in config.yml under "claims".
 * - Supports trusted players per claim.
 * - Provides small helpers used by listeners/GUI (owner names, claim center, etc).
 * - 1.2 additions: getByKey(), isWithinRaidShield(), cleanupExpiredClaims(), reloadFromConfig().
 */
public class PlotManager {

    private final ProShield plugin;

    /** Key = "world:chunkX:chunkZ" -> Claim */
    private final Map<String, Claim> claims = new HashMap<>();

    /** Owner UUID -> number of owned chunks (for max-claims checks) */
    private final Map<UUID, Integer> ownerCounts = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadAll();
    }

    /* ------------- Keys & Basics ------------- */

    private String key(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getChunk().getX() + ":" + loc.getChunk().getZ();
    }

    /** Expose by key (used by roles manager / admin panels) */
    public Optional<Claim> getByKey(String key) {
        return Optional.ofNullable(claims.get(key));
    }

    /* ------------- Create / Remove ------------- */

    /**
     * Create a claim for the given player at the given location (chunk-based).
     * Respects limits.max-claims unless player has "proshield.unlimited".
     */
    public boolean createClaim(UUID owner, Location loc) {
        String k = key(loc);
        if (claims.containsKey(k)) return false;

        int max = plugin.getConfig().getInt("limits.max-claims", -1);
        if (max >= 0) {
            var player = plugin.getServer().getPlayer(owner);
            boolean bypass = player != null && player.hasPermission("proshield.unlimited");
            if (!bypass) {
                int used = ownerCounts.getOrDefault(owner, 0);
                if (used >= max) return false;
            }
        }

        Claim c = new Claim(owner,
                loc.getWorld().getName(),
                loc.getChunk().getX(),
                loc.getChunk().getZ(),
                System.currentTimeMillis());

        claims.put(k, c);
        ownerCounts.put(owner, ownerCounts.getOrDefault(owner, 0) + 1);
        saveClaim(c);
        return true;
    }

    /**
     * Remove a claim in the current chunk.
     * @param requester who is attempting removal
     * @param loc chunk location
     * @param adminForce if true, bypasses owner check
     */
    public boolean removeClaim(UUID requester, Location loc, boolean adminForce) {
        String k = key(loc);
        Claim c = claims.get(k);
        if (c == null) return false;
        if (!adminForce && !Objects.equals(c.getOwner(), requester)) return false;

        claims.remove(k);
        ownerCounts.put(c.getOwner(), Math.max(0, ownerCounts.getOrDefault(c.getOwner(), 1) - 1));
        removeClaimFromConfig(k);
        return true;
    }

    /* ------------- Queries ------------- */

    public boolean isClaimed(Location loc) { return claims.containsKey(key(loc)); }

    public boolean isOwner(UUID uuid, Location loc) {
        Claim c = claims.get(key(loc));
        return c != null && c.getOwner().equals(uuid);
    }

    public boolean isTrustedOrOwner(UUID uuid, Location loc) {
        Claim c = claims.get(key(loc));
        if (c == null) return false;
        return c.getOwner().equals(uuid) || c.getTrusted().contains(uuid);
    }

    public Optional<Claim> getClaim(Location loc) {
        return Optional.ofNullable(claims.get(key(loc)));
    }

    /* ------------- Trust List ------------- */

    public boolean trust(UUID owner, Location loc, UUID target) {
        Claim c = claims.get(key(loc));
        if (c == null || !c.getOwner().equals(owner)) return false;
        if (c.getTrusted().add(target)) {
            saveClaim(c);
            return true;
        }
        return false;
    }

    public boolean untrust(UUID owner, Location loc, UUID target) {
        Claim c = claims.get(key(loc));
        if (c == null || !c.getOwner().equals(owner)) return false;
        if (c.getTrusted().remove(target)) {
            saveClaim(c);
            return true;
        }
        return false;
    }

    public List<String> listTrusted(Location loc) {
        Claim c = claims.get(key(loc));
        if (c == null) return Collections.emptyList();
        return c.getTrusted().stream()
                .map(uuid -> {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                    return (op.getName() != null ? op.getName() : uuid.toString());
                })
                .collect(Collectors.toList());
    }

    public String ownerName(UUID uuid) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        return op.getName() != null ? op.getName() : uuid.toString();
    }

    /* ------------- Load / Save ------------- */

    private void loadAll() {
        claims.clear();
        ownerCounts.clear();

        ConfigurationSection root = plugin.getConfig().getConfigurationSection("claims");
        if (root == null) return;

        for (String k : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(k);
            if (sec == null) continue;
            try {
                UUID owner = UUID.fromString(sec.getString("owner"));
                String world = sec.getString("world");
                int cx = sec.getInt("chunkX");
                int cz = sec.getInt("chunkZ");
                long created = sec.getLong("createdAt", System.currentTimeMillis());

                Claim c = new Claim(owner, world, cx, cz, created);

                List<String> t = sec.getStringList("trusted");
                if (t != null) for (String s : t) {
                    try { c.getTrusted().add(UUID.fromString(s)); } catch (Exception ignored) {}
                }

                claims.put(k, c);
                ownerCounts.put(owner, ownerCounts.getOrDefault(owner, 0) + 1);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load claim: " + k + " -> " + e.getMessage());
            }
        }
    }

    public void saveAll() {
        plugin.getConfig().set("claims", null);
        for (Claim c : claims.values()) {
            saveClaim(c);
        }
        plugin.saveConfig();
    }

    private void saveClaim(Claim c) {
        String k = c.key();
        String path = "claims." + k + ".";
        plugin.getConfig().set(path + "owner", c.getOwner().toString());
        plugin.getConfig().set(path + "world", c.getWorld());
        plugin.getConfig().set(path + "chunkX", c.getChunkX());
        plugin.getConfig().set(path + "chunkZ", c.getChunkZ());
        plugin.getConfig().set(path + "createdAt", c.getCreatedAt());

        List<String> t = c.getTrusted().stream().map(UUID::toString).collect(Collectors.toList());
        plugin.getConfig().set(path + "trusted", t);
        plugin.saveConfig();
    }

    private void removeClaimFromConfig(String key) {
        plugin.getConfig().set("claims." + key, null);
        plugin.saveConfig();
    }

    /* ------------- Stats / Helpers ------------- */

    public int getClaimCount() { return claims.size(); }

    public int getOwnerCount(UUID uuid) { return ownerCounts.getOrDefault(uuid, 0); }

    /** Unmodifiable set of claim keys (e.g., "world:chunkX:chunkZ"). */
    public Set<String> getAllClaimKeys() {
        return Collections.unmodifiableSet(claims.keySet());
    }

    /** Convert a claim key into the *center* of that chunk, at a safe Y. */
    public Location keyToCenter(String key) {
        try {
            String[] parts = key.split(":");
            String worldName = parts[0];
            int cx = Integer.parseInt(parts[1]);
            int cz = Integer.parseInt(parts[2]);
            World w = Bukkit.getWorld(worldName);
            if (w == null) return null;
            int x = (cx << 4) + 8;
            int z = (cz << 4) + 8;
            int y = Math.max(w.getHighestBlockYAt(x, z), 64);
            return new Location(w, x + 0.5, y, z + 0.5);
        } catch (Exception ignored) { return null; }
    }

    /** Reload in-memory cache from config (used by /proshield reload). */
    public void reloadFromConfig() {
        loadAll();
    }

    /* ------------- 1.2 QoL: Raid Shield & Expiry ------------- */

    /**
     * @return true if the claim at 'loc' is younger than 'shieldSeconds' (under raid-protection).
     */
    public boolean isWithinRaidShield(Location loc, int shieldSeconds) {
        Claim c = getClaim(loc).orElse(null);
        if (c == null) return false;
        long ageSec = (System.currentTimeMillis() - c.getCreatedAt()) / 1000L;
        return ageSec < Math.max(0, shieldSeconds);
    }

    /**
     * Expiry maintenance. If reviewOnly is true, we only *count* candidates
     * (you can log/mark for admin review). If false, we remove them.
     * Currently uses creation time as a heuristic—feel free to replace with real last-seen.
     *
     * @return number of claims removed when reviewOnly=false, otherwise 0.
     */
    public int cleanupExpiredClaims(int days, boolean reviewOnly) {
        long cutoff = System.currentTimeMillis() - days * 86_400_000L;
        List<String> toRemove = new ArrayList<>();
        int candidates = 0;

        for (var e : claims.entrySet()) {
            Claim c = e.getValue();
            if (c.getCreatedAt() < cutoff) {
                candidates++;
                if (!reviewOnly) toRemove.add(e.getKey());
            }
        }

        if (!reviewOnly) {
            for (String k : toRemove) {
                Claim c = claims.remove(k);
                if (c != null) {
                    ownerCounts.put(c.getOwner(), Math.max(0, ownerCounts.getOrDefault(c.getOwner(), 1) - 1));
                    removeClaimFromConfig(k);
                }
            }
            if (!toRemove.isEmpty()) plugin.saveConfig();
            return toRemove.size();
        }

        // If review-only, we didn't delete; return 0 removed (but you could log 'candidates')
        return 0;
    }
}
