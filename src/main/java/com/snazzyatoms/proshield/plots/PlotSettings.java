// src/main/java/com/snazzyatoms/proshield/plots/PlotSettings.java
package com.snazzyatoms.proshield.plots;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * PlotSettings
 *
 * Stores per-claim flags. Preserves all prior logic
 * and reintroduces missing flags required by listeners.
 */
public class PlotSettings {

    /* ------------------------------
     * Entity & Block Protections
     * ------------------------------ */
    private boolean explosionsAllowed = false;
    private boolean bucketsAllowed = false;
    private boolean itemFramesAllowed = false;
    private boolean armorStandsAllowed = false;
    private boolean containersAllowed = false;
    private boolean vehiclesAllowed = false;
    private boolean animalAccessAllowed = false;
    private boolean petAccessAllowed = false;
    private boolean animalInteractAllowed = false; // NEW

    /* ------------------------------
     * Damage Protections
     * ------------------------------ */
    private boolean damageProtectOwnerAndTrusted = true;
    private boolean damageCancelAll = false;
    private boolean damagePveEnabled = true;
    private boolean damageProjectilesEnabled = true;
    private boolean damageFireLavaEnabled = true;
    private boolean damageFallEnabled = true;
    private boolean damageExplosionsEnabled = true;
    private boolean damageDrownVoidSuffocateEnabled = true;
    private boolean damagePoisonWitherEnabled = true;
    private boolean damageEnvironmentEnabled = true;

    /* ------------------------------
     * Misc Flags
     * ------------------------------ */
    private boolean keepItemsEnabled = false;
    private boolean mobRepelEnabled = false;
    private boolean mobDespawnInsideEnabled = false;

    /* -------------------------------------------------------
     * Getters / Setters
     * ------------------------------------------------------- */

    public boolean isExplosionsAllowed() { return explosionsAllowed; }
    public void setExplosionsAllowed(boolean b) { this.explosionsAllowed = b; }

    public boolean isBucketsAllowed() { return bucketsAllowed; }
    public void setBucketsAllowed(boolean b) { this.bucketsAllowed = b; }

    public boolean isItemFramesAllowed() { return itemFramesAllowed; }
    public void setItemFramesAllowed(boolean b) { this.itemFramesAllowed = b; }

    public boolean isArmorStandsAllowed() { return armorStandsAllowed; }
    public void setArmorStandsAllowed(boolean b) { this.armorStandsAllowed = b; }

    public boolean isContainersAllowed() { return containersAllowed; }
    public void setContainersAllowed(boolean b) { this.containersAllowed = b; }

    public boolean isVehiclesAllowed() { return vehiclesAllowed; }
    public void setVehiclesAllowed(boolean b) { this.vehiclesAllowed = b; }

    public boolean isAnimalAccessAllowed() { return animalAccessAllowed; }
    public void setAnimalAccessAllowed(boolean b) { this.animalAccessAllowed = b; }

    public boolean isPetAccessAllowed() { return petAccessAllowed; }
    public void setPetAccessAllowed(boolean b) { this.petAccessAllowed = b; }

    public boolean isAnimalInteractAllowed() { return animalInteractAllowed; }
    public void setAnimalInteractAllowed(boolean b) { this.animalInteractAllowed = b; }

    /* --- Damage --- */
    public boolean isDamageProtectOwnerAndTrusted() { return damageProtectOwnerAndTrusted; }
    public void setDamageProtectOwnerAndTrusted(boolean b) { this.damageProtectOwnerAndTrusted = b; }

    public boolean isDamageCancelAll() { return damageCancelAll; }
    public void setDamageCancelAll(boolean b) { this.damageCancelAll = b; }

    public boolean isDamagePveEnabled() { return damagePveEnabled; }
    public void setDamagePveEnabled(boolean b) { this.damagePveEnabled = b; }

    public boolean isDamageProjectilesEnabled() { return damageProjectilesEnabled; }
    public void setDamageProjectilesEnabled(boolean b) { this.damageProjectilesEnabled = b; }

    public boolean isDamageFireLavaEnabled() { return damageFireLavaEnabled; }
    public void setDamageFireLavaEnabled(boolean b) { this.damageFireLavaEnabled = b; }

    public boolean isDamageFallEnabled() { return damageFallEnabled; }
    public void setDamageFallEnabled(boolean b) { this.damageFallEnabled = b; }

    public boolean isDamageExplosionsEnabled() { return damageExplosionsEnabled; }
    public void setDamageExplosionsEnabled(boolean b) { this.damageExplosionsEnabled = b; }

    public boolean isDamageDrownVoidSuffocateEnabled() { return damageDrownVoidSuffocateEnabled; }
    public void setDamageDrownVoidSuffocateEnabled(boolean b) { this.damageDrownVoidSuffocateEnabled = b; }

    public boolean isDamagePoisonWitherEnabled() { return damagePoisonWitherEnabled; }
    public void setDamagePoisonWitherEnabled(boolean b) { this.damagePoisonWitherEnabled = b; }

    public boolean isDamageEnvironmentEnabled() { return damageEnvironmentEnabled; }
    public void setDamageEnvironmentEnabled(boolean b) { this.damageEnvironmentEnabled = b; }

    /* --- Misc --- */
    public boolean isKeepItemsEnabled() { return keepItemsEnabled; }
    public void setKeepItemsEnabled(boolean b) { this.keepItemsEnabled = b; }

    public boolean isMobRepelEnabled() { return mobRepelEnabled; }
    public void setMobRepelEnabled(boolean b) { this.mobRepelEnabled = b; }

    public boolean isMobDespawnInsideEnabled() { return mobDespawnInsideEnabled; }
    public void setMobDespawnInsideEnabled(boolean b) { this.mobDespawnInsideEnabled = b; }

    /* -------------------------------------------------------
     * Serialization
     * ------------------------------------------------------- */

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("explosions", explosionsAllowed);
        map.put("buckets", bucketsAllowed);
        map.put("itemFrames", itemFramesAllowed);
        map.put("armorStands", armorStandsAllowed);
        map.put("containers", containersAllowed);
        map.put("vehicles", vehiclesAllowed);
        map.put("animals", animalAccessAllowed);
        map.put("pets", petAccessAllowed);
        map.put("animalInteract", animalInteractAllowed);

        map.put("damageProtectOwnerAndTrusted", damageProtectOwnerAndTrusted);
        map.put("damageCancelAll", damageCancelAll);
        map.put("damagePve", damagePveEnabled);
        map.put("damageProjectiles", damageProjectilesEnabled);
        map.put("damageFireLava", damageFireLavaEnabled);
        map.put("damageFall", damageFallEnabled);
        map.put("damageExplosions", damageExplosionsEnabled);
        map.put("damageDrownVoidSuffocate", damageDrownVoidSuffocateEnabled);
        map.put("damagePoisonWither", damagePoisonWitherEnabled);
        map.put("damageEnvironment", damageEnvironmentEnabled);

        map.put("keepItems", keepItemsEnabled);
        map.put("mobRepel", mobRepelEnabled);
        map.put("mobDespawnInside", mobDespawnInsideEnabled);

        return map;
    }

    public void deserialize(ConfigurationSection sec) {
        if (sec == null) return;

        explosionsAllowed = sec.getBoolean("explosions", explosionsAllowed);
        bucketsAllowed = sec.getBoolean("buckets", bucketsAllowed);
        itemFramesAllowed = sec.getBoolean("itemFrames", itemFramesAllowed);
        armorStandsAllowed = sec.getBoolean("armorStands", armorStandsAllowed);
        containersAllowed = sec.getBoolean("containers", containersAllowed);
        vehiclesAllowed = sec.getBoolean("vehicles", vehiclesAllowed);
        animalAccessAllowed = sec.getBoolean("animals", animalAccessAllowed);
        petAccessAllowed = sec.getBoolean("pets", petAccessAllowed);
        animalInteractAllowed = sec.getBoolean("animalInteract", animalInteractAllowed);

        damageProtectOwnerAndTrusted = sec.getBoolean("damageProtectOwnerAndTrusted", damageProtectOwnerAndTrusted);
        damageCancelAll = sec.getBoolean("damageCancelAll", damageCancelAll);
        damagePveEnabled = sec.getBoolean("damagePve", damagePveEnabled);
        damageProjectilesEnabled = sec.getBoolean("damageProjectiles", damageProjectilesEnabled);
        damageFireLavaEnabled = sec.getBoolean("damageFireLava", damageFireLavaEnabled);
        damageFallEnabled = sec.getBoolean("damageFall", damageFallEnabled);
        damageExplosionsEnabled = sec.getBoolean("damageExplosions", damageExplosionsEnabled);
        damageDrownVoidSuffocateEnabled = sec.getBoolean("damageDrownVoidSuffocate", damageDrownVoidSuffocateEnabled);
        damagePoisonWitherEnabled = sec.getBoolean("damagePoisonWither", damagePoisonWitherEnabled);
        damageEnvironmentEnabled = sec.getBoolean("damageEnvironment", damageEnvironmentEnabled);

        keepItemsEnabled = sec.getBoolean("keepItems", keepItemsEnabled);
        mobRepelEnabled = sec.getBoolean("mobRepel", mobRepelEnabled);
        mobDespawnInsideEnabled = sec.getBoolean("mobDespawnInside", mobDespawnInsideEnabled);
    }
}
