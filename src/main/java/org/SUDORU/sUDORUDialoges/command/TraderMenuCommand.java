package org.SUDORU.sUDORUDialoges.command;

import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.SUDORU.sUDORUDialoges.menu.TraderMenuGUI;
import org.SUDORU.sUDORUDialoges.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * /tradermenu — открыть GUI-редактор торговцев (только для OP/admin).
 */
public class TraderMenuCommand implements CommandExecutor {

    private final SUDORUDialoges plugin;
    private final TraderMenuGUI gui;

    public TraderMenuCommand(SUDORUDialoges plugin, TraderMenuGUI gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cТолько игрок может использовать эту команду!");
            return true;
        }

        if (!player.hasPermission("sudoru.trader.admin")) {
            player.sendMessage(ColorUtil.parse("&#FF5555✗ §cНет прав для управления торговцами."));
            return true;
        }

        if (args.length > 0) {
            // /tradermenu <id> — открыть редактор конкретного торговца
            String traderId = args[0].toLowerCase();
            if (plugin.getTraderManager().getShop(traderId) == null) {
                player.sendMessage(ColorUtil.parse("&#FF5555✗ §cТорговец §f'" + traderId + "' §cне найден."));
                return true;
            }
            gui.openEditor(player, traderId);
        } else {
            gui.openMain(player);
        }
        return true;
    }
}

