package org.pablito.secureCommands.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.pablito.secureCommands.SecureCommands;

public class RemoveCommandCommand implements CommandExecutor {

    private final SecureCommands plugin;

    public RemoveCommandCommand(SecureCommands plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.isOp() || sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.no_op_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.invalid_usage_removecommand"));
            return true;
        }

        String commandToRemove = args[0].toLowerCase();
        if (commandToRemove.startsWith("/")) {
            commandToRemove = commandToRemove.substring(1);
        }

        if (plugin.getConfigManager().removeBlockedCommand(commandToRemove)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("success.command_removed_from_blocked", "%command%", commandToRemove));
        } else {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.command_not_blocked", "%command%", commandToRemove));
        }

        return true;
    }
}