// src/main/java/com/snazzyatoms/proshield/expansion/ExpansionRequestManager.java
package com.snazzyatoms.proshield.expansion;

import java.util.*;

public class ExpansionRequestManager {
    private static final List<ExpansionRequest> requests = new ArrayList<>();

    public static void addRequest(ExpansionRequest request) {
        requests.add(request);
    }

    public static List<ExpansionRequest> getRequests() {
        return Collections.unmodifiableList(requests);
    }

    public static void removeRequest(ExpansionRequest request) {
        requests.remove(request);
    }

    public static boolean hasRequests() {
        return !requests.isEmpty();
    }
}
