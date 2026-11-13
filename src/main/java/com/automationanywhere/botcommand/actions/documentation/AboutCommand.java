/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.documentation;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.BotCommand;
import com.automationanywhere.botcommand.actions.documentation.About;
import com.automationanywhere.botcommand.data.Value;
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

public final class AboutCommand
implements BotCommand {
    private static final Logger logger = LogManager.getLogger(AboutCommand.class);
    private static final Messages MESSAGES_GENERIC = MessagesFactory.getMessages("com.automationanywhere.commandsdk.generic.messages");

    @Override
    @Deprecated
    public Optional<Value> execute(Map<String, Value> parameters, Map<String, Object> sessionMap) {
        return this.execute(null, parameters, sessionMap);
    }

    @Override
    public Optional<Value> execute(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        logger.traceEntry(() -> parameters != null ? parameters.entrySet().stream().filter(en -> !Arrays.asList(new String[0]).contains(en.getKey()) && en.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString() : null, () -> sessionMap != null ? sessionMap.toString() : null);
        About command = new About();
        HashMap convertedParameters = new HashMap();
        if (parameters.containsKey("aboutDescription") && parameters.get("aboutDescription") != null && parameters.get("aboutDescription").get() != null) {
            convertedParameters.put("aboutDescription", parameters.get("aboutDescription").get());
            if (convertedParameters.get("aboutDescription") != null && !(convertedParameters.get("aboutDescription") instanceof Map)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "aboutDescription", "Map", parameters.get("aboutDescription").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("aboutDescription") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "aboutDescription"));
        }
        try {
            command.setAboutDescription((Map)convertedParameters.get("aboutDescription"));
        }
        catch (ClassCastException e) {
            Object[] objectArray = new Object[3];
            objectArray[0] = "aboutDescription";
            objectArray[1] = "Map<String, Value>";
            objectArray[2] = parameters.get("aboutDescription") != null ? (parameters.get("aboutDescription").get() != null ? parameters.get("aboutDescription").get().getClass().toString() : "null") : "null";
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
        }
        try {
            command.action();
            Optional result = Optional.empty();
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
