package org.SUDORU.sUDORUDialoges;
import org.SUDORU.sUDORUDialoges.command.ConfigMenuCommand;
import org.SUDORU.sUDORUDialoges.command.ReloadCommand;
import org.SUDORU.sUDORUDialoges.command.SellShopCommand;
import org.SUDORU.sUDORUDialoges.command.ShopCommand;
import org.SUDORU.sUDORUDialoges.command.ShopBridgeCommand;
import org.SUDORU.sUDORUDialoges.command.TraderMenuCommand;
import org.SUDORU.sUDORUDialoges.command.VillageresCommand;
import org.SUDORU.sUDORUDialoges.dialog.SellShopDialog;
import org.SUDORU.sUDORUDialoges.dialog.TraderDialogMenu;
import org.SUDORU.sUDORUDialoges.listener.ConfigMenuListener;
import org.SUDORU.sUDORUDialoges.listener.MenuEditorListener;
import org.SUDORU.sUDORUDialoges.listener.ShopMenuListener;
import org.SUDORU.sUDORUDialoges.listener.VillageresListener;
import org.SUDORU.sUDORUDialoges.menu.ConfigMenuGUI;
import org.SUDORU.sUDORUDialoges.menu.TraderMenuGUI;
import org.SUDORU.sUDORUDialoges.placeholder.TraderPlaceholder;
import org.SUDORU.sUDORUDialoges.shop.TraderManager;
import org.SUDORU.sUDORUDialoges.sync.DatapackSyncService;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import java.util.Objects;
public final class SUDORUDialoges extends JavaPlugin {
    private TraderManager traderManager;
    private TraderMenuGUI traderMenuGUI;
    private ConfigMenuGUI configMenuGUI;
    private TraderDialogMenu traderDialogMenu;
    private SellShopDialog sellShopDialog;
    private DatapackSyncService syncService;
    /** PDC-ключ для хранения цены продажи (Coins за 1 шт.) на купленных предметах */
    private NamespacedKey shopPriceKey;
    /** PDC-ключ: активный traderId для bridge-вызовов */
    private NamespacedKey activeTraderKey;
    /** PDC-ключ у NPC Villageres: привязанный traderId */
    private NamespacedKey traderNpcKey;
    @Override
    public void onEnable() {
        // -- PDC-ключ для продажи --
        shopPriceKey = new NamespacedKey(this, "shop_price");
        activeTraderKey = new NamespacedKey(this, "active_trader_id");
        traderNpcKey = new NamespacedKey(this, "trader_npc_id");
        // -- Сохраняем конфиг по умолчанию --
        saveDefaultConfig();
        // -- Инициализация менеджера торговцев --
        traderManager = new TraderManager(this);
        traderManager.loadAll();
        syncService = new DatapackSyncService(this);
        Bukkit.getScheduler().runTaskLater(this, () -> syncService.syncAll(), 1L);
        traderMenuGUI = new TraderMenuGUI(this);
        configMenuGUI = new ConfigMenuGUI(this);
        traderDialogMenu = new TraderDialogMenu(this);
        sellShopDialog = new SellShopDialog(this);
        // -- Команды --
        ShopCommand shopCmd = new ShopCommand(this);
        Objects.requireNonNull(getCommand("trader")).setExecutor(shopCmd);
        Objects.requireNonNull(getCommand("trader")).setTabCompleter(shopCmd);
        Objects.requireNonNull(getCommand("traderreload")).setExecutor(new ReloadCommand(this));
        Objects.requireNonNull(getCommand("tradermenu")).setExecutor(new TraderMenuCommand(this, traderMenuGUI));
        ConfigMenuCommand cfgCmd = new ConfigMenuCommand(this, configMenuGUI);
        Objects.requireNonNull(getCommand("traderconfig")).setExecutor(cfgCmd);
        Objects.requireNonNull(getCommand("traderconfig")).setTabCompleter(cfgCmd);
        SellShopCommand sellCmd = new SellShopCommand(this);
        Objects.requireNonNull(getCommand("sellshop")).setExecutor(sellCmd);
        Objects.requireNonNull(getCommand("sellshop")).setTabCompleter(sellCmd);
        ShopBridgeCommand bridgeCmd = new ShopBridgeCommand(this);
        Objects.requireNonNull(getCommand("shopbridge")).setExecutor(bridgeCmd);
        Objects.requireNonNull(getCommand("shopbridge")).setTabCompleter(bridgeCmd);
        Objects.requireNonNull(getCommand("villageres")).setExecutor(new VillageresCommand(this));
        // -- Слушатели --
        getServer().getPluginManager().registerEvents(new ShopMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuEditorListener(this, traderMenuGUI), this);
        getServer().getPluginManager().registerEvents(new ConfigMenuListener(this, configMenuGUI), this);
        getServer().getPluginManager().registerEvents(traderDialogMenu, this);
        getServer().getPluginManager().registerEvents(new VillageresListener(this), this);
        // -- PlaceholderAPI (опционально) --
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TraderPlaceholder(this).register();
            getLogger().info("PlaceholderAPI найден -- плейсхолдеры зарегистрированы.");
        }
        getLogger().info("╔══════════════════════════════════════╗");
        getLogger().info("║  SUDORU Диалоговая Торговля  v1.1.5  ║");
        getLogger().info("║  Валюта: Scoreboard Coins            ║");
        getLogger().info("║  Торговцев загружено: "
                + String.format("%-15s", traderManager.getShopIds().size()) + "║");
        getLogger().info("╚══════════════════════════════════════╝");
    }
    @Override
    public void onDisable() {
        if (traderManager != null) traderManager.shutdown();
        getLogger().info("SUDORUDialoges выключен.");
    }
    // --- Валюта (Scoreboard Coins) ---
    /** Название валюты для сообщений */
    public String getCurrencyName() {
        return getConfig().getString("currency.name", "Coins");
    }
    /** Максимум предметов за одну закупку через кнопку x? */
    public int getMaxPurchaseAmount() {
        return getConfig().getInt("shop.max-purchase-amount", 1000);
    }
    /**
     * Возвращает баланс Coins из main scoreboard.
     * Если objective "Coins" не найден -- возвращает 0.
     */
    public int getCurrencyAmount(Player player) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective obj = sb.getObjective("Coins");
        if (obj == null) return 0;
        Score score = obj.getScore(player.getName());
        return score.getScore();
    }
    /**
     * Снимает amount Coins с игрока.
     * Возвращает false если не хватает или objective не найден.
     */
    public boolean takeCurrency(Player player, int amount) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective obj = sb.getObjective("Coins");
        if (obj == null) {
            player.sendMessage("§c✗ Scoreboard-объект 'Coins' не найден! Загрузите датапак.");
            getLogger().warning("Scoreboard objective 'Coins' не найден! Датапак загружен?");
            return false;
        }
        Score score = obj.getScore(player.getName());
        int balance = score.getScore();
        if (balance < amount) return false;
        score.setScore(balance - amount);
        return true;
    }
    /**
     * Добавляет amount Coins игроку (при продаже).
     */
    public void addCurrency(Player player, int amount) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective obj = sb.getObjective("Coins");
        if (obj == null) {
            player.sendMessage("§c✗ Scoreboard-объект 'Coins' не найден! Загрузите датапак.");
            getLogger().warning("Scoreboard objective 'Coins' не найден! Датапак загружен?");
            return;
        }
        Score score = obj.getScore(player.getName());
        score.setScore(score.getScore() + amount);
    }
    // --- PDC-ключ ---
    public NamespacedKey getShopPriceKey() { return shopPriceKey; }
    public NamespacedKey getActiveTraderKey() { return activeTraderKey; }
    public NamespacedKey getTraderNpcKey() { return traderNpcKey; }
    // --- Геттеры ---
    public TraderManager getTraderManager() { return traderManager; }
    public DatapackSyncService getSyncService() { return syncService; }
    @SuppressWarnings("unused") public TraderMenuGUI getTraderMenuGUI() { return traderMenuGUI; }
    @SuppressWarnings("unused") public ConfigMenuGUI getConfigMenuGUI() { return configMenuGUI; }
    public TraderDialogMenu getTraderDialogMenu() { return traderDialogMenu; }
    public SellShopDialog getSellShopDialog() { return sellShopDialog; }
}