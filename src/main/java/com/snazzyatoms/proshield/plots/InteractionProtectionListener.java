package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Set;

/**
 * Handles container & interaction protection inside claims.
 * - Role-based checks for doors, trapdoors, buttons, levers, etc.
 * - Configurable blacklist/whitelist for interactions
 */
public class InteractionProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public InteractionProtectionListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("proshield.bypass")) return; // admins bypass

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        Chunk chunk = block.getChunk();
        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return; // wilderness, allow unless disabled globally

        ClaimRole role = roleManager.getRole(plot, player);

        FileConfiguration config = plugin.getConfig();
        boolean enabled = config.getBoolean("protection.interactions.enabled", true);
        if (!enabled) return;

        // Determine interaction protection mode
        String mode = config.getString("protection.interactions.mode", "blacklist");
        Set<String> categories = Set.copyOf(config.getStringList("protection.interactions.categories"));
        Set<String> list = Set.copyOf(config.getStringList("protection.interactions.list"));

        Material type = block.getType();
        String materialName = type.name().toLowerCase();

        boolean restricted;
        if (mode.equalsIgnoreCase("blacklist")) {
            restricted = list.contains(materialName) || categories.contains(getCategory(type));
        } else {
            restricted = !(list.contains(materialName) || categories.contains(getCategory(type)));
        }

        if (restricted && !roleManager.canInteract(role)) {
            event.setCancelled(true);
            messages.send(player, "protection.interact-denied", "%block%", type.name());
        }
    }

    /**
     * Maps block types into configured categories (e.g., doors, buttons, etc.)
     */
    private String getCategory(Material type) {
        String name = type.name().toLowerCase();
        if (name.contains("door")) return "doors";
        if (name.contains("trapdoor")) return "trapdoors";
        if (name.contains("gate")) return "fence_gates";
        if (name.contains("button")) return "buttons";
        if (name.contains("lever")) return "levers";
        if (name.contains("pressure_plate")) return "pressure_plates";
        return "other";
    }
}
