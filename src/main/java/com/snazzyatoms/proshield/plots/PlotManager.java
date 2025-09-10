package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Monster;

import java.util.*;

public class PlotManager {
    private final ProShield plugin;
    private final Map<UUID, List<Claim>> playerClaims = new HashMap<>();
    private final Map<String, Claim> claimsByChunk = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    public ProShield getPlugin() {
        return plugin;
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    // ==========================
    // Claim Management
    // ==========================

    public boolean isInsideClaim(Location loc) {
        String key = key(loc);
        return claimsByChunk.containsKey(key);
    }

    public Claim getClaim(Location loc) {
        return claimsByChunk.get(key(loc));
    }

    public String getClaimName(Location loc) {
        Claim claim = getClaim(loc);
        return claim == null ? "Wilderness" : claim.getOwnerName() + "'s Claim";
    }

    public void addClaim(Player owner, Location loc) {
        Claim claim = new Claim(owner.getUniqueId(), owner.getName(), loc.getChunk());
        claimsByChunk.put(key(loc), claim);
        playerClaims.computeIfAbsent(owner.getUniqueId(), k -> new ArrayList<>()).add(claim);
    }

    public void removeClaim(Location loc) {
        Claim claim = claimsByChunk.remove(key(loc));
        if (claim != null) {
            playerClaims.getOrDefault(claim.getOwner(), new ArrayList<>()).remove(claim);
        }
    }

    private String key(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getChunk().getX() + "," + loc.getChunk().getZ();
    }

    // ==========================
    // Build / Interact Permissions
    // ==========================

    public boolean canBuild(Player p, Location loc) {
        Claim claim = getClaim(loc);
        if (claim == null) return true;
        return claim.isTrusted(p.getUniqueId(), "builder") || claim.isOwner(p);
    }

    public boolean canInteract(Player p, org.bukkit.block.Block block) {
        if (block == null) return true;
        Claim claim = getClaim(block.getLocation());
        if (claim == null) return true;
        return claim.isTrusted(p.getUniqueId(), "member") || claim.isOwner(p);
    }

    // ==========================
    // PvP
    // ==========================

    public boolean canPvp(Player attacker, Player victim) {
        Claim claim = getClaim(victim.getLocation());
        if (claim == null) return true;
        boolean globalPvp = getConfig().getBoolean("protection.pvp-in-claims", false);
        if (!globalPvp) return false;
        return claim.getFlags().getOrDefault("pvp", false);
    }

    // ==========================
    // Fire
    // ==========================

    public boolean canIgnite(Location loc, BlockIgniteEvent.IgniteCause cause) {
        Claim claim = getClaim(loc);
        if (claim == null) return true;
        return claim.getFlags().getOrDefault("fire", false);
    }

    // ==========================
    // Explosions
    // ==========================

    public boolean canExplode(Location loc, Entity source) {
        Claim claim = getClaim(loc);
        if (claim == null) return true;
        return claim.getFlags().getOrDefault("explosions", false);
    }

    // ==========================
    // Entity Grief
    // ==========================

    public boolean canEntityGrief(Location loc, Entity entity) {
        Claim claim = getClaim(loc);
        if (claim == null) return true;
        return claim.getFlags().getOrDefault("entity-grief", false);
    }

    // ==========================
    // Spawn Guard
    // ==========================

    public boolean canClaimHere(Location loc) {
        int radius = getConfig().getInt("spawn.radius", 32);
        Location spawn = loc.getWorld().getSpawnLocation();
        return loc.distance(spawn) > radius || plugin.hasAdminBypass(loc.getWorld());
    }

    // ==========================
    // Mob Control
    // ==========================

    public boolean isAllowedMob(LivingEntity mob) {
        if (mob instanceof Player) return true;
        return !(mob instanceof Monster);
    }

    public Location getNearbyClaimBorder(Location loc) {
        Claim claim = getClaim(loc);
        if (claim == null) return null;
        return claim.getCenter();
    }

    // ==========================
    // Debug & Admin
    // ==========================

    public void purgeExpired(int days) {
        long cutoff = System.currentTimeMillis() - (days * 86400000L);
        Iterator<Map.Entry<String, Claim>> it = claimsByChunk.entrySet().iterator();
        while (it.hasNext()) {
            Claim claim = it.next().getValue();
            if (claim.getLastActive() < cutoff) {
                it.remove();
                playerClaims.getOrDefault(claim.getOwner(), new ArrayList<>()).remove(claim);
                Bukkit.getLogger().info("[ProShield] Purged expired claim: " + claim.getOwnerName());
            }
        }
    }
}
