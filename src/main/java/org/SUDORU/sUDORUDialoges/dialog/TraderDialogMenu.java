package org.SUDORU.sUDORUDialoges.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogInstancesProvider;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.ItemDialogBody;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.SUDORU.sUDORUDialoges.shop.ShopItem;
import org.SUDORU.sUDORUDialoges.shop.TraderShop;
import org.SUDORU.sUDORUDialoges.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"UnstableApiUsage", "deprecation"})
public class TraderDialogMenu implements Listener {

    private static final int MULTI_QTY = 5;
    private final SUDORUDialoges plugin;

    /** Игроки, ожидающие ввода количества в чат */
    private final Map<UUID, PendingBuy> pendingBuys = new ConcurrentHashMap<>();

    // ── Внутренний класс состояния ─────────────────────────────────
    private static class PendingBuy {
        final TraderShop shop;
        final int slotIndex;
        final int pricePerBatch;    // цена за 1 покупку (data.getPrice())
        final int amountPerBatch;   // кол-во предметов в 1 покупке (si.getAmount())

        PendingBuy(TraderShop shop, int slotIndex, int pricePerBatch, int amountPerBatch) {
            this.shop = shop;
            this.slotIndex = slotIndex;
            this.pricePerBatch = pricePerBatch;
            this.amountPerBatch = amountPerBatch;
        }
    }

    public TraderDialogMenu(SUDORUDialoges plugin) {
        this.plugin = plugin;
    }

    // ── Чат-обработчик ввода количества ───────────────────────────

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PendingBuy pending = pendingBuys.get(player.getUniqueId());
        if (pending == null) return;

        event.setCancelled(true);
        String input = event.getMessage().trim();

        if (input.equalsIgnoreCase("отмена") || input.equalsIgnoreCase("cancel")) {
            pendingBuys.remove(player.getUniqueId());
            player.sendMessage(ColorUtil.parse("§7Покупка отменена."));
            Bukkit.getScheduler().runTask(plugin, () -> openItemCard(player, pending.shop, pending.slotIndex));
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(input);
            if (qty <= 0) throw new NumberFormatException("<=0");
        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtil.parse("§c✗ Введите целое положительное число! §7(или §cотмена§7)"));
            return;
        }

        final int finalQty = qty;
        final int totalCost = pending.pricePerBatch * qty;
        final int totalItems = pending.amountPerBatch * qty;

        pendingBuys.remove(player.getUniqueId());

        Bukkit.getScheduler().runTask(plugin, () -> {
            player.sendMessage(ColorUtil.parse(
                    "&#AAAAAA Покупка: §f" + totalItems + " §7шт. | Итого: &#FFD700" +
                    totalCost + " §7" + plugin.getCurrencyName()));
            boolean ok = pending.shop.tryPurchase(player, pending.slotIndex, finalQty);
            if (ok) {
                openItemCard(player, pending.shop, pending.slotIndex);
            }
        });
    }

    // ── Главный список товаров ─────────────────────────────────────

    /** Открыть главный список товаров торговца */
    public void open(Player player, TraderShop shop) {
        pendingBuys.remove(player.getUniqueId()); // сброс любого ожидания

        var cfg = shop.getConfig();
        var prov = DialogInstancesProvider.instance();
        ClickCallback.Options opts = makeOpts();

        List<DialogBody> bodyList = new ArrayList<>();
        String desc = cfg.getDescription();
        if (desc != null && !desc.isEmpty()) {
            bodyList.add(prov.plainMessageDialogBody(
                    ColorUtil.parse(desc.replace("\\n", "\n")), 280));
        }

        List<ActionButton> buttons = new ArrayList<>();
        Map<Integer, TraderShop.SlotData> slots = shop.getActiveSlots();

        if (slots.isEmpty()) {
            buttons.add(ActionButton.builder(
                    Component.text("— Нет товаров —", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false))
                    .action(prov.register((v, a) -> {}, opts)).width(280).build());
        }

        for (int i = 0; i < shop.getProductSlotsCount(); i++) {
            TraderShop.SlotData data = slots.get(i);
            if (data == null) continue;
            final int idx = i;

            if (data.isBought()) {
                String rawName = stripColors(data.getItem().getName());
                buttons.add(ActionButton.builder(
                        Component.empty().decoration(TextDecoration.ITALIC, false)
                                .append(Component.text("✗ ", NamedTextColor.DARK_RED))
                                .append(Component.text(rawName, NamedTextColor.DARK_RED)
                                        .decorate(TextDecoration.BOLD))
                                .append(Component.text(" — КУПЛЕНО", NamedTextColor.DARK_RED)))
                        .action(prov.register((v, a) -> {}, opts)).width(280).build());
            } else {
                ShopItem si = data.getItem();
                Component label = Component.empty().decoration(TextDecoration.ITALIC, false)
                        .append(Component.text("▶ ", NamedTextColor.GREEN))
                        .append(ColorUtil.parse(si.getName()).decorate(TextDecoration.BOLD))
                        .append(Component.text("  —  ", NamedTextColor.GRAY))
                        .append(Component.text(data.getPrice() + " " + plugin.getCurrencyName(),
                                NamedTextColor.GOLD));

                buttons.add(ActionButton.builder(label)
                        .tooltip(Component.text("Нажми для просмотра товара", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false))
                        .action(prov.register((v, a) -> {
                            if (!(a instanceof Player p)) return;
                            plugin.getServer().getScheduler().runTask(plugin,
                                    () -> openItemCard(p, shop, idx));
                        }, opts)).width(280).build());
            }
        }

        ActionButton exitBtn = ActionButton.builder(
                Component.text("✖ Закрыть", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false))
                .action(prov.register((v, a) -> {
                    if (!(a instanceof Player p)) return;
                    plugin.getServer().getScheduler().runTask(plugin, () -> p.closeInventory());
                }, opts)).width(150).build();

        DialogBase base = prov.dialogBaseBuilder(
                        ColorUtil.parse(cfg.getDisplayName()).decorate(TextDecoration.BOLD))
                .externalTitle(ColorUtil.parse(cfg.getDisplayName()))
                .body(bodyList).canCloseWithEscape(true).pause(false)
                .afterAction(DialogBase.DialogAfterAction.NONE).build();

        player.showDialog(Dialog.create(f -> f.empty().base(base)
                .type(prov.multiAction(buttons).exitAction(exitBtn).columns(1).build())));
    }

    // ── Карточка конкретного товара ────────────────────────────────

    /** Открыть карточку конкретного товара */
    private void openItemCard(Player player, TraderShop shop, int slotIndex) {
        TraderShop.SlotData data = shop.getActiveSlots().get(slotIndex);
        if (data == null) { open(player, shop); return; }

        var prov = DialogInstancesProvider.instance();
        ClickCallback.Options opts = makeOpts();
        ShopItem si = data.getItem();
        int price = data.getPrice();

        // Строка с названием предмета (жирная)
        var nameBody = prov.plainMessageDialogBody(
                ColorUtil.parse(si.getName())
                        .decoration(TextDecoration.ITALIC, false)
                        .decorate(TextDecoration.BOLD), 310);

        // Иконка + описание (лор + цена)
        ItemDialogBody itemBody = prov.itemDialogBodyBuilder(buildDisplayStack(si))
                .description(prov.plainMessageDialogBody(buildLoreComponent(si, price), 200))
                .showDecorations(true).showTooltip(false).width(310).height(64).build();

        ActionButton buy1Btn;
        ActionButton buyNBtn;

        if (data.isBought()) {
            Component soldLabel = Component.text("✗ Уже куплено", NamedTextColor.DARK_RED)
                    .decoration(TextDecoration.ITALIC, false);
            buy1Btn = ActionButton.builder(soldLabel)
                    .action(prov.register((v, a) -> {}, opts)).width(130).build();
            buyNBtn = ActionButton.builder(soldLabel)
                    .action(prov.register((v, a) -> {}, opts)).width(130).build();
        } else {
            // ── Купить ×1 ──────────────────────────────────────────
            buy1Btn = ActionButton.builder(
                    Component.text("▶ Купить ×1", NamedTextColor.GREEN)
                            .decoration(TextDecoration.ITALIC, false)
                            .decorate(TextDecoration.BOLD))
                    .tooltip(Component.empty().decoration(TextDecoration.ITALIC, false)
                            .append(Component.text("Купить " + si.getAmount() + " шт.\n", NamedTextColor.GRAY))
                            .append(Component.text("Цена: " + price + " " + plugin.getCurrencyName(),
                                    NamedTextColor.GOLD)))
                    .action(prov.register((v, a) -> {
                        if (!(a instanceof Player p)) return;
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            shop.tryPurchase(p, slotIndex, 1);
                            openItemCard(p, shop, slotIndex);
                        });
                    }, opts)).width(130).build();

            // ── Купить ×N (ввод через чат) ─────────────────────────
            buyNBtn = ActionButton.builder(
                    Component.text("▶ Купить ×?", NamedTextColor.AQUA)
                            .decoration(TextDecoration.ITALIC, false)
                            .decorate(TextDecoration.BOLD))
                    .tooltip(Component.empty().decoration(TextDecoration.ITALIC, false)
                            .append(Component.text("Введи количество в чат.\n", NamedTextColor.GRAY))
                            .append(Component.text("Цена за 1 покупку: ", NamedTextColor.GRAY))
                            .append(Component.text(price + " " + plugin.getCurrencyName(), NamedTextColor.GOLD)))
                    .action(prov.register((v, a) -> {
                        if (!(a instanceof Player p)) return;
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            // Закрыть диалог и ждать ввода в чат
                            pendingBuys.put(p.getUniqueId(),
                                    new PendingBuy(shop, slotIndex, price, si.getAmount()));
                            p.closeInventory();
                            p.sendMessage(ColorUtil.parse(
                                    "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                            p.sendMessage(ColorUtil.parse(
                                    "&#FFAA00➤ §6Сколько раз купить §f" +
                                    si.getName() + "§6?"));
                            p.sendMessage(ColorUtil.parse(
                                    "&#AAAAAA  1 покупка = §f" + si.getAmount() +
                                    " §7шт. | Цена за 1: &#FFD700" + price +
                                    " §7" + plugin.getCurrencyName()));
                            p.sendMessage(ColorUtil.parse(
                                    "&#888888  Введи число или §cотмена"));
                            p.sendMessage(ColorUtil.parse(
                                    "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                        });
                    }, opts)).width(130).build();
        }

        ActionButton backBtn = ActionButton.builder(
                Component.text("← Назад", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false))
                .action(prov.register((v, a) -> {
                    if (!(a instanceof Player p)) return;
                    plugin.getServer().getScheduler().runTask(plugin, () -> open(p, shop));
                }, opts)).width(200).build();

        Component cardTitle = ColorUtil.parse(si.getName())
                .decoration(TextDecoration.ITALIC, false)
                .decorate(TextDecoration.BOLD);

        DialogBase base = prov.dialogBaseBuilder(cardTitle).externalTitle(cardTitle)
                .body(List.of(nameBody, itemBody)).canCloseWithEscape(true).pause(false)
                .afterAction(DialogBase.DialogAfterAction.NONE).build();

        player.showDialog(Dialog.create(f -> f.empty().base(base)
                .type(prov.multiAction(List.of(buy1Btn, buyNBtn)).exitAction(backBtn).columns(2).build())));
    }

    // ── Утилиты ───────────────────────────────────────────────────

    private ClickCallback.Options makeOpts() {
        return ClickCallback.Options.builder()
                .uses(ClickCallback.UNLIMITED_USES)
                .lifetime(Duration.ofMinutes(10)).build();
    }

    /** Строит ItemStack для отображения в диалоге (с жирным именем) */
    private ItemStack buildDisplayStack(ShopItem si) {
        Material mat = si.getMaterial() != null ? si.getMaterial() : Material.CHEST;
        ItemStack stack = new ItemStack(mat, si.getAmount());
        if (si.getPotionType() != null && !si.getPotionType().isEmpty()) {
            if (stack.getItemMeta() instanceof PotionMeta pm) {
                try {
                    pm.setBasePotionType(PotionType.valueOf(si.getPotionType().toUpperCase()));
                    stack.setItemMeta(pm);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.parse(si.getName())
                    .decoration(TextDecoration.ITALIC, false)
                    .decorate(TextDecoration.BOLD));
            List<Component> loreComps = new ArrayList<>();
            for (String line : si.getLore())
                loreComps.add(ColorUtil.parse(line).decoration(TextDecoration.ITALIC, false));
            meta.lore(loreComps);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    /** Строит компонент с лором и ценой для панели описания */
    private Component buildLoreComponent(ShopItem si, int price) {
        var sb = Component.text().decoration(TextDecoration.ITALIC, false);
        for (String line : si.getLore())
            sb.append(ColorUtil.parse(line)).append(Component.newline());
        if (!si.getLore().isEmpty()) sb.append(Component.newline());
        sb.append(Component.text("Цена: ", NamedTextColor.GRAY))
                .append(Component.text(price + " " + plugin.getCurrencyName(), NamedTextColor.GOLD));
        if (si.getAmount() > 1)
            sb.append(Component.newline())
              .append(Component.text("Количество: " + si.getAmount() + " шт.", NamedTextColor.GRAY));
        return sb.build();
    }

    private String stripColors(String name) {
        if (name == null) return "";
        return name.replaceAll("&#[0-9A-Fa-f]{6}", "")
                .replaceAll("&[0-9a-fk-orA-FK-OR]", "")
                .replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();
    }
}