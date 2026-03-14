package org.SUDORU.sUDORUDialoges.listener;

import net.kyori.adventure.text.Component;
import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.SUDORU.sUDORUDialoges.menu.TraderMenuGUI;
import org.SUDORU.sUDORUDialoges.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

/**
 * Слушатель для GUI-редактора торговцев (/tradermenu).
 */
public class MenuEditorListener implements Listener {

    private final SUDORUDialoges plugin;
    private final TraderMenuGUI gui;

    public MenuEditorListener(SUDORUDialoges plugin, TraderMenuGUI gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Component title = event.getView().title();

        // ── ГЛАВНОЕ МЕНЮ ──────────────────────────────────────────────
        if (TraderMenuGUI.isManagerMenu(title)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot >= 54) return; // клик вне верхнего инвентаря

            // Закрыть
            if (slot == 53) { player.closeInventory(); return; }

            // Кнопка «создать»
            if (slot == 49) { gui.openCreateWizard(player); return; }

            // Кнопка «настройки валюты»
            if (slot == 47) { gui.openCurrencySettings(player); return; }

            // Клик по торговцу
            List<String> shopIds = new java.util.ArrayList<>(plugin.getTraderManager().getShopIds());
            int[] traderSlots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
            for (int i = 0; i < traderSlots.length && i < shopIds.size(); i++) {
                if (traderSlots[i] == slot) {
                    String id = shopIds.get(i);
                    if (event.getClick() == ClickType.SHIFT_LEFT) {
                        // Открыть магазин
                        player.closeInventory();
                        plugin.getTraderManager().getShop(id).openFor(player);
                    } else if (event.getClick() == ClickType.RIGHT) {
                        // Удалить торговца
                        deleteTrader(player, id);
                    } else {
                        // Редактировать
                        gui.openEditor(player, id);
                    }
                    return;
                }
            }
        }

        // ── МЕНЮ РЕДАКТОРА ТОРГОВЦА ───────────────────────────────────
        if (TraderMenuGUI.isEditorMenu(title)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot >= 54) return;

            TraderMenuGUI.EditorState state = gui.getState(player.getUniqueId());
            if (state == null) return;
            String traderId = state.traderId;

            if (slot == 53) { player.closeInventory(); return; }
            if (slot == 45) { gui.openMain(player); return; }

            // Обновить ассортимент
            if (slot == 15) {
                var shop = plugin.getTraderManager().getShop(traderId);
                if (shop != null) shop.rollAssortment();
                player.sendMessage(ColorUtil.parse("&#55FF55✔ §aАссортимент §f" + traderId + " §aобновлён!"));
                gui.openEditor(player, traderId);
                return;
            }

            // Редактирование полей — запрашиваем через чат
            String field = switch (slot) {
                case 10 -> "display-name";
                case 11 -> "icon-material";
                case 12 -> "refresh-seconds";
                case 14 -> "description";
                default -> null;
            };

            // Мин/макс предметов кнопками
            if (slot == 13) {
                var cfg = plugin.getTraderManager().getShop(traderId).getConfig();
                int min = cfg.getMinItems(), max = cfg.getMaxItems();
                if (event.getClick() == ClickType.LEFT)           min = Math.max(1, min - 1);
                else if (event.getClick() == ClickType.RIGHT)     min = Math.min(max, min + 1);
                else if (event.getClick() == ClickType.SHIFT_LEFT) max = Math.max(min, max - 1);
                else if (event.getClick() == ClickType.SHIFT_RIGHT) max = Math.min(8, max + 1);
                saveTraderValue(traderId, "min-items", String.valueOf(min));
                saveTraderValue(traderId, "max-items", String.valueOf(max));
                reloadAndReopen(player, traderId);
                return;
            }

            if (field != null) {
                state.pendingField = field;
                gui.setState(player.getUniqueId(), state);
                player.closeInventory();
                player.sendMessage(ColorUtil.parse(
                        "&#FFAA00➤ §6Введи новое значение для §f" + field + " §6в чат."));
                player.sendMessage(ColorUtil.parse("§7Введи §cотмена §7для отмены."));
            }
        }

        // ── МЕНЮ НАСТРОЕК ВАЛЮТЫ ──────────────────────────────────────
        if (TraderMenuGUI.isCurrencyMenu(title)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot >= 54) return;

            if (slot == 53) { player.closeInventory(); return; }
            if (slot == 45) { gui.openMain(player); return; }

            TraderMenuGUI.EditorState state = gui.getState(player.getUniqueId());
            if (state == null) return;

            String field = switch (slot) {
                case 20 -> "currency.item-name";
                case 24 -> "currency.item-material";
                default -> null;
            };

            if (field != null) {
                state.pendingField = field;
                gui.setState(player.getUniqueId(), state);
                player.closeInventory();
                player.sendMessage(ColorUtil.parse(
                        "&#FFAA00➤ §6Введи новое значение для §f" + field + " §6в чат."));
                player.sendMessage(ColorUtil.parse("§7Введи §cотмена §7для отмены."));
            }
        }

        // ── МЕНЮ СОЗДАНИЯ ─────────────────────────────────────────────
        if (TraderMenuGUI.isCreateMenu(title)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot >= 54) return;
            if (slot == 53) { player.closeInventory(); return; }
            if (slot == 45) { gui.openMain(player); return; }

            if (slot == 13) {
                TraderMenuGUI.EditorState state = new TraderMenuGUI.EditorState(null, TraderMenuGUI.EditorMode.CREATE);
                state.pendingField = "new-id";
                gui.setState(player.getUniqueId(), state);
                player.closeInventory();
                player.sendMessage(ColorUtil.parse(
                        "&#55FF55➤ §aВведи §fID §aнового торговца в чат (только a-z, 0-9, _):"));
                player.sendMessage(ColorUtil.parse("§7Введи §cотмена §7для отмены."));
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        TraderMenuGUI.EditorState state = gui.getState(player.getUniqueId());
        if (state == null || state.pendingField == null) return;

        event.setCancelled(true);
        String input = event.getMessage().trim();

        if (input.equalsIgnoreCase("отмена") || input.equalsIgnoreCase("cancel")) {
            gui.removeState(player.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> gui.openMain(player));
            player.sendMessage(ColorUtil.parse("§7Отменено."));
            return;
        }

        String field = state.pendingField;

        // Создание нового торговца
        if ("new-id".equals(field)) {
            if (!input.matches("[a-z0-9_]+")) {
                player.sendMessage(ColorUtil.parse("§c✗ Недопустимый ID! Используй только a-z, 0-9, _"));
                return;
            }
            if (plugin.getTraderManager().getShop(input) != null) {
                player.sendMessage(ColorUtil.parse("§c✗ Торговец с ID §f'" + input + "' §cуже существует!"));
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> createNewTrader(player, input));
            return;
        }

        // Настройки валюты
        if (field.startsWith("currency.")) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getConfig().set(field, input);
                plugin.saveConfig();
                gui.removeState(player.getUniqueId());
                player.sendMessage(ColorUtil.parse("&#55FF55✔ §aСохранено: §f" + field + " §7= §f" + input));
                gui.openCurrencySettings(player);
            });
            return;
        }

        // Редактирование поля торговца
        String traderId = state.traderId;
        if (traderId == null) return;

        final String finalInput = input;
        Bukkit.getScheduler().runTask(plugin, () -> {
            saveTraderValue(traderId, field, finalInput);
            reloadAndReopen(player, traderId);
            player.sendMessage(ColorUtil.parse("&#55FF55✔ §aСохранено: §f" + field + " §7= §f" + finalInput));
        });
    }

    // ─── Утилиты ──────────────────────────────────────────────────────

    private void saveTraderValue(String traderId, String field, String value) {
        FileConfiguration cfg = plugin.getConfig();
        String path = "traders." + traderId + "." + field;
        // Числовые поля
        if (field.equals("refresh-seconds") || field.equals("min-items") || field.equals("max-items")) {
            try { cfg.set(path, Integer.parseInt(value)); }
            catch (NumberFormatException e) { return; }
        } else {
            cfg.set(path, value);
        }
        plugin.saveConfig();
    }

    private void reloadAndReopen(Player player, String traderId) {
        plugin.reloadConfig();
        plugin.getTraderManager().loadAll();
        gui.removeState(player.getUniqueId());
        gui.openEditor(player, traderId);
    }

    private void createNewTrader(Player player, String id) {
        FileConfiguration cfg = plugin.getConfig();
        String base = "traders." + id;
        cfg.set(base + ".display-name", "&7" + id);
        cfg.set(base + ".description", "&7Новый торговец");
        cfg.set(base + ".icon-material", "CHEST");
        cfg.set(base + ".refresh-seconds", 300);
        cfg.set(base + ".min-items", 5);
        cfg.set(base + ".max-items", 8);
        // Добавляем дефолтный предмет чтобы торговец не был пустым
        java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
        java.util.Map<String, Object> defItem = new java.util.LinkedHashMap<>();
        defItem.put("material", "STONE");
        defItem.put("name", "&7Камень");
        defItem.put("lore", java.util.List.of("&7Пример предмета."));
        defItem.put("price", 1);
        defItem.put("price-range", 0);
        defItem.put("chance", 100.0);
        defItem.put("amount", 1);
        defItem.put("enchantments", java.util.List.of());
        items.add(defItem);
        cfg.set(base + ".items", items);
        plugin.saveConfig();
        plugin.reloadConfig();
        plugin.getTraderManager().loadAll();

        gui.removeState(player.getUniqueId());
        player.sendMessage(ColorUtil.parse("&#55FF55✔ §aТорговец §f'" + id + "' §aсоздан!"));
        gui.openEditor(player, id);
    }

    private void deleteTrader(Player player, String traderId) {
        plugin.getConfig().set("traders." + traderId, null);
        plugin.saveConfig();
        plugin.reloadConfig();
        plugin.getTraderManager().loadAll();
        player.sendMessage(ColorUtil.parse("§c✖ Торговец §f'" + traderId + "' §cудалён."));
        gui.openMain(player);
    }
}

