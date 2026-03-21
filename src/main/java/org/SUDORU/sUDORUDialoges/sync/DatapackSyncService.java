package org.SUDORU.sUDORUDialoges.sync;
import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.SUDORU.sUDORUDialoges.shop.TraderShop;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.LinkedHashMap;
import java.util.Map;
/**
 * Синхронизация Plugin -> Datapack.
 * Пушит activeSlots в storage api-shop:config shop_data.<ShopID>.
 * PDC-ключ цены в датапаке: "sudorudialogs:shop_price"
 */
public class DatapackSyncService {
    private final SUDORUDialoges plugin;
    private final Map<String, Integer> shopIdMap = new LinkedHashMap<>();
    private final Map<String, Long> lastSyncTime = new LinkedHashMap<>();
    public DatapackSyncService(SUDORUDialoges plugin) {
        this.plugin = plugin;
        rebuildIdMap();
    }
    public void rebuildIdMap() {
        shopIdMap.clear();
        int idx = 1;
        for (String id : plugin.getTraderManager().getShopIds())
            shopIdMap.put(id.toLowerCase(), idx++);
    }
    public int getShopId(String traderId) {
        return shopIdMap.getOrDefault(traderId.toLowerCase(), 0);
    }
    public Long getLastSyncTime(String traderId) {
        return lastSyncTime.get(traderId.toLowerCase());
    }
    public void syncAll() {
        rebuildIdMap();
        for (String id : plugin.getTraderManager().getShopIds()) {
            TraderShop shop = plugin.getTraderManager().getShop(id);
            if (shop != null) syncShop(id, shop.getActiveSlots());
        }
        plugin.getLogger().info("[DatapackSync] Все торговцы синкнуты -> storage api-shop:config shop_data");
    }
    public void syncShop(String traderId, Map<Integer, TraderShop.SlotData> activeSlots) {
        int shopId = getShopId(traderId);
        if (shopId <= 0) return;
        String label = plugin.getConfig().getString("traders." + traderId + ".dialog-label",
                plugin.getConfig().getString("datapack.dialog-label", "shop"));
        StringBuilder snbt = new StringBuilder("{");
        for (Map.Entry<Integer, TraderShop.SlotData> e : activeSlots.entrySet()) {
            TraderShop.SlotData d = e.getValue();
            String name = strip(d.getItem().getName()).replace("\"", "'");
            snbt.append(String.format("%02d", e.getKey()))
                .append(":{Name:\"").append(name)
                .append("\",Price:").append(d.getPrice())
                .append(",Count:").append(d.getItem().getAmount()).append("},");
        }
        snbt.append("DialogLabel:\"").append(label).append("\"}");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "data modify storage api-shop:config shop_data." + shopId + " set value " + snbt);
        lastSyncTime.put(traderId.toLowerCase(), System.currentTimeMillis());
        plugin.getLogger().info("[DatapackSync] '" + traderId + "' -> ShopID " + shopId
                + " (" + activeSlots.size() + " слотов)");
    }
    public void showDialog(Player player, TraderShop shop) {
        int shopId = getShopId(shop.getConfig().getId());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "advancement revoke " + player.getName() + " only api-shop:plr-interacted/shop");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "scoreboard players enable " + player.getName() + " ShopBuyTrigger");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "execute as " + player.getName() + " run dialog show @s " + buildJson(shop, shopId));
    }
    private String buildJson(TraderShop shop, int shopId) {
        String shopName = strip(shop.getConfig().getDisplayName());
        String cur = plugin.getCurrencyName();
        StringBuilder body = new StringBuilder("[");
        boolean first = true;
        for (Map.Entry<Integer, TraderShop.SlotData> e : shop.getActiveSlots().entrySet()) {
            if (!first) body.append(",");
            first = false;
            TraderShop.SlotData d = e.getValue();
            String mat = "minecraft:" + d.getItem().getMaterial().name().toLowerCase();
            String name = strip(d.getItem().getName()).replace("\"", "\\\"");
            int trigger = shopId * 100 + e.getKey();
            body.append("{\"type\":\"minecraft:item\",\"item\":{\"id\":\"").append(mat)
                .append("\"},\"height\":70,\"width\":70,\"show_decorations\":false,")
                .append("\"description\":[")
                .append("{\"text\":\"").append(name).append("\",\"bold\":true},")
                .append("{\"text\":\"\\n\"},")
                .append("{\"text\":\"Цена: \",\"color\":\"white\"},")
                .append("{\"text\":\"").append(d.getPrice()).append(" ").append(cur).append("\",\"color\":\"gold\"},")
                .append("{\"text\":\"\\n\"},")
                .append("{\"text\":\"Кол-во: ").append(d.getItem().getAmount()).append("\",\"color\":\"gray\"},")
                .append("{\"text\":\"\\n\"},")
                .append("{\"text\":\"[Купить]\",\"color\":\"green\",")
                .append("\"click_event\":{\"action\":\"run_command\",\"command\":\"shopbridge buy ")
                .append(trigger).append("\"}}]}");
        }
        body.append("]");
        return "{\"type\":\"notice\",\"can_close_with_escape\":false,\"pause\":false,"
                + "\"title\":\"" + shopName + "\",\"external_title\":\"" + shopName + "\","
                + "\"action\":{\"label\":\"Выйти\",\"width\":150,"
                + "\"action\":{\"type\":\"run_command\",\"command\":\"shopbridge buy 0\"}},"
                + "\"body\":" + body + "}";
    }
    private String strip(String s) {
        if (s == null) return "";
        return s.replaceAll("&#[0-9A-Fa-f]{6}", "")
                .replaceAll("&[0-9a-fk-orA-FK-OR]", "")
                .replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "").trim();
    }
}
