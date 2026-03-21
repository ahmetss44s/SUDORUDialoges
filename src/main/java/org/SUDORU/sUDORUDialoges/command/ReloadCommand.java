package org.SUDORU.sUDORUDialoges.command;

import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * /traderreload вЂ” РїРµСЂРµР·Р°РіСЂСѓР·РєР° РєРѕРЅС„РёРіСѓСЂР°С†РёРё
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
            sender.sendMessage("В§cвњ— РќРµС‚ РїСЂР°РІ РґР»СЏ РїРµСЂРµР·Р°РіСЂСѓР·РєРё.");
            return true;
        }

        sender.sendMessage("В§eвџі РџРµСЂРµР·Р°РіСЂСѓР¶Р°СЋ РєРѕРЅС„РёРіСѓСЂР°С†РёСЋ С‚РѕСЂРіРѕРІС†РµРІ...");
        try {
            plugin.reloadConfig();
            plugin.getTraderManager().loadAll();
            sender.sendMessage("В§aвњ” РљРѕРЅС„РёРіСѓСЂР°С†РёСЏ СѓСЃРїРµС€РЅРѕ РїРµСЂРµР·Р°РіСЂСѓР¶РµРЅР°!");
            sender.sendMessage("В§7Р—Р°РіСЂСѓР¶РµРЅРѕ С‚РѕСЂРіРѕРІС†РµРІ: В§f"
                    + plugin.getTraderManager().getShopIds().size());
        } catch (Exception e) {
            sender.sendMessage("В§cвњ— РћС€РёР±РєР° РїСЂРё РїРµСЂРµР·Р°РіСЂСѓР·РєРµ: " + e.getMessage());
            plugin.getLogger().severe("РћС€РёР±РєР° РїСЂРё РїРµСЂРµР·Р°РіСЂСѓР·РєРµ: " + e.getMessage());
        }
        return true;
    }
}


