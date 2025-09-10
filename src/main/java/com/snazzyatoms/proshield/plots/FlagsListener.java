package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Handles GUI flag toggling for claims.
 * Uses PlotSettings explicit booleans instead of generic setFlag.
 */
public class FlagsListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public FlagsListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlagToggle(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        if (inv == null || inv.getTitle() == null) return;

        if (!inv.getTitle().equalsIgnoreCase("§dClaim Flags")) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            messages.send(player, "general.claim-required");
            return;
        }

        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.isOwnerOrCoOwner(role)) {
            messages.send(player, "claims.not-owner");
            return;
        }

        PlotSettings settings = plot.getSettings();
        Material type = clicked.getType();

        switch (type) {
            case DIAMOND_SWORD -> {
                settings.setPvpEnabled(!settings.isPvpEnabled());
                messages.send(player, "protection.pvp-disabled");
            }
            case TNT -> {
                settings.setExplosionsEnabled(!settings.isExplosionsEnabled());
                messages.send(player, "protection.explosion-denied");
            }
            case FLINT_AND_STEEL -> {
                settings.setFireEnabled(!settings.isFireEnabled());
                messages.send(player, "protection.fire-denied");
            }
            case ENDERMAN_SPAWN_EGG -> {
                settings.setEntityGriefingAllowed(!settings.isEntityGriefingAllowed());
                messages.send(player, "protection.entity-grief-denied");
            }
            case REDSTONE -> {
                settings.setRedstoneEnabled(!settings.isRedstoneEnabled());
                messages.send(player, "protection.redstone-denied");
            }
            case CHEST -> {
                settings.setContainersEnabled(!settings.isContainersEnabled());
                messages.send(player, "protection.container-denied");
            }
            case ITEM_FRAME -> {
                settings.setItemFramesAllowed(!settings.isItemFramesAllowed());
                messages.send(player, "protection.item-frame-denied");
            }
            case ARMOR_STAND -> {
                settings.setArmorStandsAllowed(!settings.isArmorStandsAllowed());
                messages.send(player, "protection.armor-stand-denied");
            }
            case MINECART -> {
                settings.setVehiclesAllowed(!settings.isVehiclesAllowed());
                messages.send(player, "protection.vehicle-denied");
            }
            default -> {
                // Do nothing
            }
        }

        // Persist change
        plotManager.savePlot(plot);
        player.closeInventory();
    }
}
