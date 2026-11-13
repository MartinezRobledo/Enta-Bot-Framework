/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.utilities.logger;

import botframework.shaded.commons.io.FilenameUtils;
import botframework.shaded.commons.io.IOUtils;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.BooleanValue;
import com.automationanywhere.botcommand.data.impl.CredentialObject;
import com.automationanywhere.botcommand.data.impl.DateTimeValue;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.data.impl.ListValue;
import com.automationanywhere.botcommand.data.impl.NumberValue;
import com.automationanywhere.botcommand.data.impl.RecordValue;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.data.impl.TableValue;
import com.automationanywhere.botcommand.data.model.record.Record;
import com.automationanywhere.botcommand.data.model.table.Table;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.text.StringEscapeUtils;

public class HTMLGenerator {
    private static final String NULL = "NULL";
    private static final String TEMPLATE_PATH = "/templates/variables.html";

    public static String generateHTML(Map<String, Value> valueMap) {
        if (valueMap == null || valueMap.isEmpty()) {
            return "";
        }
        int variableCount = valueMap.size();
        return "<span class='variable-count'>" + variableCount + " variable" + (variableCount > 1 ? "s" : "") + "</span>";
    }

    public static String generateVariableFile(Map<String, Value> valueMap, String folderPath, String logEventId) {
        if (valueMap == null || valueMap.isEmpty()) {
            return "";
        }
        String filename = "variables_" + logEventId + ".html";
        Path filePath = Paths.get(folderPath, filename);
        StringBuilder htmlBuilder = new StringBuilder();
        try {
            byte[] templateBytes = IOUtils.toByteArray(Objects.requireNonNull(HTMLGenerator.class.getResourceAsStream(TEMPLATE_PATH)));
            String headerTemplate = new String(templateBytes, StandardCharsets.UTF_8);
            htmlBuilder.append(headerTemplate);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!htmlBuilder.toString().contains("<div class='container'>")) {
            htmlBuilder.append("<div class='container'>\n");
        }
        for (Map.Entry<String, Value> entry : valueMap.entrySet()) {
            HTMLGenerator.appendVariableToHTML(entry.getKey(), entry.getValue(), htmlBuilder);
        }
        htmlBuilder.append("</div>\n").append("</body>\n").append("</html>");
        System.out.println("Generated HTML file: " + String.valueOf(filePath));
        try {
            Files.write(filePath, htmlBuilder.toString().getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
        }
        catch (IOException e) {
            System.err.println("Error writing variables HTML file: " + e.getMessage());
            return "";
        }
        return "variables/" + filename;
    }

    private static void appendVariableToHTML(String name, Value value, StringBuilder htmlBuilder) {
        boolean isTopLevel;
        name = name != null ? name : NULL;
        boolean bl = isTopLevel = name != null && !name.startsWith("Index ");
        if (isTopLevel) {
            htmlBuilder.append("<div class='variable-card'>\n").append("<div class='variable-name'>").append(StringEscapeUtils.escapeHtml4(name)).append("</div>\n");
        }
        if (value == null) {
            htmlBuilder.append("<div class='variable-type'>NULL</div>\n").append("<div class='variable-value'>NULL</div>\n");
        } else {
            String classname = value.getClass().getSimpleName();
            String variableType = classname.toLowerCase().replace("object", "").replace("value", "").toUpperCase();
            if (isTopLevel) {
                htmlBuilder.append("<div class='variable-type'>").append(StringEscapeUtils.escapeHtml4(variableType)).append("</div>\n");
            }
            switch (variableType) {
                case "LIST": {
                    HTMLGenerator.appendListValue((ListValue)value, htmlBuilder);
                    break;
                }
                case "RECORD": {
                    HTMLGenerator.appendRecordValue((RecordValue)value, htmlBuilder);
                    break;
                }
                case "TABLE": {
                    HTMLGenerator.appendTableValue((TableValue)value, htmlBuilder);
                    break;
                }
                case "DICTIONARY": {
                    HTMLGenerator.appendDictionaryValue((DictionaryValue)value, htmlBuilder);
                    break;
                }
                default: {
                    HTMLGenerator.appendScalarValue(value, htmlBuilder);
                }
            }
        }
        if (isTopLevel) {
            htmlBuilder.append("</div>\n");
        }
    }

    private static void appendListValue(ListValue listValue, StringBuilder htmlBuilder) {
        java.util.List<Value> list = listValue.get();
        htmlBuilder.append("<details>\n").append("<summary>List: ").append(list.size()).append(" items</summary>\n");
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); ++i) {
                Value itemValue = (Value)list.get(i);
                String indexName = "Index " + i;
                htmlBuilder.append("<div class='variable-card'>\n").append("<div class='variable-name'>").append(indexName).append("</div>\n");
                if (itemValue == null) {
                    htmlBuilder.append("<div class='variable-value'>NULL</div>\n");
                } else {
                    String itemType = itemValue.getClass().getSimpleName().toLowerCase().replace("object", "").replace("value", "").toUpperCase();
                    htmlBuilder.append("<div class='variable-type'>").append(StringEscapeUtils.escapeHtml4(itemType)).append("</div>\n");
                    HTMLGenerator.appendVariableValueWithoutCard(itemValue, htmlBuilder);
                }
                htmlBuilder.append("</div>\n");
            }
        }
        htmlBuilder.append("</details>\n");
    }

    private static void appendVariableValueWithoutCard(Value value, StringBuilder htmlBuilder) {
        String variableType;
        if (value == null) {
            htmlBuilder.append("<div class='variable-value'>NULL</div>\n");
            return;
        }
        switch (variableType = value.getClass().getSimpleName().toLowerCase().replace("object", "").replace("value", "").toUpperCase()) {
            case "LIST": {
                HTMLGenerator.appendListValue((ListValue)value, htmlBuilder);
                break;
            }
            case "RECORD": {
                HTMLGenerator.appendRecordValue((RecordValue)value, htmlBuilder);
                break;
            }
            case "TABLE": {
                HTMLGenerator.appendTableValue((TableValue)value, htmlBuilder);
                break;
            }
            case "DICTIONARY": {
                HTMLGenerator.appendDictionaryValue((DictionaryValue)value, htmlBuilder);
                break;
            }
            default: {
                HTMLGenerator.appendScalarValue(value, htmlBuilder);
            }
        }
    }

    private static void appendDictionaryValue(DictionaryValue dictionaryValue, StringBuilder htmlBuilder) {
        Map<String, Value> dictionaryMap = dictionaryValue.get();
        htmlBuilder.append("<details>\n").append("<summary>Dictionary: ").append(dictionaryMap.size()).append(" entries</summary>\n");
        for (Map.Entry entry : dictionaryMap.entrySet()) {
            String key = (String)entry.getKey();
            Value val = (Value)entry.getValue();
            htmlBuilder.append("<div class='variable-card'>\n").append("<div class='variable-name'>").append(StringEscapeUtils.escapeHtml4(key)).append("</div>\n");
            if (val == null) {
                htmlBuilder.append("<div class='variable-value'>NULL</div>\n");
            } else {
                String valType = val.getClass().getSimpleName().toLowerCase().replace("object", "").replace("value", "").toUpperCase();
                htmlBuilder.append("<div class='variable-type'>").append(StringEscapeUtils.escapeHtml4(valType)).append("</div>\n");
                HTMLGenerator.appendVariableValueWithoutCard(val, htmlBuilder);
            }
            htmlBuilder.append("</div>\n");
        }
        htmlBuilder.append("</details>\n");
    }

    private static void appendRecordValue(RecordValue recordValue, StringBuilder htmlBuilder) {
        Record record = recordValue.get();
        htmlBuilder.append("<details class='full-width'>\n").append("<summary>Record: ").append(record.getSchema().size()).append(" columns</summary>\n").append("<div class='table-container'>\n").append("<table>\n").append("<tr>\n");
        for (int i = 0; i < record.getSchema().size(); ++i) {
            htmlBuilder.append("<th>").append(StringEscapeUtils.escapeHtml4(record.getSchema().get(i).getName())).append("</th>\n");
        }
        htmlBuilder.append("</tr>\n");
        htmlBuilder.append("<tr>\n");
        for (Value v : record.getValues()) {
            htmlBuilder.append("<td>\n");
            if (v == null) {
                htmlBuilder.append(NULL);
            } else {
                HTMLGenerator.appendValueInCell(v, htmlBuilder);
            }
            htmlBuilder.append("</td>\n");
        }
        htmlBuilder.append("</tr>\n").append("</table>\n").append("</div>\n").append("</details>\n");
    }

    private static void appendTableValue(TableValue tableValue, StringBuilder htmlBuilder) {
        int i;
        Table table = tableValue.get();
        htmlBuilder.append("<details class='full-width'>\n").append("<summary>Table: ").append(table.getRows().size()).append(" rows x ").append(table.getSchema().size()).append(" columns</summary>\n").append("<div class='table-container'>\n").append("<table>\n").append("<tr>\n");
        for (i = 0; i < table.getSchema().size(); ++i) {
            htmlBuilder.append("<th>").append(StringEscapeUtils.escapeHtml4(table.getSchema().get(i).getName())).append("</th>\n");
        }
        htmlBuilder.append("</tr>\n");
        for (i = 0; i < table.getRows().size(); ++i) {
            htmlBuilder.append("<tr>\n");
            for (int j = 0; j < table.getRows().get(i).getValues().size(); ++j) {
                Value cellValue = table.getRows().get(i).getValues().get(j);
                htmlBuilder.append("<td>\n");
                if (cellValue == null) {
                    htmlBuilder.append(NULL);
                } else {
                    HTMLGenerator.appendValueInCell(cellValue, htmlBuilder);
                }
                htmlBuilder.append("</td>\n");
            }
            htmlBuilder.append("</tr>\n");
        }
        htmlBuilder.append("</table>\n").append("</div>\n").append("</details>\n");
    }

    private static void appendValueInCell(Value value, StringBuilder htmlBuilder) {
        if (value == null) {
            htmlBuilder.append(NULL);
            return;
        }
        if (value instanceof ListValue) {
            HTMLGenerator.appendListValue((ListValue)value, htmlBuilder);
        } else if (value instanceof DictionaryValue) {
            HTMLGenerator.appendDictionaryValue((DictionaryValue)value, htmlBuilder);
        } else if (value instanceof RecordValue) {
            HTMLGenerator.appendRecordValue((RecordValue)value, htmlBuilder);
        } else if (value instanceof TableValue) {
            HTMLGenerator.appendTableValue((TableValue)value, htmlBuilder);
        } else {
            HTMLGenerator.appendScalarValue(value, htmlBuilder);
        }
    }

    private static void appendScalarValue(Value scalarValue, StringBuilder htmlBuilder) {
        String objectStringvalue = "";
        if (scalarValue instanceof CredentialObject) {
            objectStringvalue = ((CredentialObject)scalarValue).get().getInsecureString();
        } else if (scalarValue instanceof NumberValue || scalarValue instanceof StringValue || scalarValue instanceof DateTimeValue || scalarValue instanceof BooleanValue) {
            objectStringvalue = scalarValue.get().toString();
        }
        htmlBuilder.append("<div class='variable-value'>").append(StringEscapeUtils.escapeHtml4(objectStringvalue)).append("</div>\n");
    }

    public static String getScreenshotHTML(String screenshotPath) {
        if (screenshotPath == null || screenshotPath.isEmpty()) {
            return "";
        }
        String relativePath = "screenshots/" + FilenameUtils.getName(screenshotPath);
        return "<a href='" + relativePath + "' target='_blank' class='img-link'>View Screenshot</a>";
    }
}
