package org.SUDORU.sUDORUDialoges.command;

import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * /sellshop [open] — открыть диалог продажи предметов за Coins.
 * Вызывается игроком или датапаком: execute as @p run sellshop open
 */
public class SellShopCommand implements CommandExecutor, TabCompleter {

    private final SUDORUDialoges plugin;

    public SellShopCommand(SUDORUDialoges plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭту команду может использовать только игрок!");
            return true;
        }

        if (!player.hasPermission("sudoru.sellshop")) {
            player.sendMessage("§c✗ У тебя нет доступа к продаже предметов.");
            return true;
        }

        plugin.getSellShopDialog().open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return List.of("open");
        return List.of();
    }
}