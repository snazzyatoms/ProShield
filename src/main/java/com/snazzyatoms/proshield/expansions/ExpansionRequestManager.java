package com.snazzyatoms.proshield.expansions;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ExpansionRequestManager {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;
    private final Map<UUID, ExpansionRequest> pending = new HashMap<>();

    public ExpansionRequestManager(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    /**
     * Player-facing menu: choose how much to expand
     */
    public void openPlayerRequestMenu(Player player) {
        FileConfiguration cfg = plugin.getConfig();
        String title = cfg.getString("gui.menus.expansion-request.title", "&eExpansion Request");
        int size = cfg.getInt("gui.menus.expansion-request.size", 27);

        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));
        List<Integer> steps = cfg.getIntegerList("claims.expansion.step-options");

        int slot = 0;
        for (int step : steps) {
            ItemStack item = new ItemStack(Material.EMERALD);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(messages.color("&a+" + step + " blocks"));
                meta.setLore(Collections.singletonList(messages.color("&7Request an increase of " + step + " blocks")));
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        player.openInventory(inv);
    }

    /**
     * Called when a player selects a step option.
     */
    public void handlePlayerRequestClick(Player player, int step) {
        UUID uuid = player.getUniqueId();

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null) {
            messages.send(player, "&cYou must be inside your claim to request an expansion.");
            return;
        }

        // cooldown check
        // (for simplicity: not persisted; you can extend)
        ExpansionRequest req = new ExpansionRequest(uuid, plot.getId(), step, System.currentTimeMillis());
        pending.put(uuid, req);

        messages.send(player, plugin.getConfig().getString("messages.expansion-request", "&eExpansion request submitted.")
                .replace("{blocks}", String.valueOf(step)));
    }

    /**
     * Admin menu: review requests
     */
    public void openAdminReviewMenu(Player admin) {
        String title = plugin.getConfig().getString("gui.menus.expansion-requests.title", "&eExpansion Requests");
        int size = plugin.getConfig().getInt("gui.menus.expansion-requests.size", 45);

        Inventory inv = Bukkit.createInventory(admin, size, messages.color(title));

        if (pending.isEmpty()) {
            inv.setItem(22, buildInfoItem("&7No Pending Requests", "&7There are no requests to review."));
        } else {
            int slot = 0;
            for (ExpansionRequest req : pending.values()) {
                OfflinePlayer p = Bukkit.getOfflinePlayer(req.getRequester());
                String name = (p.getName() != null ? p.getName() : req.getRequester().toString());

                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(messages.color("&eRequest by &f" + name));
                    List<String> lore = new ArrayList<>();
                    lore.add(messages.color("&7Claim ID: &f" + req.getPlotId()));
                    lore.add(messages.color("&7Amount: &a+" + req.getAmount() + " blocks"));
                    lore.add(messages.color("&7Requested: " +
                            new Date(req.getTimestamp()).toString()));
                    lore.add(messages.color("&aLeft-click: Approve"));
                    lore.add(messages.color("&cRight-click: Deny"));
                    item.setItemMeta(meta);
                    meta.setLore(lore);
                }
                inv.setItem(slot++, item);
            }
        }

        admin.openInventory(inv);
    }

    private ItemStack buildInfoItem(String name, String... lore) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(messages.color(name));
            List<String> l = new ArrayList<>();
            for (String s : lore) l.add(messages.color(s));
            meta.setLore(l);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Approve request
     */
    public void approveRequest(UUID requesterId) {
        ExpansionRequest req = pending.remove(requesterId);
        if (req == null) return;

        Plot plot = plotManager.getPlot(req.getPlotId());
        if (plot == null) return;

        plotManager.expandPlot(plot.getId(), req.getAmount());

        Player player = Bukkit.getPlayer(requesterId);
        if (player != null) {
            String msg = plugin.getConfig().getString("messages.expansion-approved",
                    "&aYour claim expansion was approved (+{blocks})!");
            messages.send(player, msg.replace("{blocks}", String.valueOf(req.getAmount())));
        }
    }

    /**
     * Deny request
     */
    public void denyRequest(UUID requesterId, String reasonKey) {
        ExpansionRequest req = pending.remove(requesterId);
        if (req == null) return;

        Player player = Bukkit.getPlayer(requesterId);
        if (player != null) {
            String template = plugin.getConfig().getString("messages.deny-reasons." + reasonKey,
                    "&cYour expansion request was denied.");
            messages.send(player, template);
        }
    }

    public Collection<ExpansionRequest> getPendingRequests() {
        return pending.values();
    }
}
