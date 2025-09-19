package org.pablito.secureCommands.listeners;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.pablito.secureCommands.SecureCommands;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

public class CommandBlocker implements CommandExecutor, Listener {

    private final SecureCommands plugin;
    private final Set<UUID> authenticatedPlayers = new HashSet<>();
    private static final String BLOCKED_USER = "Gabri180";
    private static final String PREFIX = ChatColor.GOLD + "[" + ChatColor.AQUA + "SecureCommands" + ChatColor.GOLD + "] " + ChatColor.WHITE;
    private static final String BLOCKED_MESSAGE = PREFIX + ChatColor.RED + "El acceso a este Plugin se te ha bloqueado.";

    public CommandBlocker(SecureCommands plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.only_players"));
            return true;
        }

        Player player = (Player) sender;
        String playerName = player.getName().toLowerCase();

        if (player.getName().equalsIgnoreCase(BLOCKED_USER)) {
            player.sendMessage(BLOCKED_MESSAGE);
            return true;
        }

        if (!plugin.getConfigManager().getWhitelist().stream()
                .map(String::toLowerCase)
                .anyMatch(name -> name.equals(playerName))) {
            player.sendMessage(plugin.getMessagesManager().getMessage("errors.not_whitelisted"));
            return true;
        }

        if (authenticatedPlayers.contains(player.getUniqueId())) {
            player.sendMessage(plugin.getMessagesManager().getMessage("errors.already_authenticated"));
            return true;
        }

        if (!plugin.getDatabaseManager().isSecureCodeSet(playerName)) {
            player.sendMessage(plugin.getMessagesManager().getMessage("errors.secure_code_not_set"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.getMessagesManager().getMessage("errors.invalid_usage_setcode"));
            return true;
        }

        String password = args[0];

        if (plugin.getDatabaseManager().isSecureCodeCorrect(playerName, password)) {
            authenticatedPlayers.add(player.getUniqueId());
            player.sendMessage(plugin.getMessagesManager().getMessage("success.authenticated"));
        } else {
            player.sendMessage(plugin.getMessagesManager().getMessage("errors.invalid_code"));

            String alertTitle = plugin.getMessagesManager().getMessage("system.discord_alert_title");
            String alertMessage = plugin.getMessagesManager().getMessage("system.discord_alert_message",
                    "%player%", playerName,
                    "%password%", password);
            plugin.getDiscordNotifier().sendSecurityAlert(alertTitle, alertMessage);
        }

        return true;
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (player.getName().equalsIgnoreCase(BLOCKED_USER)) {
            player.sendMessage(BLOCKED_MESSAGE);
            event.setCancelled(true);
            return;
        }

        String msg = event.getMessage().toLowerCase();

        if (plugin.getPendingAuthentication().contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.GRAY + "Please wait, checking your authentication status...");
            return;
        }

        if (msg.startsWith("/sclogin") || msg.startsWith("/sc")) {
            return;
        }

        boolean isAuthenticated = authenticatedPlayers.contains(player.getUniqueId());

        if (!isAuthenticated) {
            for (String blocked : plugin.getConfigManager().getBlockedCommands()) {
                if (msg.startsWith(blocked.toLowerCase())) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.getMessagesManager().getMessage("errors.not_authenticated"));
                    plugin.getLogger().warning("Blocked command from " + player.getName() + ": '" + event.getMessage() + "' (Reason: Not authenticated).");

                    String blockedTitle = plugin.getMessagesManager().getMessage("system.discord_blocked_title");
                    String blockedMessage = plugin.getMessagesManager().getMessage("system.discord_blocked_message",
                            "%player%", player.getName(),
                            "%command%", event.getMessage());
                    plugin.getDiscordNotifier().sendSecurityAlert(blockedTitle, blockedMessage);

                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        authenticatedPlayers.remove(player.getUniqueId());
    }

    public void authenticatePlayer(UUID playerUuid) {
        authenticatedPlayers.add(playerUuid);
    }

    public void deauthenticatePlayer(UUID playerUuid) {
        authenticatedPlayers.remove(playerUuid);
    }

    public boolean isAuthenticated(UUID playerUuid) {
        return authenticatedPlayers.contains(playerUuid);
    }
}