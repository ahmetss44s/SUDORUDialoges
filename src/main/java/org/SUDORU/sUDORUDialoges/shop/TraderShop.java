package org.SUDORU.sUDORUDialoges.shop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
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
 * Одна «живая» инстанция торговца:
 * хранит текущий ассортимент, открывает GUI, обновляет таймер.
 */
public class TraderShop {

    private static final int MENU_SIZE = 54;

    // Товары: 19, 21, 23, 25, 27  Кнопки покупки: 28, 30, 32, 34, 36
    private static final int[] PRODUCT_SLOTS = {19, 21, 23, 25, 27};
    private static final int[] BUTTON_SLOTS  = {28, 30, 32, 34, 36};

    private final SUDORUDialoges plugin;
    private final TraderConfig config;

    /** Текущие активные товары: slotIndex → данные слота */
    private final Map<Integer, SlotData> activeSlots = new LinkedHashMap<>();
    private BukkitTask refreshTask;

    // Сериализатор §-кодов в Adventure Component
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    public TraderShop(SUDORUDialoges plugin, TraderConfig config) {
        this.plugin = plugin;
        this.config = config;
        rollAssortment();
        scheduleRefresh();
    }

    // ─── Генерация ассортимента ──────────────────────────────────────

    /** Выбирает от minItems до maxItems предметов по шансам (допускается дублирование). */
    public void rollAssortment() {
        activeSlots.clear();
        List<ShopItem> pool = config.getItems();
        if (pool.isEmpty()) return;

        int count = config.getMinItems()
                + (int)(Math.random() * (config.getMaxItems() - config.getMinItems() + 1));
        count = Math.min(count, PRODUCT_SLOTS.length);

        // Взвешенная случайная выборка (с возвратом — дубли разрешены)
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
            // обновить открытые меню
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (isViewingThisShop(p)) {
                    openFor(p);
                }
            }
        }, sec * 20L, sec * 20L);
    }

    public void cancelRefresh() {
        if (refreshTask != null) refreshTask.cancel();
    }

    // ─── Открытие GUI ────────────────────────────────────────────────

    public void openFor(Player player) {
        Component title = toLegacyComponent(config.getDisplayName());
        Inventory inv = Bukkit.createInventory(null, MENU_SIZE, title);

        fillDecoration(inv);
        fillHeader(inv);
        fillItems(inv);
        fillFooter(inv);

        player.openInventory(inv);
    }

    // Верхняя шапка: иконка торговца + описание
    private void fillHeader(Inventory inv) {
        // Строка 1 (0-8): рамка + иконка + описание
        Material iconMat = parseMaterial(config.getIconMaterial());
        ItemStack icon = buildItem(iconMat, config.getDisplayName(),
                splitDescription(config.getDescription()));
        inv.setItem(4, icon);

        // Разделитель
        ItemStack divider = buildItem(Material.BLACK_STAINED_GLASS_PANE, " ", Collections.emptyList());
        for (int s : new int[]{0,1,2,3,5,6,7,8, 9,10,11,12,13,14,15,16,17}) {
            inv.setItem(s, divider);
        }
    }

    private void fillItems(Inventory inv) {
        ItemStack barrier = buildItem(Material.BARRIER, "§c✗ Продано",
                List.of("§7Этот товар уже куплен."));
        ItemStack empty   = buildItem(Material.GRAY_STAINED_GLASS_PANE, "§8— Пусто —",
                Collections.emptyList());

        for (int i = 0; i < PRODUCT_SLOTS.length; i++) {
            int pSlot = PRODUCT_SLOTS[i];
            int bSlot = BUTTON_SLOTS[i];
            SlotData data = activeSlots.get(i);

            if (data == null) {
                inv.setItem(pSlot, empty);
                inv.setItem(bSlot, empty);
            } else if (data.isBought()) {
                inv.setItem(pSlot, barrier);
                inv.setItem(bSlot, buildItem(Material.GRAY_STAINED_GLASS_PANE, " ",
                        Collections.emptyList()));
            } else {
                inv.setItem(pSlot, buildProductItem(data));
                inv.setItem(bSlot, buildBuyButton(data));
            }
        }
    }

    private void fillFooter(Inventory inv) {
        // Строка 6 (45-53): информация о следующем обновлении
        ItemStack glass = buildItem(Material.BLACK_STAINED_GLASS_PANE, " ", Collections.emptyList());
        for (int s = 45; s < 54; s++) inv.setItem(s, glass);

        long sec = config.getRefreshSeconds();
        String refreshText = sec > 0 ? formatTime(sec) : "§7Никогда";
        ItemStack info = buildItem(Material.CLOCK,
                "§e⏱ Следующее обновление",
                List.of("§7Ассортимент обновится через:", "§f" + refreshText,
                        "", "§8• Всего слотов: §f" + activeSlots.size()));
        inv.setItem(49, info);

        // Кнопка закрыть
        ItemStack close = buildItem(Material.RED_STAINED_GLASS_PANE,
                "§c✖ Закрыть", List.of("§7Закрыть меню торговца."));
        inv.setItem(53, close);
    }

    private void fillDecoration(Inventory inv) {
        ItemStack filler = buildItem(Material.GRAY_STAINED_GLASS_PANE, " ", Collections.emptyList());
        // Заполняем все слоты серым для начала (потом перезапишем нужные)
        for (int s = 0; s < MENU_SIZE; s++) inv.setItem(s, filler);
    }

    // ─── Построители предметов ──────────────────────────────────────

    private ItemStack buildProductItem(SlotData data) {
        ShopItem si = data.getItem();
        Material mat = si.getMaterial();

        List<String> lore = new ArrayList<>();
        lore.add("§8──────────────────");
        lore.addAll(colorizeList(si.getLore()));
        lore.add("§8──────────────────");
        lore.add("§7Цена: §a" + data.getPrice() + " §7" + plugin.getCurrencyName());
        lore.add("§7Кол-во: §f" + si.getAmount());
        lore.add("");
        lore.add("§eНажми на кнопку ниже, чтобы купить");

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

        applyMeta(stack, colorize(si.getName()), lore);
        return stack;
    }

    private ItemStack buildBuyButton(SlotData data) {
        return buildItem(Material.LIME_STAINED_GLASS_PANE,
                "§a§l✔ КУПИТЬ §r§a— §f" + data.getPrice() + " " + plugin.getCurrencyName(),
                List.of("§7Нажми, чтобы приобрести этот товар."));
    }

    /** Строит ItemStack используя Adventure displayName и lore через компоненты. */
    public static ItemStack buildItem(Material mat, String legacyName, List<String> legacyLore) {
        ItemStack stack = new ItemStack(mat);
        applyMeta(stack, legacyName, legacyLore);
        return stack;
    }

    private static void applyMeta(ItemStack stack, String legacyName, List<String> legacyLore) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        meta.displayName(LEGACY.deserialize(legacyName));
        List<Component> loreComponents = new ArrayList<>();
        for (String line : legacyLore) {
            loreComponents.add(LEGACY.deserialize(line));
        }
        meta.lore(loreComponents);
        stack.setItemMeta(meta);
    }

    // ─── Обработка кликов ───────────────────────────────────────────

    /** Возвращает индекс слота (0-4) для кнопки покупки, или -1 */
    public int getBuySlotIndex(int rawSlot) {
        for (int i = 0; i < BUTTON_SLOTS.length; i++) {
            if (BUTTON_SLOTS[i] == rawSlot) return i;
        }
        return -1;
    }

    public boolean isCloseSlot(int rawSlot) {
        return rawSlot == 53;
    }

    /** Пытается совершить покупку. Возвращает true при успехе. */
    public boolean tryPurchase(Player player, int slotIndex) {
        SlotData data = activeSlots.get(slotIndex);
        if (data == null || data.isBought()) {
            player.sendMessage(LEGACY.deserialize("§c✗ Этот товар уже куплен или недоступен."));
            return false;
        }

        boolean bypass = player.hasPermission("sudoru.trader.bypass");
        if (!bypass && !plugin.takeCurrency(player, data.getPrice())) {
            player.sendMessage(LEGACY.deserialize(
                    "§c✗ Недостаточно " + plugin.getCurrencyName() + "!"));
            player.sendMessage(LEGACY.deserialize(
                    "§7Требуется: §f" + data.getPrice()
                    + " §7| У тебя: §f" + plugin.getCurrencyAmount(player)));
            return false;
        }

        ShopItem si = data.getItem();
        ItemStack reward = buildProductItem(data).clone();
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(reward);
        if (!overflow.isEmpty()) {
            for (ItemStack drop : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
            player.sendMessage(LEGACY.deserialize(
                    "§e⚠ Инвентарь переполнен. Часть товара упала на землю."));
        }

        data.setBought(true);
        player.sendMessage(LEGACY.deserialize("§a✔ Покупка успешна: §f" + colorize(si.getName())));
        return true;
    }

    // ─── Утилиты ─────────────────────────────────────────────────────

    boolean isViewingThisShop(Player p) {
        Component openTitle = p.getOpenInventory().title();
        Component shopTitle = toLegacyComponent(config.getDisplayName());
        return openTitle.equals(shopTitle);
    }

    private Component toLegacyComponent(String legacyText) {
        return LEGACY.deserialize(colorize(legacyText));
    }

    private String colorize(String s) {
        if (s == null) return "";
        return s.replace("&", "§");
    }

    private List<String> colorizeList(List<String> list) {
        if (list == null) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (String s : list) result.add(colorize(s));
        return result;
    }

    private List<String> splitDescription(String desc) {
        if (desc == null) return Collections.emptyList();
        return List.of(desc.split("\\\\n|\\n"));
    }

    private String formatTime(long seconds) {
        if (seconds < 60) return seconds + "с";
        long min = seconds / 60;
        long sec = seconds % 60;
        if (min < 60) return min + "м " + (sec > 0 ? sec + "с" : "");
        long h = min / 60; long m = min % 60;
        return h + "ч " + (m > 0 ? m + "м" : "");
    }

    private Material parseMaterial(String name) {
        if (name == null) return Material.CHEST;
        try { return Material.valueOf(name.toUpperCase()); }
        catch (IllegalArgumentException e) { return Material.CHEST; }
    }

    public TraderConfig getConfig() { return config; }

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
