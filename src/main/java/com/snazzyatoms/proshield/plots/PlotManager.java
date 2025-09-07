package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Claim storage + logic with robust expiry handling for offline players.
 *
 * Stores claims both in-memory and under config path: claims.<key>...
 * Key format: world:chunkX:chunkZ
 *
 * Features:
 *  - create/remove claims with max-claims limit & admin force remove
 *  - trust / untrust users per claim
 *  - owner/trusted checks & lookups
 *  - list trusted, owner display name
 *  - reload from config & save all
 *  - expiry cleanup based on OfflinePlayer last played (safe)
 */
public class PlotManager {

    private final ProShield plugin;

    // In-memory cache
    private final Map<String, Claim> claims = new HashMap<>();
    private final Map<UUID, Integer> ownerCounts = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadAll();
    }

    // ------------------------------------------------------------------------
    // Keys & lookups
    // ------------------------------------------------------------------------

    private String key(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getChunk().getX() + ":" + loc.getChunk().getZ();
    }

    private String key(String world, int chunkX, int chunkZ) {
        return world + ":" + chunkX + ":" + chunkZ;
    }

    public boolean isClaimed(Location loc) {
        return claims.containsKey(key(loc));
    }

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

    public int getClaimCount() {
        return claims.size();
    }

    public int getOwnerCount(UUID uuid) {
        return ownerCounts.getOrDefault(uuid, 0);
    }

    public Set<String> getAllClaimKeys() {
        return Collections.unmodifiableSet(claims.keySet());
    }

    public Location keyToCenter(String key) {
        try {
            String[] parts = key.split(":");
            String world = parts[0];
            int cx = Integer.parseInt(parts[1]);
            int cz = Integer.parseInt(parts[2]);
            World w = Bukkit.getWorld(world);
            if (w == null) return null;
            int x = (cx << 4) + 8;
            int z = (cz << 4) + 8;
            int y = Math.max(w.getHighestBlockYAt(x, z), 64);
            return new Location(w, x, y, z);
        } catch (Exception ignored) {
            return null;
        }
    }

    public String ownerName(UUID uuid) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        return (op.getName() != null ? op.getName() : uuid.toString());
    }

    public List<String> listTrusted(Location loc) {
        Claim c = claims.get(key(loc));
        if (c == null) return Collections.emptyList();
        return c.getTrusted().stream()
                .map(u -> {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(u);
                    return (op.getName() != null ? op.getName() : u.toString());
                })
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------------
    // Create / Remove
    // ------------------------------------------------------------------------

    public boolean createClaim(UUID owner, Location loc) {
        String k = key(loc);
        if (claims.containsKey(k)) return false;

        int max = plugin.getConfig().getInt("limits.max-claims", -1);
        if (max >= 0) {
            boolean bypass = Optional.ofNullable(plugin.getServer().getPlayer(owner))
                    .map(p -> p.hasPermission("proshield.unlimited"))
                    .orElse(false);
            if (!bypass) {
                int used = ownerCounts.getOrDefault(owner, 0);
                if (used >= max) return false;
            }
        }

        Claim c = new Claim(owner, loc.getWorld().getName(),
                loc.getChunk().getX(), loc.getChunk().getZ(), System.currentTimeMillis());

        claims.put(k, c);
        ownerCounts.put(owner, ownerCounts.getOrDefault(owner, 0) + 1);
        saveClaim(c);
        return true;
    }

    /**
     * Removes a claim at loc. If adminForce is true, requester doesn't need to be owner.
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

    // ------------------------------------------------------------------------
    // Trust system
    // ------------------------------------------------------------------------

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

    // ------------------------------------------------------------------------
    // Persistence
    // ------------------------------------------------------------------------

    public void saveAll() {
        plugin.getConfig().set("claims", null);
        for (Claim c : claims.values()) saveClaim(c);
        plugin.saveConfig();
    }

    private void saveClaim(Claim c) {
        String path = "claims." + c.key() + ".";
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

    /** Reload in-memory cache from config (used by /proshield reload). */
    public void reloadFromConfig() {
        loadAll();
    }

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
                    try { c.getTrusted().add(UUID.fromString(s)); } catch (IllegalArgumentException ignored) {}
                }

                claims.put(k, c);
                ownerCounts.put(owner, ownerCounts.getOrDefault(owner, 0) + 1);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load claim: " + k + " -> " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------------
    // Expiry (inactive owner cleanup)
    // ------------------------------------------------------------------------

    /**
     * Remove claims whose owners have been inactive for at least 'days' (and are not online).
     * Uses OfflinePlayer#getLastPlayed (milliseconds since epoch) safely and handles edge cases:
     *  - If player is online -> never expires
     *  - If player never joined / lastPlayed == 0 -> does NOT expire (safer default)
     *  - Now - lastPlayed >= days -> expires
     *
     * @return number of claims removed
     */
    public int cleanupExpiredClaims(int days) {
        if (days <= 0) return 0;

        final long now = System.currentTimeMillis();
        final long thresholdMs = daysToMillis(days);

        int removed = 0;
        Iterator<Map.Entry<String, Claim>> it = claims.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, Claim> entry = it.next();
            Claim c = entry.getValue();
            UUID owner = c.getOwner();

            // If owner is online, skip
            if (Bukkit.getPlayer(owner) != null) continue;

            OfflinePlayer op = Bukkit.getOfflinePlayer(owner);

            // If we cannot determine last played, be conservative and skip
            long lastPlayed = op.getLastPlayed(); // 0 if unknown
            if (lastPlayed <= 0L) continue;

            long inactiveFor = now - lastPlayed;
            if (inactiveFor >= thresholdMs) {
                // Expire this claim
                it.remove();
                ownerCounts.put(owner, Math.max(0, ownerCounts.getOrDefault(owner, 1) - 1));
                removeClaimFromConfig(c.key());
                removed++;
            }
        }

        if (removed > 0) {
            plugin.getLogger().info("Expired " + removed + " claim(s) due to owner inactivity >= " + days + " day(s).");
        }
        return removed;
    }

    private long daysToMillis(int days) {
        // Avoid overflow for large values
        return Math.multiplyExact(24L * 60L * 60L * 1000L, days);
    }
}
