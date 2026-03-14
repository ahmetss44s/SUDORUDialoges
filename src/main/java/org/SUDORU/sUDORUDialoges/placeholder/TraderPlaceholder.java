package org.SUDORU.sUDORUDialoges.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.SUDORU.sUDORUDialoges.shop.TraderShop;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI интеграция.
 *
 * Доступные плейсхолдеры:
 *  %sudoru_currency%              — кол-во валюты у игрока
 *  %sudoru_currency_name%         — название валюты
 *  %sudoru_trader_<id>_name%      — имя торговца
 *  %sudoru_trader_<id>_items%     — кол-во предметов в пуле торговца
 *  %sudoru_trader_<id>_refresh%   — время обновления торговца
 *  %sudoru_traders_count%         — кол-во загруженных торговцев
 */
public class TraderPlaceholder extends PlaceholderExpansion {

    private final SUDORUDialoges plugin;

    public TraderPlaceholder(SUDORUDialoges plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() { return "sudoru"; }

    @Override
    public @NotNull String getAuthor() { return "SUDORU"; }

    @Override
    public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }

    @Override
    public boolean persist() { return true; }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        // %sudoru_currency%
        if (params.equalsIgnoreCase("currency")) {
            if (player == null) return "0";
            return String.valueOf(plugin.getCurrencyAmount(player));
        }

        // %sudoru_currency_name%
        if (params.equalsIgnoreCase("currency_name")) {
            return plugin.getCurrencyName();
        }

        // %sudoru_traders_count%
        if (params.equalsIgnoreCase("traders_count")) {
            return String.valueOf(plugin.getTraderManager().getShopIds().size());
        }

        // %sudoru_trader_<id>_name%  /  _items  /  _refresh
        if (params.startsWith("trader_")) {
            String rest = params.substring("trader_".length()); // "<id>_name" etc.
            int lastUnderscore = rest.lastIndexOf('_');
            if (lastUnderscore <= 0) return null;

            String traderId = rest.substring(0, lastUnderscore).toLowerCase();
            String field    = rest.substring(lastUnderscore + 1).toLowerCase();
            TraderShop shop = plugin.getTraderManager().getShop(traderId);
            if (shop == null) return "unknown";

            return switch (field) {
                case "name"    -> shop.getConfig().getDisplayName().replace("&", "§");
                case "items"   -> String.valueOf(shop.getConfig().getItems().size());
                case "refresh" -> {
                    long s = shop.getConfig().getRefreshSeconds();
                    yield s > 0 ? formatTime(s) : "выкл.";
                }
                default -> null;
            };
        }

        return null;
    }

    private String formatTime(long seconds) {
        if (seconds < 60) return seconds + "с";
        long min = seconds / 60; long sec = seconds % 60;
        if (min < 60) return min + "м" + (sec > 0 ? " " + sec + "с" : "");
        long h = min / 60; long m = min % 60;
        return h + "ч" + (m > 0 ? " " + m + "м" : "");
    }
}

