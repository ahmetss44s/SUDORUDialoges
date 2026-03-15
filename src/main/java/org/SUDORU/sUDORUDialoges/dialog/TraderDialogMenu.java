package org.SUDORU.sUDORUDialoges.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogInstancesProvider;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.SUDORU.sUDORUDialoges.shop.ShopItem;
import org.SUDORU.sUDORUDialoges.shop.TraderShop;
import org.SUDORU.sUDORUDialoges.util.ColorUtil;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Диалоговое меню торговца в стиле скриншота:
 * тёмный оверлей, заголовок, кнопки по центру, кнопка «Выход».
 * Использует Paper 1.21.8 Dialog API.
 */
@SuppressWarnings("UnstableApiUsage")
public class TraderDialogMenu {

    private final SUDORUDialoges plugin;

    public TraderDialogMenu(SUDORUDialoges plugin) {
        this.plugin = plugin;
    }

    /**
     * Открывает диалоговое меню торговца для игрока.
     */
    public void open(Player player, TraderShop shop) {
        var cfg = shop.getConfig();
        var prov = DialogInstancesProvider.instance();

        // ── Опции колбека ─────────────────────────────────────────
        ClickCallback.Options opts = ClickCallback.Options.builder()
                .uses(ClickCallback.UNLIMITED_USES)
                .lifetime(Duration.ofMinutes(10))
                .build();

        // ── Тело диалога: описание торговца ───────────────────────
        List<DialogBody> bodyList = new ArrayList<>();
        String desc = cfg.getDescription();
        if (desc != null && !desc.isEmpty()) {
            // Разбиваем по \n
            String[] lines = desc.split("\\\\n|\\n");
            StringBuilder joined = new StringBuilder();
            for (String line : lines) {
                if (!joined.isEmpty()) joined.append("\n");
                joined.append(line);
            }
            bodyList.add(prov.plainMessageDialogBody(
                    ColorUtil.parse(joined.toString()), 220));
        }

        // ── Кнопки товаров ────────────────────────────────────────
        List<ActionButton> buttons = new ArrayList<>();
        Map<Integer, TraderShop.SlotData> activeSlots = shop.getActiveSlots();
        int maxSlots = shop.getProductSlotsCount();

        for (int i = 0; i < maxSlots; i++) {
            TraderShop.SlotData data = activeSlots.get(i);
            if (data == null) continue;

            final int slotIndex = i;

            if (data.isBought()) {
                // Уже куплено — показываем неактивной кнопкой
                String rawName = stripColors(data.getItem().getName());
                ActionButton btn = ActionButton.builder(
                        Component.text("✗ #" + (i + 1) + " " + rawName + " — ПРОДАНО")
                                .color(NamedTextColor.DARK_GRAY)
                                .decoration(TextDecoration.ITALIC, false))
                        .action(prov.register((view, audience) -> {}, opts))
                        .width(220)
                        .build();
                buttons.add(btn);
            } else {
                // Доступно к покупке
                ShopItem item = data.getItem();
                Component label = buildItemLabel(i + 1, item.getName(), data.getPrice());

                ActionButton btn = ActionButton.builder(label)
                        .tooltip(buildItemTooltip(item, data.getPrice()))
                        .action(prov.register(
                                (view, audience) -> {
                                    if (!(audience instanceof Player p)) return;
                                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                                        shop.tryPurchase(p, slotIndex);
                                        open(p, shop); // переоткрываем с обновлённым состоянием
                                    });
                                }, opts))
                        .width(220)
                        .build();
                buttons.add(btn);
            }
        }

        // ── Кнопка «Выход» ────────────────────────────────────────
        ActionButton exitButton = ActionButton.builder(
                Component.text("Выход")
                        .color(NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false))
                .action(prov.register((view, audience) -> {
                    if (!(audience instanceof Player p)) return;
                    plugin.getServer().getScheduler().runTask(plugin, (Runnable) p::closeInventory);
                }, opts))
                .width(150)
                .build();

        // ── Тело диалога ──────────────────────────────────────────
        DialogBase base = prov.dialogBaseBuilder(
                        ColorUtil.parse(cfg.getDisplayName()))
                .externalTitle(ColorUtil.parse(cfg.getDisplayName()))
                .body(bodyList)
                .canCloseWithEscape(true)
                .pause(false)
                .afterAction(DialogBase.DialogAfterAction.NONE)
                .build();

        // ── Тип: MultiAction (список кнопок 1 колонка) ────────────
        var type = DialogType.multiAction(buttons)
                .exitAction(exitButton)
                .columns(1)
                .build();

        // ── Создаём и показываем диалог ───────────────────────────
        Dialog dialog = Dialog.create(factory ->
                factory.empty()
                        .base(base)
                        .type(type));

        player.showDialog(dialog);
    }

    // ─── Вспомогательные методы ───────────────────────────────────

    private Component buildItemLabel(int num, String name, int price) {
        return Component.empty()
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text("#" + num + " ", NamedTextColor.AQUA))
                .append(ColorUtil.parse(name))
                .append(Component.text(" — ", NamedTextColor.GRAY))
                .append(Component.text(price + " " + plugin.getCurrencyName(),
                        NamedTextColor.GOLD));
    }

    private Component buildItemTooltip(ShopItem item, int price) {
        var sb = Component.text()
                .decoration(TextDecoration.ITALIC, false)
                .append(ColorUtil.parse(item.getName()))
                .append(Component.newline());
        for (String loreLine : item.getLore()) {
            sb.append(ColorUtil.parse(loreLine)).append(Component.newline());
        }
        sb.append(Component.text("Цена: ", NamedTextColor.GRAY))
                .append(Component.text(price + " " + plugin.getCurrencyName(),
                        NamedTextColor.GOLD));
        if (item.getAmount() > 1) {
            sb.append(Component.newline())
                    .append(Component.text("Количество: " + item.getAmount(),
                            NamedTextColor.GRAY));
        }
        return sb.build();
    }

    /** Убирает &-коды и &#HEX из строки для plain-текста */
    private String stripColors(String name) {
        if (name == null) return "";
        return name.replaceAll("&#[0-9A-Fa-f]{6}", "")
                .replaceAll("&[0-9a-fk-orA-FK-OR]", "")
                .replaceAll("§[0-9a-fk-orA-FK-OR]", "")
                .trim();
    }
}

