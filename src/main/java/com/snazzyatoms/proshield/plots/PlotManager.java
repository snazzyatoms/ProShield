package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.stream.Collectors;

/** Claim storage + logic */
public class PlotManager {

    private final ProShield plugin;

    private final Map<String, Claim> claims = new HashMap<>();
    // quick owner -> count cache
    private final Map<UUID, Integer> ownerCounts = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadAll();
    }

    /* -------------------- Keys -------------------- */

    private String key(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getChunk().getX() + ":" + loc.getChunk().getZ();
    }

    /* -------------------- CRUD -------------------- */

    public boolean createClaim(UUID owner, Location loc) {
        String k = key(loc);
        if (claims.containsKey(k)) return false;

        // claim limits
        int max = plugin.getConfig().getInt("limits.max-claims", -1);
        if (max >= 0 && !plugin.getServer().getPlayer(owner).hasPermission("proshield.unlimited")) {
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

    /* -------------------- Trust -------------------- */

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

    /* -------------------- Info -------------------- */

    public String ownerName(UUID uuid) {
        var op = Bukkit.getOfflinePlayer(uuid);
        return op.getName() != null ? op.getName() : uuid.toString();
    }

    /* -------------------- Persistence -------------------- */

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

                // trusted
                List<String> t = sec.getStringList("trusted");
                if (t != null) for (String s : t) c.getTrusted().add(UUID.fromString(s));

                claims.put(k, c);
                ownerCounts.put(owner, ownerCounts.getOrDefault(owner, 0) + 1);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load claim: " + k + " -> " + e.getMessage());
            }
        }
    }

    public void saveAll() {
        plugin.getConfig().set("claims", null);
        for (Claim c : claims.values()) saveClaim(c);
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

    /* -------------------- Counts -------------------- */

    public int getClaimCount() { return claims.size(); }
    public int getOwnerCount(UUID uuid) { return ownerCounts.getOrDefault(uuid, 0); }
}
