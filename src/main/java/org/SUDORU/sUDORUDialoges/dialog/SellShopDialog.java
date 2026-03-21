package org.SUDORU.sUDORUDialoges.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogInstancesProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.SUDORU.sUDORUDialoges.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Диалог продажи предметов за Coins (Scoreboard).
 * Открывается через /sellshop или датапак.
 * Показывает предметы из инвентаря с PDC-ключом sudoru:shop_price.
 */
@SuppressWarnings({"UnstableApiUsage", "deprecation"})
public class SellShopDialog {

    private final SUDORUDialoges plugin;

    public SellShopDialog(SUDORUDialoges plugin) {
        this.plugin = plugin;
    }

    // ── Открыть диалог продажи ─────────────────────────────────────

    public void open(Player player) {
        var prov = DialogInstancesProvider.instance();
        ClickCallback.Options opts = ClickCallback.Options.builder()
                .uses(ClickCallback.UNLIMITED_USES)
                .lifetime(Duration.ofMinutes(10)).build();

        List<SellEntry> entries = findSellableItems(player);
        List<ActionButton> buttons = new ArrayList<>();

        if (entries.isEmpty()) {
            // Нет предметов для продажи
            buttons.add(ActionButton.builder(
                    Component.text("— Нет предметов для продажи —", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false))
                    .action(prov.register((v, a) -> {}, opts)).width(350).build());
        } else {
            int balance = plugin.getCurrencyAmount(player);

            // Заголовок баланса
            buttons.add(ActionButton.builder(
                    Component.empty().decoration(TextDecoration.ITALIC, false)
                            .append(Component.text("Баланс: ", NamedTextColor.GRAY))
                            .append(Component.text(balance + " " + plugin.getCurrencyName(),
                                    NamedTextColor.GOLD).decorate(TextDecoration.BOLD)))
                    .action(prov.register((v, a) -> {
                        // Клик по балансу — обновляем диалог
                        if (!(a instanceof Player p)) return;
                        plugin.getServer().getScheduler().runTask(plugin, () -> open(p));
                    }, opts)).width(350).build());

            for (SellEntry entry : entries) {
                final int slotIdx = entry.slot();
                final int pricePerItem = entry.pricePerItem();
                final int amount = entry.stack().getAmount();
                final int totalPrice = pricePerItem * amount;

                Component itemName = getItemNameComponent(entry.stack());

                // Левая кнопка: название предмета + кол-во + итого
                Component nameLabel = Component.empty().decoration(TextDecoration.ITALIC, false)
                        .append(Component.text("⚙ ", NamedTextColor.YELLOW))
                        .append(itemName.decorate(TextDecoration.BOLD))
                        .append(Component.text("  ×" + amount + "  →  ", NamedTextColor.GRAY))
                        .append(Component.text("+" + totalPrice + " " + plugin.getCurrencyName(),
                                NamedTextColor.GREEN));

                buttons.add(ActionButton.builder(nameLabel)
                        .action(prov.register((v, a) -> {}, opts)).width(220).build());

                // Правая кнопка: Продать
                buttons.add(ActionButton.builder(
                        Component.text("Продать ×" + amount, NamedTextColor.GREEN)
                                .decoration(TextDecoration.ITALIC, false)
                                .decorate(TextDecoration.BOLD))
                        .tooltip(Component.empty().decoration(TextDecoration.ITALIC, false)
                                .append(Component.text("Продать " + amount + " шт.\n", NamedTextColor.GRAY))
                                .append(Component.text("Выручка: +" + totalPrice
                                        + " " + plugin.getCurrencyName(), NamedTextColor.GREEN))
                                .append(Component.newline())
                                .append(Component.text("(" + pricePerItem + " " + plugin.getCurrencyName() + "/шт.)",
                                        NamedTextColor.DARK_GRAY)))
                        .action(prov.register((v, a) -> {
                            if (!(a instanceof Player p)) return;
                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                sellItem(p, slotIdx, pricePerItem);
                                open(p); // обновить диалог
                            });
                        }, opts)).width(130).build());
            }
        }

        ActionButton exitBtn = ActionButton.builder(
                Component.text("✖ Закрыть", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false))
                .action(prov.register((v, a) -> {
                    if (!(a instanceof Player p)) return;
                    plugin.getServer().getScheduler().runTask(plugin, () -> p.closeInventory());
                }, opts)).width(150).build();

        Component title = Component.text("⚙ Магазин продажи", NamedTextColor.YELLOW)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        DialogBase base = prov.dialogBaseBuilder(title)
                .externalTitle(Component.text("Продажа предметов")
                        .decoration(TextDecoration.ITALIC, false))
                .body(List.of(prov.plainMessageDialogBody(
                        Component.text("Предметы из инвентаря с ценой продажи:", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false), 350)))
                .canCloseWithEscape(true).pause(false)
                .afterAction(DialogBase.DialogAfterAction.NONE).build();

        player.showDialog(Dialog.create(f -> f.empty().base(base)
                .type(prov.multiAction(buttons).exitAction(exitBtn).columns(2).build())));
    }

    // ── Продажа предмета из слота ──────────────────────────────────

    private void sellItem(Player player, int slot, int pricePerItem) {
        ItemStack stack = player.getInventory().getItem(slot);
        if (stack == null || stack.getType().isAir()) {
            player.sendMessage(ColorUtil.parse("§c✗ Предмет больше не найден в инвентаре."));
            return;
        }

        // Проверяем что PDC всё ещё на месте
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(plugin.getShopPriceKey())) {
            player.sendMessage(ColorUtil.parse("§c✗ Этот предмет нельзя продать."));
            return;
        }

        int amount = stack.getAmount();
        int earned = pricePerItem * amount;
        String displayName = PlainTextComponentSerializer.plainText()
                .serialize(getItemNameComponent(stack));

        // Убираем из инвентаря
        player.getInventory().setItem(slot, null);
        player.updateInventory();

        // Начисляем Coins
        plugin.addCurrency(player, earned);

        player.sendMessage(ColorUtil.parse(
                "&#55FF55✔ §aПродано: §f" + amount + " §7× §f" + displayName
                + " §7→ &#55FF55+" + earned + " §7" + plugin.getCurrencyName()));
    }

    // ── Сканирование инвентаря ────────────────────────────────────

    private List<SellEntry> findSellableItems(Player player) {
        List<SellEntry> result = new ArrayList<>();
        ItemStack[] contents = player.getInventory().getContents(); // слоты 0-35
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack == null || stack.getType().isAir()) continue;
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) continue;
            Integer price = meta.getPersistentDataContainer()
                    .get(plugin.getShopPriceKey(), PersistentDataType.INTEGER);
            if (price != null && price > 0) {
                result.add(new SellEntry(i, stack.clone(), price));
            }
        }
        return result;
    }

    // ── Утилиты ───────────────────────────────────────────────────

    private Component getItemNameComponent(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null && meta.hasDisplayName() && meta.displayName() != null) {
            return meta.displayName().decoration(TextDecoration.ITALIC, false);
        }
        // Нет кастомного имени — используем название материала
        String matName = stack.getType().name().replace("_", " ");
        return Component.text(matName, NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false);
    }

    // ── Внутренняя запись слота ───────────────────────────────────

    private record SellEntry(int slot, ItemStack stack, int pricePerItem) {}
}

