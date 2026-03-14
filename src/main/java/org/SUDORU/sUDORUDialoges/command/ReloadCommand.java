package org.SUDORU.sUDORUDialoges.command;

import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * /traderreload — перезагрузка конфигурации
 */
public class ReloadCommand implements CommandExecutor {

    private final SUDORUDialoges plugin;

    public ReloadCommand(SUDORUDialoges plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("sudoru.trader.reload")) {
            sender.sendMessage("§c✗ Нет прав для перезагрузки.");
            return true;
        }

        sender.sendMessage("§e⟳ Перезагружаю конфигурацию торговцев...");
        try {
            plugin.reloadConfig();
            plugin.getTraderManager().loadAll();
            sender.sendMessage("§a✔ Конфигурация успешно перезагружена!");
            sender.sendMessage("§7Загружено торговцев: §f"
                    + plugin.getTraderManager().getShopIds().size());
        } catch (Exception e) {
            sender.sendMessage("§c✗ Ошибка при перезагрузке: " + e.getMessage());
            plugin.getLogger().severe("Ошибка при перезагрузке: " + e.getMessage());
        }
        return true;
    }
}

