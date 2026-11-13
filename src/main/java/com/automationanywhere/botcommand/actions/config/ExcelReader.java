package com.automationanywhere.botcommand.actions.config;

import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.data.impl.ListValue;
import com.automationanywhere.botcommand.data.impl.NumberValue;
import com.automationanywhere.botcommand.data.impl.StringValue;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

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
            DictionaryValue result;
            FileValidator fileValidator = new FileValidator(inputFilePath);
            String[] allowedExtensions = new String[]{"xlsm"};
            fileValidator.validateFile(allowedExtensions);
            File file = new File(inputFilePath);
            inputStream = new FileInputStream(file);
            String pwd = isPasswordProtected != null && isPasswordProtected != false ? filePassword.getInsecureString() : null;
            workbook = pwd != null && !pwd.isEmpty() ? WorkbookFactory.create(inputStream, pwd) : WorkbookFactory.create(inputStream);
            Sheet configSheet = workbook.getSheet("Config");
            if (configSheet == null) {
                throw new BotCommandException("No se encontró la hoja principal 'Config'");
            }
            DataFormatter dataFormatter = new DataFormatter();
            DictionaryValue dictionaryValue = result = this.parseConfig(workbook, configSheet, dataFormatter, isTrimValues != null && isTrimValues != false, 2);
            this.closeWorkbook(workbook);
            this.closeInputStream(inputStream);
            return dictionaryValue;
        }
        catch (Exception e) {
            try {
                throw new BotCommandException("Error leyendo archivo Excel: " + e.getMessage(), e);
            }
            catch (Throwable throwable) {
                this.closeWorkbook(workbook);
                this.closeInputStream(inputStream);
                throw throwable;
            }
        }
    }

    // ---------------- PARSER GENERAL (SHEET DE TIPO DICCIONARIO) ----------------
    private DictionaryValue parseConfig(Workbook workbook,
                                        Sheet sheet,
                                        DataFormatter dataFormatter,
                                        boolean isTrimValues,
                                        int startRowIndex) {
        LinkedHashMap<String, Value> dict = new LinkedHashMap<String, Value>();
        block14: for (int r = startRowIndex; r <= sheet.getLastRowNum(); ++r) {
            String normType;
            Row row = sheet.getRow(r);
            if (row == null) continue;
            Cell keyCell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell valueCell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell typeCell = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            String key = dataFormatter.formatCellValue(keyCell);
            String value = dataFormatter.formatCellValue(valueCell);
            String type = dataFormatter.formatCellValue(typeCell);
            if (isTrimValues) {
                key = key != null ? key.trim() : "";
                value = value != null ? value.trim() : "";
                String string = type = type != null ? type.trim() : "";
            }
            if (key == null || key.isEmpty()) continue;
            if (type == null || type.isEmpty()) {
                throw new BotCommandException("Tipo no definido para la clave: " + key + " (fila " + (r + 1) + ")");
            }
            switch (normType = ExcelReader.normalize(type)) {
                case "texto": {
                    dict.put(key, new StringValue(value));
                    continue block14;
                }
                case "numero": {
                    try {
                        Double num = Double.parseDouble(value);
                        dict.put(key, new NumberValue(num));
                        continue block14;
                    }
                    catch (NumberFormatException e) {
                        throw new BotCommandException("Valor no numérico para la clave '" + key + "': " + value + " (fila " + (r + 1) + ")");
                    }
                }
                case "lista": {
                    Sheet listSheet = workbook.getSheet(value);
                    if (listSheet == null) {
                        throw new BotCommandException("Hoja para lista '" + value + "' no encontrada (clave '" + key + "')");
                    }
                    List<Value> items = this.parseListSheet(listSheet, dataFormatter, isTrimValues, 1);
                    ListValue listValue = new ListValue();
                    listValue.set(items);
                    dict.put(key, listValue);
                    continue block14;
                }
                case "diccionario": {
                    Sheet dictSheet = workbook.getSheet(value);
                    if (dictSheet == null) {
                        throw new BotCommandException("Hoja para diccionario '" + value + "' no encontrada (clave '" + key + "')");
                    }
                    DictionaryValue nested = this.parseConfig(workbook, dictSheet, dataFormatter, isTrimValues, 2);
                    dict.put(key, nested);
                    continue block14;
                }
                default: {
                    throw new BotCommandException("Tipo de dato no soportado: '" + type + "' para clave: " + key + " (fila " + (r + 1) + ")");
                }
            }
        }
        return new DictionaryValue(dict);
    }

    // ---------------- LIST SHEET PARSER ----------------
    private List<Value> parseListSheet(Sheet listSheet,
                                       DataFormatter dataFormatter,
                                       boolean isTrimValues,
                                       int startRowIndex) {
        ArrayList<Value> items = new ArrayList<Value>();
        for (int r = startRowIndex; r <= listSheet.getLastRowNum(); ++r) {
            Row row = listSheet.getRow(r);
            if (row == null) continue;
            Cell v = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            String listItem = dataFormatter.formatCellValue(v);
            if (isTrimValues && listItem != null) {
                listItem = listItem.trim();
            }
            if (listItem == null || listItem.isEmpty()) continue;
            items.add(new StringValue(listItem));
        }
        return items;
    }

    // ---------------- NORMALIZACIÓN DE TEXTO ----------------
    private static String normalize(String s) {
        if (s == null) {
            return "";
        }
        String lower = s.toLowerCase(Locale.ROOT);
        String norm = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return norm.replaceAll("\\p{M}+", "");
    }

    // ---------------- CLEANUP ----------------
    private void closeWorkbook(Workbook workbook) {
        if (workbook != null) {
            try {
                workbook.close();
            }
            catch (IOException e) {
                System.err.println("Error cerrando workbook: " + e.getMessage());
            }
        }
    }

    private void closeInputStream(FileInputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                System.err.println("Error cerrando input stream: " + e.getMessage());
            }
        }
    }
}
