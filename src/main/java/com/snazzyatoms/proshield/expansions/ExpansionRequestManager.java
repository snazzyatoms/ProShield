package com.snazzyatoms.proshield.expansions;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * ExpansionRequestManager
 * - Tracks, persists, and manages expansion requests.
 * - Enforces cooldowns (via timestamp).
 * - Provides requests for admin GUI review.
 * - On approval, expands only the plot where the request was made (per-plot radius).
 * - Fully integrated with GUI flows (player request + admin review + deny submenu).
 */
public class ExpansionRequestManager {

    private final ProShield plugin;
    private final MessagesUtil messages;

    // Latest request per player
    private final Map<UUID, ExpansionRequest> requests = new HashMap<>();

    // Admin deny submenu context: which player the admin is denying
    private final Map<UUID, UUID> pendingDenyTarget = new HashMap<>(); // admin -> requester

    private final File file;
    private final FileConfiguration data;

    public ExpansionRequestManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
        this.file = new File(plugin.getDataFolder(), "expansion-requests.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
        loadRequests();
    }

    /* =========================  Public API (model)  ========================= */

    public ExpansionRequest getLastRequest(UUID playerId) {
        return requests.get(playerId);
    }

    public List<ExpansionRequest> getPendingRequests() {
        List<ExpansionRequest> list = new ArrayList<>();
        for (ExpansionRequest req : requests.values()) {
            if (req.getStatus() == ExpansionRequest.Status.PENDING) {
                list.add(req);
            }
        }
        list.sort(Comparator.comparingLong(ExpansionRequest::getTimestamp).reversed());
        return list;
    }

    public void addRequest(ExpansionRequest request) {
        requests.put(request.getRequester(), request);
        saveRequests();

        OfflinePlayer op = Bukkit.getOfflinePlayer(request.getRequester());
        if (op.isOnline()) {
            String msg = plugin.getConfig().getString("messages.expansion-request",
                    "&eYour expansion request for +{blocks} blocks has been sent to admins.")
                .replace("{blocks}", String.valueOf(request.getBlocks()));
            messages.send(op.getPlayer(), msg);
        }
    }

    public void approveRequest(UUID playerId) {
        ExpansionRequest req = requests.get(playerId);
        if (req == null || req.getStatus() != ExpansionRequest.Status.PENDING) return;

        boolean expanded = false;
        if (req.getRequestedAt() != null) {
            expanded = plugin.getPlotManager().expandPlot(playerId, req.getRequestedAt(), req.getBlocks());
        }

        req.setStatus(ExpansionRequest.Status.APPROVED);
        saveRequests();

        OfflinePlayer op = Bukkit.getOfflinePlayer(playerId);
        if (op.isOnline()) {
            String template = plugin.getConfig().getString("messages.expansion-approved",
                    "&aYour claim expansion (+{blocks} blocks) was approved!");
            String msg = template.replace("{blocks}", String.valueOf(req.getBlocks()));
            messages.send(op.getPlayer(), msg);

            if (!expanded) {
                messages.send(op.getPlayer(),
                        "&eNote: Expansion was approved, but no new radius could be applied (not the owner or invalid plot).");
            }
        }
    }

    public void denyRequest(UUID playerId, String reasonKey) {
        ExpansionRequest req = requests.get(playerId);
        if (req == null || req.getStatus() != ExpansionRequest.Status.PENDING) return;

        req.setStatus(ExpansionRequest.Status.DENIED);
        req.setDenyReason(reasonKey);
        saveRequests();

        String reason = plugin.getConfig().getString("messages.deny-reasons." + reasonKey, "&cDenied.");
        OfflinePlayer op = Bukkit.getOfflinePlayer(playerId);
        if (op.isOnline()) {
            String msg = plugin.getConfig().getString("messages.expansion-denied",
                    "&cYour expansion request was denied: {reason}")
                .replace("{reason}", reason);
            messages.send(op.getPlayer(), msg);
        }
    }

    /* =========================  Player GUI (submit request)  ========================= */

    public void openPlayerRequestMenu(Player player) {
        FileConfiguration cfg = plugin.getConfig();

        if (!cfg.getBoolean("claims.expansion.enabled", true)) {
            messages.send(player, cfg.getString("messages.expansion-disabled", "&cExpansion requests are disabled by the server."));
            return;
        }

        // Must be inside a claim and be the owner
        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
        if (plot == null || !plot.getOwner().equals(player.getUniqueId())) {
            messages.send(player, "&cYou must stand in your own claim to request an expansion.");
            return;
        }

        // Cooldown check
        long cooldownH = cfg.getLong("claims.expansion.cooldown-hours", 6);
        long cooldownMs = cooldownH * 60L * 60L * 1000L;
        ExpansionRequest last = requests.get(player.getUniqueId());
        if (last != null && last.getStatus() == ExpansionRequest.Status.PENDING) {
            messages.send(player, "&cYou already have a pending request.");
            return;
        }
        if (last != null) {
            long since = System.currentTimeMillis() - last.getTimestamp();
            if (since < cooldownMs) {
                long remain = cooldownMs - since;
                long hrs = remain / 3_600_000L;
                long mins = (remain % 3_600_000L) / 60_000L;
                String title = cfg.getString("messages.expansion-cooldown-title", "&cCooldown Active");
                String body = cfg.getString("messages.expansion-cooldown-active", "&7You can request again in &f{hours}h {minutes}m&7.")
                        .replace("{hours}", String.valueOf(hrs))
                        .replace("{minutes}", String.valueOf(mins));
                messages.send(player, title + " " + body);
                return;
            }
        }

        // Build menu with step options
        String title = "&aRequest Expansion";
        int size = 45;
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        List<Integer> steps = cfg.getIntegerList("claims.expansion.step-options");
        if (steps == null || steps.isEmpty()) steps = Arrays.asList(10, 15, 20, 25);

        int slot = 10;
        for (Integer step : steps) {
            ItemStack item = new ItemStack(Material.EMERALD);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(messages.color("&a+" + step + " blocks"));
                meta.setLore(Arrays.asList(
                        messages.color("&7Click to request this amount"),
                        messages.color("&7This will be reviewed by admins")
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
            if ((slot % 9) == 8) slot += 3; // keep it grid-spaced nicely
        }

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm = close.getItemMeta();
        if (cm != null) {
            cm.setDisplayName(messages.color("&cExit"));
            close.setItemMeta(cm);
        }
        inv.setItem(size - 1, close);

        player.openInventory(inv);
    }

    public void handlePlayerRequestClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || clicked.getType() == Material.AIR) return;

        String name = clicked.getItemMeta().getDisplayName();
        if (name == null) return;

        if (name.contains("Exit")) {
            player.closeInventory();
            return;
        }

        // parse “+{blocks} blocks”
        String stripped = org.bukkit.ChatColor.stripColor(name);
        if (stripped == null) return;
        stripped = stripped.trim();
        if (!stripped.startsWith("+") || !stripped.toLowerCase(Locale.ROOT).contains("blocks")) return;

        int plusIdx = stripped.indexOf('+');
        int spaceIdx = stripped.indexOf(' ', plusIdx + 1);
        int amount;
        try {
            amount = Integer.parseInt(stripped.substring(plusIdx + 1, spaceIdx));
        } catch (Exception e) {
            return;
        }

        // Validations again (owner + cooldown)
        FileConfiguration cfg = plugin.getConfig();
        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
        if (plot == null || !plot.getOwner().equals(player.getUniqueId())) {
            messages.send(player, "&cYou must stand in your own claim to request an expansion.");
            return;
        }

        long cooldownH = cfg.getLong("claims.expansion.cooldown-hours", 6);
        long cooldownMs = cooldownH * 60L * 60L * 1000L;
        ExpansionRequest last = requests.get(player.getUniqueId());
        if (last != null && last.getStatus() == ExpansionRequest.Status.PENDING) {
            messages.send(player, "&cYou already have a pending request.");
            return;
        }
        if (last != null) {
            long since = System.currentTimeMillis() - last.getTimestamp();
            if (since < cooldownMs) {
                long remain = cooldownMs - since;
                long hrs = remain / 3_600_000L;
                long mins = (remain % 3_600_000L) / 60_000L;
                String title = cfg.getString("messages.expansion-cooldown-title", "&cCooldown Active");
                String body = cfg.getString("messages.expansion-cooldown-active", "&7You can request again in &f{hours}h {minutes}m&7.")
                        .replace("{hours}", String.valueOf(hrs))
                        .replace("{minutes}", String.valueOf(mins));
                messages.send(player, title + " " + body);
                return;
            }
        }

        // Max increase clamp (per request)
        int maxIncrease = cfg.getInt("claims.expansion.max-increase", 100);
        int finalAmount = Math.min(amount, maxIncrease);

        ExpansionRequest req = new ExpansionRequest(player.getUniqueId(), finalAmount, player.getLocation());
        addRequest(req);
        messages.send(player, "&aExpansion request submitted for &f+" + finalAmount + " &ablocks.");
        player.closeInventory();
    }

    /* =========================  Admin GUI (review + deny)  ========================= */

    public void openRequestMenu(Player player) {
        String title = plugin.getConfig().getString("gui.menus.expansion-requests.title", "&eExpansion Requests");
        int size = plugin.getConfig().getInt("gui.menus.expansion-requests.size", 45);

        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        int slot = 0;
        for (ExpansionRequest req : getPendingRequests()) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(req.getRequester());

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(messages.color("&f" + (target.getName() != null ? target.getName() : req.getRequester().toString())));
                List<String> lore = new ArrayList<>();
                lore.add(messages.color("&7Blocks: &b" + req.getBlocks()));
                lore.add(messages.color("&7Requested: &f" + new Date(req.getTimestamp())));
                lore.add(messages.color("&aLeft-click: Approve"));
                lore.add(messages.color("&cRight-click: Deny"));
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        if (slot == 0) {
            ItemStack item = new ItemStack(Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(messages.color("&cNo pending requests"));
                item.setItemMeta(meta);
            }
            inv.setItem(22, item);
        }

        player.openInventory(inv);
    }

    public void handleRequestClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = clicked.getItemMeta().getDisplayName();
        if (name == null) return;

        UUID targetUuid = null;
        for (ExpansionRequest req : getPendingRequests()) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(req.getRequester());
            String targetName = (op.getName() != null ? op.getName() : req.getRequester().toString());
            if (name.contains(targetName)) {
                targetUuid = req.getRequester();
                break;
            }
        }

        if (targetUuid == null) return;

        if (event.isLeftClick()) {
            approveRequest(targetUuid);
            messages.send(player, "&aApproved expansion for " + name);
            openRequestMenu(player);
        } else if (event.isRightClick()) {
            pendingDenyTarget.put(player.getUniqueId(), targetUuid);
            openDenyMenu(player);
        }
    }

    private void openDenyMenu(Player player) {
        String title = plugin.getConfig().getString("gui.menus.deny-reasons.title", "&cDeny Reasons");
        int size = 27;
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        ConfigurationSection sec = plugin.getMessagesConfig().getConfigurationSection("messages.deny-reasons");
        int slot = 10;
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                String reason = plugin.getMessagesConfig().getString("messages.deny-reasons." + key, "&c" + key);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta meta = paper.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(messages.color(reason));
                    // encode key in lore for robust parsing
                    meta.setLore(Collections.singletonList(messages.color("&8key:" + key)));
                    paper.setItemMeta(meta);
                }
                inv.setItem(slot++, paper);
                if ((slot % 9) == 8) slot += 3;
            }
        }

        // Back
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        if (bm != null) {
            bm.setDisplayName(messages.color("&7Back"));
            back.setItemMeta(bm);
        }
        inv.setItem(size - 9, back);

        // Exit
        ItemStack exit = new ItemStack(Material.BARRIER);
        ItemMeta em = exit.getItemMeta();
        if (em != null) {
            em.setDisplayName(messages.color("&cExit"));
            exit.setItemMeta(em);
        }
        inv.setItem(size - 1, exit);

        player.openInventory(inv);
    }

    public void handleDenyClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = clicked.getItemMeta().getDisplayName();
        if (name == null) return;

        String stripped = org.bukkit.ChatColor.stripColor(name);
        if (stripped == null) return;

        if (stripped.equalsIgnoreCase("Back")) {
            openRequestMenu(player);
            return;
        }
        if (stripped.equalsIgnoreCase("Exit")) {
            player.closeInventory();
            return;
        }

        // Extract key from lore line "&8key:{key}"
        String key = null;
        List<String> lore = clicked.getItemMeta().getLore();
        if (lore != null) {
            for (String line : lore) {
                String s = org.bukkit.ChatColor.stripColor(line);
                if (s != null && s.startsWith("key:")) {
                    key = s.substring("key:".length());
                    break;
                }
            }
        }
        if (key == null || key.isBlank()) return;

        UUID targetId = pendingDenyTarget.remove(player.getUniqueId());
        if (targetId == null) return;

        denyRequest(targetId, key);
        messages.send(player, "&cDenied expansion (" + key + ") for &f" + name);
        openRequestMenu(player);
    }

    /* =========================  Persistence  ========================= */

    private void loadRequests() {
        if (!data.isConfigurationSection("requests")) return;
        for (String key : data.getConfigurationSection("requests").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int blocks = data.getInt("requests." + key + ".blocks");
                long timestamp = data.getLong("requests." + key + ".timestamp");
                String statusStr = data.getString("requests." + key + ".status", "PENDING");
                String denyReason = data.getString("requests." + key + ".denyReason", null);
                Location at = (Location) data.get("requests." + key + ".location");

                ExpansionRequest req = new ExpansionRequest(uuid, blocks, at);
                req.setStatus(ExpansionRequest.Status.valueOf(statusStr));
                req.setDenyReason(denyReason);
                setTimestampUnsafe(req, timestamp);

                requests.put(uuid, req);
            } catch (Exception ignored) { }
        }
    }

    private void saveRequests() {
        data.set("requests", null);
        for (Map.Entry<UUID, ExpansionRequest> entry : requests.entrySet()) {
            ExpansionRequest req = entry.getValue();
            String path = "requests." + entry.getKey();
            data.set(path + ".blocks", req.getBlocks());
            data.set(path + ".timestamp", req.getTimestamp());
            data.set(path + ".status", req.getStatus().name());
            data.set(path + ".denyReason", req.getDenyReason());
            data.set(path + ".location", req.getRequestedAt());
        }
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save expansion-requests.yml: " + e.getMessage());
        }
    }

    /* =========================  Timestamp Injection ========================= */
    private void setTimestampUnsafe(ExpansionRequest req, long ts) {
        try {
            java.lang.reflect.Field f = ExpansionRequest.class.getDeclaredField("timestamp");
            f.setAccessible(true);
            f.setLong(req, ts);
        } catch (Throwable ignored) { }
    }
}
