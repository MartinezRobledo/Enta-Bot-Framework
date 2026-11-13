/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.device;

import botframework.shaded.commons.io.FileUtils;
import botframework.shaded.commons.io.FilenameUtils;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateFolders {
    private static final String PATH_RELATIVE = "RELATIVE";
    private static final String PATH_ABSOLUTE = "ABSOLUTE";

    public DictionaryValue action(String pathType, String baseFolderPath, List<Value> folderPaths, Boolean overwriteExisting, Boolean appendToExistingDictionary, Map<String, Value> configDictionary, String suffix, String prefix) {
        try {
            HashMap<String, Value> returnDictionary = new HashMap<String, Value>();
            if (configDictionary == null) {
                configDictionary = new HashMap<String, Value>();
            }
            if (suffix == null) {
                suffix = "";
            }
            if (prefix == null) {
                suffix = "";
            }
            for (Value folderPathValue : folderPaths) {
                Path directoryPath;
                String folderPath = Paths.get(folderPathValue.get().toString(), new String[0]).toString();
                if (PATH_RELATIVE.equalsIgnoreCase(pathType)) {
                    folderPath = Paths.get(baseFolderPath, folderPath).toString();
                }
                if (Files.exists(directoryPath = Paths.get(folderPath, new String[0]), new LinkOption[0]) && overwriteExisting.booleanValue()) {
                    FileUtils.deleteDirectory(directoryPath.toFile());
                }
                Files.createDirectories(directoryPath, new FileAttribute[0]);
                String key = prefix + FilenameUtils.getName(folderPath) + suffix;
                if (appendToExistingDictionary.booleanValue()) {
                    configDictionary.put(key, new StringValue(directoryPath.toString()));
                }
                returnDictionary.put(key, new StringValue(directoryPath.toString()));
            }
            return new DictionaryValue(returnDictionary);
        }
        catch (Exception e) {
            throw new BotCommandException(e.getMessage());
        }
    }
}
