package org.SUDORU.sUDORUDialoges.command;

import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.SUDORU.sUDORUDialoges.shop.TraderShop;
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
 * РЎРІСЏР·РєР° datapack <-> plugin.
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
            sender.sendMessage("В§cР­С‚Сѓ РєРѕРјР°РЅРґСѓ РјРѕР¶РµС‚ РёСЃРїРѕР»СЊР·РѕРІР°С‚СЊ С‚РѕР»СЊРєРѕ РёРіСЂРѕРє.");
            return true;
        }
        if (!player.hasPermission("sudoru.trader.bridge")) {
            player.sendMessage("В§cвњ— РќРµС‚ РїСЂР°РІ РЅР° bridge-РєРѕРјР°РЅРґСѓ.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("В§7РСЃРїРѕР»СЊР·РѕРІР°РЅРёРµ: В§f/shopbridge <open|buy|sell> ...");
            return true;
        }

        String mode = args[0].toLowerCase();
        return switch (mode) {
            case "open" -> handleOpen(player, args[1].toLowerCase());
            case "buy" -> handleTrigger(player, "ShopBuyTrigger", args[1]);
            case "sell" -> handleTrigger(player, "ShopSellTrigger", args[1]);
            default -> {
                player.sendMessage("В§cвњ— РќРµРёР·РІРµСЃС‚РЅС‹Р№ СЂРµР¶РёРј. РСЃРїРѕР»СЊР·СѓР№ open/buy/sell.");
                yield true;
            }
        };
    }

        private boolean handleOpen(Player player, String traderId) {
        TraderShop shop = plugin.getTraderManager().getShop(traderId);
        if (shop == null) {
            player.sendMessage("§c✗ Торговец '" + traderId + "' не найден.");
            return true;
        }
        player.getPersistentDataContainer().set(
                plugin.getActiveTraderKey(), PersistentDataType.STRING, traderId);
        plugin.getSyncService().syncShop(traderId, shop.getActiveSlots());
        plugin.getSyncService().showDialog(player, shop);
        return true;
    }

    private boolean handleTrigger(Player player, String objectiveName, String rawValue) {
        int value;
        try {
            value = Integer.parseInt(rawValue);
        } catch (NumberFormatException ex) {
            player.sendMessage("В§cвњ— Р—РЅР°С‡РµРЅРёРµ РґРѕР»Р¶РЅРѕ Р±С‹С‚СЊ С‡РёСЃР»РѕРј.");
            return true;
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective == null) {
            player.sendMessage("В§cвњ— Objective '" + objectiveName + "' РЅРµ РЅР°Р№РґРµРЅ. Р”Р°С‚Р°РїР°Рє Р·Р°РіСЂСѓР¶РµРЅ?");
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

