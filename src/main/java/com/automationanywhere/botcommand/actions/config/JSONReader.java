/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.config;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.file.FileValidator;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONReader {
    private static final String INPUT_TYPE_FILE = "FILE";
    private static final String INPUT_TYPE_TEXT = "TEXT";
    private static final String PARSING_TYPE_ALL = "ALL";
    private static final String PARSING_TYPE_SPECIFIC = "SPECIFIC";

    public static Object getJSON(String jsonString) {
        try {
            return new JSONObject(jsonString);
        }
        catch (JSONException eObject) {
            try {
                return new JSONArray(jsonString);
            }
            catch (JSONException eArray) {
                throw new BotCommandException("Not a valid JSON object or array.");
            }
        }
    }

    public DictionaryValue action(String inputMethod, String jsonFilePath, String charsetName, String jsonText, String parsingMethod, List<Value> jsonPathSegments, Boolean isTrimValues) {
        try {
            Map<String, Value> jsonDictionary;
            if (jsonPathSegments == null || parsingMethod.equalsIgnoreCase(PARSING_TYPE_ALL)) {
                jsonPathSegments = List.of();
            }
            switch (inputMethod) {
                case "FILE": {
                    FileValidator fileValidator = new FileValidator(jsonFilePath);
                    String[] allowedExtensions = new String[]{"json"};
                    fileValidator.validateFile(allowedExtensions);
                    Charset charset = Charset.forName(charsetName);
                    String jsonContent = Files.readString(Path.of(jsonFilePath, new String[0]), charset);
                    jsonDictionary = this.parseJSON(jsonContent, jsonPathSegments, isTrimValues);
                    break;
                }
                case "TEXT": {
                    jsonDictionary = this.parseJSON(jsonText, jsonPathSegments, isTrimValues);
                    break;
                }
                default: {
                    throw new BotCommandException("Invalid parsing method: " + inputMethod);
                }
            }
            return new DictionaryValue(jsonDictionary);
        }
        catch (Exception e) {
            throw new BotCommandException("Error occurred: " + e.getMessage());
        }
    }

    private Map<String, Value> parseJSON(String jsonString, List<Value> jsonPathSegments, Boolean isTrimValues) {
        try {
            Object currentNode = JSONReader.getJSON(jsonString);
            LinkedHashMap<String, Value> jsonDictionary = new LinkedHashMap<String, Value>();
            for (Value pathSegment : jsonPathSegments) {
                String pathSegmentString = pathSegment.get().toString();
                if (!(currentNode instanceof JSONObject)) break;
                currentNode = ((JSONObject)currentNode).get(pathSegmentString);
            }
            this.traverseJson("", currentNode, jsonDictionary, false, isTrimValues);
            return jsonDictionary;
        }
        catch (Exception e) {
            throw new BotCommandException("Error parsing JSON: " + e.getMessage());
        }
    }

    private void traverseJson(String prefix, Object jsonNode, Map<String, Value> jsonDictionary, boolean isParentNodeObject, Boolean isTrimValues) {
        if (jsonNode instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject)jsonNode;
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonObject.get(key);
                this.traverseJson(prefix + key + ".", value, jsonDictionary, true, isTrimValues);
            }
        } else if (jsonNode instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray)jsonNode;
            for (int i = 0; i < jsonArray.length(); ++i) {
                Object arrayElement = jsonArray.get(i);
                if (isParentNodeObject) {
                    prefix = prefix.substring(0, prefix.length() - 1);
                }
                this.traverseJson(prefix + "[" + i + "].", arrayElement, jsonDictionary, false, isTrimValues);
            }
        } else {
            String currentKey = prefix.substring(0, prefix.length() - 1);
            String value = jsonNode == null ? "" : jsonNode.toString();
            value = isTrimValues != false ? value.strip() : value;
            jsonDictionary.put(currentKey, new StringValue(value));
        }
    }
}
