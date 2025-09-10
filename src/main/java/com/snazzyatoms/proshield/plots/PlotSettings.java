package com.snazzyatoms.proshield.plots;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds per-plot toggleable settings (flags).
 *
 * Preserves all prior logic, extended to include
 * missing flags like interactions, item frames,
 * armor stands, animals, pets, containers, vehicles.
 */
public class PlotSettings {

    private boolean pvpEnabled = false;
    private boolean explosionsAllowed = false;
    private boolean fireAllowed = false;
    private boolean entityGriefingAllowed = false;
    private boolean redstoneAllowed = true;
    private boolean interactionsAllowed = true;

    private boolean itemFramesAllowed = true;
    private boolean armorStandsAllowed = true;
    private boolean containersAllowed = true;
    private boolean animalAccessAllowed = true;
    private boolean petAccessAllowed = true;
    private boolean vehiclesAllowed = true;

    /* -------------------------------------------------------
     * Getters / Setters
     * ------------------------------------------------------- */

    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean value) { this.pvpEnabled = value; }

    public boolean isExplosionsAllowed() { return explosionsAllowed; }
    public void setExplosionsAllowed(boolean value) { this.explosionsAllowed = value; }

    public boolean isFireAllowed() { return fireAllowed; }
    public void setFireAllowed(boolean value) { this.fireAllowed = value; }

    public boolean isEntityGriefingAllowed() { return entityGriefingAllowed; }
    public void setEntityGriefingAllowed(boolean value) { this.entityGriefingAllowed = value; }

    public boolean isRedstoneAllowed() { return redstoneAllowed; }
    public void setRedstoneAllowed(boolean value) { this.redstoneAllowed = value; }

    public boolean isInteractionsAllowed() { return interactionsAllowed; }
    public void setInteractionsAllowed(boolean value) { this.interactionsAllowed = value; }

    public boolean isItemFramesAllowed() { return itemFramesAllowed; }
    public void setItemFramesAllowed(boolean value) { this.itemFramesAllowed = value; }

    public boolean isArmorStandsAllowed() { return armorStandsAllowed; }
    public void setArmorStandsAllowed(boolean value) { this.armorStandsAllowed = value; }

    public boolean isContainersAllowed() { return containersAllowed; }
    public void setContainersAllowed(boolean value) { this.containersAllowed = value; }

    public boolean isAnimalAccessAllowed() { return animalAccessAllowed; }
    public void setAnimalAccessAllowed(boolean value) { this.animalAccessAllowed = value; }

    public boolean isPetAccessAllowed() { return petAccessAllowed; }
    public void setPetAccessAllowed(boolean value) { this.petAccessAllowed = value; }

    public boolean isVehiclesAllowed() { return vehiclesAllowed; }
    public void setVehiclesAllowed(boolean value) { this.vehiclesAllowed = value; }

    /* -------------------------------------------------------
     * Serialization
     * ------------------------------------------------------- */

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("pvp", pvpEnabled);
        data.put("explosions", explosionsAllowed);
        data.put("fire", fireAllowed);
        data.put("entity-griefing", entityGriefingAllowed);
        data.put("redstone", redstoneAllowed);
        data.put("interactions", interactionsAllowed);

        data.put("item-frames", itemFramesAllowed);
        data.put("armor-stands", armorStandsAllowed);
        data.put("containers", containersAllowed);
        data.put("animals", animalAccessAllowed);
        data.put("pets", petAccessAllowed);
        data.put("vehicles", vehiclesAllowed);

        return data;
    }

    public void deserialize(ConfigurationSection sec) {
        if (sec == null) return;

        this.pvpEnabled = sec.getBoolean("pvp", pvpEnabled);
        this.explosionsAllowed = sec.getBoolean("explosions", explosionsAllowed);
        this.fireAllowed = sec.getBoolean("fire", fireAllowed);
        this.entityGriefingAllowed = sec.getBoolean("entity-griefing", entityGriefingAllowed);
        this.redstoneAllowed = sec.getBoolean("redstone", redstoneAllowed);
        this.interactionsAllowed = sec.getBoolean("interactions", interactionsAllowed);

        this.itemFramesAllowed = sec.getBoolean("item-frames", itemFramesAllowed);
        this.armorStandsAllowed = sec.getBoolean("armor-stands", armorStandsAllowed);
        this.containersAllowed = sec.getBoolean("containers", containersAllowed);
        this.animalAccessAllowed = sec.getBoolean("animals", animalAccessAllowed);
        this.petAccessAllowed = sec.getBoolean("pets", petAccessAllowed);
        this.vehiclesAllowed = sec.getBoolean("vehicles", vehiclesAllowed);
    }
}
