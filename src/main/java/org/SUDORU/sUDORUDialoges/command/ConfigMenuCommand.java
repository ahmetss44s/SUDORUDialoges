package org.SUDORU.sUDORUDialoges.command;

import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.SUDORU.sUDORUDialoges.menu.ConfigMenuGUI;
import org.SUDORU.sUDORUDialoges.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * /traderconfig — полное GUI-меню всех настроек из config.yml.
 */
public class ConfigMenuCommand implements CommandExecutor, TabCompleter {

    private final SUDORUDialoges plugin;
    private final ConfigMenuGUI gui;

    public ConfigMenuCommand(SUDORUDialoges plugin, ConfigMenuGUI gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cТолько игрок!");
            return true;
        }
        if (!player.hasPermission("sudoru.trader.admin")) {
            player.sendMessage(ColorUtil.parse("&#FF5555✗ §cНет прав."));
            return true;
        }

        if (args.length == 0) {
            gui.openMain(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "currency" -> gui.openCurrency(player);
            case "trader" -> {
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.parse("§c§lИспользование: §r§c/traderconfig trader <id>"));
                    return true;
                }
                String id = args[1].toLowerCase();
                if (plugin.getTraderManager().getShop(id) == null) {
                    player.sendMessage(ColorUtil.parse("&#FF5555✗ §cТорговец §f'" + id + "' §cне найден."));
                    return true;
                }
                gui.openTrader(player, id);
            }
            case "items" -> {
                if (args.length < 2) { gui.openMain(player); return true; }
                gui.openItems(player, args[1].toLowerCase(), 0);
            }
            default -> gui.openMain(player);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            for (String s : List.of("currency", "trader", "items"))
                if (s.startsWith(args[0].toLowerCase())) result.add(s);
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("trader")
                || args[0].equalsIgnoreCase("items"))) {
            for (String id : plugin.getTraderManager().getShopIds())
                if (id.startsWith(args[1].toLowerCase())) result.add(id);
        }
        return result;
    }
}
