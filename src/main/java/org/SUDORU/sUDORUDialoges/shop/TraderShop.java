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

    // Слоты товаров (ряд 3) и кнопок покупки (ряд 4)
    private static final int[] PRODUCT_SLOTS = {19, 21, 23, 25, 27};
    private static final int[] BUTTON_SLOTS  = {28, 30, 32, 34, 36};

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
        count = Math.min(count, PRODUCT_SLOTS.length);

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
        fillDecoration(inv);
        fillHeader(inv);
        fillItems(inv);
        fillFooter(inv);
        player.openInventory(inv);
    }

    private void fillDecoration(Inventory inv) {
        // Тёмный фон
        ItemStack darkBg  = buildItem(Material.BLACK_STAINED_GLASS_PANE, "§0 ");
        ItemStack grayBg  = buildItem(Material.GRAY_STAINED_GLASS_PANE,  "§8 ");
        for (int i = 0; i < MENU_SIZE; i++) inv.setItem(i, darkBg);
        // Строки 3-4 (18-35) — светлее
        for (int i = 18; i <= 44; i++) inv.setItem(i, grayBg);
    }

    private void fillHeader(Inventory inv) {
        // Иконка торговца в слоте 4
        Material iconMat = parseMaterial(config.getIconMaterial());
        ItemStack icon = buildItem(iconMat,
                config.getDisplayName(),
                splitDescription(config.getDescription()));
        inv.setItem(4, icon);

        // Боковые украшения в шапке
        ItemStack purpleDec = buildItem(Material.PURPLE_STAINED_GLASS_PANE, "§5 ");
        ItemStack cyanDec   = buildItem(Material.CYAN_STAINED_GLASS_PANE,   "§3 ");
        inv.setItem(0,  purpleDec); inv.setItem(8,  purpleDec);
        inv.setItem(1,  cyanDec);   inv.setItem(7,  cyanDec);
        inv.setItem(2,  purpleDec); inv.setItem(6,  purpleDec);
        inv.setItem(3,  cyanDec);   inv.setItem(5,  cyanDec);

        // Разделитель (строка 2)
        ItemStack divider = buildItem(Material.BLACK_STAINED_GLASS_PANE, "§0 ");
        for (int s = 9; s <= 17; s++) inv.setItem(s, divider);

        // Украшения между товарами
        ItemStack spacer = buildItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§7 ");
        inv.setItem(18, spacer); inv.setItem(20, spacer); inv.setItem(22, spacer);
        inv.setItem(24, spacer); inv.setItem(26, spacer);
        inv.setItem(29, spacer); inv.setItem(31, spacer); inv.setItem(33, spacer);
        inv.setItem(35, spacer); inv.setItem(37, spacer); inv.setItem(39, spacer);
        inv.setItem(41, spacer); inv.setItem(43, spacer);
    }

    private void fillItems(Inventory inv) {
        // Куплено — показываем барьер с красивым описанием
        ItemStack barrier = buildItem(Material.BARRIER, "&#FF5555✗ §cПродано",
                List.of("&#888888▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                        "&#AAAAAA Этот товар уже куплен.",
                        "&#888888▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        ItemStack darkPane = buildItem(Material.BLACK_STAINED_GLASS_PANE, "§0 ");
        ItemStack empty    = buildItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "&#888888— Пусто —");

        for (int i = 0; i < PRODUCT_SLOTS.length; i++) {
            int pSlot = PRODUCT_SLOTS[i];
            int bSlot = BUTTON_SLOTS[i];
            SlotData data = activeSlots.get(i);

            if (data == null) {
                inv.setItem(pSlot, empty);
                inv.setItem(bSlot, darkPane);
            } else if (data.isBought()) {
                inv.setItem(pSlot, barrier);
                inv.setItem(bSlot, darkPane);
            } else {
                inv.setItem(pSlot, buildProductItem(data));
                inv.setItem(bSlot, buildBuyButton(data));
            }
        }
    }

    private void fillFooter(Inventory inv) {
        // Строка 6 (45-53)
        ItemStack darkPane = buildItem(Material.BLACK_STAINED_GLASS_PANE, "§0 ");
        for (int s = 45; s < 54; s++) inv.setItem(s, darkPane);

        long sec = config.getRefreshSeconds();
        String refreshText = sec > 0 ? formatTime(sec) : "&#AAAAAA Никогда";
        ItemStack info = buildItem(Material.CLOCK,
                "&#FFFF55⏱ §eОбновление",
                List.of("&#888888▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                        "&#AAAAAA Следующее обновление: §f" + refreshText,
                        "&#AAAAAA Предметов сегодня: §f" + activeSlots.size(),
                        "&#888888▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        inv.setItem(49, info);

        ItemStack close = buildItem(Material.RED_STAINED_GLASS_PANE,
                "&#FF5555✖ §cЗакрыть",
                List.of("&#AAAAAA Закрыть меню торговца."));
        inv.setItem(53, close);

        // Украшения по краям
        ItemStack purpleDec = buildItem(Material.PURPLE_STAINED_GLASS_PANE, "§5 ");
        ItemStack cyanDec   = buildItem(Material.CYAN_STAINED_GLASS_PANE, "§3 ");
        inv.setItem(45, purpleDec); inv.setItem(46, cyanDec);
        inv.setItem(50, cyanDec);  inv.setItem(51, cyanDec); inv.setItem(52, cyanDec);
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

    private ItemStack buildBuyButton(SlotData data) {
        return buildItem(Material.LIME_STAINED_GLASS_PANE,
                "&#55FF55§l✔ §r&#55FF55КУПИТЬ &#FFDD00§l" + data.getPrice() + " §r&#AAAAAA" + plugin.getCurrencyName(),
                List.of("&#AAAAAA Нажми чтобы купить этот товар."));
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

    public int getBuySlotIndex(int rawSlot) {
        for (int i = 0; i < BUTTON_SLOTS.length; i++) {
            if (BUTTON_SLOTS[i] == rawSlot) return i;
        }
        return -1;
    }

    public boolean isCloseSlot(int rawSlot) { return rawSlot == 53; }

    public boolean tryPurchase(Player player, int slotIndex) {
        SlotData data = activeSlots.get(slotIndex);
        if (data == null || data.isBought()) {
            player.sendMessage(ColorUtil.parse("&#FF5555✗ §cЭтот товар уже куплен или недоступен."));
            return false;
        }

        boolean bypass = player.hasPermission("sudoru.trader.bypass");
        if (!bypass && !plugin.takeCurrency(player, data.getPrice())) {
            player.sendMessage(ColorUtil.parse("&#FF5555✗ §cНедостаточно §f" + plugin.getCurrencyName() + "§c!"));
            player.sendMessage(ColorUtil.parse("&#AAAAAA Требуется: &#FFD700" + data.getPrice()
                    + " &#AAAAAA| У тебя: &#FFD700" + plugin.getCurrencyAmount(player)));
            return false;
        }

        ShopItem si = data.getItem();
        ItemStack reward = buildProductItem(data).clone();
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(reward);
        if (!overflow.isEmpty()) {
            for (ItemStack drop : overflow.values())
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            player.sendMessage(ColorUtil.parse("&#FFAA00⚠ §eИнвентарь переполнен. Часть упала на землю."));
        }

        data.setBought(true);
        player.sendMessage(ColorUtil.parse("&#55FF55✔ §aПокупка успешна: §f" + si.getName()));
        return true;
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
