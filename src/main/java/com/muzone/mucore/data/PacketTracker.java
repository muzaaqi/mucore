package com.muzone.mucore.data;

import com.comphenix.protocol.PacketType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enterprise-grade packet counter.
 * Thread-safe dan High-performance.
 */
public class PacketTracker {
    
    private long lastResetTime;
    private final AtomicInteger globalCount;
    // Map untuk paket spesifik yang berisiko tinggi saja (Hemat RAM)
    private final Map<String, AtomicInteger> specificCounts;

    public PacketTracker() {
        this.lastResetTime = System.currentTimeMillis();
        this.globalCount = new AtomicInteger(0);
        this.specificCounts = new ConcurrentHashMap<>();
    }

    /**
     * @return true jika hitungan di-reset (detik baru)
     */
    public boolean incrementAndCheckReset() {
        long now = System.currentTimeMillis();
        // Reset setiap 1000ms (1 detik)
        if (now - lastResetTime > 1000) {
            lastResetTime = now;
            globalCount.set(0);
            specificCounts.values().forEach(ai -> ai.set(0));
            return true; // Menandakan detik baru dimulai
        }
        return false;
    }

    public int incrementGlobal() {
        return globalCount.incrementAndGet();
    }

    public int incrementSpecific(PacketType type) {
        // Kita gunakan String key dari PacketType name untuk efisiensi Map
        return specificCounts.computeIfAbsent(type.name(), k -> new AtomicInteger(0))
                            .incrementAndGet();
    }
}