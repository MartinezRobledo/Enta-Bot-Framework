package com.automationanywhere.botcommand.actions.conversion;

import java.util.regex.Pattern;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.NumberValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;

import java.math.BigDecimal;

@BotCommand
@CommandPkg(
        name = "string_to_number_smart",
        label = "String → Number",
        node_label = "Parsear {{amount}}",
        description = "Valida y convierte un importe (String) a NUMBER con reglas estrictas",
        group_label = "Conversion",
        icon = "conversion.svg",
        return_type = DataType.NUMBER,
        return_label = "Importe numérico"
)
public class StringToNumberSmart {

    @Execute
    public Value<Double> action(
            @Idx(index = "1", type = AttributeType.TEXT)
            @Pkg(
                    label = "Importe (String)",
                    description = "Ej: 123 | 1,234.56 | 1.234,56 | $-1,234.56 | (123) | $(1,234) | \"123\" | '123'"
            )
            @NotEmpty
            String amount
    ) {
        BigDecimal bd = StringToNumberSmart.parseStrictAmount(amount);
        return new NumberValue(bd.toPlainString(), true);
    }

    // ------------------------- Parser estricto -------------------------

    private static BigDecimal parseStrictAmount(String input) {
        if (input == null) {
            throw new BotCommandException("El valor no puede ser nulo.");
        }
        String s = input.trim();
        if (s.isEmpty()) {
            throw new BotCommandException("El valor no puede estar vacío.");
        }
        if (s.startsWith("\"") && s.endsWith("\"") || s.startsWith("'") && s.endsWith("'")) {
            if (s.length() < 2) {
                throw new BotCommandException("Comillas inválidas.");
            }
            if ((s = s.substring(1, s.length() - 1).trim()).isEmpty()) {
                throw new BotCommandException("El valor no puede estar vacío.");
            }
            if (s.indexOf(34) >= 0 || s.indexOf(39) >= 0) {
                throw new BotCommandException("Las comillas solo pueden envolver el valor completo.");
            }
        }
        if (!(s = s.replaceAll("[\\s\\u00A0\\u202F]+", "")).matches("^[0-9\\.,\\$\\(\\)\\-]+$")) {
            throw new BotCommandException("Caracteres inválidos. Solo dígitos, ',', '.', '$', '(', ')', '-'.");
        }
        boolean negative = false;
        if (s.startsWith("$(") && s.endsWith(")")) {
            negative = true;
            s = s.substring(2, s.length() - 1);
        } else if (s.startsWith("(") && s.endsWith(")")) {
            negative = true;
            if ((s = s.substring(1, s.length() - 1)).contains("$")) {
                throw new BotCommandException("El símbolo '$' debe estar al inicio absoluto.");
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
                throw new BotCommandException("El '-' solo puede ir al inicio (o inmediatamente después de '$').");
            }
            if (s.indexOf(40) >= 0 || s.indexOf(41) >= 0) {
                throw new BotCommandException("Los paréntesis deben envolver todo el número.");
            }
            if (s.indexOf(36) >= 0) {
                throw new BotCommandException("El '$' debe estar al inicio absoluto.");
            }
        }
        if (s.isEmpty()) {
            throw new BotCommandException("Falta el valor numérico.");
        }
        int countDot = StringToNumberSmart.countChar(s, '.');
        int countComma = StringToNumberSmart.countChar(s, ',');
        if (countDot > 0 && countComma > 0) {
            Character groupSep;
            Character decimalSep;
            int lastComma;
            int lastDot = s.lastIndexOf(46);
            if (lastDot > (lastComma = s.lastIndexOf(44))) {
                decimalSep = Character.valueOf('.');
                groupSep = Character.valueOf(',');
            } else {
                decimalSep = Character.valueOf(',');
                groupSep = Character.valueOf('.');
            }
            if (StringToNumberSmart.countChar(s, decimalSep.charValue()) != 1) {
                throw new BotCommandException("Formato inválido: múltiples separadores decimales.");
            }
            return StringToNumberSmart.parseWithBothSeps(s, decimalSep.charValue(), groupSep.charValue(), negative);
        }
        if (countDot > 0 ^ countComma > 0) {
            char sep = countDot > 0 ? (char)'.' : ',';
            BigDecimal asDecimal = StringToNumberSmart.tryParseAsDecimal(s, sep, negative);
            BigDecimal asGrouping = StringToNumberSmart.tryParseAsGrouping(s, sep, negative);
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
            throw new BotCommandException("La parte entera debe contener solo dígitos.");
        }
        BigDecimal bd = new BigDecimal(s);
        return negative ? bd.negate() : bd;
    }

    // --------------------- Utilidades de validación ---------------------

    private static BigDecimal parseWithBothSeps(String s, char decimalSep, char groupSep, boolean negative) {
        int idx = s.lastIndexOf(decimalSep);
        String intPart = s.substring(0, idx);
        String fracPart = s.substring(idx + 1);
        if (fracPart.isEmpty() || !fracPart.matches("\\d+")) {
            throw new BotCommandException("Parte decimal inválida.");
        }
        if (StringToNumberSmart.countChar(intPart, decimalSep) > 0) {
            throw new BotCommandException("Separadores inconsistentes en la parte entera.");
        }
        if (!StringToNumberSmart.matchesGrouping(intPart, groupSep)) {
            throw new BotCommandException("Separadores de miles mal ubicados.");
        }
        String normalizedInt = intPart.replace(String.valueOf(groupSep), "");
        String normalized = normalizedInt + "." + fracPart;
        try {
            BigDecimal bd = new BigDecimal(normalized);
            return negative ? bd.negate() : bd;
        }
        catch (NumberFormatException ex) {
            throw new BotCommandException("Número inválido.", ex);
        }
    }

    private static BigDecimal tryParseAsDecimal(String s, char decimalSep, boolean negative) {
        if (StringToNumberSmart.countChar(s, decimalSep) != 1) {
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
        if (!StringToNumberSmart.matchesGrouping(s, groupSep)) {
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
