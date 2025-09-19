package org.pablito.secureCommands.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.pablito.secureCommands.SecureCommands;

import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {
    private SecureCommands plugin;
    private FileConfiguration config;

    public ConfigManager(SecureCommands plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public List<String> getWhitelist() {
        return config.getStringList("whitelist");
    }

    public List<String> getBlockedCommands() {
        return config.getStringList("blocked-commands");
    }

    public String getUpdateWebhookUrl() {
        return config.getString("discord.update-webhook-url");
    }

    public String getSecurityWebhookUrl() {
        return config.getString("discord.security-webhook-url");
    }

    public boolean isDiscordEnabled() {
        return config.getBoolean("discord.enabled", false);
    }

    public boolean isPlayerWhitelisted(String playerName) {
        List<String> whitelist = getWhitelist();
        return whitelist.stream()
                .map(String::toLowerCase)
                .anyMatch(name -> name.equals(playerName.toLowerCase()));
    }

    public boolean addPlayerToWhitelist(String player) {
        List<String> whitelist = getWhitelist();
        String playerToAdd = player.toLowerCase();

        if (whitelist.stream().anyMatch(name -> name.toLowerCase().equals(playerToAdd))) {
            return false;
        }

        whitelist.add(playerToAdd);
        config.set("whitelist", whitelist);
        plugin.saveConfig();
        return true;
    }

    public boolean removePlayerFromWhitelist(String player) {
        List<String> whitelist = getWhitelist();
        String playerToRemove = player.toLowerCase();

        boolean removed = whitelist.removeIf(name -> name.toLowerCase().equals(playerToRemove));

        if (removed) {
            config.set("whitelist", whitelist);
            plugin.saveConfig();
        }

        return removed;
    }

    public boolean addBlockedCommand(String command) {
        List<String> blockedCommands = getBlockedCommands();
        String commandToAdd = command.toLowerCase();

        if (blockedCommands.contains(commandToAdd)) {
            return false;
        }

        blockedCommands.add(commandToAdd);
        config.set("blocked-commands", blockedCommands);
        plugin.saveConfig();
        return true;
    }

    public boolean removeBlockedCommand(String command) {
        List<String> blockedCommands = getBlockedCommands();
        String commandToRemove = command.toLowerCase();

        boolean removed = blockedCommands.removeIf(cmd -> cmd.equals(commandToRemove));

        if (removed) {
            config.set("blocked-commands", blockedCommands);
            plugin.saveConfig();
        }

        return removed;
    }
}