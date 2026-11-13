/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.device;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.BotCommand;
import com.automationanywhere.botcommand.actions.device.CopyFolder;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.i18n.Messages;
import com.automationanywhere.commandsdk.i18n.MessagesFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CopyFolderCommand
implements BotCommand {
    private static final Logger logger = LogManager.getLogger(CopyFolderCommand.class);
    private static final Messages MESSAGES_GENERIC = MessagesFactory.getMessages("com.automationanywhere.commandsdk.generic.messages");

    @Override
    @Deprecated
    public Optional<Value> execute(Map<String, Value> parameters, Map<String, Object> sessionMap) {
        return this.execute(null, parameters, sessionMap);
    }

    @Override
    public Optional<Value> execute(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        logger.traceEntry(() -> parameters != null ? parameters.entrySet().stream().filter(en -> !Arrays.asList(new String[0]).contains(en.getKey()) && en.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString() : null, () -> sessionMap != null ? sessionMap.toString() : null);
        CopyFolder command = new CopyFolder();
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
                    if (!parameters.containsKey("baseFolderPath") || parameters.get("baseFolderPath") == null || parameters.get("baseFolderPath").get() == null) break;
                    convertedParameters.put("baseFolderPath", parameters.get("baseFolderPath").get());
                    if (convertedParameters.get("baseFolderPath") == null || convertedParameters.get("baseFolderPath") instanceof String) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "baseFolderPath", "String", parameters.get("baseFolderPath").get().getClass().getSimpleName()));
                }
                case "ABSOLUTE": {
                    break;
                }
                default: {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.InvalidOption", "pathType"));
                }
            }
        }
        if (parameters.containsKey("sourceFolder") && parameters.get("sourceFolder") != null && parameters.get("sourceFolder").get() != null) {
            convertedParameters.put("sourceFolder", parameters.get("sourceFolder").get());
            if (convertedParameters.get("sourceFolder") != null && !(convertedParameters.get("sourceFolder") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "sourceFolder", "String", parameters.get("sourceFolder").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("sourceFolder") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "sourceFolder"));
        }
        if (parameters.containsKey("destinationFolder") && parameters.get("destinationFolder") != null && parameters.get("destinationFolder").get() != null) {
            convertedParameters.put("destinationFolder", parameters.get("destinationFolder").get());
            if (convertedParameters.get("destinationFolder") != null && !(convertedParameters.get("destinationFolder") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "destinationFolder", "String", parameters.get("destinationFolder").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("destinationFolder") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "destinationFolder"));
        }
        if (parameters.containsKey("createMissingFolders") && parameters.get("createMissingFolders") != null && parameters.get("createMissingFolders").get() != null) {
            convertedParameters.put("createMissingFolders", parameters.get("createMissingFolders").get());
            if (convertedParameters.get("createMissingFolders") != null && !(convertedParameters.get("createMissingFolders") instanceof Boolean)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "createMissingFolders", "Boolean", parameters.get("createMissingFolders").get().getClass().getSimpleName()));
            }
        }
        if (parameters.containsKey("overwriteExisting") && parameters.get("overwriteExisting") != null && parameters.get("overwriteExisting").get() != null) {
            convertedParameters.put("overwriteExisting", parameters.get("overwriteExisting").get());
            if (convertedParameters.get("overwriteExisting") != null && !(convertedParameters.get("overwriteExisting") instanceof Boolean)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "overwriteExisting", "Boolean", parameters.get("overwriteExisting").get().getClass().getSimpleName()));
            }
        }
        try {
            Optional<Value> result = Optional.ofNullable(command.action((String)convertedParameters.get("pathType"), (String)convertedParameters.get("baseFolderPath"), (String)convertedParameters.get("sourceFolder"), (String)convertedParameters.get("destinationFolder"), (Boolean)convertedParameters.get("createMissingFolders"), (Boolean)convertedParameters.get("overwriteExisting")));
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
