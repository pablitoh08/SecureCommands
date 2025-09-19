package org.pablito.secureCommands.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.pablito.secureCommands.SecureCommands;

public class AddCommandCommand implements CommandExecutor {

    private final SecureCommands plugin;

    public AddCommandCommand(SecureCommands plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.isOp() || sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.no_op_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.invalid_usage_addcommand"));
            return true;
        }

        String commandToAdd = args[0].toLowerCase();
        if (commandToAdd.startsWith("/")) {
            commandToAdd = commandToAdd.substring(1);
        }

        if (plugin.getConfigManager().addBlockedCommand(commandToAdd)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("success.command_added_to_blocked", "%command%", commandToAdd));
        } else {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.command_already_blocked", "%command%", commandToAdd));
        }

        return true;
    }
}