package org.pablito.secureCommands.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.pablito.secureCommands.SecureCommands;

public class LoginCommandExecutor implements CommandExecutor {

    private final SecureCommands plugin;

    public LoginCommandExecutor(SecureCommands plugin) {
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

        if (!plugin.getConfigManager().isPlayerWhitelisted(playerName)) {
            player.sendMessage(plugin.getMessagesManager().getMessage("errors.not_whitelisted"));
            return true;
        }

        if (!plugin.getDatabaseManager().isSecureCodeSet(playerName)) {
            player.sendMessage(plugin.getMessagesManager().getMessage("errors.secure_code_not_set"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.getMessagesManager().getMessage("errors.invalid_usage_login"));
            return true;
        }

        String password = args[0];

        if (plugin.getDatabaseManager().isSecureCodeCorrect(playerName, password)) {
            plugin.getCommandBlocker().authenticatePlayer(player.getUniqueId());
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
}