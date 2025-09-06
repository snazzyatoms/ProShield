package com.snazzyatoms.proshield.plots;

import java.util.UUID;
import org.bukkit.Location;

public class Claim {
    private final UUID owner;
    private final Location corner1;
    private final Location corner2;

    public Claim(UUID owner, Location corner1, Location corner2) {
        this.owner = owner;
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    public UUID getOwner() {
        return owner;
    }

    public Location getCorner1() {
        return corner1;
    }

    public Location getCorner2() {
        return corner2;
    }

    public boolean isInside(Location location) {
        if (!location.getWorld().equals(corner1.getWorld())) return false;

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        double minX = Math.min(corner1.getX(), corner2.getX());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());

        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }
}
