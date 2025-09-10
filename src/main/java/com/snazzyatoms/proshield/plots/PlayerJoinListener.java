package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Handles join events:
 * - Auto-give ProShield compass to players (if enabled)
 * - Sends configurable join messages
 * - Optionally remind players to claim land
 */
public class PlayerJoinListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final MessagesUtil messages;

    public PlayerJoinListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();

        boolean autoGive = config.getBoolean("autogive.compass-on-join", true);

        // Give compass to OPs or players with permission
        if (autoGive && (player.isOp() || player.hasPermission("proshield.compass"))) {
            guiManager.giveCompass(player, true);
            messages.send(player, "compass.given");
        }

        // Optional claim reminder
        boolean remindClaims = config.getBoolean("messages.remind-claim-on-join", true);
        if (remindClaims && !plugin.getPlotManager().hasAnyClaim(player.getUniqueId())) {
            messages.send(player, "claims.reminder");
        }
    }
}
