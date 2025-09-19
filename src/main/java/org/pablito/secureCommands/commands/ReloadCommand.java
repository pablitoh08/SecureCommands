package org.pablito.secureCommands.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.pablito.secureCommands.SecureCommands;

public class ReloadCommand implements CommandExecutor {

    private final SecureCommands plugin;

    public ReloadCommand(SecureCommands plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.isOp() || sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.no_op_permission"));
            return true;
        }

        plugin.reloadPlugin();
        sender.sendMessage(plugin.getMessagesManager().getMessage("success.config_reloaded"));
        return true;
    }
}