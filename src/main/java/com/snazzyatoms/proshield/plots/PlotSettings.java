package com.snazzyatoms.proshield.plots;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * PlotSettings
 * Stores all per-claim toggles.
 *
 * ✅ Preserves all prior flags from 1.2.x
 * ✅ Adds ignite-specific fire controls (flint, lava, lightning)
 * ✅ Splits bucket protection into fill/empty
 * ✅ Keeps mob repel / despawn and damage matrix
 */
public class PlotSettings {

    /* -----------------
     * Protection Toggles
     * ----------------- */
    private boolean explosionsAllowed = false;

    // Buckets (split)
    private boolean bucketsEmptyAllowed = false; // empty water/lava bucket onto blocks
    private boolean bucketsFillAllowed  = false; // fill bucket from fluids

    private boolean itemFramesAllowed = false;
    private boolean armorStandsAllowed = false;
    private boolean animalAccessAllowed = false;
    private boolean petAccessAllowed = false;
    private boolean containersAllowed = false;
    private boolean vehiclesAllowed = false;

    // Fire / redstone / grief
    private boolean fireAllowed = false;           // generic fire allow (legacy)
    private boolean fireSpreadAllowed = false;     // controls spread/ignition fallback
    private boolean fireFlintIgniteAllowed = false;
    private boolean fireLavaIgniteAllowed = false;
    private boolean fireLightningIgniteAllowed = false;

    private boolean redstoneAllowed = true;
    private boolean entityGriefingAllowed = false;

    // PvP / Damage
    private boolean pvpEnabled = false; // legacy/simple pvp toggle
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
    private boolean mobRepelEnabled = false;          // push mobs away from (and near) claims
    private boolean mobDespawnInsideEnabled = false;  // clean up mobs that end up inside

    // Keep drops
    private boolean keepItemsEnabled = false;

    /* -----------------
     * Getters
     * ----------------- */
    public boolean isExplosionsAllowed() { return explosionsAllowed; }

    public boolean isBucketsEmptyAllowed() { return bucketsEmptyAllowed; }
    public boolean isBucketsFillAllowed() { return bucketsFillAllowed; }

    public boolean isItemFramesAllowed() { return itemFramesAllowed; }
    public boolean isArmorStandsAllowed() { return armorStandsAllowed; }
    public boolean isAnimalAccessAllowed() { return animalAccessAllowed; }
    public boolean isPetAccessAllowed() { return petAccessAllowed; }
    public boolean isContainersAllowed() { return containersAllowed; }
    public boolean isVehiclesAllowed() { return vehiclesAllowed; }

    public boolean isFireAllowed() { return fireAllowed; }
    public boolean isFireSpreadAllowed() { return fireSpreadAllowed; }
    public boolean isFireFlintIgniteAllowed() { return fireFlintIgniteAllowed; }
    public boolean isFireLavaIgniteAllowed() { return fireLavaIgniteAllowed; }
    public boolean isFireLightningIgniteAllowed() { return fireLightningIgniteAllowed; }

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

    public void setBucketsEmptyAllowed(boolean b) { bucketsEmptyAllowed = b; }
    public void setBucketsFillAllowed(boolean b)  { bucketsFillAllowed  = b; }

    public void setItemFramesAllowed(boolean b) { itemFramesAllowed = b; }
    public void setArmorStandsAllowed(boolean b) { armorStandsAllowed = b; }
    public void setAnimalAccessAllowed(boolean b) { animalAccessAllowed = b; }
    public void setPetAccessAllowed(boolean b) { petAccessAllowed = b; }
    public void setContainersAllowed(boolean b) { containersAllowed = b; }
    public void setVehiclesAllowed(boolean b) { vehiclesAllowed = b; }

    public void setFireAllowed(boolean b) { fireAllowed = b; }
    public void setFireSpreadAllowed(boolean b) { fireSpreadAllowed = b; }
    public void setFireFlintIgniteAllowed(boolean b) { fireFlintIgniteAllowed = b; }
    public void setFireLavaIgniteAllowed(boolean b) { fireLavaIgniteAllowed = b; }
    public void setFireLightningIgniteAllowed(boolean b) { fireLightningIgniteAllowed = b; }

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

        // buckets.*
        map.put("buckets.empty", bucketsEmptyAllowed);
        map.put("buckets.fill", bucketsFillAllowed);

        map.put("itemFrames", itemFramesAllowed);
        map.put("armorStands", armorStandsAllowed);
        map.put("animals", animalAccessAllowed);
        map.put("pets", petAccessAllowed);
        map.put("containers", containersAllowed);
        map.put("vehicles", vehiclesAllowed);

        // fire.*
        map.put("fire", fireAllowed);
        map.put("fireSpread", fireSpreadAllowed);
        map.put("fire.ignite.flint_and_steel", fireFlintIgniteAllowed);
        map.put("fire.ignite.lava", fireLavaIgniteAllowed);
        map.put("fire.ignite.lightning", fireLightningIgniteAllowed);

        map.put("redstone", redstoneAllowed);
        map.put("entityGriefing", entityGriefingAllowed);

        // pvp / damage.*
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

        // mobs
        map.put("mobRepel", mobRepelEnabled);
        map.put("mobDespawnInside", mobDespawnInsideEnabled);

        // items
        map.put("keepItems", keepItemsEnabled);
        return map;
    }

    public void deserialize(ConfigurationSection sec) {
        if (sec == null) return;

        explosionsAllowed = sec.getBoolean("explosions", explosionsAllowed);

        // buckets.*
        bucketsEmptyAllowed = sec.getBoolean("buckets.empty", bucketsEmptyAllowed);
        bucketsFillAllowed  = sec.getBoolean("buckets.fill",  bucketsFillAllowed);

        itemFramesAllowed = sec.getBoolean("itemFrames", itemFramesAllowed);
        armorStandsAllowed = sec.getBoolean("armorStands", armorStandsAllowed);
        animalAccessAllowed = sec.getBoolean("animals", animalAccessAllowed);
        petAccessAllowed = sec.getBoolean("pets", petAccessAllowed);
        containersAllowed = sec.getBoolean("containers", containersAllowed);
        vehiclesAllowed = sec.getBoolean("vehicles", vehiclesAllowed);

        // fire.*
        fireAllowed = sec.getBoolean("fire", fireAllowed);
        fireSpreadAllowed = sec.getBoolean("fireSpread", fireSpreadAllowed);
        fireFlintIgniteAllowed = sec.getBoolean("fire.ignite.flint_and_steel", fireFlintIgniteAllowed);
        fireLavaIgniteAllowed = sec.getBoolean("fire.ignite.lava", fireLavaIgniteAllowed);
        fireLightningIgniteAllowed = sec.getBoolean("fire.ignite.lightning", fireLightningIgniteAllowed);

        redstoneAllowed = sec.getBoolean("redstone", redstoneAllowed);
        entityGriefingAllowed = sec.getBoolean("entityGriefing", entityGriefingAllowed);

        // pvp / damage.*
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

        // mobs
        mobRepelEnabled = sec.getBoolean("mobRepel", mobRepelEnabled);
        mobDespawnInsideEnabled = sec.getBoolean("mobDespawnInside", mobDespawnInsideEnabled);

        // items
        keepItemsEnabled = sec.getBoolean("keepItems", keepItemsEnabled);
    }
}
