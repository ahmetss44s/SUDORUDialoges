package org.SUDORU.sUDORUDialoges.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.SUDORU.sUDORUDialoges.shop.TraderShop;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Слушает клики в меню торговца.
 */
public class ShopMenuListener implements Listener {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

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

        event.setCancelled(true);

        int slot = event.getRawSlot();

        if (shop.isCloseSlot(slot)) {
            player.closeInventory();
            return;
        }

        int slotIndex = shop.getBuySlotIndex(slot);
        if (slotIndex >= 0) {
            boolean success = shop.tryPurchase(player, slotIndex);
            if (success) {
                plugin.getServer().getScheduler().runTask(plugin, () -> shop.openFor(player));
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Зарезервировано для звука/эффектов
    }

    private TraderShop findShopByTitle(Component title) {
        for (String id : plugin.getTraderManager().getShopIds()) {
            TraderShop shop = plugin.getTraderManager().getShop(id);
            if (shop == null) continue;
            String rawName = shop.getConfig().getDisplayName().replace("&", "§");
            Component shopTitle = LEGACY.deserialize(rawName);
            if (shopTitle.equals(title)) return shop;
        }
        return null;
    }
}
