/* -------------------------------------------------------
     * Claim CRUD
     * ------------------------------------------------------- */

    public Plot getPlot(Chunk chunk) {
        if (chunk == null) return null;
        return getPlot(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public Plot getPlot(String world, int x, int z) {
        return claims.getOrDefault(world, Collections.emptyMap()).get(key(x, z));
    }

    public Plot getClaim(Location loc) {
        if (loc == null) return null;
        return getPlot(loc.getWorld().getName(), loc.getChunk().getX(), loc.getChunk().getZ());
    }

    public boolean isClaimed(Location loc) {
        return getClaim(loc) != null;
    }

    public boolean isOwner(UUID playerId, Plot plot) {
        return plot != null && playerId != null && playerId.equals(plot.getOwner());
    }

    public Collection<Plot> getClaims() {
        List<Plot> list = new ArrayList<>();
        for (Map<String, Plot> perWorld : claims.values()) {
            list.addAll(perWorld.values());
        }
        return list;
    }

    public void addPlot(Plot plot) {
        claims.computeIfAbsent(plot.getWorldName(), w -> new ConcurrentHashMap<>())
                .put(key(plot.getX(), plot.getZ()), plot);
        saveAsync(plot);
    }

    public void removePlot(Plot plot) {
        Map<String, Plot> worldClaims = claims.get(plot.getWorldName());
        if (worldClaims != null) {
            worldClaims.remove(key(plot.getX(), plot.getZ()));
        }
        saveAsync();
    }

    /**
     * Create a new claim at the given location if unclaimed.
     * 
     * @param owner Player UUID
     * @param loc   Location to claim (uses its chunk)
     * @return new Plot if created, or null if already claimed/invalid
     */
    public Plot createClaim(UUID owner, Location loc) {
        if (owner == null || loc == null) return null;

        Chunk chunk = loc.getChunk();
        if (getPlot(chunk) != null) {
            return null; // already claimed
        }

        Plot plot = new Plot(chunk, owner);
        addPlot(plot);
        return plot;
    }
