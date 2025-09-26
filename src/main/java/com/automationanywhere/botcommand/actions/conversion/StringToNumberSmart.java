package com.automationanywhere.botcommand.actions.conversion;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.NumberValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.GreaterThanEqualTo;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.annotations.rules.NumberInteger;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;

import java.math.BigDecimal;
import java.math.RoundingMode;

@BotCommand
@CommandPkg(
        name = "string_to_number_smart",
        label = "String → Number (Smart)",
        node_label = "Parsear {{amount}} con {{decimalPlaces}} decimales",
        description = "Valida y convierte un importe (String) a NUMBER con reglas estrictas y escala configurable",
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
            String amount,

            @Idx(index = "2", type = AttributeType.NUMBER)
            @Pkg(
                    label = "Cantidad de decimales",
                    default_value = "2",
                    default_value_type = DataType.NUMBER
            )
            @NumberInteger
            @GreaterThanEqualTo("0")
            Number decimalPlaces
    ) {
        int scale = decimalPlaces.intValue();
        if (scale < 0) {
            throw new BotCommandException("La cantidad de decimales no puede ser negativa.");
        }
        BigDecimal bd = parseStrictAmount(amount);
        bd = bd.setScale(scale, RoundingMode.HALF_UP);
        return new NumberValue(bd.doubleValue());
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

        // 0) Quitar comillas si envuelven TODO el valor: "..." o '...'
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            if (s.length() < 2) throw new BotCommandException("Comillas inválidas.");
            s = s.substring(1, s.length() - 1).trim();
            if (s.isEmpty()) throw new BotCommandException("El valor no puede estar vacío.");
            // No se permiten comillas internas
            if (s.indexOf('"') >= 0 || s.indexOf('\'') >= 0) {
                throw new BotCommandException("Las comillas solo pueden envolver el valor completo.");
            }
        }

        // 1) Chequeo de charset permitido
        if (!s.matches("^[0-9\\.,\\$\\(\\)\\-]+$")) {
            throw new BotCommandException("Caracteres inválidos. Solo dígitos, ',', '.', '$', '(', ')', '-'.");
        }

        boolean negative = false;

        // 2) $, -, ()
        if (s.startsWith("$(") && s.endsWith(")")) {
            // $( ... ) => negativo contable con moneda
            negative = true;
            s = s.substring(2, s.length() - 1);
        } else if (s.startsWith("(") && s.endsWith(")")) {
            // ( ... ) => negativo contable sin moneda
            negative = true;
            s = s.substring(1, s.length() - 1);
            if (s.contains("$")) {
                throw new BotCommandException("El símbolo '$' debe estar al inicio absoluto.");
            }
        } else {
            if (s.startsWith("$-")) {          // $-123  (permitido)
                negative = true;
                s = s.substring(2);
            } else if (s.startsWith("$")) {    // $123 o $-123
                s = s.substring(1);
                if (s.startsWith("-")) {
                    negative = true;
                    s = s.substring(1);
                }
            } else if (s.startsWith("-")) {    // -123
                negative = true;
                s = s.substring(1);
            }
            // No deben quedar símbolos en el medio
            if (s.indexOf('-') >= 0) {
                throw new BotCommandException("El '-' solo puede ir al inicio (o inmediatamente después de '$').");
            }
            if (s.indexOf('(') >= 0 || s.indexOf(')') >= 0) {
                throw new BotCommandException("Los paréntesis deben envolver todo el número.");
            }
            if (s.indexOf('$') >= 0) {
                throw new BotCommandException("El '$' debe estar al inicio absoluto.");
            }
        }

        if (s.isEmpty()) {
            throw new BotCommandException("Falta el valor numérico.");
        }

        // 3) Determinar y validar separadores (US o EU)
        int countDot = countChar(s, '.');
        int countComma = countChar(s, ',');

        Character decimalSep = null;
        Character groupSep = null;

        if (countDot > 0 && countComma > 0) {
            // Ambos presentes: el más a la derecha es decimal, el otro miles
            int lastDot = s.lastIndexOf('.');
            int lastComma = s.lastIndexOf(',');
            if (lastDot > lastComma) {
                decimalSep = '.';
                groupSep = ',';
            } else {
                decimalSep = ',';
                groupSep = '.';
            }
            // Asegurar que el decimal aparece solo una vez
            if (countChar(s, decimalSep) != 1) {
                throw new BotCommandException("Formato inválido: múltiples separadores decimales.");
            }
            // Validar estructura con ambos separadores
            return parseWithBothSeps(s, decimalSep, groupSep, negative);
        } else if (countDot > 0 ^ countComma > 0) {
            // Solo uno presente: intentar como decimal y como miles; si ambas válidas, preferir miles
            char sep = (countDot > 0) ? '.' : ',';
            BigDecimal asDecimal = tryParseAsDecimal(s, sep, negative);
            BigDecimal asGrouping = tryParseAsGrouping(s, sep, negative);

            if (asDecimal != null && asGrouping != null) {
                // Ambiguo: preferimos miles (ej: 1,000 => mil)
                return asGrouping;
            } else if (asDecimal != null) {
                return asDecimal;
            } else if (asGrouping != null) {
                return asGrouping;
            } else {
                throw new BotCommandException("Separadores mal ubicados.");
            }
        } else {
            // Sin separadores: solo dígitos
            if (!s.matches("\\d+")) {
                throw new BotCommandException("La parte entera debe contener solo dígitos.");
            }
            BigDecimal bd = new BigDecimal(s);
            return negative ? bd.negate() : bd;
        }
    }

    // --------------------- Utilidades de validación ---------------------

    private static BigDecimal parseWithBothSeps(String s, char decimalSep, char groupSep, boolean negative) {
        int idx = s.lastIndexOf(decimalSep);
        String intPart = s.substring(0, idx);
        String fracPart = s.substring(idx + 1);

        // Validar decimal
        if (fracPart.isEmpty() || !fracPart.matches("\\d+")) {
            throw new BotCommandException("Parte decimal inválida.");
        }

        // La parte entera solo puede usar el separador de miles
        if (countChar(intPart, decimalSep) > 0) {
            throw new BotCommandException("Separadores inconsistentes en la parte entera.");
        }

        if (!matchesGrouping(intPart, groupSep)) {
            throw new BotCommandException("Separadores de miles mal ubicados.");
        }

        String normalizedInt = intPart.replace(String.valueOf(groupSep), "");
        String normalized = normalizedInt + "." + fracPart;

        try {
            BigDecimal bd = new BigDecimal(normalized);
            return negative ? bd.negate() : bd;
        } catch (NumberFormatException ex) {
            throw new BotCommandException("Número inválido.", ex);
        }
    }

    private static BigDecimal tryParseAsDecimal(String s, char decimalSep, boolean negative) {
        if (countChar(s, decimalSep) != 1) return null;
        int idx = s.lastIndexOf(decimalSep);
        String intPart = s.substring(0, idx);
        String fracPart = s.substring(idx + 1);

        if (intPart.isEmpty() || !intPart.matches("\\d+")) return null;
        if (fracPart.isEmpty() || !fracPart.matches("\\d+")) return null;

        String normalized = intPart + "." + fracPart;
        try {
            BigDecimal bd = new BigDecimal(normalized);
            return negative ? bd.negate() : bd;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static BigDecimal tryParseAsGrouping(String s, char groupSep, boolean negative) {
        // No parte decimal; toda la cadena es parte entera con separador de miles
        if (!matchesGrouping(s, groupSep)) return null;
        String normalized = s.replace(String.valueOf(groupSep), "");
        if (!normalized.matches("\\d+")) return null;
        try {
            BigDecimal bd = new BigDecimal(normalized);
            return negative ? bd.negate() : bd;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static boolean matchesGrouping(String intPart, char groupSep) {
        String g = java.util.regex.Pattern.quote(String.valueOf(groupSep));
        String regex = "\\d{1,3}(" + g + "\\d{3})*";
        return intPart.matches(regex);
    }

    private static int countChar(String s, char c) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == c) n++;
        return n;
    }
}
