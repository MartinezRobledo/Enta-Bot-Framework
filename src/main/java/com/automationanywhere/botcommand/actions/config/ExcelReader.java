/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.config;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.data.impl.ListValue;
import com.automationanywhere.botcommand.data.impl.NumberValue;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.file.FileValidator;
import com.automationanywhere.core.security.SecureString;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.Normalizer;
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

public class ExcelReader {
    private static final int START_ROW_CONFIG = 2;
    private static final int START_ROW_LIST = 1;
    private static final int START_ROW_DICT = 2;

    public DictionaryValue action(String inputFilePath, Boolean isPasswordProtected, SecureString filePassword, Boolean isTrimValues) {
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
                throw new BotCommandException("No se encontr\u00f3 la hoja principal 'Config'");
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

    private DictionaryValue parseConfig(Workbook workbook, Sheet sheet, DataFormatter dataFormatter, boolean isTrimValues, int startRowIndex) {
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
                        throw new BotCommandException("Valor no num\u00e9rico para la clave '" + key + "': " + value + " (fila " + (r + 1) + ")");
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

    private List<Value> parseListSheet(Sheet listSheet, DataFormatter dataFormatter, boolean isTrimValues, int startRowIndex) {
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

    private static String normalize(String s) {
        if (s == null) {
            return "";
        }
        String lower = s.toLowerCase(Locale.ROOT);
        String norm = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return norm.replaceAll("\\p{M}+", "");
    }

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
