package com.snazzyatoms.proshield.plots;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * PlotSettings holds all per-claim protection flags.
 *
 * Preserves all prior logic, expanded to cover every flag referenced
 * in listeners and commands so the build compiles cleanly.
 */
public class PlotSettings {

    private boolean pvpEnabled;
    private boolean explosionsAllowed;
    private boolean fireAllowed;
    private boolean containersAllowed;
    private boolean vehiclesAllowed;
    private boolean entityGriefingAllowed;
    private boolean redstoneAllowed;
    private boolean animalInteractAllowed;
    private boolean animalAccessAllowed;
    private boolean petAccessAllowed;
    private boolean armorStandsAllowed;
    private boolean bucketsAllowed;
    private boolean interactionsAllowed;

    public PlotSettings() {
        // Default values (can be overridden by config if needed)
        this.pvpEnabled = false;
        this.explosionsAllowed = false;
        this.fireAllowed = false;
        this.containersAllowed = false;
        this.vehiclesAllowed = false;
        this.entityGriefingAllowed = false;
        this.redstoneAllowed = true;
        this.animalInteractAllowed = false;
        this.animalAccessAllowed = false;
        this.petAccessAllowed = false;
        this.armorStandsAllowed = false;
        this.bucketsAllowed = false;
        this.interactionsAllowed = true;
    }

    /* -------------------------------------------------
     * Getters / Setters
     * ------------------------------------------------- */

    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean pvpEnabled) { this.pvpEnabled = pvpEnabled; }

    public boolean isExplosionsAllowed() { return explosionsAllowed; }
    public void setExplosionsAllowed(boolean explosionsAllowed) { this.explosionsAllowed = explosionsAllowed; }

    public boolean isFireAllowed() { return fireAllowed; }
    public void setFireAllowed(boolean fireAllowed) { this.fireAllowed = fireAllowed; }

    public boolean isContainersAllowed() { return containersAllowed; }
    public void setContainersAllowed(boolean containersAllowed) { this.containersAllowed = containersAllowed; }

    public boolean isVehiclesAllowed() { return vehiclesAllowed; }
    public void setVehiclesAllowed(boolean vehiclesAllowed) { this.vehiclesAllowed = vehiclesAllowed; }

    public boolean isEntityGriefingAllowed() { return entityGriefingAllowed; }
    public void setEntityGriefingAllowed(boolean entityGriefingAllowed) { this.entityGriefingAllowed = entityGriefingAllowed; }

    public boolean isRedstoneAllowed() { return redstoneAllowed; }
    public void setRedstoneAllowed(boolean redstoneAllowed) { this.redstoneAllowed = redstoneAllowed; }

    public boolean isAnimalInteractAllowed() { return animalInteractAllowed; }
    public void setAnimalInteractAllowed(boolean animalInteractAllowed) { this.animalInteractAllowed = animalInteractAllowed; }

    public boolean isAnimalAccessAllowed() { return animalAccessAllowed; }
    public void setAnimalAccessAllowed(boolean animalAccessAllowed) { this.animalAccessAllowed = animalAccessAllowed; }

    public boolean isPetAccessAllowed() { return petAccessAllowed; }
    public void setPetAccessAllowed(boolean petAccessAllowed) { this.petAccessAllowed = petAccessAllowed; }

    public boolean isArmorStandsAllowed() { return armorStandsAllowed; }
    public void setArmorStandsAllowed(boolean armorStandsAllowed) { this.armorStandsAllowed = armorStandsAllowed; }

    public boolean isBucketsAllowed() { return bucketsAllowed; }
    public void setBucketsAllowed(boolean bucketsAllowed) { this.bucketsAllowed = bucketsAllowed; }

    public boolean isInteractionsAllowed() { return interactionsAllowed; }
    public void setInteractionsAllowed(boolean interactionsAllowed) { this.interactionsAllowed = interactionsAllowed; }

    /* -------------------------------------------------
     * Serialization / Deserialization
     * ------------------------------------------------- */

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("pvp", pvpEnabled);
        data.put("explosions", explosionsAllowed);
        data.put("fire", fireAllowed);
        data.put("containers", containersAllowed);
        data.put("vehicles", vehiclesAllowed);
        data.put("entityGriefing", entityGriefingAllowed);
        data.put("redstone", redstoneAllowed);
        data.put("animalInteract", animalInteractAllowed);
        data.put("animalAccess", animalAccessAllowed);
        data.put("petAccess", petAccessAllowed);
        data.put("armorStands", armorStandsAllowed);
        data.put("buckets", bucketsAllowed);
        data.put("interactions", interactionsAllowed);
        return data;
    }

    public static PlotSettings deserialize(ConfigurationSection section) {
        PlotSettings settings = new PlotSettings();
        if (section == null) return settings;

        settings.pvpEnabled = section.getBoolean("pvp", settings.pvpEnabled);
        settings.explosionsAllowed = section.getBoolean("explosions", settings.explosionsAllowed);
        settings.fireAllowed = section.getBoolean("fire", settings.fireAllowed);
        settings.containersAllowed = section.getBoolean("containers", settings.containersAllowed);
        settings.vehiclesAllowed = section.getBoolean("vehicles", settings.vehiclesAllowed);
        settings.entityGriefingAllowed = section.getBoolean("entityGriefing", settings.entityGriefingAllowed);
        settings.redstoneAllowed = section.getBoolean("redstone", settings.redstoneAllowed);
        settings.animalInteractAllowed = section.getBoolean("animalInteract", settings.animalInteractAllowed);
        settings.animalAccessAllowed = section.getBoolean("animalAccess", settings.animalAccessAllowed);
        settings.petAccessAllowed = section.getBoolean("petAccess", settings.petAccessAllowed);
        settings.armorStandsAllowed = section.getBoolean("armorStands", settings.armorStandsAllowed);
        settings.bucketsAllowed = section.getBoolean("buckets", settings.bucketsAllowed);
        settings.interactionsAllowed = section.getBoolean("interactions", settings.interactionsAllowed);

        return settings;
    }
}
