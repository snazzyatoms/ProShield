package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Manages all land claims (plots).
 */
public class PlotManager {

    private final ProShield plugin;
    private final MessagesUtil messages;

    // Map chunkKey -> Plot
    private final Map<String, Plot> plots = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
    }

    // ---------------------------
    // Helpers
    // ---------------------------
    private String chunkKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getChunk().getX() + ":" + loc.getChunk().getZ();
    }

    public Plot getPlot(Location loc) {
        return plots.get(chunkKey(loc));
    }

    public boolean isClaimed(Location loc) {
        return plots.containsKey(chunkKey(loc));
    }

    // ---------------------------
    // Claim Actions
    // ---------------------------
    public void claimPlot(Player player) {
        Location loc = player.getLocation();
        String key = chunkKey(loc);

        if (plots.containsKey(key)) {
            messages.send(player, "&cThis chunk is already claimed!");
            return;
        }

        Plot plot = new Plot(player.getUniqueId(), loc.getChunk());
        plots.put(key, plot);

        messages.send(player, "&aYou have successfully claimed this land.");
    }

    public void unclaimPlot(Player player) {
        Location loc = player.getLocation();
        String key = chunkKey(loc);

        Plot plot = plots.get(key);
        if (plot == null) {
            messages.send(player, "&cThis chunk is not claimed.");
            return;
        }

        if (!plot.getOwner().equals(player.getUniqueId())) {
            messages.send(player, "&cYou don’t own this land.");
            return;
        }

        plots.remove(key);
        messages.send(player, "&cYou unclaimed this land.");
    }

    public void sendClaimInfo(Player player) {
        Location loc = player.getLocation();
        Plot plot = getPlot(loc);

        if (plot == null) {
            messages.send(player, "&eThis chunk is unclaimed.");
            return;
        }

        String ownerName = Bukkit.getOfflinePlayer(plot.getOwner()).getName();
        messages.send(player, "&6--- Claim Info ---");
        messages.send(player, "&eOwner: &f" + ownerName);
        messages.send(player, "&eTrusted: &f" + plot.getTrusted().size() + " players");
        messages.send(player, "&eFlags: &f" + (plot.getFlags().isEmpty() ? "None" : plot.getFlags().keySet()));
    }

    // ---------------------------
    // Flags GUI
    // ---------------------------
    public void openFlagsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "ProShield Flags");

        // Example flags
        addFlagItem(inv, 11, "Mob Spawning", "mob-spawning", Material.ZOMBIE_HEAD, player);
        addFlagItem(inv, 13, "Explosions", "explosions", Material.TNT, player);
        addFlagItem(inv, 15, "Fire Spread", "fire-spread", Material.FLINT_AND_STEEL, player);

        player.openInventory(inv);
    }

    private void addFlagItem(Inventory inv, int slot, String name, String key, Material mat, Player player) {
        Location loc = player.getLocation();
        Plot plot = getPlot(loc);

        boolean enabled = plot != null && plot.getFlag(key, false);

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName((enabled ? ChatColor.GREEN : ChatColor.RED) + name);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Current: " + (enabled ? "Enabled" : "Disabled"));
            lore.add(ChatColor.YELLOW + "Click to toggle");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        inv.setItem(slot, item);
    }

    public void toggleFlag(Player player, String key) {
        Plot plot = getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "&cThis land is not claimed.");
            return;
        }

        boolean current = plot.getFlag(key, false);
        plot.setFlag(key, !current);

        messages.send(player, "&aFlag '" + key + "' set to " + !current);
    }

    // ---------------------------
    // Internal Save/Load (TODO)
    // ---------------------------
    public Map<String, Plot> getPlots() {
        return plots;
    }
}
