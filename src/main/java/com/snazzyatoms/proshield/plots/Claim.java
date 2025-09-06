package com.snazzyatoms.proshield.plots;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class Claim {

    private final UUID owner;
    private final String world;
    private final int x;
    private final int z;
    private final int radius = 16; // claim size around the player

    public Claim(UUID owner, String world, int x, int z) {
        this.owner = owner;
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public boolean isInside(Location loc) {
        World w = Bukkit.getWorld(world);
        if (w == null || !loc.getWorld().equals(w)) return false;

        return Math.abs(loc.getBlockX() - x) <= radius &&
               Math.abs(loc.getBlockZ() - z) <= radius;
    }
}
