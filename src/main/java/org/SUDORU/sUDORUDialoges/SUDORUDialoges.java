package org.SUDORU.sUDORUDialoges;

import org.SUDORU.sUDORUDialoges.command.ReloadCommand;
import org.SUDORU.sUDORUDialoges.command.ShopCommand;
import org.SUDORU.sUDORUDialoges.listener.ShopMenuListener;
import org.SUDORU.sUDORUDialoges.shop.TraderManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SUDORUDialoges extends JavaPlugin {

    private TraderManager traderManager;

    @Override
    public void onEnable() {
        // ── Сохраняем конфиг по умолчанию ──
        saveDefaultConfig();

        // ── Инициализация менеджера торговцев ──
        traderManager = new TraderManager(this);
        traderManager.loadAll();

        // ── Регистрация команд ──
        ShopCommand shopCmd = new ShopCommand(this);
        Objects.requireNonNull(getCommand("trader")).setExecutor(shopCmd);
        Objects.requireNonNull(getCommand("trader")).setTabCompleter(shopCmd);
        Objects.requireNonNull(getCommand("traderreload")).setExecutor(new ReloadCommand(this));

        // ── Регистрация слушателей ──
        getServer().getPluginManager().registerEvents(new ShopMenuListener(this), this);

        getLogger().info("╔══════════════════════════════════╗");
        getLogger().info("║  SUDORU Диалоговая Торговля      ║");
        getLogger().info("║  Загружено торговцев: "
                + String.format("%-11s", traderManager.getShopIds().size()) + "║");
        getLogger().info("╚══════════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        if (traderManager != null) traderManager.shutdown();
        getLogger().info("SUDORUDialoges выключен. Торговля завершена.");
    }

    // ─── Валюта ──────────────────────────────────────────────────────

    /** Название валюты из конфига */
    public String getCurrencyName() {
        return getConfig().getString("currency.item-name", "Изумруд");
    }

    /** Количество валюты у игрока */
    public int getCurrencyAmount(Player player) {
        String type = getConfig().getString("currency.type", "ITEM");
        if ("ITEM".equalsIgnoreCase(type)) {
            Material mat = getCurrencyMaterial();
            int count = 0;
            for (ItemStack stack : player.getInventory().getContents()) {
                if (stack != null && stack.getType() == mat) count += stack.getAmount();
            }
            return count;
        }
        return 0;
    }

    /**
     * Снимает у игрока нужное количество валюты.
     * Возвращает false, если недостаточно.
     */
    public boolean takeCurrency(Player player, int amount) {
        String type = getConfig().getString("currency.type", "ITEM");
        if ("ITEM".equalsIgnoreCase(type)) {
            if (getCurrencyAmount(player) < amount) return false;
            Material mat = getCurrencyMaterial();
            int toRemove = amount;
            ItemStack[] contents = player.getInventory().getContents();
            for (int i = 0; i < contents.length && toRemove > 0; i++) {
                ItemStack stack = contents[i];
                if (stack != null && stack.getType() == mat) {
                    int take = Math.min(stack.getAmount(), toRemove);
                    stack.setAmount(stack.getAmount() - take);
                    toRemove -= take;
                }
            }
            player.updateInventory();
            return true;
        }
        return false;
    }

    private Material getCurrencyMaterial() {
        String matName = getConfig().getString("currency.item-material", "EMERALD");
        try { return Material.valueOf(matName.toUpperCase()); }
        catch (IllegalArgumentException e) { return Material.EMERALD; }
    }

    // ─── Геттеры ─────────────────────────────────────────────────────

    public TraderManager getTraderManager() {
        return traderManager;
    }
}
