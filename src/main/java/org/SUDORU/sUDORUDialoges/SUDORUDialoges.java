package org.SUDORU.sUDORUDialoges;

import org.SUDORU.sUDORUDialoges.command.ConfigMenuCommand;
import org.SUDORU.sUDORUDialoges.command.ReloadCommand;
import org.SUDORU.sUDORUDialoges.command.ShopCommand;
import org.SUDORU.sUDORUDialoges.command.TraderMenuCommand;
import org.SUDORU.sUDORUDialoges.dialog.TraderDialogMenu;
import org.SUDORU.sUDORUDialoges.listener.ConfigMenuListener;
import org.SUDORU.sUDORUDialoges.listener.MenuEditorListener;
import org.SUDORU.sUDORUDialoges.listener.ShopMenuListener;
import org.SUDORU.sUDORUDialoges.menu.ConfigMenuGUI;
import org.SUDORU.sUDORUDialoges.menu.TraderMenuGUI;
import org.SUDORU.sUDORUDialoges.placeholder.TraderPlaceholder;
import org.SUDORU.sUDORUDialoges.shop.TraderManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SUDORUDialoges extends JavaPlugin {

    private TraderManager traderManager;
    private TraderMenuGUI traderMenuGUI;
    private ConfigMenuGUI configMenuGUI;
    private TraderDialogMenu traderDialogMenu;

    @Override
    public void onEnable() {
        // ── Сохраняем конфиг по умолчанию ──
        saveDefaultConfig();

        // ── Инициализация менеджера торговцев ──
        traderManager = new TraderManager(this);
        traderManager.loadAll();

        traderMenuGUI = new TraderMenuGUI(this);
        configMenuGUI = new ConfigMenuGUI(this);
        traderDialogMenu = new TraderDialogMenu(this);

        // ── Команды ──
        ShopCommand shopCmd = new ShopCommand(this);
        Objects.requireNonNull(getCommand("trader")).setExecutor(shopCmd);
        Objects.requireNonNull(getCommand("trader")).setTabCompleter(shopCmd);
        Objects.requireNonNull(getCommand("traderreload")).setExecutor(new ReloadCommand(this));
        Objects.requireNonNull(getCommand("tradermenu")).setExecutor(new TraderMenuCommand(this, traderMenuGUI));
        ConfigMenuCommand cfgCmd = new ConfigMenuCommand(this, configMenuGUI);
        Objects.requireNonNull(getCommand("traderconfig")).setExecutor(cfgCmd);
        Objects.requireNonNull(getCommand("traderconfig")).setTabCompleter(cfgCmd);

        // ── Слушатели ──
        getServer().getPluginManager().registerEvents(new ShopMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuEditorListener(this, traderMenuGUI), this);
        getServer().getPluginManager().registerEvents(new ConfigMenuListener(this, configMenuGUI), this);
        getServer().getPluginManager().registerEvents(traderDialogMenu, this); // чат для ×N покупки

        // ── PlaceholderAPI (опционально) ──
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TraderPlaceholder(this).register();
            getLogger().info("PlaceholderAPI найден — плейсхолдеры зарегистрированы.");
        }

        getLogger().info("╔══════════════════════════════════════╗");
        getLogger().info("║  SUDORU Диалоговая Торговля  v1.0.8  ║");
        getLogger().info("║  Торговцев загружено: "
                + String.format("%-15s", traderManager.getShopIds().size()) + "║");
        getLogger().info("╚══════════════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        if (traderManager != null) traderManager.shutdown();
        getLogger().info("SUDORUDialoges выключен.");
    }

    // ─── Валюта ──────────────────────────────────────────────────────

    /** Название валюты из конфига */
    public String getCurrencyName() {
        return getConfig().getString("currency.item-name", "Изумруд");
    }

    /** Количество валюты у игрока */
    public int getCurrencyAmount(Player player) {
        if ("ITEM".equalsIgnoreCase(getConfig().getString("currency.type", "ITEM"))) {
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
        if ("ITEM".equalsIgnoreCase(getConfig().getString("currency.type", "ITEM"))) {
            if (getCurrencyAmount(player) < amount) return false;
            Material mat = getCurrencyMaterial();
            int toRemove = amount;
            org.bukkit.inventory.Inventory inv = player.getInventory();
            ItemStack[] contents = inv.getContents();
            for (int i = 0; i < contents.length && toRemove > 0; i++) {
                ItemStack stack = contents[i];
                if (stack != null && stack.getType() == mat) {
                    int take = Math.min(stack.getAmount(), toRemove);
                    toRemove -= take;
                    if (take >= stack.getAmount()) {
                        inv.setItem(i, null); // полностью убрать стак
                    } else {
                        stack.setAmount(stack.getAmount() - take);
                        inv.setItem(i, stack);
                    }
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

    public TraderManager getTraderManager() { return traderManager; }
    @SuppressWarnings("unused") public TraderMenuGUI getTraderMenuGUI() { return traderMenuGUI; }
    @SuppressWarnings("unused") public ConfigMenuGUI getConfigMenuGUI() { return configMenuGUI; }
    public TraderDialogMenu getTraderDialogMenu() { return traderDialogMenu; }
}
