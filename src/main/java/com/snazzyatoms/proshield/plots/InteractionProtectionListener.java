package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles block & container interactions inside claims with:
 * - Wilderness rules (toggleable in config)
 * - Claim rules (owner & trusted roles)
 * - Global blacklist/whitelist
 */
public class InteractionProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final Set<Material> blacklistedInteractions = new HashSet<>();

    public InteractionProtectionListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        loadConfigBlacklist();
    }

    private void loadConfigBlacklist() {
        FileConfiguration config = plugin.getConfig();
        blacklistedInteractions.clear();
        if (config.getBoolean("protection.interactions.enabled", true)) {
            List<String> list = config.getStringList("protection.interactions.list");
            for (String mat : list) {
                try {
                    blacklistedInteractions.add(Material.valueOf(mat.toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                    plugin.getLogger().warning("[ProShield] Unknown material in interaction list: " + mat);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Material type = block.getType();

        FileConfiguration config = plugin.getConfig();
        Chunk chunk = block.getChunk();
        Plot plot = plotManager.getPlot(chunk);

        // === Wilderness interactions ===
        if (plot == null) {
            if (!config.getBoolean("protection.wilderness.allow-interactions", true)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot interact with blocks in the wilderness.");
            }
            return;
        }

        // === Claim role check ===
        ClaimRole role = roleManager.getRole(plot, player);
        if (roleManager.canInteract(role)) {
            return; // trusted, allow
        }

        // === Global interaction blacklist ===
        boolean useBlacklist = config.getString("protection.interactions.mode", "blacklist")
                .equalsIgnoreCase("blacklist");

        if (useBlacklist) {
            // deny if material is blacklisted
            if (blacklistedInteractions.contains(type)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot use that inside this claim.");
            }
        } else {
            // whitelist mode: deny everything not listed
            if (!blacklistedInteractions.contains(type)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot use that inside this claim.");
            }
        }
    }

    public void reload() {
        loadConfigBlacklist();
    }
}
