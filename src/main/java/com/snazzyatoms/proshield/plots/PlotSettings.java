package com.snazzyatoms.proshield.plots;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * PlotSettings
 * Stores all per-claim toggles.
 *
 * ✅ Preserves all prior logic
 * ✅ Includes bucketAllowed (fixed naming)
 * ✅ Fire, mob, PvP, entity grief, drops, etc.
 */
public class PlotSettings {

    /* -----------------
     * Protection Toggles
     * ----------------- */
    private boolean explosionsAllowed = false;
    private boolean bucketAllowed = false;   // ✅ fixed single flag for buckets
    private boolean itemFramesAllowed = false;
    private boolean armorStandsAllowed = false;
    private boolean animalAccessAllowed = false;
    private boolean petAccessAllowed = false;
    private boolean containersAllowed = false;
    private boolean vehiclesAllowed = false;

    // Fire / grief
    private boolean fireAllowed = false;
    private boolean fireSpreadAllowed = false;
    private boolean redstoneAllowed = true;
    private boolean entityGriefingAllowed = false;

    // PvP / Damage
    private boolean pvpEnabled = false;
    private boolean damagePvpEnabled = false;
    private boolean damageCancelAll = false;
    private boolean damageProtectOwnerAndTrusted = true;
    private boolean damagePveEnabled = false;
    private boolean damageProjectilesEnabled = false;
    private boolean damageFireLavaEnabled = false;
    private boolean damageFallEnabled = false;
    private boolean damageExplosionsEnabled = false;
    private boolean damageDrownVoidSuffocateEnabled = false;
    private boolean damagePoisonWitherEnabled = false;
    private boolean damageEnvironmentEnabled = false;

    // Mob repel / despawn
    private boolean mobRepelEnabled = false;
    private boolean mobDespawnInsideEnabled = false;

    // Keep drops
    private boolean keepItemsEnabled = false;

    /* -----------------
     * Getters
     * ----------------- */
    public boolean isExplosionsAllowed() { return explosionsAllowed; }
    public boolean isBucketAllowed() { return bucketAllowed; } // ✅ fixed
    public boolean isItemFramesAllowed() { return itemFramesAllowed; }
    public boolean isArmorStandsAllowed() { return armorStandsAllowed; }
    public boolean isAnimalAccessAllowed() { return animalAccessAllowed; }
    public boolean isPetAccessAllowed() { return petAccessAllowed; }
    public boolean isContainersAllowed() { return containersAllowed; }
    public boolean isVehiclesAllowed() { return vehiclesAllowed; }

    public boolean isFireAllowed() { return fireAllowed; }
    public boolean isFireSpreadAllowed() { return fireSpreadAllowed; }
    public boolean isRedstoneAllowed() { return redstoneAllowed; }
    public boolean isEntityGriefingAllowed() { return entityGriefingAllowed; }

    public boolean isPvpEnabled() { return pvpEnabled; }
    public boolean isDamagePvpEnabled() { return damagePvpEnabled; }
    public boolean isDamageCancelAll() { return damageCancelAll; }
    public boolean isDamageProtectOwnerAndTrusted() { return damageProtectOwnerAndTrusted; }
    public boolean isDamagePveEnabled() { return damagePveEnabled; }
    public boolean isDamageProjectilesEnabled() { return damageProjectilesEnabled; }
    public boolean isDamageFireLavaEnabled() { return damageFireLavaEnabled; }
    public boolean isDamageFallEnabled() { return damageFallEnabled; }
    public boolean isDamageExplosionsEnabled() { return damageExplosionsEnabled; }
    public boolean isDamageDrownVoidSuffocateEnabled() { return damageDrownVoidSuffocateEnabled; }
    public boolean isDamagePoisonWitherEnabled() { return damagePoisonWitherEnabled; }
    public boolean isDamageEnvironmentEnabled() { return damageEnvironmentEnabled; }

    public boolean isMobRepelEnabled() { return mobRepelEnabled; }
    public boolean isMobDespawnInsideEnabled() { return mobDespawnInsideEnabled; }

    public boolean isKeepItemsEnabled() { return keepItemsEnabled; }

    /* -----------------
     * Setters
     * ----------------- */
    public void setExplosionsAllowed(boolean b) { explosionsAllowed = b; }
    public void setBucketAllowed(boolean b) { bucketAllowed = b; } // ✅ fixed
    public void setItemFramesAllowed(boolean b) { itemFramesAllowed = b; }
    public void setArmorStandsAllowed(boolean b) { armorStandsAllowed = b; }
    public void setAnimalAccessAllowed(boolean b) { animalAccessAllowed = b; }
    public void setPetAccessAllowed(boolean b) { petAccessAllowed = b; }
    public void setContainersAllowed(boolean b) { containersAllowed = b; }
    public void setVehiclesAllowed(boolean b) { vehiclesAllowed = b; }

    public void setFireAllowed(boolean b) { fireAllowed = b; }
    public void setFireSpreadAllowed(boolean b) { fireSpreadAllowed = b; }
    public void setRedstoneAllowed(boolean b) { redstoneAllowed = b; }
    public void setEntityGriefingAllowed(boolean b) { entityGriefingAllowed = b; }

    public void setPvpEnabled(boolean b) { pvpEnabled = b; }
    public void setDamagePvpEnabled(boolean b) { damagePvpEnabled = b; }
    public void setDamageCancelAll(boolean b) { damageCancelAll = b; }
    public void setDamageProtectOwnerAndTrusted(boolean b) { damageProtectOwnerAndTrusted = b; }
    public void setDamagePveEnabled(boolean b) { damagePveEnabled = b; }
    public void setDamageProjectilesEnabled(boolean b) { damageProjectilesEnabled = b; }
    public void setDamageFireLavaEnabled(boolean b) { damageFireLavaEnabled = b; }
    public void setDamageFallEnabled(boolean b) { damageFallEnabled = b; }
    public void setDamageExplosionsEnabled(boolean b) { damageExplosionsEnabled = b; }
    public void setDamageDrownVoidSuffocateEnabled(boolean b) { damageDrownVoidSuffocateEnabled = b; }
    public void setDamagePoisonWitherEnabled(boolean b) { damagePoisonWitherEnabled = b; }
    public void setDamageEnvironmentEnabled(boolean b) { damageEnvironmentEnabled = b; }

    public void setMobRepelEnabled(boolean b) { mobRepelEnabled = b; }
    public void setMobDespawnInsideEnabled(boolean b) { mobDespawnInsideEnabled = b; }

    public void setKeepItemsEnabled(boolean b) { keepItemsEnabled = b; }

    /* -----------------
     * Serialization
     * ----------------- */
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("explosions", explosionsAllowed);
        map.put("buckets", bucketAllowed); // ✅ fixed
        map.put("itemFrames", itemFramesAllowed);
        map.put("armorStands", armorStandsAllowed);
        map.put("animals", animalAccessAllowed);
        map.put("pets", petAccessAllowed);
        map.put("containers", containersAllowed);
        map.put("vehicles", vehiclesAllowed);

        map.put("fire", fireAllowed);
        map.put("fireSpread", fireSpreadAllowed);
        map.put("redstone", redstoneAllowed);
        map.put("entityGriefing", entityGriefingAllowed);

        map.put("pvp", pvpEnabled);
        map.put("damage.pvp", damagePvpEnabled);
        map.put("damage.cancelAll", damageCancelAll);
        map.put("damage.protectTrusted", damageProtectOwnerAndTrusted);
        map.put("damage.pve", damagePveEnabled);
        map.put("damage.projectiles", damageProjectilesEnabled);
        map.put("damage.fireLava", damageFireLavaEnabled);
        map.put("damage.fall", damageFallEnabled);
        map.put("damage.explosions", damageExplosionsEnabled);
        map.put("damage.drownVoidSuffocate", damageDrownVoidSuffocateEnabled);
        map.put("damage.poisonWither", damagePoisonWitherEnabled);
        map.put("damage.environment", damageEnvironmentEnabled);

        map.put("mobRepel", mobRepelEnabled);
        map.put("mobDespawnInside", mobDespawnInsideEnabled);

        map.put("keepItems", keepItemsEnabled);
        return map;
    }

    public void deserialize(ConfigurationSection sec) {
        if (sec == null) return;

        explosionsAllowed = sec.getBoolean("explosions", explosionsAllowed);
        bucketAllowed = sec.getBoolean("buckets", bucketAllowed); // ✅ fixed
        itemFramesAllowed = sec.getBoolean("itemFrames", itemFramesAllowed);
        armorStandsAllowed = sec.getBoolean("armorStands", armorStandsAllowed);
        animalAccessAllowed = sec.getBoolean("animals", animalAccessAllowed);
        petAccessAllowed = sec.getBoolean("pets", petAccessAllowed);
        containersAllowed = sec.getBoolean("containers", containersAllowed);
        vehiclesAllowed = sec.getBoolean("vehicles", vehiclesAllowed);

        fireAllowed = sec.getBoolean("fire", fireAllowed);
        fireSpreadAllowed = sec.getBoolean("fireSpread", fireSpreadAllowed);
        redstoneAllowed = sec.getBoolean("redstone", redstoneAllowed);
        entityGriefingAllowed = sec.getBoolean("entityGriefing", entityGriefingAllowed);

        pvpEnabled = sec.getBoolean("pvp", pvpEnabled);
        damagePvpEnabled = sec.getBoolean("damage.pvp", damagePvpEnabled);
        damageCancelAll = sec.getBoolean("damage.cancelAll", damageCancelAll);
        damageProtectOwnerAndTrusted = sec.getBoolean("damage.protectTrusted", damageProtectOwnerAndTrusted);
        damagePveEnabled = sec.getBoolean("damage.pve", damagePveEnabled);
        damageProjectilesEnabled = sec.getBoolean("damage.projectiles", damageProjectilesEnabled);
        damageFireLavaEnabled = sec.getBoolean("damage.fireLava", damageFireLavaEnabled);
        damageFallEnabled = sec.getBoolean("damage.fall", damageFallEnabled);
        damageExplosionsEnabled = sec.getBoolean("damage.explosions", damageExplosionsEnabled);
        damageDrownVoidSuffocateEnabled = sec.getBoolean("damage.drownVoidSuffocate", damageDrownVoidSuffocateEnabled);
        damagePoisonWitherEnabled = sec.getBoolean("damage.poisonWither", damagePoisonWitherEnabled);
        damageEnvironmentEnabled = sec.getBoolean("damage.environment", damageEnvironmentEnabled);

        mobRepelEnabled = sec.getBoolean("mobRepel", mobRepelEnabled);
        mobDespawnInsideEnabled = sec.getBoolean("mobDespawnInside", mobDespawnInsideEnabled);

        keepItemsEnabled = sec.getBoolean("keepItems", keepItemsEnabled);
    }
}
