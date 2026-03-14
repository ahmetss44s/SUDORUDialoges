package org.SUDORU.sUDORUDialoges.menu;

import net.kyori.adventure.text.Component;
import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.SUDORU.sUDORUDialoges.shop.TraderShop;
import org.SUDORU.sUDORUDialoges.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Главное меню управления торговцами (/tradermenu).
 * Позволяет просматривать, создавать, редактировать торговцев.
 */
@SuppressWarnings("unused")
public class TraderMenuGUI {

    private static final int SIZE = 54;
    // Идентификатор меню вшит в заголовок
    public static final String TITLE_PREFIX = "§0§r§0§r§0"; // уникальный невидимый префикс
    public static final String MAIN_TITLE   = TITLE_PREFIX + "§8⚙ §6Управление §eТорговцами";

    private final SUDORUDialoges plugin;

    // Для каждого игрока: в каком режиме редактора он находится
    // null = главное меню
    private final Map<UUID, EditorState> editorStates = new HashMap<>();

    @SuppressWarnings("unused")
    public TraderMenuGUI(SUDORUDialoges plugin) {
        this.plugin = plugin;
    }

    // ─── Открытие главного меню ──────────────────────────────────────

    @SuppressWarnings("unused") public void openMain(Player player) {
        editorStates.remove(player.getUniqueId());
        Component title = ColorUtil.parse(MAIN_TITLE);
        Inventory inv = Bukkit.createInventory(null, SIZE, title);

        // Фон
        ItemStack bg = makeItem(Material.BLACK_STAINED_GLASS_PANE, "§0 ");
        for (int i = 0; i < SIZE; i++) inv.setItem(i, bg);

        // Заголовок — иконка плагина
        ItemStack header = makeItem(Material.NETHER_STAR,
                "&#FFD700⚙ §6SUDORU §eТорговая Система",
                List.of(
                        "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                        "&#a0a0a0Управляй торговцами прямо из игры.",
                        "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
                ));
        inv.setItem(4, header);

        // Список торговцев
        List<String> shopIds = new ArrayList<>(plugin.getTraderManager().getShopIds());
        int[] traderSlots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        for (int i = 0; i < shopIds.size() && i < traderSlots.length; i++) {
            String id = shopIds.get(i);
            TraderShop shop = plugin.getTraderManager().getShop(id);
            if (shop == null) continue;
            String displayName = shop.getConfig().getDisplayName();
            long refresh = shop.getConfig().getRefreshSeconds();
            int itemCount = shop.getConfig().getItems().size();
            ItemStack traderIcon = makeItem(
                    parseMat(shop.getConfig().getIconMaterial()),
                    ColorUtil.toColoredString(displayName),
                    List.of(
                            "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                            "§7ID: §f" + id,
                            "§7Предметов в пуле: §a" + itemCount,
                            "§7Обновление: §e" + (refresh > 0 ? formatTime(refresh) : "выкл."),
                            "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                            "",
                            "&#55FF55✎ §aЛКМ §7— редактировать",
                            "&#FF5555✖ §cПКМ §7— удалить",
                            "&#FFAA00▶ §eShift+ЛКМ §7— открыть магазин"
                    )
            );
            inv.setItem(traderSlots[i], traderIcon);
        }

        // Кнопка «Создать нового торговца»
        ItemStack createBtn = makeItem(Material.LIME_STAINED_GLASS_PANE,
                "&#55FF55✚ §aСоздать нового торговца",
                List.of("§7Нажми, чтобы создать нового торговца.", "§7Откроет мастер создания."));
        inv.setItem(49, createBtn);

        // Кнопка настроек валюты
        ItemStack currencyBtn = makeItem(Material.EMERALD,
                "&#55FFFF💎 §bНастройки валюты",
                List.of(
                        "§7Текущая валюта: §f" + plugin.getCurrencyName(),
                        "§7Материал: §f" + plugin.getConfig().getString("currency.item-material", "EMERALD"),
                        "",
                        "§eНажми для редактирования"
                ));
        inv.setItem(47, currencyBtn);

        // Кнопка закрыть
        ItemStack close = makeItem(Material.RED_STAINED_GLASS_PANE,
                "§c✖ Закрыть", List.of("§7Закрыть меню управления."));
        inv.setItem(53, close);

        // Разделители
        ItemStack div = makeItem(Material.GRAY_STAINED_GLASS_PANE, "§7 ");
        for (int s : new int[]{9,10,11,12,13,14,15,16,17, 36,37,38,39,40,41,42,43,44}) {
            inv.setItem(s, div);
        }

        player.openInventory(inv);
    }

    // ─── Меню редактирования одного торговца ─────────────────────────

    @SuppressWarnings("unused") public void openEditor(Player player, String traderId) {
        TraderShop shop = plugin.getTraderManager().getShop(traderId);
        if (shop == null) { player.sendMessage(ColorUtil.parse("§c✗ Торговец не найден.")); return; }

        editorStates.put(player.getUniqueId(), new EditorState(traderId, EditorMode.MAIN));
        Component title = ColorUtil.parse("§0§r§0§r§0§7✎ §6Редактор: §f" + traderId);
        Inventory inv = Bukkit.createInventory(null, SIZE, title);

        // Фон
        ItemStack bg = makeItem(Material.BLACK_STAINED_GLASS_PANE, "§0 ");
        for (int i = 0; i < SIZE; i++) inv.setItem(i, bg);

        var cfg = shop.getConfig();

        // Текущее имя
        inv.setItem(10, makeItem(Material.NAME_TAG,
                "&#FFD700✎ §6Имя торговца",
                List.of("§7Текущее: §f" + ColorUtil.toColoredString(cfg.getDisplayName()),
                        "", "§eНажми для изменения §7(в чат)")));

        // Иконка
        inv.setItem(11, makeItem(parseMat(cfg.getIconMaterial()),
                "&#FFD700✎ §6Иконка",
                List.of("§7Текущая: §f" + cfg.getIconMaterial(),
                        "", "§eНажми для изменения §7(в чат)")));

        // Время обновления
        inv.setItem(12, makeItem(Material.CLOCK,
                "&#FFD700✎ §6Таймер обновления",
                List.of("§7Текущий: §f" + (cfg.getRefreshSeconds() > 0 ? formatTime(cfg.getRefreshSeconds()) : "выкл."),
                        "", "§eНажми для изменения §7(секунды в чат)")));

        // Мин/макс предметов
        inv.setItem(13, makeItem(Material.HOPPER,
                "&#FFD700✎ §6Кол-во предметов",
                List.of("§7Мин: §a" + cfg.getMinItems() + " §7Макс: §a" + cfg.getMaxItems(),
                        "", "§eЛКМ §7— уменьшить мин", "§eПКМ §7— увеличить мин",
                        "§eShift+ЛКМ §7— уменьшить макс", "§eShift+ПКМ §7— увеличить макс")));

        // Описание
        inv.setItem(14, makeItem(Material.PAPER,
                "&#FFD700✎ §6Описание",
                List.of("§7Текущее:", ColorUtil.toColoredString(cfg.getDescription()),
                        "", "§eНажми для изменения §7(в чат)")));

        // Обновить ассортимент
        inv.setItem(15, makeItem(Material.ENDER_PEARL,
                "&#55FF55⟳ §aОбновить ассортимент",
                List.of("§7Принудительно пересчитать", "§7текущий ассортимент.")));

        // Список предметов
        inv.setItem(28, makeItem(Material.CHEST,
                "&#FFAA00☰ §6Предметы торговца",
                List.of("§7Предметов в пуле: §a" + cfg.getItems().size(),
                        "", "§eНажми для редактирования списка")));

        // Кнопки навигации
        inv.setItem(45, makeItem(Material.ARROW, "§7◀ Назад", List.of("§7Вернуться к списку торговцев.")));
        inv.setItem(53, makeItem(Material.RED_STAINED_GLASS_PANE, "§c✖ Закрыть", List.of("§7Закрыть меню.")));

        // Разделители
        ItemStack div = makeItem(Material.GRAY_STAINED_GLASS_PANE, "§7 ");
        for (int s : new int[]{9,17,18,27,36,44}) inv.setItem(s, div);

        player.openInventory(inv);
    }

    // ─── Меню настроек валюты ─────────────────────────────────────────

    @SuppressWarnings("unused") public void openCurrencySettings(Player player) {
        editorStates.put(player.getUniqueId(), new EditorState(null, EditorMode.CURRENCY));
        Component title = ColorUtil.parse("§0§r§0§r§0§b💎 §bНастройки Валюты");
        Inventory inv = Bukkit.createInventory(null, SIZE, title);

        ItemStack bg = makeItem(Material.BLACK_STAINED_GLASS_PANE, "§0 ");
        for (int i = 0; i < SIZE; i++) inv.setItem(i, bg);

        String currMat = plugin.getConfig().getString("currency.item-material", "EMERALD");
        String currName = plugin.getConfig().getString("currency.item-name", "Изумруд");

        inv.setItem(4, makeItem(parseMat(currMat), "&#55FFFF💎 §bВалюта: §f" + currName,
                List.of("§7Материал: §f" + currMat)));

        inv.setItem(20, makeItem(Material.NAME_TAG,
                "§6✎ Название валюты",
                List.of("§7Текущее: §f" + currName, "", "§eНажми для изменения §7(в чат)")));

        inv.setItem(24, makeItem(parseMat(currMat),
                "§6✎ Материал валюты",
                List.of("§7Текущий: §f" + currMat, "", "§eНажми для изменения §7(в чат)")));

        inv.setItem(45, makeItem(Material.ARROW, "§7◀ Назад", List.of("§7Вернуться к главному меню.")));
        inv.setItem(53, makeItem(Material.RED_STAINED_GLASS_PANE, "§c✖ Закрыть", List.of("§7Закрыть.")));

        player.openInventory(inv);
    }

    // ─── Меню создания нового торговца ───────────────────────────────

    @SuppressWarnings("unused") public void openCreateWizard(Player player) {
        editorStates.put(player.getUniqueId(), new EditorState(null, EditorMode.CREATE));
        Component title = ColorUtil.parse("§0§r§0§r§0§a✚ §aСоздать Торговца");
        Inventory inv = Bukkit.createInventory(null, SIZE, title);

        ItemStack bg = makeItem(Material.BLACK_STAINED_GLASS_PANE, "§0 ");
        for (int i = 0; i < SIZE; i++) inv.setItem(i, bg);

        inv.setItem(13, makeItem(Material.LIME_STAINED_GLASS_PANE,
                "&#55FF55✚ §aВведите ID торговца в чат",
                List.of(
                        "§7После нажатия — напиши ID торговца в чат.",
                        "§7Пример: §fmy_trader",
                        "",
                        "§8Допустимы буквы, цифры и _"
                )));

        inv.setItem(45, makeItem(Material.ARROW, "§7◀ Назад", List.of("§7Вернуться к главному меню.")));
        inv.setItem(53, makeItem(Material.RED_STAINED_GLASS_PANE, "§c✖ Закрыть", List.of("§7Закрыть.")));

        player.openInventory(inv);
    }

    // ─── Утилиты ──────────────────────────────────────────────────────

    public static ItemStack makeItem(Material mat, String name, List<String> lore) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;
        meta.displayName(ColorUtil.parse(name));
        meta.lore(ColorUtil.parseList(lore));
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack makeItem(Material mat, String name) {
        return makeItem(mat, name, Collections.emptyList());
    }

    private Material parseMat(String name) {
        if (name == null) return Material.CHEST;
        try { return Material.valueOf(name.toUpperCase()); }
        catch (IllegalArgumentException e) { return Material.CHEST; }
    }

    private String formatTime(long seconds) {
        if (seconds < 60) return seconds + "с";
        long min = seconds / 60; long sec = seconds % 60;
        if (min < 60) return min + "м " + (sec > 0 ? sec + "с" : "");
        long h = min / 60; long m = min % 60;
        return h + "ч " + (m > 0 ? m + "м" : "");
    }

    // Проверка по заголовку
    @SuppressWarnings("unused") public static boolean isManagerMenu(Component title) {
        // Сравниваем через строковое представление
        String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(title);
        String check = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(ColorUtil.parse(MAIN_TITLE));
        return plain.equals(check);
    }

    @SuppressWarnings("unused") public static boolean isEditorMenu(Component title) {
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(title).contains("Редактор:");
    }

    @SuppressWarnings("unused") public static boolean isCurrencyMenu(Component title) {
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(title).contains("Настройки Валюты");
    }

    @SuppressWarnings("unused") public static boolean isCreateMenu(Component title) {
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(title).contains("Создать Торговца");
    }

    // Геттеры состояния
    @SuppressWarnings("unused") public EditorState getState(UUID uuid) { return editorStates.get(uuid); }
    @SuppressWarnings("unused") public void removeState(UUID uuid) { editorStates.remove(uuid); }
    @SuppressWarnings("unused") public void setState(UUID uuid, EditorState state) { editorStates.put(uuid, state); }

    // ─── Состояние редактора ──────────────────────────────────────────

    public enum EditorMode { MAIN, CREATE, EDIT_TRADER, CURRENCY }

    public static class EditorState {
        public final String traderId;
        public final EditorMode mode;
        @SuppressWarnings("unused")
        public String pendingField;

        public EditorState(String traderId, EditorMode mode) {
            this.traderId = traderId;
            this.mode = mode;
        }
    }
}

