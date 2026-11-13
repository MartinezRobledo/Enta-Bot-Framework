/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.BotCommand;
import com.automationanywhere.botcommand.actions.workqueues.MaintainQueue;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.i18n.Messages;
import com.automationanywhere.commandsdk.i18n.MessagesFactory;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MaintainQueueCommand
implements BotCommand {
    private static final Logger logger = LogManager.getLogger(MaintainQueueCommand.class);
    private static final Messages MESSAGES_GENERIC = MessagesFactory.getMessages("com.automationanywhere.commandsdk.generic.messages");

    @Override
    @Deprecated
    public Optional<Value> execute(Map<String, Value> parameters, Map<String, Object> sessionMap) {
        return this.execute(null, parameters, sessionMap);
    }

    @Override
    public Optional<Value> execute(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        logger.traceEntry(() -> parameters != null ? parameters.entrySet().stream().filter(en -> !Arrays.asList(new String[0]).contains(en.getKey()) && en.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString() : null, () -> sessionMap != null ? sessionMap.toString() : null);
        MaintainQueue command = new MaintainQueue();
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
        if (parameters.containsKey("cutoffDate") && parameters.get("cutoffDate") != null && parameters.get("cutoffDate").get() != null) {
            convertedParameters.put("cutoffDate", parameters.get("cutoffDate").get());
            if (convertedParameters.get("cutoffDate") != null && !(convertedParameters.get("cutoffDate") instanceof Date)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "cutoffDate", "Date", parameters.get("cutoffDate").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("cutoffDate") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "cutoffDate"));
        }
        try {
            command.purge((String)convertedParameters.get("filePath"), (Date)convertedParameters.get("cutoffDate"));
            Optional result = Optional.empty();
            return logger.traceExit(result);
        }
        catch (ClassCastException e) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.IllegalParameters", "purge"));
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
