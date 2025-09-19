package org.pablito.secureCommands.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.pablito.secureCommands.SecureCommands;

public class ResetAllCommand implements CommandExecutor {

    private final SecureCommands plugin;

    public ResetAllCommand(SecureCommands plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.only_console"));
            return true;
        }

        if (plugin.getDatabaseManager().resetAllSecureCodes()) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("success.code_reset_all"));
        } else {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.db_error_resetting_all"));
        }
        return true;
    }
}