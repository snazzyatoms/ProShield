package com.snazzyatoms.proshield.plots;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * PlotSettings
 *
 * Central per-claim toggles. This class preserves all existing logic and
 * expands it with the complete set of getters/setters that other classes
 * in the project reference (listeners/commands).
 *
 * All booleans default to a safe/expected state for protection plugins.
 * Serialization is flat and stable for compatibility with existing data.
 */
public class PlotSettings {

    /* -------------------------------
     * Core protection flags
     * ------------------------------- */
    private boolean pvpEnabled = false;

    private boolean explosionsAllowed = false;
    private boolean fireAllowed = false;
    private boolean entityGriefingAllowed = false;

    // “Interactions” / redstone switch (kept both names as perfect aliases)
    private boolean interactionsAllowed = true; // general interact
    private boolean redstoneAllowed = true;     // alias used by FlagsListener

    private boolean containersAllowed = true;
    private boolean vehiclesAllowed = true;
    private boolean bucketsAllowed = false;

    // Entities / items
    private boolean itemFramesAllowed = true;
    private boolean armorStandsAllowed = true;
    private boolean animalAccessAllowed = true; // interact with passive
    private boolean petAccessAllowed = true;    // interact with tamed pets

    // Drops / perks
    private boolean keepItemsEnabled = false;

    // Mob borders / despawn
    private boolean mobRepelEnabled = true;           // repel near claim border
    private boolean mobDespawnInsideEnabled = false;  // despawn mobs inside claim

    /* -------------------------------
     * Damage control flags
     * ------------------------------- */
    private boolean damageProtectOwnerAndTrusted = true; // general guard
    private boolean damageCancelAll = false;

    private boolean damagePveEnabled = true;             // can mobs damage players
    private boolean damageProjectilesEnabled = true;     // arrow/snowball/etc.

    private boolean damageFireLavaEnabled = true;
    private boolean damageFallEnabled = true;
    private boolean damageExplosionsEnabled = true;
    private boolean damageDrownVoidSuffocateEnabled = true;
    private boolean damagePoisonWitherEnabled = true;
    private boolean damageEnvironmentEnabled = true;     // cactus, berry, block, etc.

    /* =======================================================
     * Getters / Setters (full API surface used project-wide)
     * ======================================================= */

    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean v) { this.pvpEnabled = v; }

    public boolean isExplosionsAllowed() { return explosionsAllowed; }
    public void setExplosionsAllowed(boolean v) { this.explosionsAllowed = v; }

    public boolean isFireAllowed() { return fireAllowed; }
    public void setFireAllowed(boolean v) { this.fireAllowed = v; }

    public boolean isEntityGriefingAllowed() { return entityGriefingAllowed; }
    public void setEntityGriefingAllowed(boolean v) { this.entityGriefingAllowed = v; }

    // Interactions (alias pair)
    public boolean isInteractionsAllowed() { return interactionsAllowed; }
    public void setInteractionsAllowed(boolean v) {
        this.interactionsAllowed = v;
        this.redstoneAllowed = v; // keep aliases in sync
    }

    public boolean isRedstoneAllowed() { return redstoneAllowed; }
    public void setRedstoneAllowed(boolean v) {
        this.redstoneAllowed = v;
        this.interactionsAllowed = v; // keep aliases in sync
    }

    public boolean isContainersAllowed() { return containersAllowed; }
    public void setContainersAllowed(boolean v) { this.containersAllowed = v; }

    public boolean isVehiclesAllowed() { return vehiclesAllowed; }
    public void setVehiclesAllowed(boolean v) { this.vehiclesAllowed = v; }

    public boolean isBucketsAllowed() { return bucketsAllowed; }
    public void setBucketsAllowed(boolean v) { this.bucketsAllowed = v; }

    public boolean isItemFramesAllowed() { return itemFramesAllowed; }
    public void setItemFramesAllowed(boolean v) { this.itemFramesAllowed = v; }

    public boolean isArmorStandsAllowed() { return armorStandsAllowed; }
    public void setArmorStandsAllowed(boolean v) { this.armorStandsAllowed = v; }

    public boolean isAnimalAccessAllowed() { return animalAccessAllowed; }
    public void setAnimalAccessAllowed(boolean v) { this.animalAccessAllowed = v; }

    public boolean isPetAccessAllowed() { return petAccessAllowed; }
    public void setPetAccessAllowed(boolean v) { this.petAccessAllowed = v; }

    public boolean isKeepItemsEnabled() { return keepItemsEnabled; }
    public void setKeepItemsEnabled(boolean v) { this.keepItemsEnabled = v; }

    public boolean isMobRepelEnabled() { return mobRepelEnabled; }
    public void setMobRepelEnabled(boolean v) { this.mobRepelEnabled = v; }

    public boolean isMobDespawnInsideEnabled() { return mobDespawnInsideEnabled; }
    public void setMobDespawnInsideEnabled(boolean v) { this.mobDespawnInsideEnabled = v; }

    // Damage flags
    public boolean isDamageProtectOwnerAndTrusted() { return damageProtectOwnerAndTrusted; }
    public void setDamageProtectOwnerAndTrusted(boolean v) { this.damageProtectOwnerAndTrusted = v; }

    public boolean isDamageCancelAll() { return damageCancelAll; }
    public void setDamageCancelAll(boolean v) { this.damageCancelAll = v; }

    public boolean isDamagePveEnabled() { return damagePveEnabled; }
    public void setDamagePveEnabled(boolean v) { this.damagePveEnabled = v; }

    public boolean isDamageProjectilesEnabled() { return damageProjectilesEnabled; }
    public void setDamageProjectilesEnabled(boolean v) { this.damageProjectilesEnabled = v; }

    public boolean isDamageFireLavaEnabled() { return damageFireLavaEnabled; }
    public void setDamageFireLavaEnabled(boolean v) { this.damageFireLavaEnabled = v; }

    public boolean isDamageFallEnabled() { return damageFallEnabled; }
    public void setDamageFallEnabled(boolean v) { this.damageFallEnabled = v; }

    public boolean isDamageExplosionsEnabled() { return damageExplosionsEnabled; }
    public void setDamageExplosionsEnabled(boolean v) { this.damageExplosionsEnabled = v; }

    public boolean isDamageDrownVoidSuffocateEnabled() { return damageDrownVoidSuffocateEnabled; }
    public void setDamageDrownVoidSuffocateEnabled(boolean v) { this.damageDrownVoidSuffocateEnabled = v; }

    public boolean isDamagePoisonWitherEnabled() { return damagePoisonWitherEnabled; }
    public void setDamagePoisonWitherEnabled(boolean v) { this.damagePoisonWitherEnabled = v; }

    public boolean isDamageEnvironmentEnabled() { return damageEnvironmentEnabled; }
    public void setDamageEnvironmentEnabled(boolean v) { this.damageEnvironmentEnabled = v; }

    /* =========================
     * Serialization / Storage
     * ========================= */

    public Map<String, Object> serialize() {
        Map<String, Object> m = new HashMap<>();

        // Core
        m.put("pvpEnabled", pvpEnabled);
        m.put("explosionsAllowed", explosionsAllowed);
        m.put("fireAllowed", fireAllowed);
        m.put("entityGriefingAllowed", entityGriefingAllowed);

        // Interaction (store both to be explicit & backward compatible)
        m.put("interactionsAllowed", interactionsAllowed);
        m.put("redstoneAllowed", redstoneAllowed);

        m.put("containersAllowed", containersAllowed);
        m.put("vehiclesAllowed", vehiclesAllowed);
        m.put("bucketsAllowed", bucketsAllowed);

        // Entities / items
        m.put("itemFramesAllowed", itemFramesAllowed);
        m.put("armorStandsAllowed", armorStandsAllowed);
        m.put("animalAccessAllowed", animalAccessAllowed);
        m.put("petAccessAllowed", petAccessAllowed);

        // Drops / perks
        m.put("keepItemsEnabled", keepItemsEnabled);

        // Mobs
        m.put("mobRepelEnabled", mobRepelEnabled);
        m.put("mobDespawnInsideEnabled", mobDespawnInsideEnabled);

        // Damage
        m.put("damageProtectOwnerAndTrusted", damageProtectOwnerAndTrusted);
        m.put("damageCancelAll", damageCancelAll);
        m.put("damagePveEnabled", damagePveEnabled);
        m.put("damageProjectilesEnabled", damageProjectilesEnabled);
        m.put("damageFireLavaEnabled", damageFireLavaEnabled);
        m.put("damageFallEnabled", damageFallEnabled);
        m.put("damageExplosionsEnabled", damageExplosionsEnabled);
        m.put("damageDrownVoidSuffocateEnabled", damageDrownVoidSuffocateEnabled);
        m.put("damagePoisonWitherEnabled", damagePoisonWitherEnabled);
        m.put("damageEnvironmentEnabled", damageEnvironmentEnabled);

        return m;
    }

    /**
     * Populate from a config section (used by Plot.deserialize()).
     * Missing values keep their defaults to preserve older saves.
     */
    public void deserialize(ConfigurationSection sec) {
        if (sec == null) return;

        // Core
        pvpEnabled = sec.getBoolean("pvpEnabled", pvpEnabled);
        explosionsAllowed = sec.getBoolean("explosionsAllowed", explosionsAllowed);
        fireAllowed = sec.getBoolean("fireAllowed", fireAllowed);
        entityGriefingAllowed = sec.getBoolean("entityGriefingAllowed", entityGriefingAllowed);

        // Interaction aliases — if only one is present, sync both
        boolean readInteractions = sec.getBoolean("interactionsAllowed", interactionsAllowed);
        boolean readRedstone = sec.getBoolean("redstoneAllowed", redstoneAllowed);

        // If a save only wrote one of them, we should not flip the other unintentionally.
        // We’ll sync them purposely to stay consistent going forward.
        interactionsAllowed = readInteractions;
        redstoneAllowed = readRedstone;
        if (sec.contains("interactionsAllowed") && !sec.contains("redstoneAllowed")) {
            redstoneAllowed = interactionsAllowed;
        } else if (!sec.contains("interactionsAllowed") && sec.contains("redstoneAllowed")) {
            interactionsAllowed = redstoneAllowed;
        }

        containersAllowed = sec.getBoolean("containersAllowed", containersAllowed);
        vehiclesAllowed = sec.getBoolean("vehiclesAllowed", vehiclesAllowed);
        bucketsAllowed = sec.getBoolean("bucketsAllowed", bucketsAllowed);

        // Entities/items
        itemFramesAllowed = sec.getBoolean("itemFramesAllowed", itemFramesAllowed);
        armorStandsAllowed = sec.getBoolean("armorStandsAllowed", armorStandsAllowed);
        animalAccessAllowed = sec.getBoolean("animalAccessAllowed", animalAccessAllowed);
        petAccessAllowed = sec.getBoolean("petAccessAllowed", petAccessAllowed);

        // Drops / perks
        keepItemsEnabled = sec.getBoolean("keepItemsEnabled", keepItemsEnabled);

        // Mobs
        mobRepelEnabled = sec.getBoolean("mobRepelEnabled", mobRepelEnabled);
        mobDespawnInsideEnabled = sec.getBoolean("mobDespawnInsideEnabled", mobDespawnInsideEnabled);

        // Damage
        damageProtectOwnerAndTrusted = sec.getBoolean("damageProtectOwnerAndTrusted", damageProtectOwnerAndTrusted);
        damageCancelAll = sec.getBoolean("damageCancelAll", damageCancelAll);
        damagePveEnabled = sec.getBoolean("damagePveEnabled", damagePveEnabled);
        damageProjectilesEnabled = sec.getBoolean("damageProjectilesEnabled", damageProjectilesEnabled);
        damageFireLavaEnabled = sec.getBoolean("damageFireLavaEnabled", damageFireLavaEnabled);
        damageFallEnabled = sec.getBoolean("damageFallEnabled", damageFallEnabled);
        damageExplosionsEnabled = sec.getBoolean("damageExplosionsEnabled", damageExplosionsEnabled);
        damageDrownVoidSuffocateEnabled = sec.getBoolean("damageDrownVoidSuffocateEnabled", damageDrownVoidSuffocateEnabled);
        damagePoisonWitherEnabled = sec.getBoolean("damagePoisonWitherEnabled", damagePoisonWitherEnabled);
        damageEnvironmentEnabled = sec.getBoolean("damageEnvironmentEnabled", damageEnvironmentEnabled);
    }
}
