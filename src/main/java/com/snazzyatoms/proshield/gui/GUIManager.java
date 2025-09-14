// Inside GUIManager.java

import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.ChatColor;

// ... rest of your imports and class header

    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        String title = event.getView().getTitle();
        String name = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        // ------------------------
        // MAIN MENU
        // ------------------------
        if (title.contains("ProShield Menu")) {
            switch (name.toLowerCase(Locale.ROOT)) {
                case "claim land" -> player.performCommand("claim");
                case "claim info" -> player.performCommand("proshield info");
                case "unclaim land" -> player.performCommand("unclaim");
                case "trusted players" -> openMenu(player, "roles");
                case "claim flags" -> openMenu(player, "flags");
                case "admin tools" -> openMenu(player, "admin-expansions");
            }
            return;
        }

        // ------------------------
        // FLAGS MENU
        // ------------------------
        if (title.contains("Claim Flags")) {
            Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
            if (plot == null) {
                plugin.getMessagesUtil().send(player, "&cYou must stand inside a claim.");
                return;
            }

            switch (name.toLowerCase(Locale.ROOT)) {
                case "explosions" -> toggleFlag(plot, "explosions", player);
                case "buckets" -> toggleFlag(plot, "buckets", player);
                case "item frames" -> toggleFlag(plot, "item-frames", player);
                case "armor stands" -> toggleFlag(plot, "armor-stands", player);
                case "containers" -> toggleFlag(plot, "containers", player);
                case "pets" -> toggleFlag(plot, "pets", player);
                case "pvp" -> toggleFlag(plot, "pvp", player);
                case "safe zone" -> toggleFlag(plot, "safezone", player);
                case "back" -> openMenu(player, "main");
            }

            if (!name.equalsIgnoreCase("back")) {
                openMenu(player, "flags"); // refresh GUI
            }
            return;
        }

        // ------------------------
        // ADMIN EXPANSIONS MENU
        // ------------------------
        if (title.contains("Expansion Requests")) {
            switch (name.toLowerCase(Locale.ROOT)) {
                case "approve selected" -> handleExpansionApproval(player);
                case "deny selected" -> handleExpansionDenial(player);
                case "back" -> openMenu(player, "main");
            }
            return;
        }

        // ------------------------
        // ROLES MENU (placeholder for now)
        // ------------------------
        if (title.contains("Trusted Players")) {
            if (name.equalsIgnoreCase("back")) {
                openMenu(player, "main");
            } else {
                plugin.getMessagesUtil().send(player, "&7This feature is still being developed.");
            }
        }
    }

    // ✅ Flag toggle logic (moved from old GUIListener)
    private void toggleFlag(Plot plot, String flag, Player player) {
        boolean current = plot.getFlag(flag, false);
        plot.setFlag(flag, !current);

        MessagesUtil messages = plugin.getMessagesUtil();
        if (current) {
            messages.send(player, "&c" + flag + " disabled.");
        } else {
            messages.send(player, "&a" + flag + " enabled.");
        }
    }
