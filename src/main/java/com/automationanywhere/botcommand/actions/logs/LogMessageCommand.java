/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.logs;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.BotCommand;
import com.automationanywhere.botcommand.actions.logs.LogMessage;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.data.impl.SessionValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.logger.CustomLogger;
import com.automationanywhere.commandsdk.i18n.Messages;
import com.automationanywhere.commandsdk.i18n.MessagesFactory;
import com.automationanywhere.toolchain.runtime.session.CloseableSessionObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LogMessageCommand
implements BotCommand {
    private static final Logger logger = LogManager.getLogger(LogMessageCommand.class);
    private static final Messages MESSAGES_GENERIC = MessagesFactory.getMessages("com.automationanywhere.commandsdk.generic.messages");

    @Override
    @Deprecated
    public Optional<Value> execute(Map<String, Value> parameters, Map<String, Object> sessionMap) {
        return this.execute(null, parameters, sessionMap);
    }

    @Override
    public Optional<Value> execute(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        logger.traceEntry(() -> parameters != null ? parameters.entrySet().stream().filter(en -> !Arrays.asList(new String[0]).contains(en.getKey()) && en.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString() : null, () -> sessionMap != null ? sessionMap.toString() : null);
        LogMessage command = new LogMessage();
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
        if (parameters.containsKey("logLevel") && parameters.get("logLevel") != null && parameters.get("logLevel").get() != null) {
            convertedParameters.put("logLevel", parameters.get("logLevel").get());
            if (convertedParameters.get("logLevel") != null && !(convertedParameters.get("logLevel") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "logLevel", "String", parameters.get("logLevel").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("logLevel") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "logLevel"));
        }
        if (convertedParameters.get("logLevel") != null) {
            switch ((String)convertedParameters.get("logLevel")) {
                case "INFO": {
                    break;
                }
                case "WARN": {
                    break;
                }
                case "ERROR": {
                    break;
                }
                default: {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.InvalidOption", "logLevel"));
                }
            }
        }
        if (parameters.containsKey("logMessage") && parameters.get("logMessage") != null && parameters.get("logMessage").get() != null) {
            convertedParameters.put("logMessage", parameters.get("logMessage").get());
            if (convertedParameters.get("logMessage") != null && !(convertedParameters.get("logMessage") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "logMessage", "String", parameters.get("logMessage").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("logMessage") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "logMessage"));
        }
        if (parameters.containsKey("captureScreenshot") && parameters.get("captureScreenshot") != null && parameters.get("captureScreenshot").get() != null) {
            convertedParameters.put("captureScreenshot", parameters.get("captureScreenshot").get());
            if (convertedParameters.get("captureScreenshot") != null && !(convertedParameters.get("captureScreenshot") instanceof Boolean)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "captureScreenshot", "Boolean", parameters.get("captureScreenshot").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("captureScreenshot") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "captureScreenshot"));
        }
        if (parameters.containsKey("logVariable") && parameters.get("logVariable") != null && parameters.get("logVariable").get() != null) {
            convertedParameters.put("logVariable", parameters.get("logVariable").get());
            if (convertedParameters.get("logVariable") != null && !(convertedParameters.get("logVariable") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "logVariable", "String", parameters.get("logVariable").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("logVariable") != null) {
            switch ((String)convertedParameters.get("logVariable")) {
                case "NO": {
                    break;
                }
                case "YES": {
                    if (parameters.containsKey("list") && parameters.get("list") != null && parameters.get("list").get() != null) {
                        convertedParameters.put("list", parameters.get("list").get());
                        if (convertedParameters.get("list") != null && !(convertedParameters.get("list") instanceof List)) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "list", "List", parameters.get("list").get().getClass().getSimpleName()));
                        }
                    }
                    if (convertedParameters.containsKey("list")) {
                        try {
                            List<Value> entryList = (List<Value>)convertedParameters.get("list");
                            for (Value element : entryList) {
                                DictionaryValue dictionaryValue = (DictionaryValue)element;
                                Map<String, Value> valueMap = dictionaryValue.get();
                                HashSet<Value> uniqueValueSet = new HashSet<Value>();
                                for (Map.Entry entry : valueMap.entrySet()) {
                                    if (((String)entry.getKey()).equals("NAME") && uniqueValueSet.contains(entry.getValue())) {
                                        throw new BotCommandException(MESSAGES_GENERIC.getString("entrylist.duplicate.rows.added", "NAME"));
                                    }
                                    if (!((String)entry.getKey()).equals("NAME")) continue;
                                    uniqueValueSet.add((Value)entry.getValue());
                                }
                            }
                        }
                        catch (ClassCastException e) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "list", "List<Value>", convertedParameters.get("list").getClass().getSimpleName()));
                        }
                        if (parameters.containsKey("NAME") && parameters.get("NAME") != null && parameters.get("NAME").get() != null) {
                            convertedParameters.put("NAME", parameters.get("NAME").get());
                            if (convertedParameters.get("NAME") != null && !(convertedParameters.get("NAME") instanceof String)) {
                                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "NAME", "String", parameters.get("NAME").get().getClass().getSimpleName()));
                            }
                        }
                        if (parameters.containsKey("VALUE") && parameters.get("VALUE") != null && parameters.get("VALUE").get() != null) {
                            convertedParameters.put("VALUE", parameters.get("VALUE").get());
                            if (!(parameters.get("VALUE") instanceof Value)) {
                                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "VALUE", "Value", parameters.get("VALUE").get().getClass().getSimpleName()));
                            }
                        }
                    }
                    if (!parameters.containsKey("sourceMap") || parameters.get("sourceMap") == null || parameters.get("sourceMap").get() == null) break;
                    convertedParameters.put("sourceMap", parameters.get("sourceMap").get());
                    if (convertedParameters.get("sourceMap") == null || convertedParameters.get("sourceMap") instanceof Map) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "sourceMap", "Map", parameters.get("sourceMap").get().getClass().getSimpleName()));
                }
                default: {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.InvalidOption", "logVariable"));
                }
            }
        }
        command.setGlobalSessionContext(globalSessionContext);
        try {
            command.action((CustomLogger)convertedParameters.get("session"), (String)convertedParameters.get("logLevel"), (String)convertedParameters.get("logMessage"), (Boolean)convertedParameters.get("captureScreenshot"), (String)convertedParameters.get("logVariable"), (List)convertedParameters.get("list"), (Map)convertedParameters.get("sourceMap"));
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
