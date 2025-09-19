package org.pablito.secureCommands.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.pablito.secureCommands.SecureCommands;

public class AddCommand implements CommandExecutor {

    private final SecureCommands plugin;

    public AddCommand(SecureCommands plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.isOp() || sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.no_op_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.invalid_usage_add"));
            return true;
        }

        String targetPlayerName = args[0];

        if (plugin.getConfigManager().addPlayerToWhitelist(targetPlayerName)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("success.player_added_to_whitelist", "%player%", targetPlayerName));
        } else {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.player_already_whitelisted", "%player%", targetPlayerName));
        }

        return true;
    }
}