package org.pablito.secureCommands.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.pablito.secureCommands.SecureCommands;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordNotifier {

    private final SecureCommands plugin;

    public DiscordNotifier(SecureCommands plugin) {
        this.plugin = plugin;
    }

    public void sendUpdateNotification(String newVersionJson) {
        if (!plugin.getConfigManager().isDiscordEnabled()) {
            plugin.getLogger().info(plugin.getMessagesManager().getMessage("discord.disabled"));
            return;
        }

        String webhookUrl = plugin.getConfigManager().getUpdateWebhookUrl();
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.equalsIgnoreCase("none")) {
            plugin.getLogger().warning(plugin.getMessagesManager().getMessage("discord.webhook_not_configured"));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                JsonObject versionObj = JsonParser.parseString(newVersionJson).getAsJsonObject();
                String latestVersion = versionObj.get("current_version").getAsString();
                String title = versionObj.get("title").getAsString();
                String tag = versionObj.get("tag").getAsString();
                String link = "https://www.spigotmc.org/resources/" + versionObj.get("id").getAsString() + "/";

                JsonObject embed = new JsonObject();
                embed.addProperty("title", "🚀 New SecureCommands Update Available!");
                embed.addProperty("description", "**" + title + "**\n"
                        + tag + "\n\n"
                        + "🔹 Current version: `" + plugin.getDescription().getVersion() + "`\n"
                        + "🔹 Latest version: `" + latestVersion + "`\n"
                        + "🔹 [Update here](" + link + ")");
                embed.addProperty("color", 0x00FF00); // Green color

                JsonObject payload = new JsonObject();
                JsonArray embeds = new JsonArray();
                embeds.add(embed);
                payload.add("embeds", embeds);

                sendWebhookMessage(webhookUrl, payload.toString());

            } catch (Exception e) {
                plugin.getLogger().warning(plugin.getMessagesManager().getMessage("system.update_check_failed", "%error%", e.getMessage()));
            }
        });
    }

    public void sendSecurityAlert(String title, String message) {
        if (!plugin.getConfigManager().isDiscordEnabled()) {
            plugin.getLogger().info(plugin.getMessagesManager().getMessage("discord.disabled"));
            return;
        }

        String webhookUrl = plugin.getConfigManager().getSecurityWebhookUrl();
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.equalsIgnoreCase("none")) {
            plugin.getLogger().warning(plugin.getMessagesManager().getMessage("discord.webhook_not_configured"));
            return;
        }

        JsonObject embed = new JsonObject();
        embed.addProperty("title", title);
        embed.addProperty("description", message);
        embed.addProperty("color", 0xFF0000);

        JsonObject payload = new JsonObject();
        JsonArray embeds = new JsonArray();
        embeds.add(embed);
        payload.add("embeds", embeds);

        sendWebhookMessage(webhookUrl, payload.toString());
    }

    private void sendWebhookMessage(String webhookUrl, String jsonPayload) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = connection.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    plugin.getLogger().info(plugin.getMessagesManager().getMessage("discord.sent"));
                } else {
                    plugin.getLogger().warning(plugin.getMessagesManager().getMessage("discord.failed", "%code%", String.valueOf(responseCode)));
                }
            } catch (Exception e) {
                plugin.getLogger().warning(plugin.getMessagesManager().getMessage("system.update_check_failed", "%error%", e.getMessage()));
            }
        });
    }
}