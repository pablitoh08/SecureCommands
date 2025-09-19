package org.pablito.secureCommands.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.pablito.secureCommands.SecureCommands;

public class ResetCodeCommand implements CommandExecutor {

    private final SecureCommands plugin;

    public ResetCodeCommand(SecureCommands plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.only_console"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.invalid_usage_resetcode"));
            return true;
        }

        String targetPlayerName = args[1];
        if (plugin.getDatabaseManager().resetSecureCode(targetPlayerName)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("success.code_reset_player", "%player%", targetPlayerName));
        } else {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.db_error_resetting_code", "%player%", targetPlayerName));
        }
        return true;
    }
}