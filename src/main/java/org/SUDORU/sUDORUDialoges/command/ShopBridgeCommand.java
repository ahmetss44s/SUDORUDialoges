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
            case "buy"  -> handleTrigger(player, "ShopBuyTrigger", args[1]);
            case "sell" -> handleTrigger(player, "ShopSellTrigger", args[1]);
            default -> {
                player.sendMessage("§c✗ Неизвестный режим. Используй open/buy/sell.");
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
            player.sendMessage("§c✗ Значение должно быть числом.");
            return true;
        }

        // value == 0: нажата кнопка «Выйти» — ничего не делаем
        if (value == 0) return true;

        // ── ITEM-валюта: покупку обрабатывает плагин напрямую ────────────────
        if ("ShopBuyTrigger".equals(objectiveName)
                && value > 0
                && plugin.getCurrencyType() == SUDORUDialoges.CurrencyType.ITEM) {
            handleItemCurrencyBuy(player, value);
            return true;
        }

        // ── SCOREBOARD-валюта (или закрытие диалога value=0): через датапак ──
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective == null) {
            player.sendMessage("§c✗ Objective '" + objectiveName + "' не найден. Датапак загружен?");
            return true;
        }
        objective.getScore(player.getName()).setScore(value);
        return true;
    }

    /**
     * Обрабатывает покупку при item-валюте.
     * Декодирует trigger: shopId = value / 100, slotIndex = value % 100.
     * Снимает предметы из инвентаря, выдаёт товар, обновляет диалог.
     */
    private void handleItemCurrencyBuy(Player player, int triggerValue) {
        int shopId    = triggerValue / 100;
        int slotIndex = triggerValue % 100;

        String traderId = plugin.getSyncService().getTraderIdByShopId(shopId);
        if (traderId == null) {
            player.sendMessage("§c✗ Магазин с ID=" + shopId + " не найден.");
            return;
        }

        TraderShop shop = plugin.getTraderManager().getShop(traderId);
        if (shop == null) {
            player.sendMessage("§c✗ Магазин '" + traderId + "' не найден.");
            return;
        }

        shop.tryPurchase(player, slotIndex, 1);

        // Пересинкаем данные и показываем обновлённый диалог
        plugin.getSyncService().syncShop(traderId, shop.getActiveSlots());
        final TraderShop finalShop = shop;
        Bukkit.getScheduler().runTaskLater(plugin,
                () -> plugin.getSyncService().showDialog(player, finalShop), 1L);
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

