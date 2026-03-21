package org.SUDORU.sUDORUDialoges.command;

import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /trader <имя торговца>
 * Теперь работает как bridge в datapack (без plugin GUI).
 */
public class ShopCommand implements CommandExecutor, TabCompleter {

    private final SUDORUDialoges plugin;

    public ShopCommand(SUDORUDialoges plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭту команду может использовать только игрок!");
            return true;
        }

        if (!player.hasPermission("sudoru.trader")) {
            player.sendMessage("§c✗ У тебя нет доступа к торговцам.");
            return true;
        }

        if (args.length == 0) {
            var ids = plugin.getTraderManager().getShopIds();
            if (ids.isEmpty()) {
                player.sendMessage("§cНет настроенных торговцев. Проверь config.yml.");
                return true;
            }
            player.sendMessage("§eДоступные traderId: §f" + ids.stream().sorted().collect(Collectors.joining(", ")));
            player.sendMessage("§7Открой через bridge: §f/trader <id>§7 или NPC Villageres.");
            return true;
        }

        String traderId = args[0].toLowerCase();
        if (plugin.getTraderManager().getShop(traderId) == null) {
            player.sendMessage("§c✗ Торговец §f'" + traderId + "' §cне найден.");
            player.sendMessage("§7Используй §f/trader §7чтобы увидеть список.");
            return true;
        }

        // Меню полностью в datapack: вызываем bridge-команду.
        player.performCommand("shopbridge open " + traderId);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            for (String id : plugin.getTraderManager().getShopIds()) {
                if (id.startsWith(partial)) completions.add(id);
            }
        }
        return completions;
    }
}

