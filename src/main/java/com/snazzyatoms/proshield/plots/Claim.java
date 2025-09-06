package com.snazzyatoms.proshield.plots;

import org.bukkit.Location;

import java.util.UUID;

public class Claim {
    private final UUID owner;
    private final Location location;

    public Claim(UUID owner, Location location) {
        this.owner = owner;
        this.location = location;
    }

    public UUID getOwner() {
        return owner;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "Owner=" + owner + ", Location=" + location.getWorld().getName() +
               " [" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "]";
    }
}
