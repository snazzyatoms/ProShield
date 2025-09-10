package com.snazzyatoms.proshield.plots;

/**
 * PlotSettings - all per-claim toggles/flags.
 * Defaults mirror config.yml; PlotManager should sync on (de)serialization.
 *
 * Extended to include interaction/containers/animals/vehicles/itemframes/armorstands/pets/etc.
 * Also exposes damage-* toggles used by DamageProtectionListener.
 */
public class PlotSettings {

    /* ---- Core toggles ---- */
    private boolean pvpEnabled = false;
    private boolean explosionsAllowed = true;
    private boolean fireAllowed = true;
    private boolean entityGriefingAllowed = true;

    /* ---- Interactions & access ---- */
    private boolean interactionsAllowed = true;   // general redstone/pressure plates etc (GUI label "interactions")
    private boolean redstoneAllowed = true;       // alias/back-compat for interactions
    private boolean containersAllowed = true;     // chests, barrels, etc.
    private boolean animalInteractAllowed = true; // breeding, leashing, shearing, milking...
    private boolean animalAccessAllowed = true;   // synonym used by some listeners
    private boolean petAccessAllowed = true;      // interact with tamed pets

    private boolean vehiclesAllowed = true;       // break/enter boats/minecarts
    private boolean itemFramesAllowed = true;
    private boolean armorStandsAllowed = true;
    private boolean bucketsAllowed = true;

    /* ---- Keep items (per-claim override) ---- */
    private boolean keepItemsEnabled = false;

    /* ---- Damage sub-toggles (used by DamageProtectionListener) ---- */
    private boolean damageEnabled = true;
    private boolean damageProtectOwnerAndTrusted = true;
    private boolean damageCancelAll = true;
    private boolean damagePveEnabled = true;
    private boolean damageProjectilesEnabled = true;
    private boolean damageEnvironmentEnabled = true;
    private boolean damageFireLavaEnabled = true;
    private boolean damageFallEnabled = true;
    private boolean damageExplosionsEnabled = true;
    private boolean damageDrownVoidSuffocateEnabled = true;
    private boolean damagePoisonWitherEnabled = true;

    /* ============================================================
     * Getters / Setters (kept verbose for clarity & API stability)
     * ============================================================ */
    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean v) { this.pvpEnabled = v; }

    public boolean isExplosionsAllowed() { return explosionsAllowed; }
    public void setExplosionsAllowed(boolean v) { this.explosionsAllowed = v; }

    public boolean isFireAllowed() { return fireAllowed; }
    public void setFireAllowed(boolean v) { this.fireAllowed = v; }

    public boolean isEntityGriefingAllowed() { return entityGriefingAllowed; }
    public void setEntityGriefingAllowed(boolean v) { this.entityGriefingAllowed = v; }

    public boolean isInteractionsAllowed() { return interactionsAllowed; }
    public void setInteractionsAllowed(boolean v) { this.interactionsAllowed = v; this.redstoneAllowed = v; }

    public boolean isRedstoneAllowed() { return redstoneAllowed; }
    public void setRedstoneAllowed(boolean v) { this.redstoneAllowed = v; this.interactionsAllowed = v; }

    public boolean isContainersAllowed() { return containersAllowed; }
    public void setContainersAllowed(boolean v) { this.containersAllowed = v; }

    public boolean isAnimalInteractAllowed() { return animalInteractAllowed; }
    public void setAnimalInteractAllowed(boolean v) { this.animalInteractAllowed = v; }

    public boolean isAnimalAccessAllowed() { return animalAccessAllowed; }
    public void setAnimalAccessAllowed(boolean v) { this.animalAccessAllowed = v; }

    public boolean isPetAccessAllowed() { return petAccessAllowed; }
    public void setPetAccessAllowed(boolean v) { this.petAccessAllowed = v; }

    public boolean isVehiclesAllowed() { return vehiclesAllowed; }
    public void setVehiclesAllowed(boolean v) { this.vehiclesAllowed = v; }

    public boolean isItemFramesAllowed() { return itemFramesAllowed; }
    public void setItemFramesAllowed(boolean v) { this.itemFramesAllowed = v; }

    public boolean isArmorStandsAllowed() { return armorStandsAllowed; }
    public void setArmorStandsAllowed(boolean v) { this.armorStandsAllowed = v; }

    public boolean isBucketsAllowed() { return bucketsAllowed; }
    public void setBucketsAllowed(boolean v) { this.bucketsAllowed = v; }

    public boolean isKeepItemsEnabled() { return keepItemsEnabled; }
    public void setKeepItemsEnabled(boolean v) { this.keepItemsEnabled = v; }

    /* ---- Damage group ---- */
    public boolean isDamageEnabled() { return damageEnabled; }
    public void setDamageEnabled(boolean v) { this.damageEnabled = v; }

    public boolean isDamageProtectOwnerAndTrusted() { return damageProtectOwnerAndTrusted; }
    public void setDamageProtectOwnerAndTrusted(boolean v) { this.damageProtectOwnerAndTrusted = v; }

    public boolean isDamageCancelAll() { return damageCancelAll; }
    public void setDamageCancelAll(boolean v) { this.damageCancelAll = v; }

    public boolean isDamagePveEnabled() { return damagePveEnabled; }
    public void setDamagePveEnabled(boolean v) { this.damagePveEnabled = v; }

    public boolean isDamageProjectilesEnabled() { return damageProjectilesEnabled; }
    public void setDamageProjectilesEnabled(boolean v) { this.damageProjectilesEnabled = v; }

    public boolean isDamageEnvironmentEnabled() { return damageEnvironmentEnabled; }
    public void setDamageEnvironmentEnabled(boolean v) { this.damageEnvironmentEnabled = v; }

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
}
