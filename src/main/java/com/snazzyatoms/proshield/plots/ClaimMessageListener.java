package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Shows enter/leave messages when crossing claim boundaries.
 * Modes: chat/title/actionbar (config: messages.show-as)
 */
public class ClaimMessageListener implements Listener {

    private final PlotManager plotManager;
    private String mode;

    public ClaimMessageListener(PlotManager plotManager) {
        this.plotManager = plotManager;
        reload();
    }

    public void reload() {
        mode = ProShield.getInstance().getConfig().getString("messages.show-as", "actionbar").toLowerCase();
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getChunk().equals(e.getTo().getChunk())) return;
        Player p = e.getPlayer();

        var fromClaim = plotManager.getClaim(e.getFrom()).orElse(null);
        var toClaim   = plotManager.getClaim(e.getTo()).orElse(null);
        if (fromClaim == toClaim) return;

        if (fromClaim != null && toClaim == null) {
            String owner = plotManager.ownerName(fromClaim.getOwner());
            String msg = format("&7Leaving &f" + owner + "&7's claim.");
            send(p, msg);
        } else if (toClaim != null && fromClaim == null) {
            String owner = plotManager.ownerName(toClaim.getOwner());
            String msg = format("&bEntering &f" + owner + "&b's claim.");
            send(p, msg);
        } else if (toClaim != null && fromClaim != null && !fromClaim.key().equals(toClaim.key())) {
            String owner = plotManager.ownerName(toClaim.getOwner());
            String msg = format("&bEntering &f" + owner + "&b's claim.");
            send(p, msg);
        }
    }

    private String format(String s) {
        return ChatColor.translateAlternateColorCodes('&',
                ProShield.getInstance().getConfig().getString("messages.prefix", "&3[ProShield]&r ") + s);
    }

    private void send(Player p, String msg) {
        switch (mode) {
            case "chat" -> p.sendMessage(msg);
            case "title" -> p.sendTitle(ChatColor.stripColor(msg), "", 5, 40, 10);
            default -> p.sendActionBar(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }
}
