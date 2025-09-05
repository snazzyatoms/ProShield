package com.snazzyatoms.proshield.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotManager {

    private final Map<UUID, Claim> claims = new HashMap<>();

    public static class Claim {
        public final UUID owner;
        public final String world;
        public final int x, z;
        public final int radius;

        public Claim(UUID owner, String world, int x, int z, int radius) {
            this.owner = owner;
            this.world = world;
            this.x = x;
            this.z = z;
            this.radius = radius;
        }
    }

    public boolean createClaim(Player player, Location loc, int radius) {
        UUID id = player.getUniqueId();
        if (claims.containsKey(id)) {
            player.sendMessage("§cYou already own a claim!");
            return false;
        }
        Claim c = new Claim(id, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockZ(), radius);
        claims.put(id, c);
        player.sendMessage("§aClaim created at " + loc.getBlockX() + ", " + loc.getBlockZ() + " with radius " + radius);
        return true;
    }

    public String getClaimInfo(Player player) {
        Claim c = claims.get(player.getUniqueId());
        if (c == null) return "§cYou do not own a claim.";
        return "§aClaim at " + c.x + ", " + c.z + " in " + c.world + " radius " + c.radius;
    }

    public boolean removeClaim(Player player) {
        if (claims.remove(player.getUniqueId()) != null) {
            player.sendMessage("§cYour claim has been removed.");
            return true;
        }
        player.sendMessage("§cYou do not own a claim.");
        return false;
    }

    public boolean canBuild(Player player, Location loc) {
        Claim c = claims.get(player.getUniqueId());
        if (c == null) return true;

        if (!c.world.equals(loc.getWorld().getName())) return true;

        double dx = loc.getBlockX() - c.x;
        double dz = loc.getBlockZ() - c.z;
        double distanceSquared = dx * dx + dz * dz;

        return distanceSquared <= (c.radius * c.radius);
    }
}
