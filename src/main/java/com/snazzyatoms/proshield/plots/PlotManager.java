package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.stream.Collectors;

public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Claim> claims = new HashMap<>();
    private final Map<UUID, Integer> ownerCounts = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadAll();
    }

    private String key(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getChunk().getX() + ":" + loc.getChunk().getZ();
    }

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

    public int getClaimCount() { return claims.size(); }

    public int getOwnerCount(UUID uuid) { return ownerCounts.getOrDefault(uuid, 0); }

    public Set<String> getAllClaimKeys() { return Collections.unmodifiableSet(claims.keySet()); }

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
        } catch (Exception ignored) { return null; }
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

    public boolean createClaim(UUID owner, Location loc) {
        String k = key(loc);
        if (claims.containsKey(k)) return false;

        int max = plugin.getConfig().getInt("limits.max-claims", -1);

        boolean bypass = false;
        var player = plugin.getServer().getPlayer(owner);
        if (player != null) {
            boolean hasUnlimitedPerm = player.hasPermission("proshield.unlimited");
            boolean adminIncludesUnlimited = plugin.getConfig().getBoolean("permissions.admin-includes-unlimited", false);
            boolean isAdmin = player.hasPermission("proshield.admin");
            bypass = hasUnlimitedPerm || (adminIncludesUnlimited && isAdmin);
        }

        if (max >= 0 && !bypass) {
            int used = ownerCounts.getOrDefault(owner, 0);
            if (used >= max) return false;
        }

        Claim c = new Claim(owner, loc.getWorld().getName(),
                loc.getChunk().getX(), loc.getChunk().getZ(), System.currentTimeMillis());

        claims.put(k, c);
        ownerCounts.put(owner, ownerCounts.getOrDefault(owner, 0) + 1);
        saveClaim(c);
        return true;
    }

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

    public boolean trust(UUID owner, Location loc, UUID target) {
        Claim c = claims.get(key(loc));
        if (c == null || !c.getOwner().equals(owner)) return false;
        if (c.getTrusted().add(target)) { saveClaim(c); return true; }
        return false;
    }

    public boolean untrust(UUID owner, Location loc, UUID target) {
        Claim c = claims.get(key(loc));
        if (c == null || !c.getOwner().equals(owner)) return false;
        if (c.getTrusted().remove(target)) { saveClaim(c); return true; }
        return false;
    }

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

    public void reloadFromConfig() { loadAll(); }

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

    // ===== Expiry + archival =====

    public int cleanupExpiredClaims(int days) {
        if (days <= 0) return 0;
        final long now = System.currentTimeMillis();
        final long thresholdMs = 24L * 60L * 60L * 1000L * days;

        int archived = 0;
        List<Map.Entry<String, Claim>> toArchive = new ArrayList<>();

        for (Map.Entry<String, Claim> e : claims.entrySet()) {
            Claim c = e.getValue();
            UUID owner = c.getOwner();

            if (Bukkit.getPlayer(owner) != null) continue;

            OfflinePlayer op = Bukkit.getOfflinePlayer(owner);
            long lastPlayed = op.getLastPlayed();
            if (lastPlayed <= 0L) continue;

            long inactiveFor = now - lastPlayed;
            if (inactiveFor >= thresholdMs) toArchive.add(e);
        }

        for (Map.Entry<String, Claim> e : toArchive) {
            String k = e.getKey();
            Claim c = e.getValue();

            archiveClaim(c, Bukkit.getOfflinePlayer(c.getOwner()).getLastPlayed(), System.currentTimeMillis());

            claims.remove(k);
            ownerCounts.put(c.getOwner(), Math.max(0, ownerCounts.getOrDefault(c.getOwner(), 1) - 1));
            removeClaimFromConfig(k);
            archived++;
        }

        if (archived > 0) {
            plugin.saveConfig();
            plugin.getLogger().info("Archived " + archived + " expired claim(s).");
        }
        return archived;
    }

    private void archiveClaim(Claim c, long lastPlayed, long removedAt) {
        String base = "claims_expired." + c.key() + ".";
        plugin.getConfig().set(base + "owner", c.getOwner().toString());
        plugin.getConfig().set(base + "ownerName", ownerName(c.getOwner()));
        plugin.getConfig().set(base + "world", c.getWorld());
        plugin.getConfig().set(base + "chunkX", c.getChunkX());
        plugin.getConfig().set(base + "chunkZ", c.getChunkZ());
        plugin.getConfig().set(base + "createdAt", c.getCreatedAt());
        plugin.getConfig().set(base + "trusted", c.getTrusted().stream().map(UUID::toString).collect(Collectors.toList()));
        plugin.getConfig().set(base + "lastPlayed", lastPlayed);
        plugin.getConfig().set(base + "removedAt", removedAt);
    }

    public boolean restoreExpiredClaim(String key) {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("claims_expired." + key);
        if (sec == null) return false;
        if (claims.containsKey(key)) return false;

        try {
            UUID owner = UUID.fromString(sec.getString("owner"));
            String world = sec.getString("world");
            int cx = sec.getInt("chunkX");
            int cz = sec.getInt("chunkZ");
            long createdAt = sec.getLong("createdAt", System.currentTimeMillis());

            Claim c = new Claim(owner, world, cx, cz, createdAt);
            List<String> t = sec.getStringList("trusted");
            if (t != null) for (String s : t) {
                try { c.getTrusted().add(UUID.fromString(s)); } catch (IllegalArgumentException ignored) {}
            }

            claims.put(key, c);
            ownerCounts.put(owner, ownerCounts.getOrDefault(owner, 0) + 1);
            saveClaim(c);

            plugin.getConfig().set("claims_expired." + key, null);
            plugin.saveConfig();

            return true;
        } catch (Exception ex) {
            plugin.getLogger().warning("Failed to restore expired claim: " + key + " -> " + ex.getMessage());
            return false;
        }
    }
}
