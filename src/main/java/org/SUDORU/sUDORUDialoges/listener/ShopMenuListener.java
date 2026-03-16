package org.SUDORU.sUDORUDialoges.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.SUDORU.sUDORUDialoges.shop.TraderShop;
import org.SUDORU.sUDORUDialoges.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

/**
 * Слушает клики в меню торговца.
 * Исправлен баг: клики в нижнем инвентаре (инвентарь игрока) тоже блокируются.
 * Исправлен баг: HIGHEST приоритет + updateInventory предотвращает взятие барьера.
 */
public class ShopMenuListener implements Listener {

    private final SUDORUDialoges plugin;

    public ShopMenuListener(SUDORUDialoges plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Component title = event.getView().title();
        TraderShop shop = findShopByTitle(title);
        if (shop == null) return;

        // ── ФИКС БАГА: блокируем ВСЕ клики (и верхний, и нижний инвентарь) ──
        event.setCancelled(true);
        // Принудительная синхронизация клиента — предотвращает взятие барьера
        player.updateInventory();

        int slot = event.getRawSlot();

        // Клик вне инвентаря (rawSlot = -999 или < 0) или в нижнем инвентаре игрока
        if (slot < 0 || slot >= event.getView().getTopInventory().getSize()) return;

        // Кнопка «закрыть»
        if (shop.isCloseSlot(slot)) {
            player.closeInventory();
            return;
        }

        // Кнопка «Купить ×1»
        int buy1Index = shop.getItemIndexByBuy1(slot);
        if (buy1Index >= 0) {
            boolean success = shop.tryPurchase(player, buy1Index, 1);
            if (success) plugin.getServer().getScheduler().runTask(plugin, () -> shop.openFor(player));
            return;
        }

        // Кнопка «Купить несколько»
        int buyNIndex = shop.getItemIndexByBuyN(slot);
        if (buyNIndex >= 0) {
            plugin.getTraderDialogMenu().startChatPurchase(player, shop, buyNIndex);
        }
    }

    // Блокируем drag (перетаскивание) внутри меню торговца
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Component title = event.getView().title();
        if (findShopByTitle(title) != null) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    // Блокируем перемещение предметов хоперами и т.д.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        // Проверяем, не является ли источник или назначение нашим магазином
        Component srcTitle = event.getSource().getViewers().isEmpty()
                ? null
                : event.getSource().getViewers().get(0) instanceof Player p
                    ? p.getOpenInventory().title() : null;
        if (srcTitle != null && findShopByTitle(srcTitle) != null) {
            event.setCancelled(true);
        }
    }

    private TraderShop findShopByTitle(Component title) {
        String plainTitle = PlainTextComponentSerializer.plainText().serialize(title);
        for (String id : plugin.getTraderManager().getShopIds()) {
            TraderShop shop = plugin.getTraderManager().getShop(id);
            if (shop == null) continue;
            // Сравниваем plain text заголовка с plain text имени торговца
            Component shopComp = ColorUtil.parse(shop.getConfig().getDisplayName());
            String plainShop = PlainTextComponentSerializer.plainText().serialize(shopComp);
            if (plainTitle.equals(plainShop)) return shop;
        }
        return null;
    }
}

