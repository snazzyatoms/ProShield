package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ProShieldCommand implements CommandExecutor, TabCompleter {

    private final ProShield plugin;
    private final PlotManager plots;
    private final GUIManager gui;

    public ProShieldCommand(ProShield plugin, PlotManager plots, GUIManager gui) {
        this.plugin = plugin;
        this.plots = plots;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length == 0) {
            gui.openMain(p, p.hasPermission("proshield.admin.gui"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "claim" -> {
                boolean ok = plots.createClaim(p.getUniqueId(), p.getLocation());
                p.sendMessage(ok ? green("Claim created.") : red("Cannot claim here."));
            }
            case "unclaim" -> {
                boolean ok = plots.removeClaim(p.getUniqueId(), p.getLocation(), false);
                p.sendMessage(ok ? green("Claim removed.") : red("You don't own this claim."));
            }
            case "info" -> {
                var c = plots.getClaim(p.getLocation());
                if (c.isEmpty()) p.sendMessage(gray("Wilderness."));
                else p.sendMessage(green("Owner: ") + plots.ownerName(c.get().getOwner()));
            }
            case "compass" -> {
                boolean admin = p.hasPermission("proshield.admin.gui");
                gui.giveCompass(p, admin);
                p.sendMessage(green("Compass given."));
            }
            case "trust" -> {
                if (args.length < 2) { p.sendMessage(red("Usage: /proshield trust <player> [role]")); break; }
                var t = plugin.getServer().getPlayerExact(args[1]);
                if (t == null) { p.sendMessage(red("Player not found.")); break; }
                boolean ok = plots.trust(p.getUniqueId(), p.getLocation(), t.getUniqueId());
                p.sendMessage(ok ? green("Trusted " + t.getName()) : red("Failed to trust here."));
            }
            case "untrust" -> {
                if (args.length < 2) { p.sendMessage(red("Usage: /proshield untrust <player>")); break; }
                var t = plugin.getServer().getPlayerExact(args[1]);
                if (t == null) { p.sendMessage(red("Player not found.")); break; }
                boolean ok = plots.untrust(p.getUniqueId(), p.getLocation(), t.getUniqueId());
                p.sendMessage(ok ? green("Untrusted " + t.getName()) : red("Failed to untrust here."));
            }
            case "trusted" -> {
                var list = plots.listTrusted(p.getLocation());
                p.sendMessage(gray("Trusted: ") + String.join(", ", list));
            }
            case "bypass" -> {
                if (!p.hasPermission("proshield.bypass")) { p.sendMessage(red("No permission.")); break; }
                p.sendMessage(gray("Bypass not implemented as toggle command in this minimal ref; use admin GUI toggles."));
            }
            case "preview" -> { p.sendMessage(gray("Preview coming soon.")); }
            case "transfer" -> { p.sendMessage(gray("Transfer via GUI planned; use /proshield in future builds.")); }
            case "purgeexpired" -> {
                if (!p.hasPermission("proshield.admin.expired.purge")) { p.sendMessage(red("No permission.")); break; }
                int days = plugin.getConfig().getInt("expiry.days", 30);
                int removed = plots.cleanupExpiredClaims(days, true);
                p.sendMessage(green("Expired removed: " + removed));
            }
            case "debug" -> {
                if (!p.hasPermission("proshield.admin.debug")) { p.sendMessage(red("No permission.")); break; }
                boolean on = args.length >= 2 && (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true"));
                boolean off = args.length >= 2 && (args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("false"));
                boolean cur = plugin.getConfig().getBoolean("proshield.debug", false);
                boolean next = cur;
                if (on) next = true; else if (off) next = false; else next = !cur;
                plugin.getConfig().set("proshield.debug", next);
                plugin.saveConfig();
                p.sendMessage(gray("Debug: ") + (next ? green("ON") : red("OFF")));
            }
            case "reload" -> {
                if (!p.hasPermission("proshield.admin.reload")) { p.sendMessage(red("No permission.")); break; }
                plugin.reloadAllConfigs();
                p.sendMessage(green("ProShield reloaded."));
            }
            default -> p.sendMessage(red("Unknown subcommand."));
        }
        return true;
    }

    private String green(String s) { return ChatColor.GREEN + s; }
    private String red(String s) { return ChatColor.RED + s; }
    private String gray(String s) { return ChatColor.GRAY + s; }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> base = List.of("claim","unclaim","info","compass","trust","untrust","trusted",
                "bypass","preview","transfer","purgeexpired","debug","reload");
        if (args.length == 1) return prefix(base, args[0]);
        if (args.length == 2 && args[0].equalsIgnoreCase("trust")) return new ArrayList<>();
        return new ArrayList<>();
    }

    private List<String> prefix(List<String> list, String a0) {
        String low = a0.toLowerCase();
        List<String> out = new ArrayList<>();
        for (String s : list) if (s.startsWith(low)) out.add(s);
        return out;
    }
}
