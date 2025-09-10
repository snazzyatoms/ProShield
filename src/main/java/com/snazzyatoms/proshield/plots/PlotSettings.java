package com.snazzyatoms.proshield.plots;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * Central per-plot flags/settings.
 * - Preserves previous flags
 * - Adds missing getters used in listeners
 * - Back-compat aliases for older method names
 */
public class PlotSettings {

    /* ----- Core toggles ----- */
    private boolean pvpEnabled = false;
    private boolean explosionsAllowed = false;
    private boolean fireAllowed = false;
    private boolean entityGriefingAllowed = false;

    // Interactions / redstone / containers / entities
    private boolean redstoneAllowed = true;         // sometimes called "interactions" in older code
    private boolean interactionsAllowed = true;     // alias used by older call-sites
    private boolean containersAllowed = true;
    private boolean itemFramesAllowed = true;
    private boolean armorStandsAllowed = true;
    private boolean vehiclesAllowed = true;

    private boolean animalInteractAllowed = true;   // legacy wording used in some files
    private boolean animalAccessAllowed = true;     // alternative wording used in some files
    private boolean petAccessAllowed = true;

    // Buckets / items
    private boolean bucketsAllowed = true;
    private boolean keepItemsEnabled = false;

    // Mob border repel / despawn
    private boolean mobRepelEnabled = false;
    private boolean mobDespawnInsideEnabled = false;

    /* ----- Damage matrix (granular) ----- */
    private boolean damageCancelAll = false;
    private boolean damageProtectOwnerAndTrusted = true;
    private boolean damagePveEnabled = true;
    private boolean damageProjectilesEnabled = true;
    private boolean damageFireLavaEnabled = true;
    private boolean damageFallEnabled = true;
    private boolean damageExplosionsEnabled = true;
    private boolean damageDrownVoidSuffocateEnabled = true;
    private boolean damagePoisonWitherEnabled = true;
    private boolean damageEnvironmentEnabled = true;

    public PlotSettings() {}

    /* ---------------------------
     * Serialize / Deserialize
     * --------------------------- */
    public Map<String, Object> serialize() {
        Map<String, Object> m = new HashMap<>();

        m.put("pvp-enabled", pvpEnabled);
        m.put("explosions-allowed", explosionsAllowed);
        m.put("fire-allowed", fireAllowed);
        m.put("entity-griefing-allowed", entityGriefingAllowed);

        m.put("redstone-allowed", redstoneAllowed);
        m.put("interactions-allowed", interactionsAllowed);
        m.put("containers-allowed", containersAllowed);
        m.put("item-frames-allowed", itemFramesAllowed);
        m.put("armor-stands-allowed", armorStandsAllowed);
        m.put("vehicles-allowed", vehiclesAllowed);

        m.put("animal-interact-allowed", animalInteractAllowed);
        m.put("animal-access-allowed", animalAccessAllowed);
        m.put("pet-access-allowed", petAccessAllowed);

        m.put("buckets-allowed", bucketsAllowed);
        m.put("keep-items-enabled", keepItemsEnabled);

        m.put("mob-repel-enabled", mobRepelEnabled);
        m.put("mob-despawn-inside-enabled", mobDespawnInsideEnabled);

        m.put("damage-cancel-all", damageCancelAll);
        m.put("damage-protect-owner-and-trusted", damageProtectOwnerAndTrusted);
        m.put("damage-pve-enabled", damagePveEnabled);
        m.put("damage-projectiles-enabled", damageProjectilesEnabled);
        m.put("damage-fire-lava-enabled", damageFireLavaEnabled);
        m.put("damage-fall-enabled", damageFallEnabled);
        m.put("damage-explosions-enabled", damageExplosionsEnabled);
        m.put("damage-drown-void-suffocate-enabled", damageDrownVoidSuffocateEnabled);
        m.put("damage-poison-wither-enabled", damagePoisonWitherEnabled);
        m.put("damage-environment-enabled", damageEnvironmentEnabled);

        return m;
    }

    public void deserialize(ConfigurationSection sec) {
        if (sec == null) return;

        pvpEnabled = sec.getBoolean("pvp-enabled", pvpEnabled);
        explosionsAllowed = sec.getBoolean("explosions-allowed", explosionsAllowed);
        fireAllowed = sec.getBoolean("fire-allowed", fireAllowed);
        entityGriefingAllowed = sec.getBoolean("entity-griefing-allowed", entityGriefingAllowed);

        redstoneAllowed = sec.getBoolean("redstone-allowed", redstoneAllowed);
        interactionsAllowed = sec.getBoolean("interactions-allowed", interactionsAllowed);
        containersAllowed = sec.getBoolean("containers-allowed", containersAllowed);
        itemFramesAllowed = sec.getBoolean("item-frames-allowed", itemFramesAllowed);
        armorStandsAllowed = sec.getBoolean("armor-stands-allowed", armorStandsAllowed);
        vehiclesAllowed = sec.getBoolean("vehicles-allowed", vehiclesAllowed);

        animalInteractAllowed = sec.getBoolean("animal-interact-allowed", animalInteractAllowed);
        animalAccessAllowed = sec.getBoolean("animal-access-allowed", animalAccessAllowed);
        petAccessAllowed = sec.getBoolean("pet-access-allowed", petAccessAllowed);

        bucketsAllowed = sec.getBoolean("buckets-allowed", bucketsAllowed);
        keepItemsEnabled = sec.getBoolean("keep-items-enabled", keepItemsEnabled);

        mobRepelEnabled = sec.getBoolean("mob-repel-enabled", mobRepelEnabled);
        mobDespawnInsideEnabled = sec.getBoolean("mob-despawn-inside-enabled", mobDespawnInsideEnabled);

        damageCancelAll = sec.getBoolean("damage-cancel-all", damageCancelAll);
        damageProtectOwnerAndTrusted = sec.getBoolean("damage-protect-owner-and-trusted", damageProtectOwnerAndTrusted);
        damagePveEnabled = sec.getBoolean("damage-pve-enabled", damagePveEnabled);
        damageProjectilesEnabled = sec.getBoolean("damage-projectiles-enabled", damageProjectilesEnabled);
        damageFireLavaEnabled = sec.getBoolean("damage-fire-lava-enabled", damageFireLavaEnabled);
        damageFallEnabled = sec.getBoolean("damage-fall-enabled", damageFallEnabled);
        damageExplosionsEnabled = sec.getBoolean("damage-explosions-enabled", damageExplosionsEnabled);
        damageDrownVoidSuffocateEnabled = sec.getBoolean("damage-drown-void-suffocate-enabled", damageDrownVoidSuffocateEnabled);
        damagePoisonWitherEnabled = sec.getBoolean("damage-poison-wither-enabled", damagePoisonWitherEnabled);
        damageEnvironmentEnabled = sec.getBoolean("damage-environment-enabled", damageEnvironmentEnabled);
    }

    /* ---------------------------
     * Getters / Setters
     * --------------------------- */
    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean pvpEnabled) { this.pvpEnabled = pvpEnabled; }

    public boolean isExplosionsAllowed() { return explosionsAllowed; }
    public void setExplosionsAllowed(boolean explosionsAllowed) { this.explosionsAllowed = explosionsAllowed; }

    public boolean isFireAllowed() { return fireAllowed; }
    public void setFireAllowed(boolean fireAllowed) { this.fireAllowed = fireAllowed; }

    public boolean isEntityGriefingAllowed() { return entityGriefingAllowed; }
    public void setEntityGriefingAllowed(boolean entityGriefingAllowed) { this.entityGriefingAllowed = entityGriefingAllowed; }

    public boolean isRedstoneAllowed() { return redstoneAllowed; }
    public void setRedstoneAllowed(boolean redstoneAllowed) { this.redstoneAllowed = redstoneAllowed; }

    public boolean isInteractionsAllowed() { return interactionsAllowed; }
    public void setInteractionsAllowed(boolean interactionsAllowed) { this.interactionsAllowed = interactionsAllowed; }

    public boolean isContainersAllowed() { return containersAllowed; }
    public void setContainersAllowed(boolean containersAllowed) { this.containersAllowed = containersAllowed; }

    public boolean isItemFramesAllowed() { return itemFramesAllowed; }
    public void setItemFramesAllowed(boolean itemFramesAllowed) { this.itemFramesAllowed = itemFramesAllowed; }

    public boolean isArmorStandsAllowed() { return armorStandsAllowed; }
    public void setArmorStandsAllowed(boolean armorStandsAllowed) { this.armorStandsAllowed = armorStandsAllowed; }

    public boolean isVehiclesAllowed() { return vehiclesAllowed; }
    public void setVehiclesAllowed(boolean vehiclesAllowed) { this.vehiclesAllowed = vehiclesAllowed; }

    public boolean isAnimalInteractAllowed() { return animalInteractAllowed; }
    public void setAnimalInteractAllowed(boolean animalInteractAllowed) { this.animalInteractAllowed = animalInteractAllowed; }

    public boolean isAnimalAccessAllowed() { return animalAccessAllowed; }
    public void setAnimalAccessAllowed(boolean animalAccessAllowed) { this.animalAccessAllowed = animalAccessAllowed; }

    public boolean isPetAccessAllowed() { return petAccessAllowed; }
    public void setPetAccessAllowed(boolean petAccessAllowed) { this.petAccessAllowed = petAccessAllowed; }

    public boolean isBucketsAllowed() { return bucketsAllowed; }
    public void setBucketsAllowed(boolean bucketsAllowed) { this.bucketsAllowed = bucketsAllowed; }

    public boolean isKeepItemsEnabled() { return keepItemsEnabled; }
    public void setKeepItemsEnabled(boolean keepItemsEnabled) { this.keepItemsEnabled = keepItemsEnabled; }

    public boolean isMobRepelEnabled() { return mobRepelEnabled; }
    public void setMobRepelEnabled(boolean mobRepelEnabled) { this.mobRepelEnabled = mobRepelEnabled; }

    public boolean isMobDespawnInsideEnabled() { return mobDespawnInsideEnabled; }
    public void setMobDespawnInsideEnabled(boolean mobDespawnInsideEnabled) { this.mobDespawnInsideEnabled = mobDespawnInsideEnabled; }

    public boolean isDamageCancelAll() { return damageCancelAll; }
    public void setDamageCancelAll(boolean damageCancelAll) { this.damageCancelAll = damageCancelAll; }

    public boolean isDamageProtectOwnerAndTrusted() { return damageProtectOwnerAndTrusted; }
    public void setDamageProtectOwnerAndTrusted(boolean v) { this.damageProtectOwnerAndTrusted = v; }

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

    /* ---- Back-compat aliases used in older code paths ---- */

    // "interactions" older references in Plot.java
    public boolean getInteractionsAllowed() { return isInteractionsAllowed(); }
    public void setInteractionsAllowedCompat(boolean v) { setInteractionsAllowed(v); } // not used directly but kept
}
