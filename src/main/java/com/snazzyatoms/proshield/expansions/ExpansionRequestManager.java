package com.snazzyatoms.proshield.expansions;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExpansionRequestManager {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    private final Map<UUID, ExpansionRequest> pendingRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Instant> cooldowns = new ConcurrentHashMap<>();

    public ExpansionRequestManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.messages = plugin.getMessagesUtil();
    }

    public List<ExpansionRequest> getPendingRequests() {
        return new ArrayList<>(pendingRequests.values());
    }

    public void addRequest(ExpansionRequest request) {
        pendingRequests.put(request.getRequester(), request);
    }

    public void approveRequest(UUID requester) {
        ExpansionRequest req = pendingRequests.remove(requester);
        if (req == null) return;

        Plot plot = plotManager.getPlot(req.getLocation());
        if (plot == null) return;

        // Expand plot radius
        plot.setRadius(plot.getRadius() + req.getAmount());
        plotManager.saveAll();

        Player online = Bukkit.getPlayer(requester);
        if (online != null) {
            messages.send(online, plugin.getConfig().getString("messages.expansion-approved")
                    .replace("{blocks}", String.valueOf(req.getAmount())));
        }
    }

    public void denyRequest(UUID requester, String reasonKey) {
        ExpansionRequest req = pendingRequests.remove(requester);
        if (req == null) return;

        Player online = Bukkit.getPlayer(requester);
        if (online != null) {
            String reason = plugin.getConfig().getString("messages.deny-reasons." + reasonKey,
                    "&cYour expansion request was denied.");
            messages.send(online, reason);
        }
    }

    public boolean canRequest(UUID uuid) {
        if (!plugin.getConfig().getBoolean("claims.expansion.enabled", true)) return false;
        if (cooldowns.containsKey(uuid)) {
            Instant expiry = cooldowns.get(uuid);
            return Instant.now().isAfter(expiry);
        }
        return true;
    }

    public void setCooldown(UUID uuid) {
        int hours = plugin.getConfig().getInt("claims.expansion.cooldown-hours", 6);
        cooldowns.put(uuid, Instant.now().plusSeconds(hours * 3600L));
    }

    public void openPlayerRequestMenu(Player player) {
        String title = plugin.getConfig().getString("gui.menus.expansion-request.title", "&eExpansion Request");
        int size = plugin.getConfig().getInt("gui.menus.expansion-request.size", 27);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        List<Integer> steps = plugin.getConfig().getIntegerList("claims.expansion.step-options");
        if (steps.isEmpty()) steps = Arrays.asList(10, 15, 20, 25);

        int slot = 0;
        for (int step : steps) {
            ItemStack emerald = new ItemStack(Material.EMERALD);
            ItemMeta meta = emerald.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(messages.color("&a+" + step + " blocks"));
                meta.setLore(Collections.singletonList(messages.color("&7Click to request this expansion.")));
                emerald.setItemMeta(meta);
            }
            inv.setItem(slot++, emerald);
        }

        player.openInventory(inv);
    }

    public void handleRequestClick(Player player, int amount) {
        if (!canRequest(player.getUniqueId())) {
            int hours = plugin.getConfig().getInt("claims.expansion.cooldown-hours", 6);
            messages.send(player, plugin.getConfig().getString("messages.expansion-cooldown-active")
                    .replace("{hours}", String.valueOf(hours))
                    .replace("{minutes}", "0"));
            return;
        }

        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "&cYou must be inside your claim to request an expansion.");
            return;
        }

        int maxIncrease = plugin.getConfig().getInt("claims.expansion.max-increase", 100);
        if (amount > maxIncrease) {
            messages.send(player, plugin.getConfig().getString("messages.deny-reasons.too-large"));
            return;
        }

        ExpansionRequest req = new ExpansionRequest(player.getUniqueId(), player.getLocation(), amount);
        addRequest(req);
        setCooldown(player.getUniqueId());

        messages.send(player, plugin.getConfig().getString("messages.expansion-request")
                .replace("{blocks}", String.valueOf(amount)));
    }

    public void openAdminReviewMenu(Player admin) {
        String title = plugin.getConfig().getString("gui.menus.expansion-requests.title", "&eExpansion Requests");
        int size = plugin.getConfig().getInt("gui.menus.expansion-requests.size", 45);
        Inventory inv = Bukkit.createInventory(admin, size, messages.color(title));

        List<ExpansionRequest> pending = getPendingRequests();
        if (pending.isEmpty()) {
            inv.setItem(22, simpleItem(Material.BARRIER, "&7No Pending Requests",
                    "&7There are no requests to review."));
        } else {
            int slot = 0;
            for (ExpansionRequest req : pending) {
                OfflinePlayer requester = Bukkit.getOfflinePlayer(req.getRequester());
                String name = requester != null && requester.getName() != null
                        ? requester.getName()
                        : req.getRequester().toString();

                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(messages.color("&eRequest by &f" + name));
                    List<String> lore = new ArrayList<>();
                    lore.add(messages.color("&7Amount: &a+" + req.getAmount() + " blocks"));
                    lore.add(messages.color("&7World: &f" + req.getLocation().getWorld().getName()));
                    lore.add(messages.color("&7Chunk: &f" + req.getLocation().getChunk().getX()
                            + ", " + req.getLocation().getChunk().getZ()));
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                inv.setItem(slot++, item);
            }
        }

        admin.openInventory(inv);
    }

    private ItemStack simpleItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(messages.color(name));
            List<String> colored = new ArrayList<>();
            for (String l : lore) colored.add(messages.color(l));
            meta.setLore(colored);
            item.setItemMeta(meta);
        }
        return item;
    }
}
