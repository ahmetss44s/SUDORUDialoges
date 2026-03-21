package org.SUDORU.sUDORUDialoges.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
 * Полное GUI-меню настроек из config.yml.
 * Доступно по команде /traderconfig.
 * Структура меню: главная, валюта, торговец, предметы, редактор предмета.
 */
@SuppressWarnings("unused")
public class ConfigMenuGUI {

    // ── Заголовки для определения меню по title ──────────────────────
    public static final String T_MAIN      = "§0§1§r§0§1§r§0§1§r §e⚙ §6Настройки Конфига";
    public static final String T_CURRENCY  = "§0§1§r§0§1§r§0§1§r §b💎 Валюта";
    public static final String T_TRADER    = "§0§1§r§0§1§r§0§1§r §6☰ Торговец: ";
    public static final String T_ITEMS     = "§0§1§r§0§1§r§0§1§r §e📦 Предметы: ";
    public static final String T_ITEM_EDIT = "§0§1§r§0§1§r§0§1§r §a✎ Предмет #";
    public static final String T_SYNC      = "\u00a70\u00a71\u00a7r\u00a70\u00a71\u00a7r\u00a70\u00a71\u00a7r \u00a7d\u27f3 Datapack Sync";

    private final SUDORUDialoges plugin;
    private final Map<UUID, ConfigState> states = new HashMap<>();

    @SuppressWarnings("unused")
    public ConfigMenuGUI(SUDORUDialoges plugin) {
        this.plugin = plugin;
    }

    // ════════════════════════════════════════════════════════════
    //  ГЛАВНАЯ СТРАНИЦА — обзор всего конфига
    // ════════════════════════════════════════════════════════════
    @SuppressWarnings("unused") public void openMain(Player player) {
        states.put(player.getUniqueId(), new ConfigState(ConfigPage.MAIN, null, -1));
        Inventory inv = Bukkit.createInventory(null, 54, ColorUtil.parse(T_MAIN));

        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        // ── Заголовок ──
        inv.setItem(4, item(Material.COMMAND_BLOCK,
                "&#FFD700⚙ §6Настройки §econfig.yml",
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "&#a0a0a0Здесь отображаются все параметры",
                "&#a0a0a0из файла §fconfig.yml§7.",
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "",
                "&#FFAA00ℹ §eИзменения сохраняются в файл",
                "&#FFAA00  §eавтоматически."
        ));

        // ── Секция ВАЛЮТА ──
        String currMat  = plugin.getConfig().getString("currency.item-material", "EMERALD");
        String currName = plugin.getConfig().getString("currency.item-name", "Изумруд");
        String currType = plugin.getConfig().getString("currency.type", "ITEM");
        inv.setItem(19, item(mat(currMat),
                "&#55FFFF💎 §b§lВАЛЮТА",
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Тип:       §f" + currType,
                "§7Название:  §f" + currName,
                "§7Материал:  §f" + currMat,
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "",
                "&#55FFFF▶ §bНажми для редактирования"
        ));

        // ── Секции ТОРГОВЦЕВ ──
        List<String> ids = new ArrayList<>(plugin.getTraderManager().getShopIds());
        // Слоты 21..25, 28..34 — максимум 12 торговцев
        int[] tSlots = {21,22,23,24,25,28,29,30,31,32,33,34};
        for (int i = 0; i < ids.size() && i < tSlots.length; i++) {
            String id = ids.get(i);
            TraderShop shop = plugin.getTraderManager().getShop(id);
            if (shop == null) continue;
            var cfg = shop.getConfig();
            inv.setItem(tSlots[i], item(mat(cfg.getIconMaterial()),
                    ColorUtil.toColoredString(cfg.getDisplayName()),
                    "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    "§7ID: §f" + id,
                    "§7Иконка: §f" + cfg.getIconMaterial(),
                    "§7Обновление: §e" + (cfg.getRefreshSeconds() > 0 ? fmt(cfg.getRefreshSeconds()) : "выкл."),
                    "§7Предметов: §a" + cfg.getItems().size() + " §7в пуле",
                    "§7Слотов: §amin§7=" + cfg.getMinItems() + " §7max§7=" + cfg.getMaxItems(),
                    "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    "",
                    "&#FFD700▶ §eНажми для редактирования"
            ));
        }

        // ── Разделитель ──
        ItemStack sep = item(Material.GRAY_STAINED_GLASS_PANE, "§8 ");
        for (int s : new int[]{9,10,11,12,13,14,15,16,17, 36,37,38,39,40,41,42,43,44}) inv.setItem(s, sep);

        // ── Кнопки нижней панели ──
        inv.setItem(46, item(Material.WRITABLE_BOOK,
                "&#AAFFAA✚ §aДобавить торговца",
                "§7Создать нового торговца в config.yml.",
                "§7Заполнит дефолтными значениями.",
                "",
                "&#55FF55▶ §aНажми"
        ));
        inv.setItem(48, item(Material.COMPARATOR,
                "&#FFAA00⟳ §6Перезагрузить конфиг",
                "§7Применит все изменения из файла.",
                "",
                "&#FFAA00▶ §eНажми"
        ));
        inv.setItem(50, item(Material.REPEATER,
                "&#FF5555⚠ §cЛимит закупки (×?)",
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Макс. предметов за одну закупку",
                "§7через кнопку §bКупить ×?",
                "§7Текущий: §f" + plugin.getConfig().getInt("shop.max-purchase-amount", 1000),
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "&#FFD700ЛКМ §e−100  §7|  §ePКМ §e+100",
                "&#FFD700Shift+ЛКМ §e−10  §7|  §eShift+ПКМ §e+10",
                "&#FFD700Средняя кнопка §e→ ввод вручную"
        ));
        inv.setItem(53, item(Material.RED_STAINED_GLASS_PANE, "§c✖ §cЗакрыть", "§7Закрыть меню."));

        player.openInventory(inv);
    }

    // ════════════════════════════════════════════════════════════
    //  СТРАНИЦА ВАЛЮТЫ
    // ════════════════════════════════════════════════════════════
    @SuppressWarnings("unused") public void openCurrency(Player player) {
        states.put(player.getUniqueId(), new ConfigState(ConfigPage.CURRENCY, null, -1));
        String currMat  = plugin.getConfig().getString("currency.item-material", "EMERALD");
        String currName = plugin.getConfig().getString("currency.item-name", "Изумруд");
        String currType = plugin.getConfig().getString("currency.type", "ITEM");

        Inventory inv = Bukkit.createInventory(null, 54, ColorUtil.parse(T_CURRENCY));
        fill(inv, Material.CYAN_STAINED_GLASS_PANE);
        fill2(inv, new int[]{
                9,10,11,12,13,14,15,16,17,
                18,26,27,35,
                36,37,38,39,40,41,42,43,44
        });

        // Превью валюты
        inv.setItem(4, item(mat(currMat),
                "&#55FFFF💎 §b§lВАЛЮТА",
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Тип:      §f" + currType,
                "§7Название: §f" + currName,
                "§7Материал: §f" + currMat,
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "&#AAAAAA Это валюта торговцев."
        ));

        // ── currency.type ──
        inv.setItem(19, item(Material.LEVER,
                "&#FFFF55✎ §ecurrency.type",
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Значение: §f" + currType,
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Допустимые значения:",
                "  §aITEM §7— предмет из инвентаря",
                "  §bVAULT §7— Vault экономика",
                "",
                "&#FFFF55ЛКМ §e→ §aITEM  §7| §ePКМ §e→ §bVAULT"
        ));

        // ── currency.item-name ──
        inv.setItem(21, item(Material.NAME_TAG,
                "&#FFFF55✎ §ecurrency.item-name",
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Текущее: §f" + currName,
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Название валюты в интерфейсе.",
                "§7Поддерживает §6&-коды цветов.",
                "",
                "&#FFFF55▶ §eНажми — введи в чат"
        ));

        // ── currency.item-material ──
        inv.setItem(23, item(mat(currMat),
                "&#FFFF55✎ §ecurrency.item-material",
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Текущий: §f" + currMat,
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Материал предмета, который",
                "§7используется как валюта.",
                "§7Пример: §fEMERALD, GOLD_INGOT",
                "",
                "&#FFFF55▶ §eНажми — введи в чат"
        ));

        // Превью — сколько валюты у игрока
        inv.setItem(25, item(mat(currMat),
                "&#55FF55👛 §aУ тебя: §f" + plugin.getCurrencyAmount(player) + " §7" + currName,
                "§7Текущее количество в инвентаре."
        ));

        // Кнопки навигации
        inv.setItem(45, item(Material.ARROW, "§7◀ §7Назад", "§7Вернуться к главному меню."));
        inv.setItem(53, item(Material.RED_STAINED_GLASS_PANE, "§c✖ §cЗакрыть", "§7Закрыть меню."));

        player.openInventory(inv);
    }

    // ════════════════════════════════════════════════════════════
    //  СТРАНИЦА НАСТРОЕК ТОРГОВЦА
    // ════════════════════════════════════════════════════════════
    @SuppressWarnings("unused") public void openTrader(Player player, String traderId) {
        TraderShop shop = plugin.getTraderManager().getShop(traderId);
        if (shop == null) { player.sendMessage(ColorUtil.parse("§c✗ Торговец не найден.")); return; }

        states.put(player.getUniqueId(), new ConfigState(ConfigPage.TRADER, traderId, -1));
        var cfg = shop.getConfig();

        Inventory inv = Bukkit.createInventory(null, 54,
                ColorUtil.parse(T_TRADER + traderId));
        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        // Превью торговца
        inv.setItem(4, item(mat(cfg.getIconMaterial()),
                ColorUtil.toColoredString(cfg.getDisplayName()),
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7ID торговца: §f" + traderId,
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "&#AAAAAA Редактируй параметры ниже."
        ));

        // Разделители
        ItemStack sep = item(Material.GRAY_STAINED_GLASS_PANE, "§8 ");
        for (int s : new int[]{9,10,11,12,13,14,15,16,17}) inv.setItem(s, sep);

        // ── display-name ──
        inv.setItem(19, item(Material.NAME_TAG,
                "&#FFD700✎ §6display-name",
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Имя: " + ColorUtil.toColoredString(cfg.getDisplayName()),
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Отображается в заголовке меню.",
                "§7Поддерживает &-коды и &#HEX.",
                "",
                "&#FFD700▶ §eНажми — введи в чат"
        ));

        // ── description ──
        inv.setItem(20, item(Material.PAPER,
                "&#FFD700✎ §6description",
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Текст: " + ColorUtil.toColoredString(cfg.getDescription()),
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Описание под иконкой торговца.",
                "§7Используй §f\\n §7для новой строки.",
                "",
                "&#FFD700▶ §eНажми — введи в чат"
        ));

        // ── icon-material ──
        inv.setItem(21, item(mat(cfg.getIconMaterial()),
                "&#FFD700✎ §6icon-material",
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Текущий: §f" + cfg.getIconMaterial(),
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Иконка торговца в шапке меню.",
                "§7Пример: §fANVIL, CHEST, EMERALD",
                "",
                "&#FFD700▶ §eНажми — введи в чат"
        ));

        // ── refresh-seconds ──
        inv.setItem(22, item(Material.CLOCK,
                "&#FFD700✎ §6refresh-seconds",
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Значение: §f" + cfg.getRefreshSeconds() + " §7сек",
                "§7= " + (cfg.getRefreshSeconds() > 0 ? fmt(cfg.getRefreshSeconds()) : "§cвыкл."),
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Интервал смены ассортимента.",
                "§70 = никогда не обновляется.",
                "",
                "&#FFD700ЛКМ §e-60с §7| §ePКМ §e+60с",
                "&#FFD700Shift+ЛКМ §e-300с §7| §eShift+ПКМ §e+300с",
                "&#FFD700Средняя §eВвод вручную"
        ));

        // ── min-items ──
        inv.setItem(23, item(Material.HOPPER,
                "&#FFD700✎ §6min-items",
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Значение: §a" + cfg.getMinItems(),
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Мин. предметов в ассортименте.",
                "§7Диапазон: §f1–8",
                "",
                "&#FFD700ЛКМ §e−1  §7|  §ePКМ §e+1"
        ));

        // ── max-items ──
        inv.setItem(24, item(Material.CHEST,
                "&#FFD700✎ §6max-items",
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Значение: §a" + cfg.getMaxItems(),
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Макс. предметов в ассортименте.",
                "§7Диапазон: §f1–8",
                "",
                "&#FFD700ЛКМ §e−1  §7|  §ePКМ §e+1"
        ));

        // ── Кнопка предметов ──
        inv.setItem(28, item(Material.BUNDLE,
                "&#FFAA00☰ §6Предметы торговца",
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Предметов в пуле: §a" + cfg.getItems().size(),
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Список всех возможных товаров.",
                "§7Можно добавлять и редактировать.",
                "",
                "&#FFAA00▶ §6Нажми для просмотра"
        ));

        // ── Кнопка: обновить ассортимент ──
        inv.setItem(30, item(Material.ENDER_PEARL,
                "&#55FF55⟳ §aПринудительное обновление",
                "§7Немедленно перерасчёт ассортимента.",
                "",
                "&#55FF55▶ §aНажми"
        ));

        // ── Кнопка: удалить торговца ──
        inv.setItem(32, item(Material.BARRIER,
                "&#FF5555✗ §cУдалить торговца",
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§cУдаляет §f'" + traderId + "' §cиз конфига.",
                "§c§lНЕОБРАТИМО!",
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "",
                "&#FF5555Shift+ЛКМ §cдля подтверждения"
        ));

        // Нижние кнопки
        inv.setItem(45, item(Material.ARROW, "§7◀ §7Назад", "§7Вернуться к главному меню."));
        inv.setItem(53, item(Material.RED_STAINED_GLASS_PANE, "§c✖ §cЗакрыть", "§7Закрыть меню."));

        player.openInventory(inv);
    }

    // ════════════════════════════════════════════════════════════
    //  СТРАНИЦА ПРЕДМЕТОВ ТОРГОВЦА
    // ════════════════════════════════════════════════════════════
    @SuppressWarnings("unused") public void openItems(Player player, String traderId, int page) {
        TraderShop shop = plugin.getTraderManager().getShop(traderId);
        if (shop == null) return;

        states.put(player.getUniqueId(), new ConfigState(ConfigPage.ITEMS, traderId, page));
        var cfg = shop.getConfig();
        var items = cfg.getItems();

        Inventory inv = Bukkit.createInventory(null, 54,
                ColorUtil.parse(T_ITEMS + traderId));
        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        // Заголовок
        inv.setItem(4, item(Material.BUNDLE,
                "&#FFAA00☰ §6Предметы: §f" + traderId,
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Предметов в пуле: §a" + items.size(),
                "§7Страница: §f" + (page + 1) + " / " + (Math.max(1, (items.size() + 27) / 28)),
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
        ));

        ItemStack sep = item(Material.GRAY_STAINED_GLASS_PANE, "§8 ");
        for (int s : new int[]{9,10,11,12,13,14,15,16,17}) inv.setItem(s, sep);

        // Предметы — 28 слотов (строки 3-5, слоты 18–45 без краёв)
        int[] itemSlots = {
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34,
            37,38,39,40,41,42,43
        };
        int startIdx = page * itemSlots.length;
        for (int i = 0; i < itemSlots.length; i++) {
            int idx = startIdx + i;
            if (idx >= items.size()) break;
            var si = items.get(idx);
            inv.setItem(itemSlots[i], item(
                    si.getMaterial(),
                    si.getName(),
                    "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    "§7Индекс: §f#" + (idx + 1),
                    "§7Цена: §a" + si.getBasePrice() + " §7±§c" + si.getPriceRange(),
                    "§7Шанс: §e" + si.getChance() + "%",
                    "§7Кол-во: §f" + si.getAmount(),
                    "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    "&#FFD700▶ §eЛКМ §7— редактировать",
                    "&#FF5555✗ §cПКМ §7— удалить"
            ));
        }

        // Навигация по страницам
        if (page > 0)
            inv.setItem(45, item(Material.ARROW, "§7◀ §7Пред. страница", "§7Страница " + page));
        else
            inv.setItem(45, item(Material.ARROW, "§7◀ §7Назад к торговцу", "§7Вернуться к настройкам."));

        if (startIdx + itemSlots.length < items.size())
            inv.setItem(53, item(Material.ARROW, "§7▶ §7След. страница", "§7Страница " + (page + 2)));
        else
            inv.setItem(53, item(Material.RED_STAINED_GLASS_PANE, "§c✖ §cЗакрыть", "§7Закрыть меню."));

        // Кнопка добавить предмет
        inv.setItem(49, item(Material.LIME_STAINED_GLASS_PANE,
                "&#55FF55✚ §aДобавить предмет",
                "§7Добавляет новый предмет в пул.",
                "§7Откроет мастер добавления.",
                "",
                "&#55FF55▶ §aНажми"
        ));

        player.openInventory(inv);
    }

    // ════════════════════════════════════════════════════════════
    //  СТРАНИЦА РЕДАКТИРОВАНИЯ ОДНОГО ПРЕДМЕТА
    // ════════════════════════════════════════════════════════════
    @SuppressWarnings("unused") public void openItemEdit(Player player, String traderId, int itemIdx) {
        TraderShop shop = plugin.getTraderManager().getShop(traderId);
        if (shop == null) return;
        var items = shop.getConfig().getItems();
        if (itemIdx < 0 || itemIdx >= items.size()) return;

        states.put(player.getUniqueId(), new ConfigState(ConfigPage.ITEM_EDIT, traderId, itemIdx));
        var si = items.get(itemIdx);

        Inventory inv = Bukkit.createInventory(null, 54,
                ColorUtil.parse(T_ITEM_EDIT + (itemIdx + 1) + " (" + traderId + ")"));
        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        // Превью предмета
        inv.setItem(4, item(si.getMaterial(),
                si.getName(),
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Индекс: §f#" + (itemIdx + 1),
                "§7Material: §f" + si.getMaterial(),
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
        ));

        ItemStack sep = item(Material.GRAY_STAINED_GLASS_PANE, "§8 ");
        for (int s : new int[]{9,10,11,12,13,14,15,16,17}) inv.setItem(s, sep);

        // ── material ──
        inv.setItem(19, item(si.getMaterial(),
                "&#FFD700✎ §6material",
                "§7Текущий: §f" + si.getMaterial(),
                "§7Пример: §fDIAMOND_SWORD",
                "", "&#FFD700▶ §eНажми — введи в чат"
        ));

        // ── name ──
        inv.setItem(20, item(Material.NAME_TAG,
                "&#FFD700✎ §6name",
                "§7Текущее: " + ColorUtil.toColoredString(si.getName()),
                "§7Поддерживает &-коды и &#HEX.",
                "", "&#FFD700▶ §eНажми — введи в чат"
        ));

        // ── price ──
        inv.setItem(21, item(Material.GOLD_INGOT,
                "&#FFD700✎ §6price  §7(базовая цена)",
                "§7Текущее: §a" + si.getBasePrice(),
                "", "&#FFD700ЛКМ §e−1  §7|  §ePКМ §e+1",
                "&#FFD700Shift+ЛКМ §e−10  §7|  §eShift+ПКМ §e+10"
        ));

        // ── price-range ──
        inv.setItem(22, item(Material.COMPARATOR,
                "&#FFD700✎ §6price-range  §7(разброс ±)",
                "§7Текущее: §c±" + si.getPriceRange(),
                "§7Итог: §a" + Math.max(0, si.getBasePrice() - si.getPriceRange())
                        + "§7–§a" + (si.getBasePrice() + si.getPriceRange()),
                "", "&#FFD700ЛКМ §e−1  §7|  §ePКМ §e+1"
        ));

        // ── chance ──
        inv.setItem(23, item(Material.FISHING_ROD,
                "&#FFD700✎ §6chance  §7(шанс выпадения)",
                "§7Текущее: §e" + si.getChance() + "%",
                "§7Чем выше — тем чаще появляется.",
                "", "&#FFD700ЛКМ §e−5  §7|  §ePКМ §e+5",
                "&#FFD700Shift+ЛКМ §e−1  §7|  §eShift+ПКМ §e+1"
        ));

        // ── amount ──
        inv.setItem(24, item(si.getMaterial(),
                "&#FFD700✎ §6amount  §7(кол-во в стаке)",
                "§7Текущее: §f" + si.getAmount(),
                "", "&#FFD700ЛКМ §e−1  §7|  §ePКМ §e+1"
        ));

        // ── lore ──
        inv.setItem(28, item(Material.WRITTEN_BOOK,
                "&#FFD700✎ §6lore  §7(описание предмета)",
                buildLorePreview(si.getLore()),
                "&#FFD700▶ §eНажми — введи строки в чат"
        ));

        // ── potion-type (только для зелий) ──
        if (si.getMaterial() == org.bukkit.Material.POTION
                || si.getMaterial() == org.bukkit.Material.SPLASH_POTION
                || si.getMaterial() == org.bukkit.Material.LINGERING_POTION) {
            String pt = si.getPotionType() != null ? si.getPotionType() : "нет";
            inv.setItem(30, item(Material.POTION,
                    "&#FFD700✎ §6potion-type",
                    "§7Текущий: §d" + pt,
                    "§7Пример: §fSTRONG_HEALING",
                    "", "&#FFD700▶ §eНажми — введи в чат"
            ));
        }

        // Навигация
        inv.setItem(45, item(Material.ARROW, "§7◀ §7Назад к предметам", "§7Вернуться к списку."));
        inv.setItem(53, item(Material.RED_STAINED_GLASS_PANE, "§c✖ §cЗакрыть", "§7Закрыть меню."));

        player.openInventory(inv);
    }

    // ═══════════════════════════════════════════════════════════
    //  СТРАНИЦА DATAPACK SYNC
    // ═══════════════════════════════════════════════════════════
    @SuppressWarnings("unused") public void openSync(Player player) {
        states.put(player.getUniqueId(), new ConfigState(ConfigPage.SYNC, null, -1));
        Inventory inv = Bukkit.createInventory(null, 54, ColorUtil.parse(T_SYNC));
        fill(inv, Material.BLACK_STAINED_GLASS_PANE);
        boolean autoSync = plugin.getConfig().getBoolean("datapack.auto-sync", true);
        String globalLabel = plugin.getConfig().getString("datapack.dialog-label", "shop");
        inv.setItem(4, item(Material.COMPARATOR,
                "\u00a7d\u00a7l\u27f3 Datapack Sync",
                "\u00a78\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac",
                "\u00a77Plugin \u2192 storage api-shop:config shop_data",
                "\u00a77PDC-\u043a\u043b\u044e\u0447: \u00a7f\"sudorudialogs:shop_price\"",
                "\u00a77\u0410\u0432\u0442\u043e-\u0441\u0438\u043d\u043a: " + (autoSync ? "\u00a7a\u0412\u041a\u041b" : "\u00a7c\u0412\u042b\u041a\u041b"),
                "\u00a77Dialog-label: \u00a7f" + globalLabel
        ));
        ItemStack sep = item(Material.GRAY_STAINED_GLASS_PANE, "\u00a78 ");
        for (int s : new int[]{9,10,11,12,13,14,15,16,17}) inv.setItem(s, sep);
        inv.setItem(19, item(Material.LIME_STAINED_GLASS_PANE,
                "&#55FF55\u27f3 \u00a7aSync All \u0441\u0435\u0439\u0447\u0430\u0441",
                "\u00a77\u041f\u0443\u0448\u0438\u0442 \u0432\u0441\u0435\u0445 \u0442\u043e\u0440\u0433\u043e\u0432\u0446\u0435\u0432 \u0432 storage.", "",
                "&#55FF55\u25b6 \u00a7a\u041d\u0430\u0436\u043c\u0438"));
        inv.setItem(21, item(autoSync ? Material.LIME_DYE : Material.GRAY_DYE,
                "&#FFFF55\u00a7e\u0410\u0432\u0442\u043e-\u0441\u0438\u043d\u043a: " + (autoSync ? "&#55FF55\u00a7a\u0412\u041a\u041b" : "&#FF5555\u00a7c\u0412\u042b\u041a\u041b"),
                "\u00a77\u0421\u0438\u043d\u043a\u0430\u0442\u044c \u043f\u0440\u0438 /traderreload \u0438 \u0438\u0437\u043c\u0435\u043d\u0435\u043d\u0438\u0438 \u0432 GUI.", "",
                "&#FFFF55\u25b6 \u00a7e\u041d\u0430\u0436\u043c\u0438 \u0434\u043b\u044f \u043f\u0435\u0440\u0435\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u044f"));
        inv.setItem(23, item(Material.KNOWLEDGE_BOOK,
                "&#DD55FF\u00a7d\u0413\u043b\u043e\u0431\u0430\u043b\u044c\u043d\u044b\u0439 dialog-label",
                "\u00a77\u0422\u0435\u043a\u0443\u0449\u0438\u0439: \u00a7f" + globalLabel, "",
                "&#DD55FF\u25b6 \u00a7d\u041d\u0430\u0436\u043c\u0438 \u2014 \u0432\u0432\u0435\u0434\u0438 \u0432 \u0447\u0430\u0442"));
        List<String> ids = new ArrayList<>(plugin.getTraderManager().getShopIds());
        int[] tSlots = {28,29,30,31,32,33,34,37,38,39,40,41};
        for (int i = 0; i < ids.size() && i < tSlots.length; i++) {
            String id = ids.get(i);
            TraderShop shop = plugin.getTraderManager().getShop(id);
            if (shop == null) continue;
            int shopId = plugin.getSyncService().getShopId(id);
            Long ts = plugin.getSyncService().getLastSyncTime(id);
            String tsStr = ts != null
                ? java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
                  .format(java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(ts), java.time.ZoneId.systemDefault()))
                : "\u00a7c\u043d\u0435 \u0441\u0438\u043d\u043a\u0430\u043b\u0441\u044f";
            String lbl = plugin.getConfig().getString("traders." + id + ".dialog-label", globalLabel);
            inv.setItem(tSlots[i], item(mat(shop.getConfig().getIconMaterial()),
                    ColorUtil.toColoredString(shop.getConfig().getDisplayName()),
                    "\u00a77ID: \u00a7f" + id,
                    "\u00a77ShopID: \u00a7b" + shopId + "  dialog-label: \u00a7f" + lbl,
                    "\u00a77\u0421\u0438\u043d\u043a: \u00a7f" + tsStr + "  \u0421\u043b\u043e\u0442\u043e\u0432: \u00a7a" + shop.getActiveSlots().size(), "",
                    "&#55FF55\u25b6 \u00a7a\u041d\u0430\u0436\u043c\u0438 \u2014 Push \u0441\u0435\u0439\u0447\u0430\u0441"));
        }
        inv.setItem(45, item(Material.ARROW, "\u00a77\u25c4 \u00a77\u041d\u0430\u0437\u0430\u0434", "\u00a77\u0413\u043b\u0430\u0432\u043d\u043e\u0435 \u043c\u0435\u043d\u044e."));
        inv.setItem(53, item(Material.RED_STAINED_GLASS_PANE, "\u00a7c\u2716 \u00a7c\u0417\u0430\u043a\u0440\u044b\u0442\u044c", ""));
        player.openInventory(inv);
    }    // ════════════════════════════════════════════════════════════
    //  ОПРЕДЕЛЕНИЕ МЕНЮ ПО ЗАГОЛОВКУ
    // ════════════════════════════════════════════════════════════
    @SuppressWarnings("unused") public static boolean isMain(Component c)     { return plain(c).equals(plain(ColorUtil.parse(T_MAIN))); }
    @SuppressWarnings("unused") public static boolean isCurrency(Component c) { return plain(c).equals(plain(ColorUtil.parse(T_CURRENCY))); }
    @SuppressWarnings("unused") public static boolean isTrader(Component c)   { return plain(c).startsWith(plain(ColorUtil.parse(T_TRADER))); }
    @SuppressWarnings("unused") public static boolean isItems(Component c)    { return plain(c).startsWith(plain(ColorUtil.parse(T_ITEMS))); }
    @SuppressWarnings("unused") public static boolean isSync(Component c)     { return plain(c).equals(plain(ColorUtil.parse(T_SYNC))); }
    @SuppressWarnings("unused") public static boolean isItemEdit(Component c) { return plain(c).startsWith(plain(ColorUtil.parse(T_ITEM_EDIT))); }

    /** Извлекает traderId из заголовка «☰ Предметы: <id>» */
    @SuppressWarnings("unused") public static String traderIdFromTitle(Component c, String prefix) {
        String p = plain(c);
        String pref = plain(ColorUtil.parse(prefix));
        if (p.startsWith(pref)) return p.substring(pref.length()).trim().split(" ")[0];
        return null;
    }

    // ════════════════════════════════════════════════════════════
    //  УТИЛИТЫ
    // ════════════════════════════════════════════════════════════
    private static String plain(Component c) {
        return PlainTextComponentSerializer.plainText().serialize(c);
    }

    private void fill(Inventory inv, Material mat) {
        ItemStack bg = item(mat, "§0 ");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, bg);
    }

    private void fill2(Inventory inv, int[] slots) {
        ItemStack bg = item(Material.BLACK_STAINED_GLASS_PANE, "§0 ");
        for (int s : slots) inv.setItem(s, bg);
    }

    /** Строит ItemStack с именем и lore (varargs строки → lore). */
    public static ItemStack item(Material mat, String name, String... lore) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;
        meta.displayName(ColorUtil.parse(name));
        List<Component> loreList = new ArrayList<>();
        for (String l : lore) loreList.add(ColorUtil.parse(l));
        meta.lore(loreList);
        stack.setItemMeta(meta);
        return stack;
    }

    private Material mat(String name) {
        if (name == null) return Material.CHEST;
        try { return Material.valueOf(name.toUpperCase()); }
        catch (IllegalArgumentException e) { return Material.CHEST; }
    }

    private String fmt(long s) {
        if (s < 60) return s + "с";
        long m = s / 60; long sec = s % 60;
        if (m < 60) return m + "м" + (sec > 0 ? " " + sec + "с" : "");
        long h = m / 60; long mn = m % 60;
        return h + "ч" + (mn > 0 ? " " + mn + "м" : "");
    }

    private String buildLorePreview(List<String> lore) {
        if (lore == null || lore.isEmpty()) return "§8(пусто)";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(3, lore.size()); i++) {
            if (i > 0) sb.append(" | ");
            sb.append(ColorUtil.toColoredString(lore.get(i)));
        }
        if (lore.size() > 3) sb.append("...");
        return sb.toString();
    }

    // ════════════════════════════════════════════════════════════
    //  СОСТОЯНИЕ ИГРОКА
    // ════════════════════════════════════════════════════════════
    public enum ConfigPage { MAIN, CURRENCY, TRADER, ITEMS, ITEM_EDIT, SYNC }

    public static class ConfigState {
        public final ConfigPage page;
        public final String traderId;
        public final int index;
        @SuppressWarnings("unused")
        public String pendingField;
        @SuppressWarnings("unused")
        public String pendingType;

        public ConfigState(ConfigPage page, String traderId, int index) {
            this.page = page;
            this.traderId = traderId;
            this.index = index;
        }
    }

    @SuppressWarnings("unused") public ConfigState getState(UUID uuid) { return states.get(uuid); }
    @SuppressWarnings("unused") public void putState(UUID uuid, ConfigState s) { states.put(uuid, s); }
    @SuppressWarnings("unused") public void removeState(UUID uuid) { states.remove(uuid); }
}
