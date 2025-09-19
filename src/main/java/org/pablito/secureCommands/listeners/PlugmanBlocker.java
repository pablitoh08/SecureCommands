package org.pablito.secureCommands.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.pablito.secureCommands.SecureCommands;
import org.pablito.secureCommands.managers.MessagesManager;

public class PlugmanBlocker implements Listener {
    private final SecureCommands plugin;
    private final MessagesManager messagesManager;

    public PlugmanBlocker(SecureCommands plugin) {
        this.plugin = plugin;
        this.messagesManager = plugin.getMessagesManager();
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        if (this.isBlockedCommand(message)) {
            event.setCancelled(true);
            String blockedMessage = messagesManager.getMessage("commands.plugman_blocked");
            event.getPlayer().sendMessage(blockedMessage);
        }
    }

    @EventHandler
    public void onConsoleCommand(ServerCommandEvent event) {
        String command = event.getCommand().toLowerCase();
        CommandSender sender = event.getSender();
        if (this.isBlockedCommand("/" + command)) {
            event.setCancelled(true);
            String blockedMessage = messagesManager.getMessage("commands.plugman_blocked");
            sender.sendMessage(blockedMessage);
        }
    }

    private boolean isBlockedCommand(String command) {
        return command.startsWith("/plugman") && command.contains(plugin.getName().toLowerCase()) && (command.contains("disable") || command.contains("reload") || command.contains("unload") || command.contains("restart"));
    }
}