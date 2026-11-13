/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.conversion;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

public class ReformatNumberString {
    public Value<String> action(String amount, String targetFormat) {
        Character groupSep;
        char decimalSep;
        if (amount == null || amount.trim().isEmpty()) {
            throw new BotCommandException("El valor no puede estar vac\u00edo.");
        }
        if (targetFormat == null) {
            throw new BotCommandException("Debe seleccionar un formato de salida.");
        }
        BigDecimal bd = ReformatNumberString.parseStrictAmount(amount);
        int scale = ReformatNumberString.decimalsFromPattern(targetFormat);
        switch (targetFormat) {
            case "d.ddd,dd": {
                decimalSep = ',';
                groupSep = Character.valueOf('.');
                break;
            }
            case "d,ddd.dd": {
                decimalSep = '.';
                groupSep = Character.valueOf(',');
                break;
            }
            case "dddd.dd": {
                decimalSep = '.';
                groupSep = null;
                break;
            }
            case "dddd,dd": {
                decimalSep = ',';
                groupSep = null;
                break;
            }
            default: {
                throw new BotCommandException("Formato no soportado: " + targetFormat);
            }
        }
        bd = bd.setScale(scale, RoundingMode.HALF_UP);
        String formatted = ReformatNumberString.formatWithSeparators(bd, decimalSep, groupSep, scale);
        return new StringValue(formatted);
    }

    private static int decimalsFromPattern(String pattern) {
        int idxComma;
        int idxDot = pattern.indexOf(46);
        int idx = Math.max(idxDot, idxComma = pattern.indexOf(44));
        return idx >= 0 ? pattern.length() - (idx + 1) : 0;
    }

    private static String formatWithSeparators(BigDecimal bd, char decimalSep, Character groupSep, int scale) {
        String fracPart;
        boolean negative = bd.signum() < 0;
        bd = bd.abs();
        String plain = bd.toPlainString();
        int dot = plain.indexOf(46);
        String intPart = dot >= 0 ? plain.substring(0, dot) : plain;
        String string = fracPart = dot >= 0 ? plain.substring(dot + 1) : "";
        if (scale > 0) {
            if (fracPart.length() < scale) {
                StringBuilder sb = new StringBuilder(fracPart);
                while (sb.length() < scale) {
                    sb.append('0');
                }
                fracPart = sb.toString();
            } else if (fracPart.length() > scale) {
                fracPart = fracPart.substring(0, scale);
            }
        }
        if (groupSep != null) {
            StringBuilder g = new StringBuilder();
            int count = 0;
            for (int i = intPart.length() - 1; i >= 0; --i) {
                g.append(intPart.charAt(i));
                if (++count != 3 || i <= 0) continue;
                g.append(groupSep);
                count = 0;
            }
            intPart = g.reverse().toString();
        }
        StringBuilder out = new StringBuilder();
        if (negative) {
            out.append('-');
        }
        out.append(intPart);
        if (scale > 0) {
            out.append(decimalSep).append(fracPart);
        }
        return out.toString();
    }

    private static BigDecimal parseStrictAmount(String input) {
        if (input == null) {
            throw new BotCommandException("El valor no puede ser nulo.");
        }
        String s = input.trim();
        if (s.isEmpty()) {
            throw new BotCommandException("El valor no puede estar vac\u00edo.");
        }
        if (s.startsWith("\"") && s.endsWith("\"") || s.startsWith("'") && s.endsWith("'")) {
            if (s.length() < 2) {
                throw new BotCommandException("Comillas inv\u00e1lidas.");
            }
            if ((s = s.substring(1, s.length() - 1).trim()).isEmpty()) {
                throw new BotCommandException("El valor no puede estar vac\u00edo.");
            }
            if (s.indexOf(34) >= 0 || s.indexOf(39) >= 0) {
                throw new BotCommandException("Las comillas solo pueden envolver el valor completo.");
            }
        }
        if (!s.matches("^[0-9\\.,\\$\\(\\)\\-]+$")) {
            throw new BotCommandException("Caracteres inv\u00e1lidos. Solo d\u00edgitos, ',', '.', '$', '(', ')', '-'.");
        }
        boolean negative = false;
        if (s.startsWith("$(") && s.endsWith(")")) {
            negative = true;
            s = s.substring(2, s.length() - 1);
        } else if (s.startsWith("(") && s.endsWith(")")) {
            negative = true;
            if ((s = s.substring(1, s.length() - 1)).contains("$")) {
                throw new BotCommandException("El s\u00edmbolo '$' debe estar al inicio absoluto.");
            }
        } else {
            if (s.startsWith("$-")) {
                negative = true;
                s = s.substring(2);
            } else if (s.startsWith("$")) {
                if ((s = s.substring(1)).startsWith("-")) {
                    negative = true;
                    s = s.substring(1);
                }
            } else if (s.startsWith("-")) {
                negative = true;
                s = s.substring(1);
            }
            if (s.indexOf(45) >= 0) {
                throw new BotCommandException("El '-' solo puede ir al inicio (o inmediatamente despu\u00e9s de '$').");
            }
            if (s.indexOf(40) >= 0 || s.indexOf(41) >= 0) {
                throw new BotCommandException("Los par\u00e9ntesis deben envolver todo el n\u00famero.");
            }
            if (s.indexOf(36) >= 0) {
                throw new BotCommandException("El '$' debe estar al inicio absoluto.");
            }
        }
        if (s.isEmpty()) {
            throw new BotCommandException("Falta el valor num\u00e9rico.");
        }
        int countDot = ReformatNumberString.countChar(s, '.');
        int countComma = ReformatNumberString.countChar(s, ',');
        if (countDot > 0 && countComma > 0) {
            char groupSep;
            int lastComma;
            int lastDot = s.lastIndexOf(46);
            char decimalSep = lastDot > (lastComma = s.lastIndexOf(44)) ? (char)'.' : ',';
            char c = groupSep = decimalSep == '.' ? (char)',' : '.';
            if (ReformatNumberString.countChar(s, decimalSep) != 1) {
                throw new BotCommandException("Formato inv\u00e1lido: m\u00faltiples separadores decimales.");
            }
            return ReformatNumberString.parseWithBothSeps(s, decimalSep, groupSep, negative);
        }
        if (countDot > 0 ^ countComma > 0) {
            char sep = countDot > 0 ? (char)'.' : ',';
            BigDecimal asDecimal = ReformatNumberString.tryParseAsDecimal(s, sep, negative);
            BigDecimal asGrouping = ReformatNumberString.tryParseAsGrouping(s, sep, negative);
            if (asDecimal != null && asGrouping != null) {
                return asGrouping;
            }
            if (asDecimal != null) {
                return asDecimal;
            }
            if (asGrouping != null) {
                return asGrouping;
            }
            throw new BotCommandException("Separadores mal ubicados.");
        }
        if (!s.matches("\\d+")) {
            throw new BotCommandException("La parte entera debe contener solo d\u00edgitos.");
        }
        BigDecimal bd = new BigDecimal(s);
        return negative ? bd.negate() : bd;
    }

    private static BigDecimal parseWithBothSeps(String s, char decimalSep, char groupSep, boolean negative) {
        int idx = s.lastIndexOf(decimalSep);
        String intPart = s.substring(0, idx);
        String fracPart = s.substring(idx + 1);
        if (fracPart.isEmpty() || !fracPart.matches("\\d+")) {
            throw new BotCommandException("Parte decimal inv\u00e1lida.");
        }
        if (ReformatNumberString.countChar(intPart, decimalSep) > 0) {
            throw new BotCommandException("Separadores inconsistentes en la parte entera.");
        }
        if (!ReformatNumberString.matchesGrouping(intPart, groupSep)) {
            throw new BotCommandException("Separadores de miles mal ubicados.");
        }
        String normalizedInt = intPart.replace(String.valueOf(groupSep), "");
        String normalized = normalizedInt + "." + fracPart;
        try {
            BigDecimal bd = new BigDecimal(normalized);
            return negative ? bd.negate() : bd;
        }
        catch (NumberFormatException ex) {
            throw new BotCommandException("N\u00famero inv\u00e1lido.", ex);
        }
    }

    private static BigDecimal tryParseAsDecimal(String s, char decimalSep, boolean negative) {
        if (ReformatNumberString.countChar(s, decimalSep) != 1) {
            return null;
        }
        int idx = s.lastIndexOf(decimalSep);
        String intPart = s.substring(0, idx);
        String fracPart = s.substring(idx + 1);
        if (intPart.isEmpty() || !intPart.matches("\\d+")) {
            return null;
        }
        if (fracPart.isEmpty() || !fracPart.matches("\\d+")) {
            return null;
        }
        String normalized = intPart + "." + fracPart;
        try {
            BigDecimal bd = new BigDecimal(normalized);
            return negative ? bd.negate() : bd;
        }
        catch (NumberFormatException ex) {
            return null;
        }
    }

    private static BigDecimal tryParseAsGrouping(String s, char groupSep, boolean negative) {
        if (!ReformatNumberString.matchesGrouping(s, groupSep)) {
            return null;
        }
        String normalized = s.replace(String.valueOf(groupSep), "");
        if (!normalized.matches("\\d+")) {
            return null;
        }
        try {
            BigDecimal bd = new BigDecimal(normalized);
            return negative ? bd.negate() : bd;
        }
        catch (NumberFormatException ex) {
            return null;
        }
    }

    private static boolean matchesGrouping(String intPart, char groupSep) {
        String g = Pattern.quote(String.valueOf(groupSep));
        String regex = "\\d{1,3}(" + g + "\\d{3})*";
        return intPart.matches(regex);
    }

    private static int countChar(String s, char c) {
        int n = 0;
        for (int i = 0; i < s.length(); ++i) {
            if (s.charAt(i) != c) continue;
            ++n;
        }
        return n;
    }
}
