package com.snazzyatoms.proshield.plots;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

/**
 * PlotSettings — per-claim flags & toggles.
 * Includes all flags referenced in listeners, plus damage sub-toggles.
 */
public class PlotSettings {

    /* High-level toggles */
    private boolean pvpEnabled = false;
    private boolean explosionsAllowed = true;
    private boolean fireAllowed = true;
    private boolean entityGriefingAllowed = true;
    private boolean interactionsAllowed = true;

    /* Finer-grain access flags */
    private boolean containersAllowed = true;
    private boolean animalInteractAllowed = true;
    private boolean armorStandsAllowed = true;
    private boolean petAccessAllowed = true;
    private boolean vehiclesAllowed = true;

    /* Keep-drops per claim */
    private boolean keepItemsEnabled = false;

    /* Damage micro-toggles (used by DamageProtectionListener) */
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

    public PlotSettings() {}

    /* ---------- Load/Save Helpers (optional) ---------- */

    public static PlotSettings fromConfig(ConfigurationSection sec) {
        PlotSettings s = new PlotSettings();
        if (sec == null) return s;

        s.pvpEnabled = sec.getBoolean("pvp", s.pvpEnabled);
        s.explosionsAllowed = sec.getBoolean("explosions", s.explosionsAllowed);
        s.fireAllowed = sec.getBoolean("fire", s.fireAllowed);
        s.entityGriefingAllowed = sec.getBoolean("entity-grief", s.entityGriefingAllowed);
        s.interactionsAllowed = sec.getBoolean("interactions", s.interactionsAllowed);

        s.containersAllowed = sec.getBoolean("containers", s.containersAllowed);
        s.animalInteractAllowed = sec.getBoolean("animal-interact", s.animalInteractAllowed);
        s.armorStandsAllowed = sec.getBoolean("armor-stands", s.armorStandsAllowed);
        s.petAccessAllowed = sec.getBoolean("pets", s.petAccessAllowed);
        s.vehiclesAllowed = sec.getBoolean("vehicles", s.vehiclesAllowed);

        s.keepItemsEnabled = sec.getBoolean("keep-items", s.keepItemsEnabled);

        ConfigurationSection dmg = sec.getConfigurationSection("damage");
        if (dmg != null) {
            s.damageProtectOwnerAndTrusted = dmg.getBoolean("protect-owner-and-trusted", s.damageProtectOwnerAndTrusted);
            s.damageCancelAll = dmg.getBoolean("cancel-all", s.damageCancelAll);
            s.damagePveEnabled = dmg.getBoolean("pve", s.damagePveEnabled);
            s.damageProjectilesEnabled = dmg.getBoolean("projectiles", s.damageProjectilesEnabled);
            s.damageEnvironmentEnabled = dmg.getBoolean("environment", s.damageEnvironmentEnabled);
            s.damageFireLavaEnabled = dmg.getBoolean("fire-lava", s.damageFireLavaEnabled);
            s.damageFallEnabled = dmg.getBoolean("fall", s.damageFallEnabled);
            s.damageExplosionsEnabled = dmg.getBoolean("explosions", s.damageExplosionsEnabled);
            s.damageDrownVoidSuffocateEnabled = dmg.getBoolean("drown-void-suffocate", s.damageDrownVoidSuffocateEnabled);
            s.damagePoisonWitherEnabled = dmg.getBoolean("poison-wither", s.damagePoisonWitherEnabled);
        }

        return s;
    }

    public void toConfig(ConfigurationSection sec) {
        if (sec == null) return;

        sec.set("pvp", pvpEnabled);
        sec.set("explosions", explosionsAllowed);
        sec.set("fire", fireAllowed);
        sec.set("entity-grief", entityGriefingAllowed);
        sec.set("interactions", interactionsAllowed);

        sec.set("containers", containersAllowed);
        sec.set("animal-interact", animalInteractAllowed);
        sec.set("armor-stands", armorStandsAllowed);
        sec.set("pets", petAccessAllowed);
        sec.set("vehicles", vehiclesAllowed);

        sec.set("keep-items", keepItemsEnabled);

        ConfigurationSection dmg = sec.getConfigurationSection("damage");
        if (dmg == null) dmg = sec.createSection("damage");
        dmg.set("protect-owner-and-trusted", damageProtectOwnerAndTrusted);
        dmg.set("cancel-all", damageCancelAll);
        dmg.set("pve", damagePveEnabled);
        dmg.set("projectiles", damageProjectilesEnabled);
        dmg.set("environment", damageEnvironmentEnabled);
        dmg.set("fire-lava", damageFireLavaEnabled);
        dmg.set("fall", damageFallEnabled);
        dmg.set("explosions", damageExplosionsEnabled);
        dmg.set("drown-void-suffocate", damageDrownVoidSuffocateEnabled);
        dmg.set("poison-wither", damagePoisonWitherEnabled);
    }

    /* ---------- Getters/Setters used by listeners ---------- */

    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean pvpEnabled) { this.pvpEnabled = pvpEnabled; }

    public boolean isExplosionsAllowed() { return explosionsAllowed; }
    public void setExplosionsAllowed(boolean explosionsAllowed) { this.explosionsAllowed = explosionsAllowed; }

    public boolean isFireAllowed() { return fireAllowed; }
    public void setFireAllowed(boolean fireAllowed) { this.fireAllowed = fireAllowed; }

    public boolean isEntityGriefingAllowed() { return entityGriefingAllowed; }
    public void setEntityGriefingAllowed(boolean entityGriefingAllowed) { this.entityGriefingAllowed = entityGriefingAllowed; }

    public boolean isInteractionsAllowed() { return interactionsAllowed; }
    public void setInteractionsAllowed(boolean interactionsAllowed) { this.interactionsAllowed = interactionsAllowed; }

    public boolean isContainersAllowed() { return containersAllowed; }
    public void setContainersAllowed(boolean containersAllowed) { this.containersAllowed = containersAllowed; }

    public boolean isAnimalInteractAllowed() { return animalInteractAllowed; }
    public void setAnimalInteractAllowed(boolean animalInteractAllowed) { this.animalInteractAllowed = animalInteractAllowed; }

    public boolean isArmorStandsAllowed() { return armorStandsAllowed; }
    public void setArmorStandsAllowed(boolean armorStandsAllowed) { this.armorStandsAllowed = armorStandsAllowed; }

    public boolean isPetAccessAllowed() { return petAccessAllowed; }
    public void setPetAccessAllowed(boolean petAccessAllowed) { this.petAccessAllowed = petAccessAllowed; }

    public boolean isVehiclesAllowed() { return vehiclesAllowed; }
    public void setVehiclesAllowed(boolean vehiclesAllowed) { this.vehiclesAllowed = vehiclesAllowed; }

    public boolean isKeepItemsEnabled() { return keepItemsEnabled; }
    public void setKeepItemsEnabled(boolean keepItemsEnabled) { this.keepItemsEnabled = keepItemsEnabled; }

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

    /* Equality by location */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Plot)) return false;
        PlotSettings that = (PlotSettings) o;
        return hashCode() == that.hashCode();
    }

    @Override
    public int hashCode() {
        // not important here; plot equality is handled in Plot, not settings.
        return Objects.hash(
                pvpEnabled, explosionsAllowed, fireAllowed, entityGriefingAllowed, interactionsAllowed,
                containersAllowed, animalInteractAllowed, armorStandsAllowed, petAccessAllowed, vehiclesAllowed,
                keepItemsEnabled,
                damageProtectOwnerAndTrusted, damageCancelAll, damagePveEnabled, damageProjectilesEnabled,
                damageEnvironmentEnabled, damageFireLavaEnabled, damageFallEnabled, damageExplosionsEnabled,
                damageDrownVoidSuffocateEnabled, damagePoisonWitherEnabled
        );
    }
}
