package com.muzone.mucore.data;

import com.muzone.mucore.MuCore;
import org.bukkit.entity.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class WebhookManager {

    private final MuCore plugin;
    private final HttpClient client;
    private final String webhookUrl;
    private final boolean enabled;
    private final int frequency;

    public WebhookManager(MuCore plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfigManager().getBoolean("webhook.enabled");
        this.webhookUrl = plugin.getConfigManager().getString("webhook.url");
        this.frequency = plugin.getConfigManager().getInt("webhook.alert_frequency");

        // Gunakan HTTP Client modern (Java 11+)
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void sendViolation(Player player, String checkName, double vl, String details) {
        if (!enabled || webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.contains("YOUR_WEBHOOK")) return;

        // Anti-Spam: Kirim hanya jika VL kelipatan frequency (contoh: VL 5, 10, 15...)
        if ((int) vl % frequency != 0) return;

        // Jalankan di thread terpisah (Async) agar server tidak lag
        CompletableFuture.runAsync(() -> {
            try {
                String jsonPayload = buildDiscordJson(player, checkName, vl, details);
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(webhookUrl))
                        .timeout(Duration.ofSeconds(5))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            if (response.statusCode() < 200 || response.statusCode() > 299) {
                                plugin.getLogger().warning("Failed to send webhook. Code: " + response.statusCode());
                            }
                        });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Merakit JSON manual agar tidak perlu library tambahan (Gson/Jackson)
    private String buildDiscordJson(Player player, String checkName, double vl, String details) {
        String playerName = player.getName();
        String ping = String.valueOf(player.getPing());
        String serverName = plugin.getServer().getName();
        
        // Warna Embed: Merah (16711680)
        // Format JSON Discord yang valid
        return String.format(
            "{" +
            "  \"username\": \"%s\"," +
            "  \"embeds\": [{" +
            "    \"title\": \"⚠ Security Violation Detected\"," +
            "    \"color\": 16711680," +
            "    \"fields\": [" +
            "      {\"name\": \"Player\", \"value\": \"%s\", \"inline\": true}," +
            "      {\"name\": \"Check\", \"value\": \"%s\", \"inline\": true}," +
            "      {\"name\": \"VL (Violation Level)\", \"value\": \"%.1f\", \"inline\": true}," +
            "      {\"name\": \"Ping\", \"value\": \"%sms\", \"inline\": true}," +
            "      {\"name\": \"Details\", \"value\": \"%s\", \"inline\": false}" +
            "    ]," +
            "    \"footer\": {\"text\": \"MuCore Sentry • %s\"}," +
            "    \"timestamp\": \"%s\"" +
            "  }]" +
            "}",
            plugin.getConfigManager().getString("webhook.username"), // Bot Name
            playerName,
            checkName,
            vl,
            ping,
            details,
            serverName,
            java.time.Instant.now().toString()
        );
    }
}