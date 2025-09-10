package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Handles:
 * - Auto-give compass on join
 * - Optional claim reminders
 * - Messages via MessagesUtil
 */
public class PlayerJoinListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public PlayerJoinListener(ProShield plugin, GUIManager guiManager, PlotManager plotManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 🔹 Auto-give compass
        boolean autoGiveEnabled = plugin.getConfig().getBoolean("autogive.compass-on-join", true);
        boolean dropIfFull = plugin.getConfig().getBoolean("compass.drop-if-full", true);

        if (autoGiveEnabled && (player.isOp() || player.hasPermission("proshield.compass"))) {
            guiManager.giveCompass(player, dropIfFull);
            messages.send(player, "compass.given");
        }

        // 🔹 Claim reminder
        if (plotManager.hasAnyClaim(player.getUniqueId())) {
            // Remind about their claims
            messages.send(player, "claims.reminder");
        } else {
            // Encourage claiming land
            messages.send(player, "claims.no-claims-reminder");
        }
    }
}
