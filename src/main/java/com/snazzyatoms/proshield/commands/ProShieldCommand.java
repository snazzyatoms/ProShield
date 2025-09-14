case "approve" -> {
    if (!player.hasPermission("proshield.admin")) {
        messages.send(player, "&cNo permission.");
        return true;
    }
    if (args.length < 2) {
        messages.send(player, "&cUsage: /proshield approve <playerName|UUID>");
        return true;
    }
    UUID targetId = resolvePlayerId(args[1]);
    List<ExpansionRequest> reqs = ExpansionQueue.getRequests(targetId);
    if (reqs.isEmpty()) {
        messages.send(player, "&cNo pending requests for that player.");
        return true;
    }
    ExpansionRequest req = reqs.get(0);
    ExpansionQueue.approveRequest(req);

    Player target = Bukkit.getPlayer(targetId);
    if (target != null) {
        target.sendMessage(ChatColor.GREEN + "Your expansion request was approved!");
    }
    messages.send(player, "&aApproved expansion request for &e" + args[1]);
}

case "deny" -> {
    if (!player.hasPermission("proshield.admin")) {
        messages.send(player, "&cNo permission.");
        return true;
    }
    if (args.length < 3) {
        messages.send(player, "&cUsage: /proshield deny <playerName|UUID> <reason>");
        return true;
    }
    UUID targetId = resolvePlayerId(args[1]);
    String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
    List<ExpansionRequest> reqs = ExpansionQueue.getRequests(targetId);
    if (reqs.isEmpty()) {
        messages.send(player, "&cNo pending requests for that player.");
        return true;
    }
    ExpansionRequest req = reqs.get(0);
    ExpansionQueue.denyRequest(req, reason);

    Player target = Bukkit.getPlayer(targetId);
    if (target != null) {
        target.sendMessage(ChatColor.RED + "Your expansion request was denied: " + reason);
    }
    messages.send(player, "&cDenied expansion request for &e" + args[1] + " &7(reason: " + reason + ")");
}
