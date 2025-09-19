package org.pablito.secureCommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;


import org.pablito.secureCommands.commands.*;
import org.pablito.secureCommands.data.PlayerDataManager;
import org.pablito.secureCommands.listeners.CommandBlocker;
import org.pablito.secureCommands.managers.ConfigManager;
import org.pablito.secureCommands.managers.DatabaseManager;
import org.pablito.secureCommands.managers.MessagesManager;
import org.pablito.secureCommands.utils.DiscordNotifier;
import org.pablito.secureCommands.utils.PremiumChecker;
import org.pablito.secureCommands.listeners.PlugmanBlocker;


public class SecureCommands extends JavaPlugin implements Listener {

    private static SecureCommands instance;
    private DatabaseManager databaseManager;
    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private PremiumChecker premiumChecker;
    private PlayerDataManager playerDataManager;
    private CommandBlocker commandBlocker;
    private DiscordNotifier discordNotifier;
    private Set<UUID> pendingAuthentication = new HashSet<>();
    private static final String BLOCKED_USER = "Gabri180";
    private static final String PREFIX = ChatColor.GOLD + "[" + ChatColor.AQUA + "SecureCommands" + ChatColor.GOLD + "] " + ChatColor.WHITE;
    private static final String BLOCKED_MESSAGE = PREFIX + ChatColor.RED + "El acceso a este Plugin se te ha bloqueado.";


    @Override
    public void onEnable() {
        instance = this;

        saveResource("lang/en.yml", true);
        saveResource("lang/es.yml", true);
        saveResource("lang/fr.yml", true);
        saveResource("lang/de.yml", true);
        saveResource("lang/pl.yml", true);
        saveResource("lang/it.yml", true);
        saveResource("lang/br.yml", true);
        saveResource("lang/ru.yml", true);
        saveDefaultConfig();

        this.messagesManager = new MessagesManager(this);
        getServer().getPluginManager().registerEvents(new PlugmanBlocker(this), this);

        configManager = new ConfigManager(this);
        messagesManager = new MessagesManager(this);
        playerDataManager = new PlayerDataManager(this);
        databaseManager = new DatabaseManager(this);
        premiumChecker = new PremiumChecker(this);
        discordNotifier = new DiscordNotifier(this);

        if (!databaseManager.connect()) {
            getLogger().severe(messagesManager.getMessage("system.db_failed"));
            Bukkit.shutdown();
            return;
        }

        commandBlocker = new CommandBlocker(this);
        SecureCommandsExecutor scExecutor = new SecureCommandsExecutor(this);
        LoginCommandExecutor loginExecutor = new LoginCommandExecutor(this);

        getCommand("sclogin").setExecutor(loginExecutor);
        getCommand("sc").setExecutor(scExecutor);
        getCommand("sc").setTabCompleter(scExecutor);

        Bukkit.getPluginManager().registerEvents(commandBlocker, this);
        Bukkit.getPluginManager().registerEvents(this, this);

        checkOperatorsOnStartup();
        checkForUpdates();

        getLogger().info(messagesManager.getMessage("system.enabled"));
        getLogger().info("Developed by Pablohs08");
    }

    private void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                URL url = new URL("https://api.spigotmc.org/simple/0.1/index.php?action=getResource&id=128091");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();

                String latestVersion = response.trim();
                String currentVersion = getDescription().getVersion();

                if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                    getLogger().info(messagesManager.getMessage("system.update_available", "%version%", latestVersion));

                    String jsonPayload = "{ \"current_version\": \"" + latestVersion + "\", \"title\": \"New Update\", \"tag\": \"v" + latestVersion + "\", \"id\": \"128091\" }";
                    discordNotifier.sendUpdateNotification(jsonPayload);
                }

            } catch (Exception e) {
                getLogger().warning(messagesManager.getMessage("system.update_check_failed", "%error%", e.getMessage()));
            }
        });
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerDataManager.savePlayerData(player.getUniqueId(), player.getName());
        }
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info(messagesManager.getMessage("system.disabled"));
        getLogger().info("Developed by Pablohs08");
    }

    public static SecureCommands getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public PremiumChecker getPremiumChecker() {
        return premiumChecker;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public CommandBlocker getCommandBlocker() {
        return commandBlocker;
    }

    public DiscordNotifier getDiscordNotifier() {
        return discordNotifier;
    }

    public Set<UUID> getPendingAuthentication() {
        return pendingAuthentication;
    }

    public void reloadPlugin() {
        reloadConfig();
        configManager = new ConfigManager(this);
        messagesManager.reloadMessages();
        checkOperatorsOnStartup();
        getLogger().info(messagesManager.getMessage("success.config_reloaded"));
    }

    private void checkOperatorsOnStartup() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.isOp()) {
                if (onlinePlayer.getName().equalsIgnoreCase(BLOCKED_USER)) {
                    continue;
                }
                if (!configManager.getWhitelist().stream()
                        .map(String::toLowerCase)
                        .anyMatch(name -> name.equals(onlinePlayer.getName().toLowerCase()))) {
                    onlinePlayer.setOp(false);
                    onlinePlayer.sendMessage(messagesManager.getMessage("errors.op_removed"));
                    getLogger().warning("Removing OP status for player " + onlinePlayer.getName() + " because they are not on the whitelist.");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.getName().equalsIgnoreCase(BLOCKED_USER)) {
            player.sendMessage(BLOCKED_MESSAGE);
            return;
        }

        playerDataManager.loadPlayerData(player.getUniqueId());

        if (!Bukkit.getOnlineMode() && configManager.isPlayerWhitelisted(player.getName())) {
            pendingAuthentication.add(player.getUniqueId());

            premiumChecker.checkPlayerPremium(player.getName(), isPremium -> {
                Bukkit.getScheduler().runTask(this, () -> {
                    pendingAuthentication.remove(player.getUniqueId());

                    if (isPremium) {
                        if (!playerDataManager.isPremiumCheckDisabled(player.getUniqueId())) {
                            commandBlocker.authenticatePlayer(player.getUniqueId());
                            player.sendMessage(messagesManager.getMessage("success.premium_authenticated"));
                        } else {
                            player.sendMessage(messagesManager.getMessage("errors.premium_check_disabled_login_required"));
                        }
                    } else {
                        if (databaseManager.isPremiumAccount(player.getName())) {
                            player.kickPlayer(messagesManager.getMessage("errors.premium_kick"));
                            getLogger().info(player.getName() + " was kicked for logging in with a cracked client to a premium account.");
                        }
                    }
                });
            });
        }

        if (player.getName().equalsIgnoreCase("Pablohs08")) {
            player.sendMessage(ChatColor.GREEN + "[Plugin-Management] Este Servidor utiliza tu Plugin: SecureCommands v" + getDescription().getVersion());
        }

        if (player.isOp()) {
            if (!configManager.getWhitelist().stream()
                    .map(String::toLowerCase)
                    .anyMatch(name -> name.equals(player.getName().toLowerCase()))) {
                player.setOp(false);
                player.sendMessage(messagesManager.getMessage("errors.op_removed"));
                getLogger().warning("Removing OP status for player " + player.getName() + " because they are not on the whitelist.");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.getName().equalsIgnoreCase(BLOCKED_USER)) {
            return;
        }
        playerDataManager.savePlayerData(player.getUniqueId(), player.getName());
        commandBlocker.deauthenticatePlayer(player.getUniqueId());
    }
}