package org.SUDORU.sUDORUDialoges.shop;

import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Читает config.yml и создаёт/обновляет все TraderShop-инстанции.
 */
public class TraderManager {

    private final SUDORUDialoges plugin;
    private final Map<String, TraderShop> shops = new LinkedHashMap<>();

    public TraderManager(SUDORUDialoges plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        // Остановить старые таймеры
        for (TraderShop shop : shops.values()) shop.cancelRefresh();
        shops.clear();

        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection traders = cfg.getConfigurationSection("traders");
        if (traders == null) {
            plugin.getLogger().warning("Секция 'traders' не найдена в config.yml!");
            return;
        }

        for (String id : traders.getKeys(false)) {
            ConfigurationSection sec = traders.getConfigurationSection(id);
            if (sec == null) continue;

            String displayName  = sec.getString("display-name", "&7Торговец");
            String description  = sec.getString("description", "");
            String iconMaterial = sec.getString("icon-material", "CHEST");
            long refreshSeconds = sec.getLong("refresh-seconds", 300);
            int minItems        = sec.getInt("min-items", 5);
            int maxItems        = sec.getInt("max-items", 8);

            List<ShopItem> items = loadItems(sec);
            if (items.isEmpty()) {
                plugin.getLogger().warning("Торговец '" + id + "' не имеет предметов — пропускаем.");
                continue;
            }

            TraderConfig traderCfg = new TraderConfig(id, displayName, description,
                    iconMaterial, refreshSeconds, minItems, maxItems, items);
            shops.put(id.toLowerCase(), new TraderShop(plugin, traderCfg));
            plugin.getLogger().info("Загружен торговец: " + id + " (" + items.size() + " предметов в пуле)");
        }
    }

    private List<ShopItem> loadItems(ConfigurationSection traderSec) {
        List<Map<?, ?>> rawList = traderSec.getMapList("items");
        List<ShopItem> result = new ArrayList<>();

        for (Map<?, ?> raw : rawList) {
            try {
                String matName = getString(raw, "material", "STONE");
                Material mat;
                try { mat = Material.valueOf(matName.toUpperCase()); }
                catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Неизвестный материал: " + matName + " — пропускаем предмет.");
                    continue;
                }

                String name      = getString(raw, "name", matName);
                List<String> lore = getStringList(raw, "lore");
                int price        = getInt(raw, "price", 5);
                int priceRange   = getInt(raw, "price-range", 0);
                double chance    = getDouble(raw, "chance", 50.0);
                int amount       = Math.max(1, getInt(raw, "amount", 1));
                String potionType = getString(raw, "potion-type", null);
                List<String> enchants = getStringList(raw, "enchantments");

                result.add(new ShopItem(mat, name, lore, price, priceRange, chance, amount, potionType, enchants));
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка при загрузке предмета: " + e.getMessage());
            }
        }
        return result;
    }

    // ─── Утилиты чтения Map ──────────────────────────────────────────

    private String getString(Map<?, ?> map, String key, String def) {
        Object v = map.get(key);
        return v != null ? v.toString() : def;
    }

    private int getInt(Map<?, ?> map, String key, int def) {
        Object v = map.get(key);
        if (v instanceof Number n) return n.intValue();
        if (v != null) try { return Integer.parseInt(v.toString()); } catch (NumberFormatException ignored) {}
        return def;
    }

    private double getDouble(Map<?, ?> map, String key, double def) {
        Object v = map.get(key);
        if (v instanceof Number n) return n.doubleValue();
        if (v != null) try { return Double.parseDouble(v.toString()); } catch (NumberFormatException ignored) {}
        return def;
    }

    private List<String> getStringList(Map<?, ?> map, String key) {
        Object v = map.get(key);
        if (v instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object o : list) if (o != null) result.add(o.toString());
            return result;
        }
        return Collections.emptyList();
    }

    // ─── Публичный API ───────────────────────────────────────────────

    public TraderShop getShop(String id) {
        return shops.get(id.toLowerCase());
    }

    public Set<String> getShopIds() {
        return shops.keySet();
    }

    public void shutdown() {
        for (TraderShop shop : shops.values()) shop.cancelRefresh();
        shops.clear();
    }
}

