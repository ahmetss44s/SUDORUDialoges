package org.SUDORU.sUDORUDialoges.listener;

import net.kyori.adventure.text.Component;
import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.SUDORU.sUDORUDialoges.menu.ConfigMenuGUI;
import org.SUDORU.sUDORUDialoges.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Слушатель для /traderconfig — полное GUI всех настроек config.yml.
 */
public class ConfigMenuListener implements Listener {

    private final SUDORUDialoges plugin;
    private final ConfigMenuGUI gui;

    public ConfigMenuListener(SUDORUDialoges plugin, ConfigMenuGUI gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    // ── Блокировка drag ──────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Component t = event.getView().title();
        if (ConfigMenuGUI.isMain(t) || ConfigMenuGUI.isCurrency(t)
                || ConfigMenuGUI.isTrader(t) || ConfigMenuGUI.isItems(t)
                || ConfigMenuGUI.isItemEdit(t)) {
            event.setCancelled(true);
        }
    }

    // ── Клики ────────────────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Component title = event.getView().title();
        int slot = event.getRawSlot();
        int topSize = event.getView().getTopInventory().getSize();

        // ────────── ГЛАВНАЯ СТРАНИЦА ──────────────────────────────────
        if (ConfigMenuGUI.isMain(title)) {
            event.setCancelled(true);
            if (slot >= topSize) return;

            if (slot == 53) { player.closeInventory(); return; }

            // Кнопка валюты
            if (slot == 19) { gui.openCurrency(player); return; }

            // Кнопка перезагрузки
            if (slot == 48) {
                plugin.reloadConfig();
                plugin.getTraderManager().loadAll();
                player.sendMessage(ColorUtil.parse("&#55FF55✔ §aКонфиг перезагружен!"));
                gui.openMain(player);
                return;
            }

            // Кнопка «добавить торговца»
            if (slot == 46) {
                requestInput(player, "new-trader-id", null, null, -1,
                        "&#55FF55➤ §aВведи §fID §aнового торговца §7(a-z, 0-9, _):");
                return;
            }

            // Клик по торговцу
            List<String> ids = new java.util.ArrayList<>(plugin.getTraderManager().getShopIds());
            int[] tSlots = {21,22,23,24,25,28,29,30,31,32,33,34};
            for (int i = 0; i < tSlots.length && i < ids.size(); i++) {
                if (tSlots[i] == slot) { gui.openTrader(player, ids.get(i)); return; }
            }
        }

        // ────────── СТРАНИЦА ВАЛЮТЫ ────────────────────────────────────
        if (ConfigMenuGUI.isCurrency(title)) {
            event.setCancelled(true);
            if (slot >= topSize) return;

            if (slot == 53) { player.closeInventory(); return; }
            if (slot == 45) { gui.openMain(player); return; }

            // currency.type — переключатель ЛКМ/ПКМ
            if (slot == 19) {
                String newVal = event.getClick() == ClickType.RIGHT ? "VAULT" : "ITEM";
                saveAndReload("currency.type", newVal);
                player.sendMessage(ColorUtil.parse("&#55FF55✔ §acurrency.type §7→ §f" + newVal));
                gui.openCurrency(player);
                return;
            }

            // currency.item-name
            if (slot == 21) {
                requestInput(player, "currency.item-name", null, null, -1,
                        "&#FFAA00➤ §eВведи новое §fназвание валюты §e(поддерживает &-коды):");
                return;
            }

            // currency.item-material
            if (slot == 23) {
                requestInput(player, "currency.item-material", null, null, -1,
                        "&#FFAA00➤ §eВведи §fматериал валюты §e(пример: EMERALD, GOLD_INGOT):");
                return;
            }
        }

        // ────────── СТРАНИЦА ТОРГОВЦА ──────────────────────────────────
        if (ConfigMenuGUI.isTrader(title)) {
            event.setCancelled(true);
            if (slot >= topSize) return;

            String traderId = ConfigMenuGUI.traderIdFromTitle(title, ConfigMenuGUI.T_TRADER);
            if (traderId == null) return;

            if (slot == 53) { player.closeInventory(); return; }
            if (slot == 45) { gui.openMain(player); return; }

            // display-name
            if (slot == 19) {
                requestInput(player, "display-name", traderId, "trader-field", -1,
                        "&#FFAA00➤ §eВведи новое §fимя торговца §e(поддерживает &-коды и &#HEX):");
                return;
            }
            // description
            if (slot == 20) {
                requestInput(player, "description", traderId, "trader-field", -1,
                        "&#FFAA00➤ §eВведи §fописание §e(\\n для новой строки):");
                return;
            }
            // icon-material
            if (slot == 21) {
                requestInput(player, "icon-material", traderId, "trader-field", -1,
                        "&#FFAA00➤ §eВведи §fматериал иконки §e(пример: ANVIL, CHEST):");
                return;
            }
            // refresh-seconds — кнопки ±
            if (slot == 22) {
                long cur = plugin.getConfig().getLong("traders." + traderId + ".refresh-seconds", 300);
                if (event.getClick() == ClickType.LEFT)         cur = Math.max(0, cur - 60);
                else if (event.getClick() == ClickType.RIGHT)   cur += 60;
                else if (event.getClick() == ClickType.SHIFT_LEFT)  cur = Math.max(0, cur - 300);
                else if (event.getClick() == ClickType.SHIFT_RIGHT) cur += 300;
                else if (event.getClick() == ClickType.MIDDLE) {
                    requestInput(player, "refresh-seconds", traderId, "trader-field", -1,
                            "&#FFAA00➤ §eВведи §fсекунды §e(0 = выкл.):");
                    return;
                }
                saveTraderField(traderId, "refresh-seconds", String.valueOf(cur));
                reloadAndReopenTrader(player, traderId);
                return;
            }
            // min-items
            if (slot == 23) {
                int cur = plugin.getConfig().getInt("traders." + traderId + ".min-items", 5);
                int max = plugin.getConfig().getInt("traders." + traderId + ".max-items", 8);
                if (event.getClick() == ClickType.LEFT)  cur = Math.max(1, cur - 1);
                else                                     cur = Math.min(max, cur + 1);
                saveTraderField(traderId, "min-items", String.valueOf(cur));
                reloadAndReopenTrader(player, traderId);
                return;
            }
            // max-items
            if (slot == 24) {
                int cur = plugin.getConfig().getInt("traders." + traderId + ".max-items", 8);
                int min = plugin.getConfig().getInt("traders." + traderId + ".min-items", 5);
                if (event.getClick() == ClickType.LEFT)  cur = Math.max(min, cur - 1);
                else                                     cur = Math.min(8, cur + 1);
                saveTraderField(traderId, "max-items", String.valueOf(cur));
                reloadAndReopenTrader(player, traderId);
                return;
            }
            // Предметы
            if (slot == 28) { gui.openItems(player, traderId, 0); return; }
            // Обновить ассортимент
            if (slot == 30) {
                var shop = plugin.getTraderManager().getShop(traderId);
                if (shop != null) shop.rollAssortment();
                player.sendMessage(ColorUtil.parse("&#55FF55✔ §aАссортимент обновлён!"));
                gui.openTrader(player, traderId);
                return;
            }
            // Удалить — только Shift+ЛКМ
            if (slot == 32 && event.getClick() == ClickType.SHIFT_LEFT) {
                plugin.getConfig().set("traders." + traderId, null);
                plugin.saveConfig();
                plugin.reloadConfig();
                plugin.getTraderManager().loadAll();
                player.sendMessage(ColorUtil.parse("§c✖ Торговец §f'" + traderId + "' §cудалён."));
                gui.openMain(player);
                return;
            }
        }

        // ────────── СТРАНИЦА ПРЕДМЕТОВ ─────────────────────────────────
        if (ConfigMenuGUI.isItems(title)) {
            event.setCancelled(true);
            if (slot >= topSize) return;

            String traderId = ConfigMenuGUI.traderIdFromTitle(title, ConfigMenuGUI.T_ITEMS);
            if (traderId == null) return;

            ConfigMenuGUI.ConfigState state = gui.getState(player.getUniqueId());
            int page = (state != null) ? state.index : 0;
            var shop = plugin.getTraderManager().getShop(traderId);
            if (shop == null) return;
            var items = shop.getConfig().getItems();

            int[] itemSlots = {19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
            int startIdx = page * itemSlots.length;

            // Клик по предмету
            for (int i = 0; i < itemSlots.length; i++) {
                if (itemSlots[i] == slot) {
                    int idx = startIdx + i;
                    if (idx >= items.size()) break;
                    if (event.getClick() == ClickType.RIGHT) {
                        // Удалить предмет
                        deleteItem(player, traderId, idx, page);
                    } else {
                        // Редактировать
                        gui.openItemEdit(player, traderId, idx);
                    }
                    return;
                }
            }

            // Добавить предмет
            if (slot == 49) {
                requestInput(player, "new-item-material", traderId, "item-new", -1,
                        "&#55FF55➤ §aВведи §fматериал §aнового предмета §7(пример: DIAMOND_SWORD):");
                return;
            }

            // Навигация
            if (slot == 45) {
                if (page > 0) gui.openItems(player, traderId, page - 1);
                else gui.openTrader(player, traderId);
                return;
            }
            if (slot == 53) {
                if (startIdx + itemSlots.length < items.size()) gui.openItems(player, traderId, page + 1);
                else player.closeInventory();
                return;
            }
        }

        // ────────── СТРАНИЦА РЕДАКТИРОВАНИЯ ПРЕДМЕТА ──────────────────
        if (ConfigMenuGUI.isItemEdit(title)) {
            event.setCancelled(true);
            if (slot >= topSize) return;

            ConfigMenuGUI.ConfigState state = gui.getState(player.getUniqueId());
            if (state == null) return;
            String traderId = state.traderId;
            int itemIdx = state.index;

            var shop = plugin.getTraderManager().getShop(traderId);
            if (shop == null) return;
            var si = shop.getConfig().getItems().get(itemIdx);

            if (slot == 45) { gui.openItems(player, traderId, 0); return; }
            if (slot == 53) { player.closeInventory(); return; }

            // material
            if (slot == 19) {
                requestInput(player, "item.material", traderId, "item-field", itemIdx,
                        "&#FFAA00➤ §eМатериал §7(пример: DIAMOND_SWORD):");
                return;
            }
            // name
            if (slot == 20) {
                requestInput(player, "item.name", traderId, "item-field", itemIdx,
                        "&#FFAA00➤ §eИмя предмета §7(поддерживает &-коды):");
                return;
            }
            // price ±
            if (slot == 21) {
                int cur = si.getBasePrice();
                if (event.getClick() == ClickType.LEFT)       cur = Math.max(0, cur - 1);
                else if (event.getClick() == ClickType.RIGHT) cur++;
                else if (event.getClick() == ClickType.SHIFT_LEFT)  cur = Math.max(0, cur - 10);
                else if (event.getClick() == ClickType.SHIFT_RIGHT) cur += 10;
                saveItemField(traderId, itemIdx, "price", String.valueOf(cur));
                reloadAndReopenItem(player, traderId, itemIdx);
                return;
            }
            // price-range ±
            if (slot == 22) {
                int cur = si.getPriceRange();
                if (event.getClick() == ClickType.LEFT)       cur = Math.max(0, cur - 1);
                else if (event.getClick() == ClickType.RIGHT) cur++;
                saveItemField(traderId, itemIdx, "price-range", String.valueOf(cur));
                reloadAndReopenItem(player, traderId, itemIdx);
                return;
            }
            // chance ±
            if (slot == 23) {
                double cur = si.getChance();
                if (event.getClick() == ClickType.LEFT)       cur = Math.max(0.1, cur - 5);
                else if (event.getClick() == ClickType.RIGHT) cur += 5;
                else if (event.getClick() == ClickType.SHIFT_LEFT)  cur = Math.max(0.1, cur - 1);
                else if (event.getClick() == ClickType.SHIFT_RIGHT) cur += 1;
                // Округляем до 1 знака
                cur = Math.round(cur * 10.0) / 10.0;
                saveItemField(traderId, itemIdx, "chance", String.valueOf(cur));
                reloadAndReopenItem(player, traderId, itemIdx);
                return;
            }
            // amount ±
            if (slot == 24) {
                int cur = si.getAmount();
                if (event.getClick() == ClickType.LEFT)       cur = Math.max(1, cur - 1);
                else if (event.getClick() == ClickType.RIGHT) cur = Math.min(64, cur + 1);
                saveItemField(traderId, itemIdx, "amount", String.valueOf(cur));
                reloadAndReopenItem(player, traderId, itemIdx);
                return;
            }
            // lore
            if (slot == 28) {
                requestInput(player, "item.lore", traderId, "item-field", itemIdx,
                        "&#FFAA00➤ §eВведи лор через §f| §e(пример: §7строка 1§e|§7строка 2§e):");
                return;
            }
            // potion-type
            if (slot == 30) {
                requestInput(player, "item.potion-type", traderId, "item-field", itemIdx,
                        "&#FFAA00➤ §eТип зелья §7(пример: STRONG_HEALING, INVISIBILITY):");
            }
        }
    }

    // ─── Обработка ввода в чат ────────────────────────────────────────
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ConfigMenuGUI.ConfigState state = gui.getState(player.getUniqueId());
        if (state == null || state.pendingField == null) return;

        event.setCancelled(true);
        String input = event.getMessage().trim();

        if (input.equalsIgnoreCase("отмена") || input.equalsIgnoreCase("cancel")) {
            Bukkit.getScheduler().runTask(plugin, () -> reopenCurrentPage(player, state));
            player.sendMessage(ColorUtil.parse("§7Отменено."));
            state.pendingField = null;
            return;
        }

        final String field = state.pendingField;
        final String traderId = state.traderId;
        final int idx = state.index;

        Bukkit.getScheduler().runTask(plugin, () -> {
            // ── Новый торговец ──
            if ("new-trader-id".equals(field)) {
                if (!input.matches("[a-z0-9_]+")) {
                    player.sendMessage(ColorUtil.parse("§c✗ Только строчные буквы, цифры и _"));
                    gui.openMain(player);
                    return;
                }
                if (plugin.getTraderManager().getShop(input) != null) {
                    player.sendMessage(ColorUtil.parse("§c✗ Торговец §f'" + input + "' §cуже существует!"));
                    gui.openMain(player);
                    return;
                }
                createTrader(player, input);
                return;
            }

            // ── Диспетчеризация по типу ──
            String pendingType = (state.pendingType == null) ? "" : state.pendingType;
            switch (pendingType) {
                case "" -> {
                    // Прямой путь к конфигу (currency.*)
                    saveAndReload(field, input);
                    player.sendMessage(ColorUtil.parse("&#55FF55✔ §a" + field + " §7→ §f" + input));
                    gui.openCurrency(player);
                }
                case "trader-field" -> {
                    saveTraderField(traderId, field, input);
                    reloadAndReopenTrader(player, traderId);
                    player.sendMessage(ColorUtil.parse("&#55FF55✔ §a" + field + " §7→ §f" + input));
                }
                case "item-new" -> {
                    try { Material.valueOf(input.toUpperCase()); }
                    catch (IllegalArgumentException e) {
                        player.sendMessage(ColorUtil.parse("§c✗ Неизвестный материал: §f" + input));
                        gui.openItems(player, traderId, 0);
                        return;
                    }
                    addItem(player, traderId, input.toUpperCase());
                }
                case "item-field" -> {
                    if ("item.lore".equals(field)) {
                        List<String> loreLines = List.of(input.split("\\|"));
                        saveItemLore(traderId, idx, loreLines);
                    } else {
                        saveItemField(traderId, idx, field.replace("item.", ""), input);
                    }
                    reloadAndReopenItem(player, traderId, idx);
                    player.sendMessage(ColorUtil.parse("&#55FF55✔ §a" + field + " §7→ §f" + input));
                }
            }
        });
    }

    // ─── Утилиты сохранения ───────────────────────────────────────────

    private void saveAndReload(String path, String value) {
        plugin.getConfig().set(path, value);
        plugin.saveConfig();
        plugin.reloadConfig();
        plugin.getTraderManager().loadAll();
    }

    private void saveTraderField(String traderId, String field, String value) {
        FileConfiguration cfg = plugin.getConfig();
        String path = "traders." + traderId + "." + field;
        if (List.of("refresh-seconds","min-items","max-items").contains(field)) {
            try { cfg.set(path, Long.parseLong(value)); }
            catch (NumberFormatException ignored) { return; }
        } else {
            cfg.set(path, value);
        }
        plugin.saveConfig();
    }

    private void saveItemField(String traderId, int itemIdx, String field, String value) {
        FileConfiguration cfg = plugin.getConfig();
        List<Map<?, ?>> rawList = cfg.getMapList("traders." + traderId + ".items");
        if (itemIdx >= rawList.size()) return;

        // Копируем в изменяемую карту
        Map<String, Object> entry = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : rawList.get(itemIdx).entrySet())
            entry.put(e.getKey().toString(), e.getValue());

        switch (field) {
            case "price", "price-range", "amount" ->
                    { try { entry.put(field, Integer.parseInt(value)); } catch (NumberFormatException ignored) {} }
            case "chance" ->
                    { try { entry.put(field, Double.parseDouble(value)); } catch (NumberFormatException ignored) {} }
            case "material" -> entry.put("material", value.toUpperCase());
            default -> entry.put(field, value);
        }

        java.util.List<Map<?, ?>> mutableList = new java.util.ArrayList<>(rawList);
        mutableList.set(itemIdx, entry);
        cfg.set("traders." + traderId + ".items", mutableList);
        plugin.saveConfig();
    }

    private void saveItemLore(String traderId, int itemIdx, List<String> lore) {
        FileConfiguration cfg = plugin.getConfig();
        List<Map<?, ?>> rawList = cfg.getMapList("traders." + traderId + ".items");
        if (itemIdx >= rawList.size()) return;

        Map<String, Object> entry = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : rawList.get(itemIdx).entrySet())
            entry.put(e.getKey().toString(), e.getValue());
        entry.put("lore", lore);

        java.util.List<Map<?, ?>> mutable = new java.util.ArrayList<>(rawList);
        mutable.set(itemIdx, entry);
        cfg.set("traders." + traderId + ".items", mutable);
        plugin.saveConfig();
    }

    private void deleteItem(Player player, String traderId, int itemIdx, int page) {
        FileConfiguration cfg = plugin.getConfig();
        java.util.List<Map<?, ?>> rawList = new java.util.ArrayList<>(
                cfg.getMapList("traders." + traderId + ".items"));
        if (itemIdx < rawList.size()) rawList.remove(itemIdx);
        cfg.set("traders." + traderId + ".items", rawList);
        plugin.saveConfig();
        plugin.reloadConfig();
        plugin.getTraderManager().loadAll();
        player.sendMessage(ColorUtil.parse("§c✖ Предмет §f#" + itemIdx + " §cудалён."));
        gui.openItems(player, traderId, page);
    }

    private void addItem(Player player, String traderId, String material) {
        FileConfiguration cfg = plugin.getConfig();
        java.util.List<Map<?, ?>> rawList = new java.util.ArrayList<>(
                cfg.getMapList("traders." + traderId + ".items"));
        Map<String, Object> newItem = new LinkedHashMap<>();
        newItem.put("material", material);
        newItem.put("name", "&f" + material);
        newItem.put("lore", List.of("&7Новый предмет."));
        newItem.put("price", 5);
        newItem.put("price-range", 0);
        newItem.put("chance", 50.0);
        newItem.put("amount", 1);
        newItem.put("enchantments", List.of());
        rawList.add(newItem);
        cfg.set("traders." + traderId + ".items", rawList);
        plugin.saveConfig();
        plugin.reloadConfig();
        plugin.getTraderManager().loadAll();
        int newIdx = rawList.size() - 1;
        player.sendMessage(ColorUtil.parse("&#55FF55✔ §aДобавлен предмет §f" + material + " §7(#" + newIdx + ")"));
        gui.openItemEdit(player, traderId, newIdx);
    }

    private void createTrader(Player player, String id) {
        FileConfiguration cfg = plugin.getConfig();
        String base = "traders." + id;
        cfg.set(base + ".display-name", "&7" + id);
        cfg.set(base + ".description", "&7Новый торговец");
        cfg.set(base + ".icon-material", "CHEST");
        cfg.set(base + ".refresh-seconds", 300);
        cfg.set(base + ".min-items", 5);
        cfg.set(base + ".max-items", 8);
        Map<String, Object> defItem = new LinkedHashMap<>();
        defItem.put("material", "STONE");
        defItem.put("name", "&7Камень");
        defItem.put("lore", List.of("&7Пример предмета."));
        defItem.put("price", 1);
        defItem.put("price-range", 0);
        defItem.put("chance", 100.0);
        defItem.put("amount", 1);
        defItem.put("enchantments", List.of());
        cfg.set(base + ".items", List.of(defItem));
        plugin.saveConfig();
        plugin.reloadConfig();
        plugin.getTraderManager().loadAll();
        player.sendMessage(ColorUtil.parse("&#55FF55✔ §aТорговец §f'" + id + "' §aсоздан!"));
        gui.openTrader(player, id);
    }

    // ─── Переоткрытие текущей страницы ───────────────────────────────

    private void reloadAndReopenTrader(Player player, String traderId) {
        plugin.reloadConfig();
        plugin.getTraderManager().loadAll();
        gui.openTrader(player, traderId);
    }

    private void reloadAndReopenItem(Player player, String traderId, int idx) {
        plugin.reloadConfig();
        plugin.getTraderManager().loadAll();
        gui.openItemEdit(player, traderId, idx);
    }

    private void reopenCurrentPage(Player player, ConfigMenuGUI.ConfigState state) {
        switch (state.page) {
            case MAIN      -> gui.openMain(player);
            case CURRENCY  -> gui.openCurrency(player);
            case TRADER    -> { if (state.traderId != null) gui.openTrader(player, state.traderId); }
            case ITEMS     -> { if (state.traderId != null) gui.openItems(player, state.traderId, 0); }
            case ITEM_EDIT -> { if (state.traderId != null) gui.openItemEdit(player, state.traderId, state.index); }
        }
    }

    // ─── requestInput — запрашивает ввод в чат ─────────────────────────
    private void requestInput(Player player, String field, String traderId,
                              String type, int index, String prompt) {
        ConfigMenuGUI.ConfigState state = new ConfigMenuGUI.ConfigState(
                gui.getState(player.getUniqueId()) != null
                        ? gui.getState(player.getUniqueId()).page
                        : ConfigMenuGUI.ConfigPage.MAIN,
                traderId, index);
        state.pendingField = field;
        state.pendingType  = type;
        gui.putState(player.getUniqueId(), state);
        player.closeInventory();
        player.sendMessage(ColorUtil.parse(prompt));
        player.sendMessage(ColorUtil.parse("&#888888 Введи §cотмена §8для отмены."));
    }
}

