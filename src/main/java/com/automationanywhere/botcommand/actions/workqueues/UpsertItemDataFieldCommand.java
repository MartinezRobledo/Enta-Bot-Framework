/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.BotCommand;
import com.automationanywhere.botcommand.actions.workqueues.UpsertItemDataField;
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

public final class UpsertItemDataFieldCommand
implements BotCommand {
    private static final Logger logger = LogManager.getLogger(UpsertItemDataFieldCommand.class);
    private static final Messages MESSAGES_GENERIC = MessagesFactory.getMessages("com.automationanywhere.commandsdk.generic.messages");

    @Override
    @Deprecated
    public Optional<Value> execute(Map<String, Value> parameters, Map<String, Object> sessionMap) {
        return this.execute(null, parameters, sessionMap);
    }

    @Override
    public Optional<Value> execute(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        logger.traceEntry(() -> parameters != null ? parameters.entrySet().stream().filter(en -> !Arrays.asList(new String[0]).contains(en.getKey()) && en.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString() : null, () -> sessionMap != null ? sessionMap.toString() : null);
        UpsertItemDataField command = new UpsertItemDataField();
        HashMap convertedParameters = new HashMap();
        if (parameters.containsKey("filePath") && parameters.get("filePath") != null && parameters.get("filePath").get() != null) {
            convertedParameters.put("filePath", parameters.get("filePath").get());
            if (convertedParameters.get("filePath") != null && !(convertedParameters.get("filePath") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "filePath", "String", parameters.get("filePath").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("filePath") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "filePath"));
        }
        if (parameters.containsKey("identifyBy") && parameters.get("identifyBy") != null && parameters.get("identifyBy").get() != null) {
            convertedParameters.put("identifyBy", parameters.get("identifyBy").get());
            if (convertedParameters.get("identifyBy") != null && !(convertedParameters.get("identifyBy") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "identifyBy", "String", parameters.get("identifyBy").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("identifyBy") != null) {
            switch ((String)convertedParameters.get("identifyBy")) {
                case "id": {
                    if (!parameters.containsKey("itemId") || parameters.get("itemId") == null || parameters.get("itemId").get() == null) break;
                    convertedParameters.put("itemId", parameters.get("itemId").get());
                    if (convertedParameters.get("itemId") == null || convertedParameters.get("itemId") instanceof Double) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "itemId", "Double", parameters.get("itemId").get().getClass().getSimpleName()));
                }
                case "key": {
                    if (!parameters.containsKey("itemKey") || parameters.get("itemKey") == null || parameters.get("itemKey").get() == null) break;
                    convertedParameters.put("itemKey", parameters.get("itemKey").get());
                    if (convertedParameters.get("itemKey") == null || convertedParameters.get("itemKey") instanceof String) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "itemKey", "String", parameters.get("itemKey").get().getClass().getSimpleName()));
                }
                default: {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.InvalidOption", "identifyBy"));
                }
            }
        }
        if (parameters.containsKey("dataKey") && parameters.get("dataKey") != null && parameters.get("dataKey").get() != null) {
            convertedParameters.put("dataKey", parameters.get("dataKey").get());
            if (convertedParameters.get("dataKey") != null && !(convertedParameters.get("dataKey") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "dataKey", "String", parameters.get("dataKey").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("dataKey") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "dataKey"));
        }
        if (parameters.containsKey("dataValue") && parameters.get("dataValue") != null && parameters.get("dataValue").get() != null) {
            convertedParameters.put("dataValue", parameters.get("dataValue").get());
            if (convertedParameters.get("dataValue") != null && !(convertedParameters.get("dataValue") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "dataValue", "String", parameters.get("dataValue").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("dataValue") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "dataValue"));
        }
        try {
            Optional<Value> result = Optional.ofNullable(command.upsert((String)convertedParameters.get("filePath"), (String)convertedParameters.get("identifyBy"), (Double)convertedParameters.get("itemId"), (String)convertedParameters.get("itemKey"), (String)convertedParameters.get("dataKey"), (String)convertedParameters.get("dataValue")));
            return logger.traceExit(result);
        }
        catch (ClassCastException e) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.IllegalParameters", "upsert"));
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
