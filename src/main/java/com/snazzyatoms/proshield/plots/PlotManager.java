// path: src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
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
    private ClaimRoleManager roleManager;

    private final Map<String, Claim> claims = new HashMap<>();
    private final Map<UUID, Integer> ownerCounts = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadAll();
    }

    public void setRoleManager(ClaimRoleManager roleManager) {
        this.roleManager = roleManager;
    }

    private String key(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getChunk().getX() + ":" + loc.getChunk().getZ();
    }

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

    // ===== roles helpers =====
    public ClaimRole getRoleAt(Location loc, UUID player) {
        if (isOwner(player, loc)) return ClaimRole.CO_OWNER;
        if (roleManager == null) return ClaimRole.VISITOR;
        return roleManager.getRole(loc, player);
    }

    public boolean hasRoleAtLeast(Location loc, UUID player, ClaimRole required) {
        if (isOwner(player, loc)) return true;
        return getRoleAt(loc, player).atLeast(required);
    }

    public Optional<Claim> getClaim(Location loc) {
        return Optional.ofNullable(claims.get(key(loc)));
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
        var op = Bukkit.getOfflinePlayer(uuid);
        return op.getName() != null ? op.getName() : uuid.toString();
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

    public int getClaimCount() { return claims.size(); }
    public int getOwnerCount(UUID uuid) { return ownerCounts.getOrDefault(uuid, 0); }

    public Set<String> getAllClaimKeys() { return Collections.unmodifiableSet(claims.keySet()); }

    public Location keyToCenter(String key) {
        try {
            String[] parts = key.split(":");
            String world = parts[0];
            int cx = Integer.parseInt(parts[1]);
            int cz = Integer.parseInt(parts[2]);
            var w = Bukkit.getWorld(world);
            if (w == null) return null;
            int x = (cx << 4) + 8;
            int z = (cz << 4) + 8;
            int y = Math.max(w.getHighestBlockYAt(x, z), 64);
            return new Location(w, x, y, z);
        } catch (Exception ignored) { return null; }
    }

    // Reload from config
    public void reloadFromConfig() {
        loadAll();
    }

    /** Transfer claim ownership (owner→newOwner) for the claim at loc. */
    public boolean transferOwnership(UUID fromOwner, Location loc, UUID toOwner) {
        String k = key(loc);
        Claim c = claims.get(k);
        if (c == null) return false;
        if (!c.getOwner().equals(fromOwner)) return false;

        // update counts
        ownerCounts.put(fromOwner, Math.max(0, ownerCounts.getOrDefault(fromOwner, 1) - 1));
        ownerCounts.put(toOwner, ownerCounts.getOrDefault(toOwner, 0) + 1);

        // set and persist
        c.setOwner(toOwner);
        saveClaim(c);
        return true;
    }

    // Expiry: removes claims whose owners haven't joined in N days
    /** @param commit true = actually delete; false = preview only (count) */
    public int cleanupExpiredClaims(int days, boolean commit) {
        long cutoff = System.currentTimeMillis() - days * 24L * 60L * 60L * 1000L;
        List<String> toRemove = new ArrayList<>();

        for (Map.Entry<String, Claim> e : claims.entrySet()) {
            String k = e.getKey();
            Claim c = e.getValue();
            UUID owner = c.getOwner();
            OfflinePlayer op = Bukkit.getOfflinePlayer(owner);
            long last = Math.max(
                    // handle API differences gracefully
                    safe(op.getLastPlayed()),
                    safe(op.getFirstPlayed())
            );
            if (last == 0L) continue; // no data, skip
            if (last < cutoff) toRemove.add(k);
        }

        if (!commit) return toRemove.size();

        for (String k : toRemove) {
            Claim c = claims.remove(k);
            if (c != null) {
                ownerCounts.put(c.getOwner(), Math.max(0, ownerCounts.getOrDefault(c.getOwner(), 1) - 1));
                removeClaimFromConfig(k);
            }
        }
        plugin.saveConfig();
        return toRemove.size();
    }

    private long safe(long val) { return val < 0 ? 0 : val; }
}
