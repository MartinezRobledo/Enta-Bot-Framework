package com.automationanywhere.botcommand.actions.config;

import com.automationanywhere.botcommand.utilities.file.FileValidator;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.CredentialAllowPassword;
import com.automationanywhere.commandsdk.annotations.rules.FileExtension;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.model.DataType;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.core.security.SecureString;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.*;

// import static si lo necesitás
// import static com.automationanywhere.commandsdk.model.AttributeType.CREDENTIAL;

@BotCommand
@CommandPkg(
        label = "Read Excel Config",
        description = "Read hierarchical config values from Excel",
        icon = "excel.svg",
        name = "config_read_excel",
        group_label = "Config",
        return_label = "Output: config dictionary",
        return_type = DataType.DICTIONARY,
        return_sub_type = DataType.STRING,
        return_name = "Config",
        return_Direct = true,
        return_required = true
)
public class ExcelReader {

    // --- si querés centralizar los inicios por hoja:
    private static final int START_ROW_CONFIG = 2; // fila 3 (0-based)
    private static final int START_ROW_LIST   = 1; // fila 2 (0-based)
    private static final int START_ROW_DICT   = 2; // fila 3 (0-based) -> poné 1 si tus diccionarios NO tienen banner

    @Execute
    public DictionaryValue action(
            @Idx(index = "1", type = AttributeType.FILE)
            @Pkg(label = "Enter Excel file path")
            @NotEmpty
            @FileExtension("xlsm")
            String inputFilePath,

            @Idx(index = "2", type = AttributeType.CHECKBOX)
            @Pkg(label = "File is password protected", description = "Select to supply password")
            Boolean isPasswordProtected,

            @Idx(index = "2.1", type = AttributeType.CREDENTIAL)  // si usás import static CREDENTIAL, podés dejar CREDENTIAL
            @Pkg(label = "Password")
            @CredentialAllowPassword
            SecureString filePassword,

            @Idx(index = "3", type = AttributeType.BOOLEAN)
            @Pkg(label = "Trim values", default_value_type = DataType.BOOLEAN, default_value = "false")
            Boolean isTrimValues
    ) {
        FileInputStream inputStream = null;
        Workbook workbook = null;

        try {
            // Validar archivo (usa tu FileValidator real)
            FileValidator fileValidator = new FileValidator(inputFilePath);
            String[] allowedExtensions = {"xlsm"};
            fileValidator.validateFile(allowedExtensions);

            // Abrir archivo
            File file = new File(inputFilePath);
            inputStream = new FileInputStream(file);

            String pwd = (isPasswordProtected != null && isPasswordProtected) ? filePassword.getInsecureString() : null;
            workbook = (pwd != null && !pwd.isEmpty())
                    ? WorkbookFactory.create(inputStream, pwd)
                    : WorkbookFactory.create(inputStream);

            // Hoja principal
            Sheet configSheet = workbook.getSheet("Config");
            if (configSheet == null) {
                throw new BotCommandException("No se encontró la hoja principal 'Config'");
            }

            DataFormatter dataFormatter = new DataFormatter();

            // Procesar Config arrancando en fila 3 (índice 2)
            DictionaryValue result =
                    parseConfig(workbook, configSheet, dataFormatter, (isTrimValues != null && isTrimValues), START_ROW_CONFIG);

            return result;

        } catch (Exception e) {
            throw new BotCommandException("Error leyendo archivo Excel: " + e.getMessage(), e);
        } finally {
            closeWorkbook(workbook);
            closeInputStream(inputStream);
        }
    }

    // ---------------- PARSER GENERAL (SHEET DE TIPO DICCIONARIO) ----------------
    private DictionaryValue parseConfig(Workbook workbook,
                                        Sheet sheet,
                                        DataFormatter dataFormatter,
                                        boolean isTrimValues,
                                        int startRowIndex) {

        Map<String, Value> dict = new LinkedHashMap<>();

        // Recorremos por índice (controlamos desde qué fila empezar)
        for (int r = startRowIndex; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            Cell keyCell   = row.getCell(0, MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell valueCell = row.getCell(1, MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell typeCell  = row.getCell(2, MissingCellPolicy.CREATE_NULL_AS_BLANK);

            String key   = dataFormatter.formatCellValue(keyCell);
            String value = dataFormatter.formatCellValue(valueCell);
            String type  = dataFormatter.formatCellValue(typeCell);

            if (isTrimValues) {
                key   = (key   != null) ? key.trim()   : "";
                value = (value != null) ? value.trim() : "";
                type  = (type  != null) ? type.trim()  : "";
            }

            if (key == null || key.isEmpty()) continue;
            if (type == null || type.isEmpty()) {
                throw new BotCommandException("Tipo no definido para la clave: " + key + " (fila " + (r + 1) + ")");
            }

            // Normalizamos el tipo (sin acentos, minúsculas)
            String normType = normalize(type);

            switch (normType) {
                case "texto":
                    dict.put(key, new StringValue(value));
                    break;

                case "numero":
                    try {
                        Double num = Double.parseDouble(value);
                        dict.put(key, new NumberValue(num));
                    } catch (NumberFormatException e) {
                        throw new BotCommandException("Valor no numérico para la clave '" + key + "': " + value + " (fila " + (r + 1) + ")");
                    }
                    break;

                case "lista": {
                    // Hoja para la lista
                    Sheet listSheet = workbook.getSheet(value);
                    if (listSheet == null) {
                        throw new BotCommandException("Hoja para lista '" + value + "' no encontrada (clave '" + key + "')");
                    }

                    List<Value> items = parseListSheet(listSheet, dataFormatter, isTrimValues, START_ROW_LIST);

                    ListValue listValue = new ListValue();
                    listValue.set(items);
                    dict.put(key, listValue);
                    break;
                }

                case "diccionario": {
                    // Hoja para diccionario anidado
                    Sheet dictSheet = workbook.getSheet(value);
                    if (dictSheet == null) {
                        throw new BotCommandException("Hoja para diccionario '" + value + "' no encontrada (clave '" + key + "')");
                    }

                    // OJO: si tus diccionarios anidados NO tienen banner, cambiá START_ROW_DICT a 1
                    DictionaryValue nested =
                            parseConfig(workbook, dictSheet, dataFormatter, isTrimValues, START_ROW_DICT);

                    dict.put(key, nested);
                    break;
                }

                default:
                    throw new BotCommandException("Tipo de dato no soportado: '" + type + "' para clave: " + key + " (fila " + (r + 1) + ")");
            }
        }

        return new DictionaryValue(dict);
    }

    // ---------------- LIST SHEET PARSER ----------------
    private List<Value> parseListSheet(Sheet listSheet,
                                       DataFormatter dataFormatter,
                                       boolean isTrimValues,
                                       int startRowIndex) {
        List<Value> items = new ArrayList<>();

        for (int r = startRowIndex; r <= listSheet.getLastRowNum(); r++) {
            Row row = listSheet.getRow(r);
            if (row == null) continue;

            Cell v = row.getCell(0, MissingCellPolicy.CREATE_NULL_AS_BLANK);
            String listItem = dataFormatter.formatCellValue(v);
            if (isTrimValues && listItem != null) listItem = listItem.trim();

            if (listItem != null && !listItem.isEmpty()) {
                items.add(new StringValue(listItem));
            }
        }
        return items;
    }

    // ---------------- NORMALIZACIÓN DE TEXTO ----------------
    private static String normalize(String s) {
        if (s == null) return "";
        // toLowerCase + quitar acentos
        String lower = s.toLowerCase(Locale.ROOT);
        String norm  = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return norm.replaceAll("\\p{M}+", ""); // quita diacríticos
    }

    // ---------------- CLEANUP ----------------
    private void closeWorkbook(Workbook workbook) {
        if (workbook != null) {
            try { workbook.close(); }
            catch (IOException e) { System.err.println("Error cerrando workbook: " + e.getMessage()); }
        }
    }

    private void closeInputStream(FileInputStream inputStream) {
        if (inputStream != null) {
            try { inputStream.close(); }
            catch (IOException e) { System.err.println("Error cerrando input stream: " + e.getMessage()); }
        }
    }
}
