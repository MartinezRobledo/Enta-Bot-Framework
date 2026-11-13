/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.device;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.BotCommand;
import com.automationanywhere.botcommand.actions.device.CreateFolders;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.i18n.Messages;
import com.automationanywhere.commandsdk.i18n.MessagesFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CreateFoldersCommand
implements BotCommand {
    private static final Logger logger = LogManager.getLogger(CreateFoldersCommand.class);
    private static final Messages MESSAGES_GENERIC = MessagesFactory.getMessages("com.automationanywhere.commandsdk.generic.messages");

    @Override
    @Deprecated
    public Optional<Value> execute(Map<String, Value> parameters, Map<String, Object> sessionMap) {
        return this.execute(null, parameters, sessionMap);
    }

    @Override
    public Optional<Value> execute(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        logger.traceEntry(() -> parameters != null ? parameters.entrySet().stream().filter(en -> !Arrays.asList(new String[0]).contains(en.getKey()) && en.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString() : null, () -> sessionMap != null ? sessionMap.toString() : null);
        CreateFolders command = new CreateFolders();
        HashMap convertedParameters = new HashMap();
        if (parameters.containsKey("pathType") && parameters.get("pathType") != null && parameters.get("pathType").get() != null) {
            convertedParameters.put("pathType", parameters.get("pathType").get());
            if (convertedParameters.get("pathType") != null && !(convertedParameters.get("pathType") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "pathType", "String", parameters.get("pathType").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("pathType") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "pathType"));
        }
        if (convertedParameters.get("pathType") != null) {
            switch ((String)convertedParameters.get("pathType")) {
                case "RELATIVE": {
                    if (parameters.containsKey("baseFolderPath") && parameters.get("baseFolderPath") != null && parameters.get("baseFolderPath").get() != null) {
                        convertedParameters.put("baseFolderPath", parameters.get("baseFolderPath").get());
                        if (convertedParameters.get("baseFolderPath") != null && !(convertedParameters.get("baseFolderPath") instanceof String)) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "baseFolderPath", "String", parameters.get("baseFolderPath").get().getClass().getSimpleName()));
                        }
                    }
                    if (convertedParameters.get("baseFolderPath") != null) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "baseFolderPath"));
                }
                case "ABSOLUTE": {
                    break;
                }
                default: {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.InvalidOption", "pathType"));
                }
            }
        }
        if (parameters.containsKey("folderPaths") && parameters.get("folderPaths") != null && parameters.get("folderPaths").get() != null) {
            convertedParameters.put("folderPaths", parameters.get("folderPaths").get());
            if (convertedParameters.get("folderPaths") != null && !(convertedParameters.get("folderPaths") instanceof List)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "folderPaths", "List", parameters.get("folderPaths").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("folderPaths") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "folderPaths"));
        }
        if (parameters.containsKey("overwriteExisting") && parameters.get("overwriteExisting") != null && parameters.get("overwriteExisting").get() != null) {
            convertedParameters.put("overwriteExisting", parameters.get("overwriteExisting").get());
            if (convertedParameters.get("overwriteExisting") != null && !(convertedParameters.get("overwriteExisting") instanceof Boolean)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "overwriteExisting", "Boolean", parameters.get("overwriteExisting").get().getClass().getSimpleName()));
            }
        }
        if (parameters.containsKey("appendToExistingDictionary") && parameters.get("appendToExistingDictionary") != null && parameters.get("appendToExistingDictionary").get() != null) {
            convertedParameters.put("appendToExistingDictionary", parameters.get("appendToExistingDictionary").get());
            if (convertedParameters.get("appendToExistingDictionary") != null && !(convertedParameters.get("appendToExistingDictionary") instanceof Boolean)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "appendToExistingDictionary", "Boolean", parameters.get("appendToExistingDictionary").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("appendToExistingDictionary") != null && ((Boolean)convertedParameters.get("appendToExistingDictionary")).booleanValue()) {
            if (parameters.containsKey("configDictionary") && parameters.get("configDictionary") != null && parameters.get("configDictionary").get() != null) {
                convertedParameters.put("configDictionary", parameters.get("configDictionary").get());
                if (convertedParameters.get("configDictionary") != null && !(convertedParameters.get("configDictionary") instanceof Map)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "configDictionary", "Map", parameters.get("configDictionary").get().getClass().getSimpleName()));
                }
            }
            if (parameters.containsKey("suffix") && parameters.get("suffix") != null && parameters.get("suffix").get() != null) {
                convertedParameters.put("suffix", parameters.get("suffix").get());
                if (convertedParameters.get("suffix") != null && !(convertedParameters.get("suffix") instanceof String)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "suffix", "String", parameters.get("suffix").get().getClass().getSimpleName()));
                }
            }
            if (parameters.containsKey("prefix") && parameters.get("prefix") != null && parameters.get("prefix").get() != null) {
                convertedParameters.put("prefix", parameters.get("prefix").get());
                if (convertedParameters.get("prefix") != null && !(convertedParameters.get("prefix") instanceof String)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "prefix", "String", parameters.get("prefix").get().getClass().getSimpleName()));
                }
            }
        }
        try {
            Optional<Value> result = Optional.ofNullable(command.action((String)convertedParameters.get("pathType"), (String)convertedParameters.get("baseFolderPath"), (List)convertedParameters.get("folderPaths"), (Boolean)convertedParameters.get("overwriteExisting"), (Boolean)convertedParameters.get("appendToExistingDictionary"), (Map)convertedParameters.get("configDictionary"), (String)convertedParameters.get("suffix"), (String)convertedParameters.get("prefix")));
            return logger.traceExit(result);
        }
        catch (ClassCastException e) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.IllegalParameters", "action"));
        }
        catch (BotCommandException e) {
            logger.fatal(e.getMessage(), (Throwable)e);
            throw e;
        }
        catch (Throwable e) {
            logger.fatal(e.getMessage(), e);
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.NotBotCommandException", e.getMessage()), e);
        }
    }

    @Override
    public Map<String, Value> executeAndReturnMany(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        return null;
    }
}
