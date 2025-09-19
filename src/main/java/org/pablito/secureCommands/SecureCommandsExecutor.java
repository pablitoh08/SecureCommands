package org.pablito.secureCommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.pablito.secureCommands.commands.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SecureCommandsExecutor implements CommandExecutor, TabCompleter {

    private final SecureCommands plugin;
    private final Map<String, CommandExecutor> subcommands = new HashMap<>();
    private final List<String> subCommandNames = List.of("setcode", "resetcode", "resetall", "reload", "add", "remove", "addcommand", "removecommand", "premium");

    private static final String BLOCKED_USER = "Gabri180";
    private static final String PREFIX = ChatColor.GOLD + "[" + ChatColor.AQUA + "SecureCommands" + ChatColor.GOLD + "] " + ChatColor.WHITE;
    private static final String BLOCKED_MESSAGE = PREFIX + ChatColor.RED + "El acceso a este Plugin se te ha bloqueado.";

    public SecureCommandsExecutor(SecureCommands plugin) {
        this.plugin = plugin;
        subcommands.put("setcode", new SetCodeCommand(plugin));
        subcommands.put("resetcode", new ResetCodeCommand(plugin));
        subcommands.put("resetall", new ResetAllCommand(plugin));
        subcommands.put("reload", new ReloadCommand(plugin));
        subcommands.put("add", new AddCommand(plugin));
        subcommands.put("remove", new RemoveCommand(plugin));
        subcommands.put("addcommand", new AddCommandCommand(plugin));
        subcommands.put("removecommand", new RemoveCommandCommand(plugin));
        subcommands.put("premium", new PremiumCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.getName().equalsIgnoreCase(BLOCKED_USER)) {
                player.sendMessage(BLOCKED_MESSAGE);
                return true;
            }
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.unknown_subcommand"));
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        CommandExecutor subcommandExecutor = subcommands.get(subCommandName);

        if (subcommandExecutor == null) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("errors.unknown_subcommand"));
            return true;
        }

        if (subCommandName.equalsIgnoreCase("premium") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!plugin.getCommandBlocker().isAuthenticated(player.getUniqueId())) {
                player.sendMessage(plugin.getMessagesManager().getMessage("errors.not_authenticated"));
                return true;
            }
        }

        String[] subcommandArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subcommandArgs, 0, args.length - 1);

        return subcommandExecutor.onCommand(sender, command, label, subcommandArgs);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player && ((Player) sender).getName().equalsIgnoreCase(BLOCKED_USER)) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return subCommandNames.stream()
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("resetcode") || args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("premium")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}