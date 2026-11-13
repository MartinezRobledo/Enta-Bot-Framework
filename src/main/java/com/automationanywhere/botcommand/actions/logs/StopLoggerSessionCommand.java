/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.logs;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.BotCommand;
import com.automationanywhere.botcommand.actions.logs.StopLoggerSession;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.SessionValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.logger.CustomLogger;
import com.automationanywhere.commandsdk.i18n.Messages;
import com.automationanywhere.commandsdk.i18n.MessagesFactory;
import com.automationanywhere.toolchain.runtime.session.CloseableSessionObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class StopLoggerSessionCommand
implements BotCommand {
    private static final Logger logger = LogManager.getLogger(StopLoggerSessionCommand.class);
    private static final Messages MESSAGES_GENERIC = MessagesFactory.getMessages("com.automationanywhere.commandsdk.generic.messages");

    @Override
    @Deprecated
    public Optional<Value> execute(Map<String, Value> parameters, Map<String, Object> sessionMap) {
        return this.execute(null, parameters, sessionMap);
    }

    @Override
    public Optional<Value> execute(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        logger.traceEntry(() -> parameters != null ? parameters.entrySet().stream().filter(en -> !Arrays.asList(new String[0]).contains(en.getKey()) && en.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString() : null, () -> sessionMap != null ? sessionMap.toString() : null);
        StopLoggerSession command = new StopLoggerSession();
        HashMap<String, Object> convertedParameters = new HashMap<String, Object>();
        if (parameters.containsKey("session") && parameters.get("session") != null) {
            if (((SessionValue)parameters.get("session")).hasObjectValue()) {
                convertedParameters.put("session", ((SessionValue)parameters.get("session")).getSession());
            } else if (parameters.get("session").get() != null) {
                convertedParameters.put("session", sessionMap.get(parameters.get("session").get()));
            }
            if (convertedParameters.get("session") != null && ((CloseableSessionObject)convertedParameters.get("session")).isClosed()) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("session.IsAlreadyClosed", parameters.get("session").get()));
            }
            if (convertedParameters.get("session") != null && !(convertedParameters.get("session") instanceof CustomLogger)) {
                Class<?>[] interfaces;
                for (Class<?> iface : interfaces = convertedParameters.get("session").getClass().getInterfaces()) {
                    if (!iface.getName().equals(CustomLogger.class.getCanonicalName())) continue;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("session.package.mismatch", "session"));
                }
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "session", "CustomLogger", convertedParameters.get("session").getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("session") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "session"));
        }
        try {
            command.stop((CustomLogger)convertedParameters.get("session"));
            Optional result = Optional.empty();
            return logger.traceExit(result);
        }
        catch (ClassCastException e) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.IllegalParameters", "stop"));
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
