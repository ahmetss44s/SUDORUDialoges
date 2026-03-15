package org.SUDORU.sUDORUDialoges.shop;

import net.kyori.adventure.text.Component;
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

    /*  Layout (9 cols × 6 rows):
     *  Col:  0    1    2    3    4   | 5  | 6    7    8
     *  R0:  [HDR][HDR][HDR][HDR][HDR][HDR][HDR][HDR][HDR]  header
     *  R1:  [NM0][NM1][NM2][NM3][NM4][SEP][   ][ICO][   ]  names  │ trader icon
     *  R2:  [IC0][IC1][IC2][IC3][IC4][SEP][DSC][DSC][DSC]  icons   │ description
     *  R3:  [B10][B11][B12][B13][B14][SEP][DSC][DSC][DSC]  buy×1   │ description
     *  R4:  [BN0][BN1][BN2][BN3][BN4][SEP][INF][INF][INF]  buy×5   │ refresh info
     *  R5:  [   ][   ][   ][   ][   ][SEP][   ][   ][CLO]  footer
     */
    private static final int   MAX_ITEM_SLOTS = 5;
    private static final int[] NAME_SLOTS  = {9,  10, 11, 12, 13};
    private static final int[] ICON_SLOTS  = {18, 19, 20, 21, 22};
    private static final int[] BUY1_SLOTS  = {27, 28, 29, 30, 31};
    private static final int[] BUY5_SLOTS  = {36, 37, 38, 39, 40};
    private static final int   CLOSE_SLOT  = 53;
    private static final int   MULTI_QTY   = 5;   // кол-во для кнопки "×5"

    private final SUDORUDialoges plugin;
    private final TraderConfig config;

    /** slotIndex → данные слота */
    private final Map<Integer, SlotData> activeSlots = new LinkedHashMap<>();
    private BukkitTask refreshTask;

    public TraderShop(SUDORUDialoges plugin, TraderConfig config) {
        this.plugin = plugin;
        this.config = config;
        rollAssortment();
        scheduleRefresh();
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
        ItemStack dark = buildItem(Material.BLACK_STAINED_GLASS_PANE, "§0 ");
        for (int i = 0; i < MENU_SIZE; i++) inv.setItem(i, dark);

        // Заголовок (ряд 0): иконка торговца по центру
        Material iconMat = parseMaterial(config.getIconMaterial());
        ItemStack titleIcon = buildItem(iconMat, config.getDisplayName(),
                splitDescription(config.getDescription()));
        inv.setItem(4, titleIcon);

        // Украшения заголовка
        inv.setItem(3, buildItem(Material.CYAN_STAINED_GLASS_PANE,   "§3 "));
        inv.setItem(5, buildItem(Material.CYAN_STAINED_GLASS_PANE,   "§3 "));
        inv.setItem(2, buildItem(Material.PURPLE_STAINED_GLASS_PANE, "§5 "));
        inv.setItem(6, buildItem(Material.PURPLE_STAINED_GLASS_PANE, "§5 "));

        // Разделитель (колонка 5 — слоты 14,23,32,41,50)
        ItemStack sep = buildItem(Material.GRAY_STAINED_GLASS_PANE, "§8 ");
        for (int r = 1; r <= 5; r++) inv.setItem(r * 9 + 5, sep);

        // Зона карточек (строки 1-4, колонки 0-4) — светлый фон
        ItemStack cardBg = buildItem(Material.GRAY_STAINED_GLASS_PANE, "§8 ");
        for (int row = 1; row <= 4; row++)
            for (int col = 0; col < 5; col++)
                inv.setItem(row * 9 + col, cardBg);
    }

    // ─── Карточки предметов ─────────────────────────────────────────

    private void fillCards(Inventory inv) {
        ItemStack soldPane = buildItem(Material.RED_STAINED_GLASS_PANE,
                "&#FF5555✗ §cПродано",
                List.of("&#888888 Этот товар уже куплен."));

        for (int i = 0; i < MAX_ITEM_SLOTS; i++) {
            SlotData data = activeSlots.get(i);

            if (data == null) {
                // Пустой слот
                ItemStack empty = buildItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "&#888888— Пусто —");
                inv.setItem(NAME_SLOTS[i], empty);
                inv.setItem(ICON_SLOTS[i], empty);
                inv.setItem(BUY1_SLOTS[i], empty);
                inv.setItem(BUY5_SLOTS[i], empty);
                continue;
            }

            if (data.isBought()) {
                // Куплено
                inv.setItem(NAME_SLOTS[i], soldPane);
                inv.setItem(ICON_SLOTS[i], buildItem(Material.BARRIER,
                        "&#FF5555✗ §c" + stripColors(data.getItem().getName())));
                inv.setItem(BUY1_SLOTS[i], soldPane);
                inv.setItem(BUY5_SLOTS[i], soldPane);
                continue;
            }

            ShopItem si = data.getItem();

            // Имя предмета (строка выше иконки)
            inv.setItem(NAME_SLOTS[i], buildItem(Material.PAPER,
                    si.getName(),
                    List.of("&#888888▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                            "&#FFAA00💰 §6Цена: &#FFD700§l" + data.getPrice()
                                    + " §r§7" + plugin.getCurrencyName(),
                            "&#888888▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")));

            // Иконка предмета (большой квадрат — по центру карточки)
            inv.setItem(ICON_SLOTS[i], buildProductItem(data));

            // Кнопка «Купить ×1»
            inv.setItem(BUY1_SLOTS[i], buildItem(Material.LIME_STAINED_GLASS_PANE,
                    "&#55FF55§l▶ §r§aCупить §l×1",
                    List.of("&#AAAAAA Купить " + si.getAmount() + " шт.",
                            "&#FFAA00Цена: &#FFD700" + data.getPrice()
                                    + " §7" + plugin.getCurrencyName())));

            // Кнопка «Купить ×5»
            int price5 = data.getPrice() * MULTI_QTY;
            inv.setItem(BUY5_SLOTS[i], buildItem(Material.CYAN_STAINED_GLASS_PANE,
                    "&#00AAFF§l▶ §r§bКупить §l×" + MULTI_QTY,
                    List.of("&#AAAAAA Купить " + (si.getAmount() * MULTI_QTY) + " шт.",
                            "&#FFAA00Цена: &#FFD700" + price5
                                    + " §7" + plugin.getCurrencyName())));
        }
    }

    // ─── Панель описания (колонки 6-8) ──────────────────────────────

    private void fillDescriptionPanel(Inventory inv) {
        // Иконка торговца — слот 17 (ряд 1, кол 8)
        Material iconMat = parseMaterial(config.getIconMaterial());
        inv.setItem(17, buildItem(iconMat, config.getDisplayName(),
                splitDescription(config.getDescription())));

        // Описание — слоты 24-26 (ряд 2, кол 6-8)
        List<String> descLines = splitDescription(config.getDescription());
        String[] descSlotNames = {"", "", ""};
        for (int i = 0; i < Math.min(3, descLines.size()); i++) {
            descSlotNames[i] = descLines.get(i);
        }
        for (int col = 0; col < 3; col++) {
            int slot = 24 + col;
            inv.setItem(slot, buildItem(Material.PAPER,
                    descSlotNames[col].isEmpty() ? "§8 " : descSlotNames[col]));
        }

        // Описание продолжение — слоты 33-35 (ряд 3, кол 6-8)
        String[] descLines2 = {"", "", ""};
        for (int i = 0; i < 3 && (i + 3) < descLines.size(); i++) {
            descLines2[i] = descLines.get(i + 3);
        }
        for (int col = 0; col < 3; col++) {
            int slot = 33 + col;
            inv.setItem(slot, buildItem(Material.PAPER,
                    descLines2[col].isEmpty() ? "§8 " : descLines2[col]));
        }

        // Информация об обновлении — слоты 42-44 (ряд 4, кол 6-8)
        long sec = config.getRefreshSeconds();
        String refreshText = sec > 0 ? formatTime(sec) : "Выкл.";
        inv.setItem(42, buildItem(Material.CLOCK,
                "&#FFFF55⏱ §eОбновление",
                List.of("&#AAAAAA Через: §f" + refreshText)));
        inv.setItem(43, buildItem(Material.CHEST,
                "&#AAAAAA Товаров: §f" + activeSlots.size() + " / " + MAX_ITEM_SLOTS));
        inv.setItem(44, buildItem(Material.GRAY_STAINED_GLASS_PANE, "§8 "));
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

    /** Возвращает индекс слота (0-4) для кнопки «×1», или -1 */
    public int getBuy1SlotIndex(int rawSlot) {
        for (int i = 0; i < BUY1_SLOTS.length; i++)
            if (BUY1_SLOTS[i] == rawSlot) return i;
        return -1;
    }

    /** Возвращает индекс слота (0-4) для кнопки «×5», или -1 */
    public int getBuy5SlotIndex(int rawSlot) {
        for (int i = 0; i < BUY5_SLOTS.length; i++)
            if (BUY5_SLOTS[i] == rawSlot) return i;
        return -1;
    }

    /** @deprecated используй getBuy1SlotIndex / getBuy5SlotIndex */
    public int getBuySlotIndex(int rawSlot) {
        int r = getBuy1SlotIndex(rawSlot);
        return r >= 0 ? r : getBuy5SlotIndex(rawSlot);
    }

    public boolean isCloseSlot(int rawSlot) { return rawSlot == CLOSE_SLOT; }

    /** Покупка quantity раз (quantity=1 → обычная, quantity=MULTI_QTY → «несколько») */
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
