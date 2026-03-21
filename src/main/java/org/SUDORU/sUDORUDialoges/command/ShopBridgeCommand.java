package org.SUDORU.sUDORUDialoges.command;

import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Связка datapack <-> plugin.
 *
 * /shopbridge open <traderId>
 * /shopbridge buy <value>
 * /shopbridge sell <value>
 */
public class ShopBridgeCommand implements CommandExecutor, TabCompleter {
    private final SUDORUDialoges plugin;

    public ShopBridgeCommand(SUDORUDialoges plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭту команду может использовать только игрок.");
            return true;
        }
        if (!player.hasPermission("sudoru.trader.bridge")) {
            player.sendMessage("§c✗ Нет прав на bridge-команду.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§7Использование: §f/shopbridge <open|buy|sell> ...");
            return true;
        }

        String mode = args[0].toLowerCase();
        return switch (mode) {
            case "open" -> handleOpen(player, args[1].toLowerCase());
            case "buy" -> handleTrigger(player, "ShopBuyTrigger", args[1]);
            case "sell" -> handleTrigger(player, "ShopSellTrigger", args[1]);
            default -> {
                player.sendMessage("§c✗ Неизвестный режим. Используй open/buy/sell.");
                yield true;
            }
        };
    }

    private boolean handleOpen(Player player, String traderId) {
        if (plugin.getTraderManager().getShop(traderId) == null) {
            player.sendMessage("§c✗ Торговец '" + traderId + "' не найден.");
            return true;
        }

        player.getPersistentDataContainer().set(
                plugin.getActiveTraderKey(),
                PersistentDataType.STRING,
                traderId
        );

        String cmd = "execute as " + player.getName() + " run function api-shop:dialog/shop/store_data";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        return true;
    }

    private boolean handleTrigger(Player player, String objectiveName, String rawValue) {
        int value;
        try {
            value = Integer.parseInt(rawValue);
        } catch (NumberFormatException ex) {
            player.sendMessage("§c✗ Значение должно быть числом.");
            return true;
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective == null) {
            player.sendMessage("§c✗ Objective '" + objectiveName + "' не найден. Датапак загружен?");
            return true;
        }

        objective.getScore(player.getName()).setScore(value);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return List.of("open", "buy", "sell");
        if (args.length == 2 && "open".equalsIgnoreCase(args[0])) {
            List<String> list = new ArrayList<>(plugin.getTraderManager().getShopIds());
            list.sort(String::compareTo);
            return list;
        }
        return List.of();
    }
}
