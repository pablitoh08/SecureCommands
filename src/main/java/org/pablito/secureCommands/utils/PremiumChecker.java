package org.pablito.secureCommands.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

    private static final String MOJANG_API_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private final SecureCommands plugin;

    private final Map<UUID, Boolean> premiumCache = new ConcurrentHashMap<>();

    public PremiumChecker(SecureCommands plugin) {
        this.plugin = plugin;
    }

    public void checkPlayerPremium(String playerName, Callback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(MOJANG_API_URL + playerName);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                boolean isPremium = responseCode == HttpURLConnection.HTTP_OK;
                Player player = Bukkit.getPlayerExact(playerName);
                if (player != null) {
                    premiumCache.put(player.getUniqueId(), isPremium);
                }
                callback.onResult(isPremium);

                connection.disconnect();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check premium status for player " + playerName + ": " + e.getMessage());
                Player player = Bukkit.getPlayerExact(playerName);
                if (player != null) {
                    premiumCache.put(player.getUniqueId(), false);
                }
                callback.onResult(false);
            }
        });
    }

    public boolean isPremium(Player player) {
        return premiumCache.getOrDefault(player.getUniqueId(), false);
    }

    public void clearCache(UUID playerUUID) {
        premiumCache.remove(playerUUID);
    }

    public interface Callback<T> {
        void onResult(T result);
    }
}