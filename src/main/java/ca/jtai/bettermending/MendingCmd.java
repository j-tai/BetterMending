package ca.jtai.bettermending;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MendingCmd implements CommandExecutor, TabCompleter {
    private final Store store;

    public MendingCmd(Store store) {
        this.store = store;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only in-game players can use that command.");
            return true;
        }
        MendingMode mode = null;
        if (args.length == 1) {
            try {
                mode = MendingMode.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        Player player = (Player) sender;
        if (mode != null) {
            // Correct syntax
            store.setMode(player, mode);
            player.sendMessage(ChatColor.GOLD + "Set your Mending mode to " + mode.name().toLowerCase() + ".");
        } else {
            // Incorrect syntax
            String currentMode = store.getMode(player).name().toLowerCase();
            sender.sendMessage(ChatColor.YELLOW + "Your Mending mode is " + ChatColor.GOLD + currentMode + ChatColor.YELLOW + ".");
            sender.sendMessage(ChatColor.GOLD + "Choose a Mending mode:");
            for (MendingMode m : MendingMode.values()) {
                String name = m.name().toLowerCase();
                TextComponent component = new TextComponent("- ");
                component.setColor(ChatColor.YELLOW);
                TextComponent button = new TextComponent(name);
                button.setColor(ChatColor.GOLD);
                button.setUnderlined(true);
                button.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new Text("Click to change your\nMending mode to " + name)
                ));
                button.setClickEvent(new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/mending " + name
                ));
                component.addExtra(button);
                component.addExtra(
                    " - " + m.getDescription() + " (" + m.getExplanation() + ")"
                );
                sender.spigot().sendMessage(component);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Arrays.stream(MendingMode.values())
                .map(mode -> mode.name().toLowerCase())
                .filter(arg -> arg.startsWith(prefix))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
