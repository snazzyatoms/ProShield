    /* -------------------
     * Compatibility Shims (for GUIManager 1.2.6)
     * ------------------- */

    /** GUI expects createRequest(UUID, UUID, int, String) */
    public ExpansionRequest createRequest(UUID requester, UUID reviewer, int amount, String reason) {
        ExpansionRequest req = createRequest(requester, amount);
        if (reason != null && !reason.isBlank()) {
            req.setDenialReason(reason); // optional metadata
        }
        if (reviewer != null) {
            req.setReviewedBy(reviewer);
        }
        return req;
    }

    /** GUI expects approve(String id, UUID reviewer, String note) */
    public void approve(String id, UUID reviewer, String note) {
        try {
            UUID reqId = UUID.fromString(id);
            ExpansionRequest req = byId.get(reqId);
            if (req != null) {
                approveRequest(req, reviewer);
                if (note != null && !note.isBlank()) {
                    req.setDenialReason(note); // treat as moderator note
                }
                save();
            }
        } catch (IllegalArgumentException ignored) {}
    }

    /** GUI expects deny(String id, UUID reviewer, String reason) */
    public void deny(String id, UUID reviewer, String reason) {
        try {
            UUID reqId = UUID.fromString(id);
            ExpansionRequest req = byId.get(reqId);
            if (req != null) {
                denyRequest(req, reviewer, reason);
            }
        } catch (IllegalArgumentException ignored) {}
    }

    /** GUI expects submitRequest(Player, int) */
    public ExpansionRequest submitRequest(org.bukkit.entity.Player player, int amount) {
        if (player == null) return null;
        return createRequest(player.getUniqueId(), amount);
    }

    /** 🔧 GUI expects getAllPending() */
    public List<ExpansionRequest> getAllPending() {
        return getPendingRequests();
    }

    /** 🔧 GUI expects getAllByPlayer(UUID) */
    public List<ExpansionRequest> getAllByPlayer(UUID requester) {
        return getAllRequestsFor(requester);
    }
}
