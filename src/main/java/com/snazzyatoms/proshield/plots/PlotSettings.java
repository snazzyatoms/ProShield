// src/main/java/com/snazzyatoms/proshield/plots/PlotSettings.java
package com.snazzyatoms.proshield.plots;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Holds per-claim flags and settings.
 * Extended to include all flags referenced by listeners/commands.
 */
public class PlotSettings {

    // --- Damage umbrella ---
    private boolean damageEnabled = true;
    private boolean damageProtectOwnerAndTrusted = true;
    private boolean damageCancelAll = false;
    private boolean damagePveEnabled = true;
    private boolean damageProjectilesEnabled = true;
    private boolean damageEnvironmentEnabled = true;
    private boolean damageFireLavaEnabled = true;
    private boolean damageFallEnabled = true;
    private boolean damageExplosionsEnabled = true;
    private boolean damageDrownVoidSuffocateEnabled = true;
    private boolean damagePoisonWitherEnabled = true;

    // --- Core flags ---
    private boolean pvpEnabled = false;
    private boolean explosionsAllowed = false;
    private boolean fireAllowed = false;
    private boolean entityGriefingAllowed = false;

    // --- Interactions & access ---
    private boolean redstoneAllowed = true;
    private boolean containersAllowed = true;
    private boolean animalInteractAllowed = true;   // for breeding/leads/milk etc
    private boolean petAccessAllowed = true;        // tamed pets
    private boolean armorStandsAllowed = true;
    private boolean itemFramesAllowed = true;
    private boolean vehiclesAllowed = false;        // boats/minecarts
    private boolean bucketsAllowed = false;

    // --- Items ---
    private boolean keepItemsEnabled = false;       // per-claim override

    // --- Mobs (border repel) ---
    private boolean mobRepelEnabled = false;
    private boolean mobDespawnInsideEnabled = false;

    public PlotSettings() {}

    // ===== getters/setters (used by listeners) =====
    public boolean isDamageEnabled() { return damageEnabled; }
    public boolean isDamageProtectOwnerAndTrusted() { return damageProtectOwnerAndTrusted; }
    public boolean isDamageCancelAll() { return damageCancelAll; }
    public boolean isDamagePveEnabled() { return damagePveEnabled; }
    public boolean isDamageProjectilesEnabled() { return damageProjectilesEnabled; }
    public boolean isDamageEnvironmentEnabled() { return damageEnvironmentEnabled; }
    public boolean isDamageFireLavaEnabled() { return damageFireLavaEnabled; }
    public boolean isDamageFallEnabled() { return damageFallEnabled; }
    public boolean isDamageExplosionsEnabled() { return damageExplosionsEnabled; }
    public boolean isDamageDrownVoidSuffocateEnabled() { return damageDrownVoidSuffocateEnabled; }
    public boolean isDamagePoisonWitherEnabled() { return damagePoisonWitherEnabled; }

    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean v) { pvpEnabled = v; }

    public boolean isExplosionsAllowed() { return explosionsAllowed; }
    public void setExplosionsAllowed(boolean v) { explosionsAllowed = v; }

    public boolean isFireAllowed() { return fireAllowed; }
    public void setFireAllowed(boolean v) { fireAllowed = v; }

    public boolean isEntityGriefingAllowed() { return entityGriefingAllowed; }
    public void setEntityGriefingAllowed(boolean v) { entityGriefingAllowed = v; }

    public boolean isRedstoneAllowed() { return redstoneAllowed; }
    public void setRedstoneAllowed(boolean v) { redstoneAllowed = v; }

    public boolean isContainersAllowed() { return containersAllowed; }
    public void setContainersAllowed(boolean v) { containersAllowed = v; }

    public boolean isAnimalInteractAllowed() { return animalInteractAllowed; }
    public void setAnimalInteractAllowed(boolean v) { animalInteractAllowed = v; }

    public boolean isPetAccessAllowed() { return petAccessAllowed; }
    public void setPetAccessAllowed(boolean v) { petAccessAllowed = v; }

    public boolean isArmorStandsAllowed() { return armorStandsAllowed; }
    public void setArmorStandsAllowed(boolean v) { armorStandsAllowed = v; }

    public boolean isItemFramesAllowed() { return itemFramesAllowed; }
    public void setItemFramesAllowed(boolean v) { itemFramesAllowed = v; }

    public boolean isVehiclesAllowed() { return vehiclesAllowed; }
    public void setVehiclesAllowed(boolean v) { vehiclesAllowed = v; }

    public boolean isBucketsAllowed() { return bucketsAllowed; }
    public void setBucketsAllowed(boolean v) { bucketsAllowed = v; }

    public boolean isKeepItemsEnabled() { return keepItemsEnabled; }
    public void setKeepItemsEnabled(boolean v) { keepItemsEnabled = v; }

    public boolean isMobRepelEnabled() { return mobRepelEnabled; }
    public void setMobRepelEnabled(boolean v) { mobRepelEnabled = v; }

    public boolean isMobDespawnInsideEnabled() { return mobDespawnInsideEnabled; }
    public void setMobDespawnInsideEnabled(boolean v) { mobDespawnInsideEnabled = v; }

    // ===== load/save helpers (optional call from manager) =====
    public void loadFrom(ConfigurationSection sec) {
        if (sec == null) return;
        damageEnabled = sec.getBoolean("damage.enabled", damageEnabled);
        damageProtectOwnerAndTrusted = sec.getBoolean("damage.protect-owner-and-trusted", damageProtectOwnerAndTrusted);
        damageCancelAll = sec.getBoolean("damage.cancel-all", damageCancelAll);
        damagePveEnabled = sec.getBoolean("damage.pve", damagePveEnabled);
        damageProjectilesEnabled = sec.getBoolean("damage.projectiles", damageProjectilesEnabled);
        damageEnvironmentEnabled = sec.getBoolean("damage.environment", damageEnvironmentEnabled);
        damageFireLavaEnabled = sec.getBoolean("damage.fire-lava", damageFireLavaEnabled);
        damageFallEnabled = sec.getBoolean("damage.fall", damageFallEnabled);
        damageExplosionsEnabled = sec.getBoolean("damage.explosions", damageExplosionsEnabled);
        damageDrownVoidSuffocateEnabled = sec.getBoolean("damage.drown-void-suffocate", damageDrownVoidSuffocateEnabled);
        damagePoisonWitherEnabled = sec.getBoolean("damage.poison-wither", damagePoisonWitherEnabled);

        pvpEnabled = sec.getBoolean("pvp", pvpEnabled);
        explosionsAllowed = sec.getBoolean("explosions", explosionsAllowed);
        fireAllowed = sec.getBoolean("fire", fireAllowed);
        entityGriefingAllowed = sec.getBoolean("entity-grief", entityGriefingAllowed);

        redstoneAllowed = sec.getBoolean("redstone", redstoneAllowed);
        containersAllowed = sec.getBoolean("containers", containersAllowed);
        animalInteractAllowed = sec.getBoolean("animal-interact", animalInteractAllowed);
        petAccessAllowed = sec.getBoolean("pet-access", petAccessAllowed);
        armorStandsAllowed = sec.getBoolean("armor-stands", armorStandsAllowed);
        itemFramesAllowed = sec.getBoolean("item-frames", itemFramesAllowed);
        vehiclesAllowed = sec.getBoolean("vehicles", vehiclesAllowed);
        bucketsAllowed = sec.getBoolean("buckets", bucketsAllowed);

        keepItemsEnabled = sec.getBoolean("keep-items", keepItemsEnabled);

        mobRepelEnabled = sec.getBoolean("mobs.repel.enabled", mobRepelEnabled);
        mobDespawnInsideEnabled = sec.getBoolean("mobs.despawn-inside", mobDespawnInsideEnabled);
    }

    public void saveTo(ConfigurationSection sec) {
        if (sec == null) return;
        sec.set("damage.enabled", damageEnabled);
        sec.set("damage.protect-owner-and-trusted", damageProtectOwnerAndTrusted);
        sec.set("damage.cancel-all", damageCancelAll);
        sec.set("damage.pve", damagePveEnabled);
        sec.set("damage.projectiles", damageProjectilesEnabled);
        sec.set("damage.environment", damageEnvironmentEnabled);
        sec.set("damage.fire-lava", damageFireLavaEnabled);
        sec.set("damage.fall", damageFallEnabled);
        sec.set("damage.explosions", damageExplosionsEnabled);
        sec.set("damage.drown-void-suffocate", damageDrownVoidSuffocateEnabled);
        sec.set("damage.poison-wither", damagePoisonWitherEnabled);

        sec.set("pvp", pvpEnabled);
        sec.set("explosions", explosionsAllowed);
        sec.set("fire", fireAllowed);
        sec.set("entity-grief", entityGriefingAllowed);

        sec.set("redstone", redstoneAllowed);
        sec.set("containers", containersAllowed);
        sec.set("animal-interact", animalInteractAllowed);
        sec.set("pet-access", petAccessAllowed);
        sec.set("armor-stands", armorStandsAllowed);
        sec.set("item-frames", itemFramesAllowed);
        sec.set("vehicles", vehiclesAllowed);
        sec.set("buckets", bucketsAllowed);

        sec.set("keep-items", keepItemsEnabled);

        sec.set("mobs.repel.enabled", mobRepelEnabled);
        sec.set("mobs.despawn-inside", mobDespawnInsideEnabled);
    }
}
