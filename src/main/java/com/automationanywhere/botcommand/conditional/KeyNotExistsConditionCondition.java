/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.conditional;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.Condition;
import com.automationanywhere.botcommand.conditional.KeyNotExistsCondition;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.i18n.Messages;
import com.automationanywhere.commandsdk.i18n.MessagesFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class KeyNotExistsConditionCondition
implements Condition {
    private static final Logger logger = LogManager.getLogger(KeyNotExistsConditionCondition.class);
    private static final Messages MESSAGES_GENERIC = MessagesFactory.getMessages("com.automationanywhere.commandsdk.generic.messages");

    @Override
    public boolean test(Map<String, Value> parameters) {
        return this.test(null, parameters, null);
    }

    @Override
    public boolean test(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        logger.traceEntry(() -> parameters != null ? parameters.entrySet().stream().filter(en -> !Arrays.asList(new String[0]).contains(en.getKey()) && en.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString() : null, () -> sessionMap != null ? sessionMap.toString() : null);
        KeyNotExistsCondition command = new KeyNotExistsCondition();
        HashMap convertedParameters = new HashMap();
        if (parameters.containsKey("dictionary") && parameters.get("dictionary") != null && parameters.get("dictionary").get() != null) {
            convertedParameters.put("dictionary", parameters.get("dictionary").get());
            if (convertedParameters.get("dictionary") != null && !(convertedParameters.get("dictionary") instanceof Map)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "dictionary", "Map", parameters.get("dictionary").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("dictionary") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "dictionary"));
        }
        if (parameters.containsKey("key") && parameters.get("key") != null && parameters.get("key").get() != null) {
            convertedParameters.put("key", parameters.get("key").get());
            if (convertedParameters.get("key") != null && !(convertedParameters.get("key") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "key", "String", parameters.get("key").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("key") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "key"));
        }
        try {
            boolean result = command.keyNotExists((Map)convertedParameters.get("dictionary"), (String)convertedParameters.get("key"));
            return logger.traceExit(result);
        }
        catch (ClassCastException e) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.IllegalParameters", "keyNotExists"));
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
}
