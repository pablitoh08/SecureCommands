package org.pablito.secureCommands.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.pablito.secureCommands.SecureCommands;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final SecureCommands plugin;
    private final File playerDataFolder;
    private final Map<UUID, Boolean> premiumCheckStatus = new HashMap<>();

    public PlayerDataManager(SecureCommands plugin) {
        this.plugin = plugin;
        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
    }

    public boolean playerDataExists(UUID playerUuid) {
        File playerFile = new File(playerDataFolder, playerUuid.toString() + ".yml");
        return playerFile.exists();
    }

    public void loadPlayerData(UUID playerUuid) {
        File playerFile = new File(playerDataFolder, playerUuid.toString() + ".yml");
        if (playerFile.exists()) {
            FileConfiguration dataConfig = YamlConfiguration.loadConfiguration(playerFile);
            boolean isDisabled = dataConfig.getBoolean("premium-check-disabled", false);
            premiumCheckStatus.put(playerUuid, Boolean.valueOf(isDisabled));
        } else {
            premiumCheckStatus.put(playerUuid, Boolean.FALSE);
        }
    }

    public void savePlayerData(UUID playerUuid, String playerName) {
        if (premiumCheckStatus.containsKey(playerUuid)) {
            File playerFile = new File(playerDataFolder, playerUuid.toString() + ".yml");
            FileConfiguration dataConfig = YamlConfiguration.loadConfiguration(playerFile);

            dataConfig.set("player-name", playerName);
            dataConfig.set("premium-check-disabled", premiumCheckStatus.get(playerUuid));
            try {
                dataConfig.save(playerFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save player data for " + playerName + " (" + playerUuid.toString() + "): " + e.getMessage());
            }
        }
    }

    public boolean isPremiumCheckDisabled(UUID playerUuid) {
        return premiumCheckStatus.getOrDefault(playerUuid, Boolean.FALSE);
    }

    public void setPremiumCheckDisabled(UUID playerUuid, boolean disabled) {
        premiumCheckStatus.put(playerUuid, Boolean.valueOf(disabled));
    }
}
