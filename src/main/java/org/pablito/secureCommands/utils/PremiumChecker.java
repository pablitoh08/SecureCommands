package org.pablito.secureCommands.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.pablito.secureCommands.SecureCommands;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PremiumChecker {

    private static final String ASHCON_API_URL = "https://api.ashcon.app/mojang/v2/";

    private final SecureCommands plugin;
    private final Map<UUID, Boolean> premiumCache = new ConcurrentHashMap<>();

    public PremiumChecker(SecureCommands plugin) {
        this.plugin = plugin;
    }

    public void checkPlayerPremium(String playerName, Callback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Boolean isPremium = Boolean.FALSE;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(ASHCON_API_URL + playerName);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                isPremium = responseCode == HttpURLConnection.HTTP_OK ? Boolean.TRUE : Boolean.FALSE;

                if (responseCode == 429) {
                    plugin.getLogger().warning("Rate limited by Ashcon API while checking player: " + playerName);
                }

                Player player = Bukkit.getPlayerExact(playerName);
                if (player != null) {
                    premiumCache.put(player.getUniqueId(), isPremium);
                }

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check premium status for player " + playerName + ": " + e.getMessage());

                Player player = Bukkit.getPlayerExact(playerName);
                if (player != null) {
                    premiumCache.put(player.getUniqueId(), Boolean.FALSE);
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                callback.onResult(isPremium);
            }
        });
    }

    public boolean isPremium(Player player) {
        return premiumCache.getOrDefault(player.getUniqueId(), Boolean.FALSE); // Usamos Boolean.FALSE como valor por defecto
    }

    public void clearCache(UUID playerUUID) {
        premiumCache.remove(playerUUID);
    }

    public interface Callback<T> {
        void onResult(T result);
    }
}
