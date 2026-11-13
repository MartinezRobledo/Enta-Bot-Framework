/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.list;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.BotCommand;
import com.automationanywhere.botcommand.actions.list.AddItemAdvanced;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.ListValue;
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

public final class AddItemAdvancedCommand
implements BotCommand {
    private static final Logger logger = LogManager.getLogger(AddItemAdvancedCommand.class);
    private static final Messages MESSAGES_GENERIC = MessagesFactory.getMessages("com.automationanywhere.commandsdk.generic.messages");

    @Override
    @Deprecated
    public Optional<Value> execute(Map<String, Value> parameters, Map<String, Object> sessionMap) {
        return this.execute(null, parameters, sessionMap);
    }

    @Override
    public Optional<Value> execute(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        logger.traceEntry(() -> parameters != null ? parameters.entrySet().stream().filter(en -> !Arrays.asList(new String[0]).contains(en.getKey()) && en.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString() : null, () -> sessionMap != null ? sessionMap.toString() : null);
        AddItemAdvanced command = new AddItemAdvanced();
        HashMap convertedParameters = new HashMap();
        if (parameters.containsKey("inputList") && parameters.get("inputList") != null && parameters.get("inputList").get() != null) {
            convertedParameters.put("inputList", parameters.get("inputList").get());
            if (convertedParameters.get("inputList") != null && !(convertedParameters.get("inputList") instanceof List)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "inputList", "List", parameters.get("inputList").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("inputList") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "inputList"));
        }
        if (parameters.containsKey("insertPosition") && parameters.get("insertPosition") != null && parameters.get("insertPosition").get() != null) {
            convertedParameters.put("insertPosition", parameters.get("insertPosition").get());
            if (convertedParameters.get("insertPosition") != null && !(convertedParameters.get("insertPosition") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "insertPosition", "String", parameters.get("insertPosition").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("insertPosition") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "insertPosition"));
        }
        if (convertedParameters.get("insertPosition") != null) {
            switch ((String)convertedParameters.get("insertPosition")) {
                case "BEGINNING": {
                    break;
                }
                case "END": {
                    break;
                }
                case "INDEX": {
                    if (!parameters.containsKey("index") || parameters.get("index") == null || parameters.get("index").get() == null) break;
                    convertedParameters.put("index", parameters.get("index").get());
                    if (convertedParameters.get("index") == null || convertedParameters.get("index") instanceof Double) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "index", "Double", parameters.get("index").get().getClass().getSimpleName()));
                }
                default: {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.InvalidOption", "insertPosition"));
                }
            }
        }
        if (parameters.containsKey("itemType") && parameters.get("itemType") != null && parameters.get("itemType").get() != null) {
            convertedParameters.put("itemType", parameters.get("itemType").get());
            if (convertedParameters.get("itemType") != null && !(convertedParameters.get("itemType") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "itemType", "String", parameters.get("itemType").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("itemType") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "itemType"));
        }
        if (convertedParameters.get("itemType") != null) {
            switch ((String)convertedParameters.get("itemType")) {
                case "STRING": {
                    if (!parameters.containsKey("stringItem") || parameters.get("stringItem") == null || parameters.get("stringItem").get() == null) break;
                    convertedParameters.put("stringItem", parameters.get("stringItem").get());
                    if (parameters.get("stringItem") instanceof Value) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "stringItem", "Value", parameters.get("stringItem").get().getClass().getSimpleName()));
                }
                case "NUMBER": {
                    if (!parameters.containsKey("numberItem") || parameters.get("numberItem") == null || parameters.get("numberItem").get() == null) break;
                    convertedParameters.put("numberItem", parameters.get("numberItem").get());
                    if (parameters.get("numberItem") instanceof Value) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "numberItem", "Value", parameters.get("numberItem").get().getClass().getSimpleName()));
                }
                case "BOOLEAN": {
                    if (!parameters.containsKey("booleanItem") || parameters.get("booleanItem") == null || parameters.get("booleanItem").get() == null) break;
                    convertedParameters.put("booleanItem", parameters.get("booleanItem").get());
                    if (parameters.get("booleanItem") instanceof Value) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "booleanItem", "Value", parameters.get("booleanItem").get().getClass().getSimpleName()));
                }
                case "DICTIONARY": {
                    if (!parameters.containsKey("dictionaryItem") || parameters.get("dictionaryItem") == null || parameters.get("dictionaryItem").get() == null) break;
                    convertedParameters.put("dictionaryItem", parameters.get("dictionaryItem").get());
                    if (parameters.get("dictionaryItem") instanceof Value) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "dictionaryItem", "Value", parameters.get("dictionaryItem").get().getClass().getSimpleName()));
                }
                case "LIST": {
                    if (!parameters.containsKey("listItem") || parameters.get("listItem") == null || parameters.get("listItem").get() == null) break;
                    convertedParameters.put("listItem", parameters.get("listItem").get());
                    if (parameters.get("listItem") instanceof Value) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "listItem", "Value", parameters.get("listItem").get().getClass().getSimpleName()));
                }
                case "DATETIME": {
                    if (!parameters.containsKey("dateTimeItem") || parameters.get("dateTimeItem") == null || parameters.get("dateTimeItem").get() == null) break;
                    convertedParameters.put("dateTimeItem", parameters.get("dateTimeItem").get());
                    if (parameters.get("dateTimeItem") instanceof Value) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "dateTimeItem", "Value", parameters.get("dateTimeItem").get().getClass().getSimpleName()));
                }
                default: {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.InvalidOption", "itemType"));
                }
            }
        }
        try {
            Optional<Value> result = Optional.ofNullable(command.action((List)convertedParameters.get("inputList"), (String)convertedParameters.get("insertPosition"), (Double)convertedParameters.get("index"), (String)convertedParameters.get("itemType"), parameters.get("stringItem"), parameters.get("numberItem"), parameters.get("booleanItem"), parameters.get("dictionaryItem"), parameters.get("listItem"), parameters.get("dateTimeItem")));
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
