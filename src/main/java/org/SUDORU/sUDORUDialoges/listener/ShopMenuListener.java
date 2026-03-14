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

/**
 * Слушает клики в меню торговца.
 * Исправлен баг: клики в нижнем инвентаре (инвентарь игрока) тоже блокируются.
 */
public class ShopMenuListener implements Listener {

    private final SUDORUDialoges plugin;

    public ShopMenuListener(SUDORUDialoges plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Component title = event.getView().title();
        TraderShop shop = findShopByTitle(title);
        if (shop == null) return;

        // ── ФИКС БАГА: блокируем ВСЕ клики (и верхний, и нижний инвентарь) ──
        event.setCancelled(true);

        int slot = event.getRawSlot();

        // Клик в нижнем инвентаре игрока — просто блокируем, ничего не делаем
        if (slot >= event.getView().getTopInventory().getSize()) return;

        // Кнопка «закрыть»
        if (shop.isCloseSlot(slot)) {
            player.closeInventory();
            return;
        }

        // Кнопка «купить»
        int slotIndex = shop.getBuySlotIndex(slot);
        if (slotIndex >= 0) {
            boolean success = shop.tryPurchase(player, slotIndex);
            if (success) {
                plugin.getServer().getScheduler().runTask(plugin, () -> shop.openFor(player));
            }
        }
    }

    // Блокируем drag (перетаскивание) внутри меню торговца
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Component title = event.getView().title();
        if (findShopByTitle(title) != null) {
            event.setCancelled(true);
        }
    }

    private TraderShop findShopByTitle(Component title) {
        String plainTitle = PlainTextComponentSerializer.plainText().serialize(title);
        for (String id : plugin.getTraderManager().getShopIds()) {
            TraderShop shop = plugin.getTraderManager().getShop(id);
            if (shop == null) continue;
            String shopName = ColorUtil.toColoredString(shop.getConfig().getDisplayName());
            // Сравниваем plain text
            Component shopComp = ColorUtil.parse(shopName);
            String plainShop = PlainTextComponentSerializer.plainText().serialize(shopComp);
            if (plainTitle.equals(plainShop)) return shop;
        }
        return null;
    }
}
