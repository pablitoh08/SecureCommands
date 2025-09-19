package org.pablito.secureCommands.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.pablito.secureCommands.SecureCommands;

public class SetCodeCommand implements CommandExecutor {

    private final SecureCommands plugin;

    public SetCodeCommand(SecureCommands plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.only_players"));
            return true;
        }

        Player player = (Player) sender;
        String playerName = player.getName().toLowerCase();

        boolean isPremium = plugin.getPremiumChecker().isPremium(player);

        if (isPremium) {
            player.sendMessage(plugin.getMessagesManager().getMessage("success.premium_no_code"));
            return true;
        }

        if (!plugin.getConfigManager().getWhitelist().stream()
                .map(String::toLowerCase)
                .anyMatch(name -> name.equals(playerName))) {
            player.sendMessage(plugin.getMessagesManager().getMessage("errors.not_whitelisted"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getMessagesManager().getMessage("errors.invalid_usage_setcode"));
            return true;
        }

        String newCode = args[0];
        if (plugin.getDatabaseManager().isSecureCodeSet(playerName)) {
            player.sendMessage(plugin.getMessagesManager().getMessage("errors.secure_code_already_set"));
            return true;
        }

        if (plugin.getDatabaseManager().setSecureCode(playerName, newCode, isPremium)) {
            player.sendMessage(plugin.getMessagesManager().getMessage("success.code_set"));
        } else {
            player.sendMessage(plugin.getMessagesManager().getMessage("errors.db_error_setting_code"));
        }
        return true;
    }
}