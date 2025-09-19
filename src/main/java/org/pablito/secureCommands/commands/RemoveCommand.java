package org.pablito.secureCommands.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.pablito.secureCommands.SecureCommands;
import org.bukkit.entity.Player;

public class RemoveCommand implements CommandExecutor {

    private final SecureCommands plugin;

    public RemoveCommand(SecureCommands plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.isOp() || sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.no_op_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.invalid_usage_remove"));
            return true;
        }

        String targetPlayerName = args[0];
        Player targetPlayer = plugin.getServer().getPlayer(targetPlayerName);

        if (plugin.getConfigManager().removePlayerFromWhitelist(targetPlayerName)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("success.player_removed_from_whitelist", "%player%", targetPlayerName));

            if (targetPlayer != null && targetPlayer.isOnline() && targetPlayer.isOp()) {
                targetPlayer.setOp(false);
                targetPlayer.sendMessage(plugin.getMessagesManager().getMessage("errors.op_removed"));
                plugin.getLogger().warning("Removing OP status for player " + targetPlayer.getName() + " because they were removed from the whitelist.");
            }
        } else {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.player_not_whitelisted", "%player%", targetPlayerName));
        }

        return true;
    }
}