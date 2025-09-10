package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Handles PvE and environment damage:
 * - Wilderness rules (configurable)
 * - Per-claim rules (owner can override in GUI/flags)
 * - Global defaults (from config)
 */
public class DamageProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public DamageProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        FileConfiguration config = plugin.getConfig();
        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        // === Wilderness rules ===
        if (plot == null) {
            if (!config.getBoolean("protection.wilderness.allow-damage", true)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou are protected from damage in the wilderness.");
            }
            return;
        }

        // === Claim rules ===
        boolean claimDamageEnabled = plot.getSettings().isDamageEnabled();
        if (!claimDamageEnabled) {
            event.setCancelled(true);
            return;
        }

        // === Global damage toggles (apply inside claims if enabled) ===
        switch (event.getCause()) {
            case FALL -> {
                if (!config.getBoolean("protection.damage.fall", true)) {
                    event.setCancelled(true);
                }
            }
            case FIRE, FIRE_TICK, LAVA -> {
                if (!config.getBoolean("protection.damage.fire-lava", true)) {
                    event.setCancelled(true);
                }
            }
            case DROWNING, VOID, SUFFOCATION -> {
                if (!config.getBoolean("protection.damage.drown-void-suffocate", true)) {
                    event.setCancelled(true);
                }
            }
            case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> {
                if (!config.getBoolean("protection.damage.explosions", true)) {
                    event.setCancelled(true);
                }
            }
            case POISON, WITHER -> {
                if (!config.getBoolean("protection.damage.poison-wither", true)) {
                    event.setCancelled(true);
                }
            }
            default -> {
                if (!config.getBoolean("protection.damage.environment", true)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        if (!(victim instanceof Player player)) return;

        Entity damager = event.getDamager();
        FileConfiguration config = plugin.getConfig();

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        // === Wilderness PvE rules ===
        if (plot == null) {
            if (!config.getBoolean("protection.wilderness.allow-pve", true)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cMobs cannot hurt you in the wilderness.");
            }
            return;
        }

        // === Claim PvE rules ===
        boolean allowClaimPvE = plot.getSettings().isPveEnabled();
        if (!allowClaimPvE) {
            event.setCancelled(true);
            return;
        }

        // Fallback to global
        if (!config.getBoolean("protection.damage.pve", true)) {
            event.setCancelled(true);
        }
    }
}
