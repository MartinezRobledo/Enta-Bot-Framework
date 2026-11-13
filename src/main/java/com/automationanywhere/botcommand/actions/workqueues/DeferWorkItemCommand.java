/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.BotCommand;
import com.automationanywhere.botcommand.actions.workqueues.DeferWorkItem;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.i18n.Messages;
import com.automationanywhere.commandsdk.i18n.MessagesFactory;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DeferWorkItemCommand
implements BotCommand {
    private static final Logger logger = LogManager.getLogger(DeferWorkItemCommand.class);
    private static final Messages MESSAGES_GENERIC = MessagesFactory.getMessages("com.automationanywhere.commandsdk.generic.messages");

    @Override
    @Deprecated
    public Optional<Value> execute(Map<String, Value> parameters, Map<String, Object> sessionMap) {
        return this.execute(null, parameters, sessionMap);
    }

    @Override
    public Optional<Value> execute(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        logger.traceEntry(() -> parameters != null ? parameters.entrySet().stream().filter(en -> !Arrays.asList(new String[0]).contains(en.getKey()) && en.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString() : null, () -> sessionMap != null ? sessionMap.toString() : null);
        DeferWorkItem command = new DeferWorkItem();
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
        if (convertedParameters.get("identifyBy") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "identifyBy"));
        }
        if (convertedParameters.get("identifyBy") != null) {
            switch ((String)convertedParameters.get("identifyBy")) {
                case "id": {
                    if (parameters.containsKey("itemId") && parameters.get("itemId") != null && parameters.get("itemId").get() != null) {
                        convertedParameters.put("itemId", parameters.get("itemId").get());
                        if (convertedParameters.get("itemId") != null && !(convertedParameters.get("itemId") instanceof Double)) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "itemId", "Double", parameters.get("itemId").get().getClass().getSimpleName()));
                        }
                    }
                    if (convertedParameters.get("itemId") != null) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "itemId"));
                }
                case "key": {
                    if (parameters.containsKey("itemKey") && parameters.get("itemKey") != null && parameters.get("itemKey").get() != null) {
                        convertedParameters.put("itemKey", parameters.get("itemKey").get());
                        if (convertedParameters.get("itemKey") != null && !(convertedParameters.get("itemKey") instanceof String)) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "itemKey", "String", parameters.get("itemKey").get().getClass().getSimpleName()));
                        }
                    }
                    if (convertedParameters.get("itemKey") != null) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "itemKey"));
                }
                default: {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.InvalidOption", "identifyBy"));
                }
            }
        }
        if (parameters.containsKey("selectBy") && parameters.get("selectBy") != null && parameters.get("selectBy").get() != null) {
            convertedParameters.put("selectBy", parameters.get("selectBy").get());
            if (convertedParameters.get("selectBy") != null && !(convertedParameters.get("selectBy") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "selectBy", "String", parameters.get("selectBy").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("selectBy") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "selectBy"));
        }
        if (convertedParameters.get("selectBy") != null) {
            switch ((String)convertedParameters.get("selectBy")) {
                case "date": {
                    if (parameters.containsKey("deferUntil") && parameters.get("deferUntil") != null && parameters.get("deferUntil").get() != null) {
                        convertedParameters.put("deferUntil", parameters.get("deferUntil").get());
                        if (convertedParameters.get("deferUntil") != null && !(convertedParameters.get("deferUntil") instanceof ZonedDateTime)) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "deferUntil", "ZonedDateTime", parameters.get("deferUntil").get().getClass().getSimpleName()));
                        }
                    }
                    if (convertedParameters.get("deferUntil") != null) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "deferUntil"));
                }
                case "offset": {
                    if (parameters.containsKey("deferOffset") && parameters.get("deferOffset") != null && parameters.get("deferOffset").get() != null) {
                        convertedParameters.put("deferOffset", parameters.get("deferOffset").get());
                        if (convertedParameters.get("deferOffset") != null && !(convertedParameters.get("deferOffset") instanceof Double)) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "deferOffset", "Double", parameters.get("deferOffset").get().getClass().getSimpleName()));
                        }
                    }
                    if (convertedParameters.get("deferOffset") != null) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "deferOffset"));
                }
                default: {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.InvalidOption", "selectBy"));
                }
            }
        }
        try {
            command.execute((String)convertedParameters.get("filePath"), (String)convertedParameters.get("identifyBy"), (Double)convertedParameters.get("itemId"), (String)convertedParameters.get("itemKey"), (String)convertedParameters.get("selectBy"), (ZonedDateTime)convertedParameters.get("deferUntil"), (Double)convertedParameters.get("deferOffset"));
            Optional result = Optional.empty();
            return logger.traceExit(result);
        }
        catch (ClassCastException e) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.IllegalParameters", "execute"));
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
