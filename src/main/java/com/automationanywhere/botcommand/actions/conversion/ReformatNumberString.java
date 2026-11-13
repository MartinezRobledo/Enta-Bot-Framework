package com.automationanywhere.botcommand.actions.conversion;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;

import java.math.BigDecimal;
import java.math.RoundingMode;

@BotCommand
@CommandPkg(
        name = "reformat_number_string",
        label = "Formatear Número (String)",
        node_label = "Formatear {{amount}} como {{targetFormat}}",
        description = "Convierte un número en texto a otro formato textual (separadores y decimales) manteniendo el valor.",
        group_label = "Conversion",
        icon = "conversion.svg",
        return_type = DataType.STRING,
        return_label = "Número formateado"
)
public class ReformatNumberString {

    @Execute
    public Value<String> action(
            @Idx(index = "1", type = AttributeType.TEXT)
            @Pkg(
                    label = "Importe (String)",
                    description = "Ej: 123 | 1,234.56 | 1.234,56 | $-1,234.56 | (123) | \"123\""
            )
            String amount,

            @Idx(
                    index = "2",
                    type = AttributeType.SELECT,
                    options = {
                            @Idx.Option(index = "2.1", pkg = @Pkg(label = "d.ddd,dd", value = "d.ddd,dd")),
                            @Idx.Option(index = "2.2", pkg = @Pkg(label = "d,ddd.dd", value = "d,ddd.dd")),
                            @Idx.Option(index = "2.3", pkg = @Pkg(label = "dddd.dd",  value = "dddd.dd")),
                            @Idx.Option(index = "2.4", pkg = @Pkg(label = "dddd,dd",  value = "dddd,dd")),
                    }
            )
            @Pkg(label = "Formato de salida")
            String targetFormat
    ) {
        if (amount == null || amount.trim().isEmpty()) {
            throw new BotCommandException("El valor no puede estar vacío.");
        }
        if (targetFormat == null) {
            throw new BotCommandException("Debe seleccionar un formato de salida.");
        }

        // 1) Parseo estricto -> BigDecimal normalizado (decimal '.')
        BigDecimal bd = parseStrictAmount(amount); // Reutiliza la misma lógica que tu acción existente. [1](https://entaconsulting-my.sharepoint.com/personal/amartinez_entaconsulting_com/Documents/Archivos%20de%20chat%20de%20Microsoft%C2%A0Copilot/StringToNumberSmart.java)

        // 2) Determinar separadores y escala desde el patrón
        char decimalSep;
        Character groupSep; // null = sin separación de miles
        int scale = decimalsFromPattern(targetFormat);

        switch (targetFormat) {
            case "d.ddd,dd":
                decimalSep = ',';
                groupSep = '.';
                break;
            case "d,ddd.dd":
                decimalSep = '.';
                groupSep = ',';
                break;
            case "dddd.dd":
                decimalSep = '.';
                groupSep = null;
                break;
            case "dddd,dd":
                decimalSep = ',';
                groupSep = null;
                break;
            default:
                throw new BotCommandException("Formato no soportado: " + targetFormat);
        }

        // 3) Redondeo a la escala objetivo (HALF_UP)
        bd = bd.setScale(scale, RoundingMode.HALF_UP);

        // 4) Formateo final como texto
        String formatted = formatWithSeparators(bd, decimalSep, groupSep, scale);
        return new StringValue(formatted);
    }

    // ---- Utilidades ----

    private static int decimalsFromPattern(String pattern) {
        int idxDot = pattern.indexOf('.');
        int idxComma = pattern.indexOf(',');
        int idx = Math.max(idxDot, idxComma);
        return (idx >= 0) ? pattern.length() - (idx + 1) : 0;
    }

    private static String formatWithSeparators(BigDecimal bd, char decimalSep, Character groupSep, int scale) {
        boolean negative = bd.signum() < 0;
        bd = bd.abs();

        // Separamos entero y fracción con '.' (porque BigDecimal.toPlainString usa '.')
        String plain = bd.toPlainString();
        int dot = plain.indexOf('.');
        String intPart = (dot >= 0) ? plain.substring(0, dot) : plain;
        String fracPart = (dot >= 0) ? plain.substring(dot + 1) : "";

        // Asegurar longitud de la fracción = scale (relleno con ceros si hace falta)
        if (scale > 0) {
            if (fracPart.length() < scale) {
                StringBuilder sb = new StringBuilder(fracPart);
                while (sb.length() < scale) sb.append('0');
                fracPart = sb.toString();
            } else if (fracPart.length() > scale) {
                // No debería suceder por el setScale, pero por seguridad:
                fracPart = fracPart.substring(0, scale);
            }
        }

        // Agrupar miles si corresponde (cada 3)
        if (groupSep != null) {
            StringBuilder g = new StringBuilder();
            int count = 0;
            for (int i = intPart.length() - 1; i >= 0; i--) {
                g.append(intPart.charAt(i));
                count++;
                if (count == 3 && i > 0) {
                    g.append(groupSep);
                    count = 0;
                }
            }
            intPart = g.reverse().toString();
        }

        StringBuilder out = new StringBuilder();
        if (negative) out.append('-');
        out.append(intPart);
        if (scale > 0) {
            out.append(decimalSep).append(fracPart);
        }
        return out.toString();
    }

    // -------------------- Parser estricto (idéntico al de tu acción) --------------------
    // Copiado para mantener comportamiento uniforme y sin dependencias cruzadas. [1](https://entaconsulting-my.sharepoint.com/personal/amartinez_entaconsulting_com/Documents/Archivos%20de%20chat%20de%20Microsoft%C2%A0Copilot/StringToNumberSmart.java)
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
            if (s.indexOf('"') >= 0 || s.indexOf('\'') >= 0) {
                throw new BotCommandException("Las comillas solo pueden envolver el valor completo.");
            }
        }
        // 1) Charset permitido
        if (!s.matches("^[0-9\\.,\\$\\(\\)\\-]+$")) {
            throw new BotCommandException("Caracteres inválidos. Solo dígitos, ',', '.', '$', '(', ')', '-'.");
        }
        boolean negative = false;
        // 2) $, -, ()
        if (s.startsWith("$(") && s.endsWith(")")) {
            negative = true;
            s = s.substring(2, s.length() - 1);
        } else if (s.startsWith("(") && s.endsWith(")")) {
            negative = true;
            s = s.substring(1, s.length() - 1);
            if (s.contains("$")) {
                throw new BotCommandException("El símbolo '$' debe estar al inicio absoluto.");
            }
        } else {
            if (s.startsWith("$-")) {
                negative = true;
                s = s.substring(2);
            } else if (s.startsWith("$")) {
                s = s.substring(1);
                if (s.startsWith("-")) {
                    negative = true;
                    s = s.substring(1);
                }
            } else if (s.startsWith("-")) {
                negative = true;
                s = s.substring(1);
            }
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

        int countDot = countChar(s, '.');
        int countComma = countChar(s, ',');

        if (countDot > 0 && countComma > 0) {
            int lastDot = s.lastIndexOf('.');
            int lastComma = s.lastIndexOf(',');
            char decimalSep = (lastDot > lastComma) ? '.' : ',';
            char groupSep = (decimalSep == '.') ? ',' : '.';
            if (countChar(s, decimalSep) != 1) {
                throw new BotCommandException("Formato inválido: múltiples separadores decimales.");
            }
            return parseWithBothSeps(s, decimalSep, groupSep, negative);
        } else if ((countDot > 0) ^ (countComma > 0)) {
            char sep = (countDot > 0) ? '.' : ',';
            BigDecimal asDecimal = tryParseAsDecimal(s, sep, negative);
            BigDecimal asGrouping = tryParseAsGrouping(s, sep, negative);
            if (asDecimal != null && asGrouping != null) {
                return asGrouping; // Ambiguo -> preferir miles (p.ej., "1,000")
            } else if (asDecimal != null) {
                return asDecimal;
            } else if (asGrouping != null) {
                return asGrouping;
            } else {
                throw new BotCommandException("Separadores mal ubicados.");
            }
        } else {
            if (!s.matches("\\d+")) {
                throw new BotCommandException("La parte entera debe contener solo dígitos.");
            }
            BigDecimal bd = new BigDecimal(s);
            return negative ? bd.negate() : bd;
        }
    }

    private static BigDecimal parseWithBothSeps(String s, char decimalSep, char groupSep, boolean negative) {
        int idx = s.lastIndexOf(decimalSep);
        String intPart = s.substring(0, idx);
        String fracPart = s.substring(idx + 1);
        if (fracPart.isEmpty() || !fracPart.matches("\\d+")) {
            throw new BotCommandException("Parte decimal inválida.");
        }
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