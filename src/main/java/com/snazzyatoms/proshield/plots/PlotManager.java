package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.stream.Collectors;

/** Claim storage + logic */
public class PlotManager {

    public enum ClaimResult {
        SUCCESS,
        ALREADY_CLAIMED,
        LIMIT_REACHED,
        SPAWN_PROTECTED,
        WORLD_OR_DATA_INVALID
    }

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

    /* ===== Spawn-protection helpers ===== */

    public boolean isSpawnProtectionEnabled(World world) {
        if (world == null) return false;
        return plugin.getConfig().getBoolean("no-claim.spawn.enabled", true);
    }

    public int spawnRadiusBlocks(World world) {
        if (world == null) return 0;
        return Math.max(0, plugin.getConfig().getInt("no-claim.spawn.radius-blocks", 96));
    }

    /** Returns true if the given location is inside the protected radius around the world spawn. */
    public boolean isInsideSpawnProtected(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        if (!isSpawnProtectionEnabled(loc.getWorld())) return false;

        Location spawn = loc.getWorld().getSpawnLocation();
        if (!Objects.equals(spawn.getWorld(), loc.getWorld())) return false;

        int radius = spawnRadiusBlocks(loc.getWorld());
        if (radius <= 0) return false;

        // Horizontal distance check
        double dx = loc.getX() - spawn.getX();
        double dz = loc.getZ() - spawn.getZ();
        double dist2 = dx * dx + dz * dz;
        return dist2 <= (double) radius * radius;
    }

    /** Used by commands/GUI to know if a player is allowed to claim at a location. */
    public boolean canClaimHere(UUID player, Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        if (!isInsideSpawnProtected(loc)) return true;

        // Allow explicit bypass via permission
        if (player != null) {
            var p = plugin.getServer().getPlayer(player);
            if (p != null && p.hasPermission("proshield.admin.spawnoverride")) {
                return true;
            }
        }
        return false;
    }

    /* ===== Claim core ===== */

    public boolean createClaim(UUID owner, Location loc) {
        return createClaimDetailed(owner, loc) == ClaimResult.SUCCESS;
    }

    /** New: detailed result for better UX messaging. */
    public ClaimResult createClaimDetailed(UUID owner, Location loc) {
        if (owner == null || loc == null || loc.getWorld() == null) return ClaimResult.WORLD_OR_DATA_INVALID;

        // Spawn protection
        if (!canClaimHere(owner, loc)) return ClaimResult.SPAWN_PROTECTED;

        String k = key(loc);
        if (claims.containsKey(k)) return ClaimResult.ALREADY_CLAIMED;

        int max = plugin.getConfig().getInt("limits.max-claims", -1);
        if (max >= 0) {
            var player = plugin.getServer().getPlayer(owner);
            boolean bypass = player != null && player.hasPermission("proshield.unlimited");
            if (!bypass) {
                int used = ownerCounts.getOrDefault(owner, 0);
                if (used >= max) return ClaimResult.LIMIT_REACHED;
            }
        }

        Claim c = new Claim(owner, loc.getWorld().getName(),
                loc.getChunk().getX(), loc.getChunk().getZ(), System.currentTimeMillis());

        claims.put(k, c);
        ownerCounts.put(owner, ownerCounts.getOrDefault(owner, 0) + 1);
        saveClaim(c);
        return ClaimResult.SUCCESS;
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
        if (isOwner(player, loc)) return ClaimRole.CO_OWNER; // treat owner as highest equivalent
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

    // Expiry: removes claims whose owners haven't joined in N days
    public int cleanupExpiredClaims(int days, boolean dryRun) {
        long cutoff = System.currentTimeMillis() - days * 24L * 60L * 60L * 1000L;
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, Claim> e : claims.entrySet()) {
            UUID owner = e.getValue().getOwner();
            OfflinePlayer op = Bukkit.getOfflinePlayer(owner);
            long last = (op.getLastPlayed() > 0 ? op.getLastPlayed() : op.getFirstPlayed());
            if (last == 0L) continue; // no data, skip
            if (last < cutoff) toRemove.add(e.getKey());
        }
        if (dryRun) return toRemove.size();

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
}
