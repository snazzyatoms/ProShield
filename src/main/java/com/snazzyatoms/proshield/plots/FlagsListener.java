package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Handles clicks in the Claim Flags GUI and toggles per-claim flags.
 * Preserves prior behavior and extends to use MessagesUtil + PlotSettings API.
 *
 * Title expected: "§dClaim Flags" (from GUIManager).
 */
public class FlagsListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final MessagesUtil msg;

    public FlagsListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plots = plotManager;
        this.roles = roleManager;
        this.msg = plugin.getMessagesUtil();
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlagsClick(InventoryClickEvent event) {
        HumanEntity who = event.getWhoClicked();
        if (!(who instanceof Player player)) return;

        final String title = event.getView().getTitle();
        if (title == null || !title.equals("§dClaim Flags")) return;

        event.setCancelled(true); // GUI only

        final Plot plot = plots.getClaim(player.getLocation());
        if (plot == null) {
            msg.send(player, "errors.not-in-claim");
            return;
        }

        // Only owner or co-owner should be able to toggle flags
        if (!plot.isOwner(player.getUniqueId())) {
            ClaimRole r = roles.getRole(plot, player.getUniqueId());
            if (r != ClaimRole.COOWNER) {
                msg.send(player, "errors.not-owner");
                return;
            }
        }

        final Material clicked = event.getCurrentItem() != null ? event.getCurrentItem().getType() : null;
        if (clicked == null) return;

        boolean newValue;
        switch (clicked) {
            case DIAMOND_SWORD -> {
                newValue = !plot.getSettings().isPvpEnabled();
                plot.getSettings().setPvpEnabled(newValue);
                plots.saveAsync(plot);
                msg.send(player, "flags.toggled", "pvp", msg.onOff(newValue));
            }
            case TNT -> {
                newValue = !plot.getSettings().isExplosionsAllowed();
                plot.getSettings().setExplosionsAllowed(newValue);
                plots.saveAsync(plot);
                msg.send(player, "flags.toggled", "explosions", msg.onOff(newValue));
            }
            case FLINT_AND_STEEL -> {
                newValue = !plot.getSettings().isFireAllowed();
                plot.getSettings().setFireAllowed(newValue);
                plots.saveAsync(plot);
                msg.send(player, "flags.toggled", "fire", msg.onOff(newValue));
            }
            case ENDERMAN_SPAWN_EGG -> {
                newValue = !plot.getSettings().isEntityGriefingAllowed();
                plot.getSettings().setEntityGriefingAllowed(newValue);
                plots.saveAsync(plot);
                msg.send(player, "flags.toggled", "entity-grief", msg.onOff(newValue));
            }
            case REDSTONE -> {
                newValue = !plot.getSettings().isRedstoneAllowed();
                plot.getSettings().setRedstoneAllowed(newValue);
                plots.saveAsync(plot);
                msg.send(player, "flags.toggled", "interactions", msg.onOff(newValue));
            }
            case CHEST -> {
                newValue = !plot.getSettings().isContainersAllowed();
                plot.getSettings().setContainersAllowed(newValue);
                plots.saveAsync(plot);
                msg.send(player, "flags.toggled", "containers", msg.onOff(newValue));
            }
            case LEAD -> {
                newValue = !plot.getSettings().isAnimalInteractAllowed();
                plot.getSettings().setAnimalInteractAllowed(newValue);
                plots.saveAsync(plot);
                msg.send(player, "flags.toggled", "animals", msg.onOff(newValue));
            }
            case MINECART -> {
                newValue = !plot.getSettings().isVehiclesAllowed();
                plot.getSettings().setVehiclesAllowed(newValue);
                plots.saveAsync(plot);
                msg.send(player, "flags.toggled", "vehicles", msg.onOff(newValue));
            }
            default -> {
                // ignore filler/back buttons here; handled by GUIManager if needed
            }
        }
    }
}
