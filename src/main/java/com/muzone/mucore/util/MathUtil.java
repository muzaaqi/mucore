package com.muzone.mucore.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class untuk operasi matematika geometris dan vektor.
 * Dirancang dengan optimasi performa (High Performance Math).
 */
public final class MathUtil {

    // Private constructor agar class ini tidak bisa di-instantiate
    private MathUtil() {}

    /**
     * Menghitung selisih terkecil antara dua sudut (Derajat).
     * Berguna untuk OmniSprint, KillAura (Angle check), dan Rotation check.
     * * @param alpha Sudut pertama
     * @param beta Sudut kedua
     * @return Selisih absolut (0.0 - 180.0)
     */
    public static double getAngleDifference(float alpha, float beta) {
        // Normalisasi selisih agar selalu positif (0 - 360)
        float phi = Math.abs(beta - alpha) % 360;
        
        // Cari jarak terpendek (wrap around)
        // Contoh: Jarak antara 350 derajat dan 10 derajat adalah 20, bukan 340.
        return phi > 180 ? 360 - phi : phi;
    }

    /**
     * Membulatkan angka desimal agar log/alert lebih rapi.
     * @param value Angka asli
     * @param places Jumlah angka di belakang koma
     * @return Angka yang sudah dibulatkan
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Menghitung jarak horizontal (X dan Z saja) tanpa akar kuadrat (Squared).
     * Lebih cepat daripada Math.hypot() jika hanya untuk perbandingan limit.
     * (Berguna untuk optimasi Speed Check nanti)
     */
    public static double offset2DSquared(Vector v1, Vector v2) {
        double x = v1.getX() - v2.getX();
        double z = v1.getZ() - v2.getZ();
        return (x * x) + (z * z);
    }

    /**
     * Menghitung jarak horizontal (X dan Z) secara presisi.
     */
    public static double offset2D(Location l1, Location l2) {
        return Math.hypot(l1.getX() - l2.getX(), l1.getZ() - l2.getZ());
    }

    /**
     * Mengubah Vector gerakan menjadi Yaw (Arah hadap).
     * Penting untuk membandingkan arah lari vs arah pandang.
     * * @param deltaX Perubahan posisi X
     * @param deltaZ Perubahan posisi Z
     * @return Sudut (Yaw) dalam derajat
     */
    public static float getYawFromVector(double deltaX, double deltaZ) {
        double yaw = Math.toDegrees(Math.atan2(-deltaX, deltaZ));
        return (float) yaw;
    }
    
    /**
     * Simulasi pergerakan Vanilla untuk deteksi akurat.
     * Menghitung gesekan (friction) berdasarkan blok yang diinjak.
     */
    public static float getFriction(float lastFriction) {
        // 0.91 adalah gesekan udara/blok standar, 
        // nanti bisa dikembangkan untuk mendeteksi Ice/Slime
        return lastFriction * 0.91f; 
    }
}