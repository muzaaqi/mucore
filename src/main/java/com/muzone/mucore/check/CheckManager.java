package com.muzone.mucore.check;

import com.muzone.mucore.MuCore;
import java.util.ArrayList;
import java.util.List;

public class CheckManager {
    private final MuCore plugin;
    private final List<Check> checks = new ArrayList<>();

    public CheckManager(MuCore plugin) {
        this.plugin = plugin;
    }

    public void register(Check check) {
        checks.add(check);
        plugin.getLogger().info("Registered check: " + check.getName());
    }

    public List<Check> getChecks() {
        return checks;
    }
    
    // Fungsi untuk mematikan check tertentu (Feature Toggle) bisa ditambahkan disini
}