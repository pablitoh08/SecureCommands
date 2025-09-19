package org.pablito.secureCommands.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.pablito.secureCommands.SecureCommands;

public class PremiumCommand implements CommandExecutor {

    private final SecureCommands plugin;

    public PremiumCommand(SecureCommands plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.isOp() || sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.no_op_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.invalid_usage_premium"));
            return true;
        }

        String targetPlayerName = args[0];
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.player_not_online", "%player%", targetPlayerName));
            return true;
        }

        if (plugin.getPlayerDataManager().isPremiumCheckDisabled(targetPlayer.getUniqueId())) {
            plugin.getPlayerDataManager().setPremiumCheckDisabled(targetPlayer.getUniqueId(), false);
            sender.sendMessage(plugin.getMessagesManager().getMessage("success.premium_check_enabled", "%player%", targetPlayerName));
        } else {
            plugin.getPlayerDataManager().setPremiumCheckDisabled(targetPlayer.getUniqueId(), true);
            sender.sendMessage(plugin.getMessagesManager().getMessage("success.premium_check_disabled", "%player%", targetPlayerName));
        }

        plugin.getPlayerDataManager().savePlayerData(targetPlayer.getUniqueId(), targetPlayer.getName());

        return true;
    }
}