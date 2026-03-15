package org.SUDORU.sUDORUDialoges.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Утилита для работы с цветами: поддерживает &-коды, §-коды, HEX (#RRGGBB и &#RRGGBB).
 */
public class ColorUtil {

    // &#RRGGBB или #RRGGBB в начале/после &
    private static final Pattern HEX_PATTERN   = Pattern.compile("&#([0-9A-Fa-f]{6})");
    private static final Pattern HEX_PATTERN2  = Pattern.compile("#([0-9A-Fa-f]{6})");
    private static final MiniMessage MM         = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    /**
     * Принимает строку с &-кодами и/или HEX (#AABBCC или &#AABBCC),
     * возвращает Adventure Component.
     */
    public static Component parse(String input) {
        if (input == null) return Component.empty();
        // Конвертируем &#RRGGBB → §x§R§R§G§G§B§B (legacy hex)
        String converted = convertHex(input);
        // Конвертируем & → §
        converted = converted.replace("&", "§");
        return LEGACY.deserialize(converted);
    }

    /**
     * Принимает строку, возвращает строку с §-кодами (для сравнения заголовков и т.д.).
     */
    public static String toColoredString(String input) {
        if (input == null) return "";
        String converted = convertHex(input);
        return converted.replace("&", "§");
    }

    /**
     * Список строк → список компонентов.
     */
    public static List<Component> parseList(List<String> list) {
        List<Component> result = new ArrayList<>();
        if (list == null) return result;
        for (String s : list) result.add(parse(s));
        return result;
    }

    // Конвертирует &#RRGGBB → §x§R§R§G§G§B§B
    private static String convertHex(String text) {
        // Сначала &#RRGGBB формат
        StringBuffer sb = new StringBuffer();
        Matcher m = HEX_PATTERN.matcher(text);
        while (m.find()) {
            String hex = m.group(1).toUpperCase();
            m.appendReplacement(sb, Matcher.quoteReplacement(buildLegacyHex(hex)));
        }
        m.appendTail(sb);
        String result = sb.toString();

        // Затем просто #RRGGBB формат (не предшествует &)
        sb = new StringBuffer();
        m = HEX_PATTERN2.matcher(result);
        while (m.find()) {
            // Проверяем что перед # нет §x (уже обработано)
            int start = m.start();
            if (start > 0 && result.charAt(start - 1) == '\u00A7') {
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0)));
                continue;
            }
            String hex = m.group(1).toUpperCase();
            m.appendReplacement(sb, Matcher.quoteReplacement(buildLegacyHex(hex)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String buildLegacyHex(String hex) {
        StringBuilder builder = new StringBuilder("§x");
        for (char c : hex.toCharArray()) {
            builder.append('§').append(c);
        }
        return builder.toString();
    }
}

