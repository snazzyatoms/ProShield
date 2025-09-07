// path: src/main/java/com/snazzyatoms/proshield/plots/ClaimRole.java
package com.snazzyatoms.proshield.plots;

public enum ClaimRole {
    VISITOR(0),
    MEMBER(1),
    CONTAINER(2),
    BUILDER(3),
    CO_OWNER(4);

    private final int rank;
    ClaimRole(int rank) { this.rank = rank; }
    public int rank() { return rank; }

    public boolean atLeast(ClaimRole other) {
        return this.rank >= other.rank;
    }

    public static ClaimRole from(String s, ClaimRole def) {
        if (s == null) return def;
        try { return ClaimRole.valueOf(s.toUpperCase()); }
        catch (IllegalArgumentException e) { return def; }
    }
}
