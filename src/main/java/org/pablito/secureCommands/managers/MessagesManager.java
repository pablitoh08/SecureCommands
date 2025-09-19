package org.pablito.secureCommands.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.pablito.secureCommands.SecureCommands;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class MessagesManager {

    private final SecureCommands plugin;
    private FileConfiguration messagesConfig;
    private String langCode;

    public MessagesManager(SecureCommands plugin) {
        this.plugin = plugin;
        this.langCode = plugin.getConfig().getString("language", "en");
        loadMessagesFile();
    }

    private void loadMessagesFile() {
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        File messagesFile = new File(langDir, langCode + ".yml");

        if (!new File(langDir, "en.yml").exists()) {
            plugin.saveResource("lang/en.yml", true);
        }
        if (!new File(langDir, "es.yml").exists()) {
            plugin.saveResource("lang/es.yml", true);
        }

        if (!messagesFile.exists()) {
            plugin.getLogger().warning("Language file '" + langCode + ".yml' not found. Falling back to English.");
            this.langCode = "en";
            messagesFile = new File(langDir, "en.yml");
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadMessages() {
        this.langCode = plugin.getConfig().getString("language", "en");
        loadMessagesFile();
    }

    public String getMessage(String path, String... replacements) {
        String message = messagesConfig.getString(path, "Message not found: " + path);

        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }

        if (message.contains("%prefix%")) {
            String prefix = messagesConfig.getString("prefix", "&b[SecureCommands] &r");
            message = message.replace("%prefix%", prefix);
        } else if (message.startsWith("&")) {
            String prefix = messagesConfig.getString("prefix", "&b[SecureCommands] &r");
            message = ChatColor.translateAlternateColorCodes('&', prefix) + ChatColor.translateAlternateColorCodes('&', message);
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}