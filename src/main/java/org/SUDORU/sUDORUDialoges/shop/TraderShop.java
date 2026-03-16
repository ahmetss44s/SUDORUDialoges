package org.SUDORU.sUDORUDialoges.shop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.SUDORU.sUDORUDialoges.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Одна «живая» инстанция торговца.
 * Поддерживает HEX цвета через ColorUtil.
 */
public class TraderShop {

    private static final int MENU_SIZE = 54;

    /*
     *  New Layout (6 items grid + Side Panel):
     *  Item Block (3x2):
     *    [ICON] [NAME] [DECR]
     *    [    ] [BUY1] [BUYN]
     *
     *  Grid:
     *    Item 0 (0,0) | Item 1 (0,3) | Side Panel (Cols 6-8)
     *    Item 2 (2,0) | Item 3 (2,3) |
     *    Item 4 (4,0) | Item 5 (4,3) |
     */
    private static final int MAX_ITEM_SLOTS = 6;
    private static final int MULTI_QTY      = 5;

    // Side Panel Slots
    private static final int TRADER_ICON_SLOT = 8; // Top right corner
    private static final int CLOSE_SLOT       = 53;

    private final SUDORUDialoges plugin;
    private final TraderConfig config;

    /** slotIndex (0..5) -> данные слота */
    private final Map<Integer, SlotData> activeSlots = new LinkedHashMap<>();
    private BukkitTask refreshTask;

    public TraderShop(SUDORUDialoges plugin, TraderConfig config) {
        this.plugin = plugin;
        this.config = config;
        rollAssortment();
        scheduleRefresh(); // Запускаем таймер (анимация обновления)
    }

    // ─── Хелперы слотов (Grid System) ───────────────────────────────

    // Rows: 0, 2, 4. Cols: 0, 3.
    private int getBaseRow(int index) { return (index / 2) * 2; }
    private int getBaseCol(int index) { return (index % 2) * 3; }

    public int getIconSlot(int index) { return (getBaseRow(index) * 9) + getBaseCol(index); }
    public int getNameSlot(int index) { return (getBaseRow(index) * 9) + getBaseCol(index) + 1; }
    public int getDecorSlot(int index) { return (getBaseRow(index) * 9) + getBaseCol(index) + 2; }
    
    public int getBuy1Slot(int index) { return ((getBaseRow(index) + 1) * 9) + getBaseCol(index) + 1; }
    public int getBuyNSlot(int index) { return ((getBaseRow(index) + 1) * 9) + getBaseCol(index) + 2; }

    /** Возвращает индекс товара по сырому слоту или -1 */
    public int getItemIndexByBuy1(int rawSlot) {
        for (int i=0; i<MAX_ITEM_SLOTS; i++) if (getBuy1Slot(i) == rawSlot) return i;
        return -1;
    }
    public int getItemIndexByBuyN(int rawSlot) {
        for (int i=0; i<MAX_ITEM_SLOTS; i++) if (getBuyNSlot(i) == rawSlot) return i;
        return -1;
    }

    // ─── Генерация ассортимента ──────────────────────────────────────

    public void rollAssortment() {
        activeSlots.clear();
        List<ShopItem> pool = config.getItems();
        if (pool.isEmpty()) return;

        int count = config.getMinItems()
                + (int)(Math.random() * (config.getMaxItems() - config.getMinItems() + 1));
        count = Math.min(count, MAX_ITEM_SLOTS);

        double totalChance = pool.stream().mapToDouble(ShopItem::getChance).sum();
        for (int i = 0; i < count; i++) {
            ShopItem chosen = weightedRandom(pool, totalChance);
            if (chosen != null) {
                activeSlots.put(i, new SlotData(chosen, chosen.calculatePrice(), false));
            }
        }
    }

    private ShopItem weightedRandom(List<ShopItem> pool, double total) {
        double roll = Math.random() * total;
        double cumulative = 0;
        for (ShopItem item : pool) {
            cumulative += item.getChance();
            if (roll <= cumulative) return item;
        }
        return pool.get(pool.size() - 1);
    }

    // ─── Таймер обновления ───────────────────────────────────────────

    private void scheduleRefresh() {
        if (refreshTask != null) refreshTask.cancel();
        long sec = config.getRefreshSeconds();
        if (sec <= 0) return;
        refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            rollAssortment();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (isViewingThisShop(p)) openFor(p);
            }
        }, sec * 20L, sec * 20L);
    }

    public void cancelRefresh() {
        if (refreshTask != null) refreshTask.cancel();
    }

    // ─── Открытие GUI ────────────────────────────────────────────────

    public void openFor(Player player) {
        Component title = ColorUtil.parse(config.getDisplayName());
        Inventory inv = Bukkit.createInventory(null, MENU_SIZE, title);
        fillBackground(inv);
        fillCards(inv);
        fillDescriptionPanel(inv);
        fillFooter(inv);
        player.openInventory(inv);
    }

    // ─── Фон ────────────────────────────────────────────────────────

    private void fillBackground(Inventory inv) {
        // Заполняем черным стеклом
        ItemStack dark = buildItem(Material.BLACK_STAINED_GLASS_PANE, "§0 ");
        for (int i = 0; i < MENU_SIZE; i++) inv.setItem(i, dark);

        // Header and decorative elements could be placed here if needed
        // but since we use a dense grid, we keep it minimal.
    }

    // ─── Карточки предметов ─────────────────────────────────────────

    private void fillCards(Inventory inv) {
        ItemStack soldPane = buildItem(Material.RED_STAINED_GLASS_PANE,
                "&#FF5555✗ §cПродано",
                List.of("&#888888 Этот товар уже куплен."));

        for (int i = 0; i < MAX_ITEM_SLOTS; i++) {
            SlotData data = activeSlots.get(i);
            int iconSlot = getIconSlot(i);
            int nameSlot = getNameSlot(i);
            int decorSlot = getDecorSlot(i);
            int b1Slot   = getBuy1Slot(i);
            int bNSlot   = getBuyNSlot(i);

            // Очищаем слоты перед заполнением (если там был фон)
            // Но мы уже заполнили черным стеклом, так что ок.

            if (data == null) {
                // Пустой слот товара - оставляем черное стекло или ставим заглушку "Пусто"
                ItemStack empty = buildItem(Material.GRAY_STAINED_GLASS_PANE, "&#888888— Пусто —");
                inv.setItem(iconSlot, empty);
                // Остальные слоты блока можно оставить темными или тоже empty
                continue;
            }

            if (data.isBought()) {
                inv.setItem(iconSlot, buildItem(Material.BARRIER,
                        "&#FF5555✗ §c" + stripColors(data.getItem().getName())));
                inv.setItem(nameSlot, soldPane);
                inv.setItem(b1Slot, soldPane);
                inv.setItem(bNSlot, soldPane);
                inv.setItem(decorSlot, soldPane);
                continue;
            }

            ShopItem si = data.getItem();

            // 1. Иконка предмета (Левый верхний слот блока)
            inv.setItem(iconSlot, buildProductItem(data));

            // 2. Инфо-панель (Справа от иконки)
            // Используем PAPER или SIGN с названием и ценой
            ItemStack infoItem = new ItemStack(Material.PAPER);
            {
                ItemMeta nm = infoItem.getItemMeta();
                if (nm != null) {
                    nm.displayName(ColorUtil.parse(si.getName())
                            .decoration(TextDecoration.ITALIC, false)
                            .decorate(TextDecoration.BOLD));
                    nm.lore(List.of(
                            ColorUtil.parse("&#888888▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                            ColorUtil.parse("&#FFAA00💰 §6Цена: &#FFD700§l" + data.getPrice()
                                    + " §r§7" + plugin.getCurrencyName()),
                            ColorUtil.parse("&#888888▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                            ColorUtil.parse("&#AAAAAA(Описание предмета см. на иконке)")
                    ));
                    infoItem.setItemMeta(nm);
                }
            }
            inv.setItem(nameSlot, infoItem);

            // 3. Декор (Справа от инфо, над кнопкой BuyN)
            // Можно поставить сюда ценник или просто пустоту
            inv.setItem(decorSlot, buildItem(Material.GRAY_STAINED_GLASS_PANE, "§0 "));

            // 4. Кнопка «Купить ×1» (Под инфо)
            inv.setItem(b1Slot, buildItem(Material.LIME_STAINED_GLASS_PANE,
                    "&#55FF55§l▶ §r§aCупить §l×1",
                    List.of("&#AAAAAA Купить " + si.getAmount() + " шт.",
                            "&#FFAA00Цена: &#FFD700" + data.getPrice()
                                    + " §7" + plugin.getCurrencyName())));

            // 5. Кнопка «Купить несколько» (Справа от Buy1)
            inv.setItem(bNSlot, buildItem(Material.CYAN_STAINED_GLASS_PANE,
                    "&#00AAFF§l▶ §r§bКупить §lнесколько",
                    List.of("&#AAAAAA Нажми, чтобы указать",
                            "&#AAAAAA точное количество в чате.",
                            "",
                            "&#FFAA00Цена за 1 шт: &#FFD700" + data.getPrice()
                                    + " §7" + plugin.getCurrencyName())));
        }
    }

    // ─── Панель описания (колонки 6-8) ──────────────────────────────

    private void fillDescriptionPanel(Inventory inv) {
        // Мы используем колонки 6, 7, 8 для инфо-панели магазина.
        // Слот 8 (TRADER_ICON_SLOT) - иконка
        Material iconMat = parseMaterial(config.getIconMaterial());
        inv.setItem(TRADER_ICON_SLOT, buildItem(iconMat, config.getDisplayName(),
                splitDescription(config.getDescription())));

        // Декоративные панели вокруг
        // Слот 8 занят иконкой.
        // Слоты 6, 7, 15, 16, 17, 24, 25, 26 ... 51, 52, 53 - это правая часть.

        // Заполним их серым стеклом или инфой
        int[] sideSlots = {6,7, 15,16,17, 24,25,26, 33,34,35, 42,43,44, 51,52}; 
        ItemStack sideBg = buildItem(Material.GRAY_STAINED_GLASS_PANE, "§7Информация");
        for (int s : sideSlots) inv.setItem(s, sideBg);

        // Описание магазина (разбиваем на строки в разные слоты)
        List<String> descLines = splitDescription(config.getDescription());
        // Разместим их в колонке 7 (середина панели)
        int[] textSlots = {16, 25, 34};
        for (int i=0; i < textSlots.length && i < descLines.size(); i++) {
             inv.setItem(textSlots[i], buildItem(Material.PAPER, descLines.get(i)));
        }

        // Таймер и кол-во товаров
        long sec = config.getRefreshSeconds();
        String refreshText = sec > 0 ? formatTime(sec) : "Выкл.";
        
        inv.setItem(42, buildItem(Material.CLOCK, // Слот 42 (Row 4, Col 6)
                "&#FFFF55⏱ §eОбновление",
                List.of("&#AAAAAA Через: §f" + refreshText)));
        
        inv.setItem(43, buildItem(Material.CHEST, // Слот 43 (Row 4, Col 7)
                "&#AAAAAA Товаров: §f" + activeSlots.size() + " / " + MAX_ITEM_SLOTS));
    }

    // ─── Подвал ──────────────────────────────────────────────────────

    private void fillFooter(Inventory inv) {
        // Кнопка закрытия
        inv.setItem(CLOSE_SLOT, buildItem(Material.RED_STAINED_GLASS_PANE,
                "&#FF5555✖ §cЗакрыть",
                List.of("&#AAAAAA Закрыть меню торговца.")));
    }

    // ─── Построители предметов ──────────────────────────────────────

    private ItemStack buildProductItem(SlotData data) {
        ShopItem si = data.getItem();
        Material mat = si.getMaterial();

        List<String> lore = new ArrayList<>();
        lore.add("&#555555▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        for (String line : si.getLore()) lore.add(line);
        lore.add("&#555555▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        lore.add("&#FFAA00💰 §6Цена: &#FFD700§l" + data.getPrice() + " §r§7" + plugin.getCurrencyName());
        lore.add("&#AAAAAA Количество: §f" + si.getAmount());
        lore.add("");
        lore.add("&#55FF55▼ §aНажми кнопку ниже для покупки");

        ItemStack stack = new ItemStack(mat, si.getAmount());

        // Зелья
        if (si.getPotionType() != null && !si.getPotionType().isEmpty()) {
            if (stack.getItemMeta() instanceof PotionMeta pm) {
                try {
                    pm.setBasePotionType(PotionType.valueOf(si.getPotionType().toUpperCase()));
                    stack.setItemMeta(pm);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        applyMeta(stack, si.getName(), lore);
        return stack;
    }

    /**
     * Создаёт чистый ItemStack для выдачи игроку:
     * только оригинальный лор из конфига, без пометок магазина.
     */
    private ItemStack buildRewardItem(ShopItem si) {
        ItemStack stack = new ItemStack(si.getMaterial(), si.getAmount());

        // Зелья
        if (si.getPotionType() != null && !si.getPotionType().isEmpty()) {
            if (stack.getItemMeta() instanceof PotionMeta pm) {
                try {
                    pm.setBasePotionType(PotionType.valueOf(si.getPotionType().toUpperCase()));
                    stack.setItemMeta(pm);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        applyMeta(stack, si.getName(), si.getLore());
        return stack;
    }

    private ItemStack buildBuyButton(SlotData data) {
        // Оставлен для совместимости, не используется в новом layout
        return buildItem(Material.LIME_STAINED_GLASS_PANE,
                "&#55FF55§l✔ §r&#55FF55КУПИТЬ &#FFDD00§l" + data.getPrice()
                        + " §r&#AAAAAA" + plugin.getCurrencyName(),
                List.of("&#AAAAAA Нажми чтобы купить."));
    }

    public static ItemStack buildItem(Material mat, String name, List<String> lore) {
        ItemStack stack = new ItemStack(mat);
        applyMeta(stack, name, lore);
        return stack;
    }

    public static ItemStack buildItem(Material mat, String name) {
        return buildItem(mat, name, Collections.emptyList());
    }

    private static void applyMeta(ItemStack stack, String name, List<String> lore) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        meta.displayName(ColorUtil.parse(name));
        List<Component> loreComps = new ArrayList<>();
        for (String line : lore) loreComps.add(ColorUtil.parse(line));
        meta.lore(loreComps);
        stack.setItemMeta(meta);
    }

    // ─── Обработка кликов ───────────────────────────────────────────

    public boolean isCloseSlot(int rawSlot) { return rawSlot == CLOSE_SLOT; }

    /** Покупка quantity раз (quantity=1 → обычная, quantity > 1 → «несколько» или чат-ввод) */
    public boolean tryPurchase(Player player, int slotIndex, int quantity) {
        SlotData data = activeSlots.get(slotIndex);
        if (data == null || data.isBought()) {
            player.sendMessage(ColorUtil.parse("&#FF5555✗ §cЭтот товар уже куплен или недоступен."));
            return false;
        }

        int totalPrice = data.getPrice() * quantity;
        boolean bypass = player.hasPermission("sudoru.trader.bypass");
        if (!bypass && !plugin.takeCurrency(player, totalPrice)) {
            player.sendMessage(ColorUtil.parse("&#FF5555✗ §cНедостаточно §f" + plugin.getCurrencyName() + "§c!"));
            player.sendMessage(ColorUtil.parse("&#AAAAAA Требуется: &#FFD700" + totalPrice
                    + " &#AAAAAA| У тебя: &#FFD700" + plugin.getCurrencyAmount(player)));
            return false;
        }

        ShopItem si = data.getItem();
        int remaining = si.getAmount() * quantity;
        int maxStack  = si.getMaterial().getMaxStackSize();
        while (remaining > 0) {
            int give = Math.min(remaining, maxStack);
            ItemStack reward = buildRewardItem(si);
            reward.setAmount(give);
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(reward);
            if (!overflow.isEmpty()) {
                for (ItemStack drop : overflow.values())
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                player.sendMessage(ColorUtil.parse("&#FFAA00⚠ §eИнвентарь переполнен. Часть упала на землю."));
            }
            remaining -= give;
        }

        data.setBought(true);
        String qtyStr = quantity > 1 ? " §7×" + quantity : "";
        player.sendMessage(ColorUtil.parse("&#55FF55✔ §aПокупка успешна: §f" + si.getName() + qtyStr));
        return true;
    }

    public boolean tryPurchase(Player player, int slotIndex) {
        return tryPurchase(player, slotIndex, 1);
    }

    // ─── Утилиты ─────────────────────────────────────────────────────

    boolean isViewingThisShop(Player p) {
        Component openTitle = p.getOpenInventory().title();
        Component shopTitle = ColorUtil.parse(config.getDisplayName());
        String plain1 = PlainTextComponentSerializer.plainText().serialize(openTitle);
        String plain2 = PlainTextComponentSerializer.plainText().serialize(shopTitle);
        return plain1.equals(plain2);
    }

    private List<String> splitDescription(String desc) {
        if (desc == null) return Collections.emptyList();
        return List.of(desc.split("\\\\n|\\n"));
    }

    private String formatTime(long seconds) {
        if (seconds < 60) return seconds + "с";
        long min = seconds / 60; long sec = seconds % 60;
        if (min < 60) return min + "м " + (sec > 0 ? sec + "с" : "");
        long h = min / 60; long m = min % 60;
        return h + "ч " + (m > 0 ? m + "м" : "");
    }

    private Material parseMaterial(String name) {
        if (name == null) return Material.CHEST;
        try { return Material.valueOf(name.toUpperCase()); }
        catch (IllegalArgumentException e) { return Material.CHEST; }
    }

    private String stripColors(String name) {
        if (name == null) return "";
        return name.replaceAll("&#[0-9A-Fa-f]{6}", "")
                .replaceAll("&[0-9a-fk-orA-FK-OR]", "")
                .replaceAll("§[0-9a-fk-orA-FK-OR]", "")
                .trim();
    }

    public TraderConfig getConfig() { return config; }
    /** Возвращает текущий ассортимент (slotIndex → SlotData) */
    public Map<Integer, SlotData> getActiveSlots() { return Collections.unmodifiableMap(activeSlots); }
    /** Количество слотов товаров */
    public int getProductSlotsCount() { return MAX_ITEM_SLOTS; }

    // ─── Внутренний класс данных слота ───────────────────────────────

    public static class SlotData {
        private final ShopItem item;
        private final int price;
        private boolean bought;

        public SlotData(ShopItem item, int price, boolean bought) {
            this.item = item;
            this.price = price;
            this.bought = bought;
        }
        public ShopItem getItem() { return item; }
        public int getPrice() { return price; }
        public boolean isBought() { return bought; }
        public void setBought(boolean b) { this.bought = b; }
    }
}
