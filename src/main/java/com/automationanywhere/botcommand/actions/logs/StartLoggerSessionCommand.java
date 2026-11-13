/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.logs;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.BotCommand;
import com.automationanywhere.botcommand.actions.logs.StartLoggerSession;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.SessionValue;
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

public final class StartLoggerSessionCommand
implements BotCommand {
    private static final Logger logger = LogManager.getLogger(StartLoggerSessionCommand.class);
    private static final Messages MESSAGES_GENERIC = MessagesFactory.getMessages("com.automationanywhere.commandsdk.generic.messages");

    @Override
    @Deprecated
    public Optional<Value> execute(Map<String, Value> parameters, Map<String, Object> sessionMap) {
        return this.execute(null, parameters, sessionMap);
    }

    @Override
    public Optional<Value> execute(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        logger.traceEntry(() -> parameters != null ? parameters.entrySet().stream().filter(en -> !Arrays.asList(new String[0]).contains(en.getKey()) && en.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString() : null, () -> sessionMap != null ? sessionMap.toString() : null);
        StartLoggerSession command = new StartLoggerSession();
        HashMap convertedParameters = new HashMap();
        if (parameters.containsKey("logLevelsAndFileOption") && parameters.get("logLevelsAndFileOption") != null && parameters.get("logLevelsAndFileOption").get() != null) {
            convertedParameters.put("logLevelsAndFileOption", parameters.get("logLevelsAndFileOption").get());
            if (convertedParameters.get("logLevelsAndFileOption") != null && !(convertedParameters.get("logLevelsAndFileOption") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "logLevelsAndFileOption", "String", parameters.get("logLevelsAndFileOption").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("logLevelsAndFileOption") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "logLevelsAndFileOption"));
        }
        if (convertedParameters.get("logLevelsAndFileOption") != null) {
            switch ((String)convertedParameters.get("logLevelsAndFileOption")) {
                case "COMMON_FILE": {
                    if (parameters.containsKey("logFilePath") && parameters.get("logFilePath") != null && parameters.get("logFilePath").get() != null) {
                        convertedParameters.put("logFilePath", parameters.get("logFilePath").get());
                        if (convertedParameters.get("logFilePath") != null && !(convertedParameters.get("logFilePath") instanceof String)) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "logFilePath", "String", parameters.get("logFilePath").get().getClass().getSimpleName()));
                        }
                    }
                    if (convertedParameters.get("logFilePath") == null) {
                        throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "logFilePath"));
                    }
                    if (!convertedParameters.containsKey("logFilePath")) break;
                    String filePath = (String)convertedParameters.get("logFilePath");
                    int lastIndxDot = filePath.lastIndexOf(".");
                    if (lastIndxDot == -1 || lastIndxDot >= filePath.length()) {
                        throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.FileExtension", "logFilePath", "html"));
                    }
                    String fileExtension = filePath.substring(lastIndxDot + 1);
                    if (Arrays.stream("html".split(",")).anyMatch(fileExtension::equalsIgnoreCase)) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.FileExtension", "logFilePath", "html"));
                }
                case "CONFIGURABLE_FILE": {
                    String fileExtension;
                    int lastIndxDot;
                    String filePath;
                    if (parameters.containsKey("infoLogFilePath") && parameters.get("infoLogFilePath") != null && parameters.get("infoLogFilePath").get() != null) {
                        convertedParameters.put("infoLogFilePath", parameters.get("infoLogFilePath").get());
                        if (convertedParameters.get("infoLogFilePath") != null && !(convertedParameters.get("infoLogFilePath") instanceof String)) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "infoLogFilePath", "String", parameters.get("infoLogFilePath").get().getClass().getSimpleName()));
                        }
                    }
                    if (convertedParameters.get("infoLogFilePath") == null) {
                        throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "infoLogFilePath"));
                    }
                    if (convertedParameters.containsKey("infoLogFilePath")) {
                        filePath = (String)convertedParameters.get("infoLogFilePath");
                        lastIndxDot = filePath.lastIndexOf(".");
                        if (lastIndxDot == -1 || lastIndxDot >= filePath.length()) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.FileExtension", "infoLogFilePath", "html"));
                        }
                        fileExtension = filePath.substring(lastIndxDot + 1);
                        if (!Arrays.stream("html".split(",")).anyMatch(fileExtension::equalsIgnoreCase)) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.FileExtension", "infoLogFilePath", "html"));
                        }
                    }
                    if (parameters.containsKey("warnLogFilePath") && parameters.get("warnLogFilePath") != null && parameters.get("warnLogFilePath").get() != null) {
                        convertedParameters.put("warnLogFilePath", parameters.get("warnLogFilePath").get());
                        if (convertedParameters.get("warnLogFilePath") != null && !(convertedParameters.get("warnLogFilePath") instanceof String)) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "warnLogFilePath", "String", parameters.get("warnLogFilePath").get().getClass().getSimpleName()));
                        }
                    }
                    if (convertedParameters.get("warnLogFilePath") == null) {
                        throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "warnLogFilePath"));
                    }
                    if (convertedParameters.containsKey("warnLogFilePath")) {
                        filePath = (String)convertedParameters.get("warnLogFilePath");
                        lastIndxDot = filePath.lastIndexOf(".");
                        if (lastIndxDot == -1 || lastIndxDot >= filePath.length()) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.FileExtension", "warnLogFilePath", "html"));
                        }
                        fileExtension = filePath.substring(lastIndxDot + 1);
                        if (!Arrays.stream("html".split(",")).anyMatch(fileExtension::equalsIgnoreCase)) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.FileExtension", "warnLogFilePath", "html"));
                        }
                    }
                    if (parameters.containsKey("errorLogFilePath") && parameters.get("errorLogFilePath") != null && parameters.get("errorLogFilePath").get() != null) {
                        convertedParameters.put("errorLogFilePath", parameters.get("errorLogFilePath").get());
                        if (convertedParameters.get("errorLogFilePath") != null && !(convertedParameters.get("errorLogFilePath") instanceof String)) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "errorLogFilePath", "String", parameters.get("errorLogFilePath").get().getClass().getSimpleName()));
                        }
                    }
                    if (convertedParameters.get("errorLogFilePath") == null) {
                        throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "errorLogFilePath"));
                    }
                    if (!convertedParameters.containsKey("errorLogFilePath")) break;
                    filePath = (String)convertedParameters.get("errorLogFilePath");
                    lastIndxDot = filePath.lastIndexOf(".");
                    if (lastIndxDot == -1 || lastIndxDot >= filePath.length()) {
                        throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.FileExtension", "errorLogFilePath", "html"));
                    }
                    fileExtension = filePath.substring(lastIndxDot + 1);
                    if (Arrays.stream("html".split(",")).anyMatch(fileExtension::equalsIgnoreCase)) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.FileExtension", "errorLogFilePath", "html"));
                }
                default: {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.InvalidOption", "logLevelsAndFileOption"));
                }
            }
        }
        if (parameters.containsKey("rollingFileSizeMB") && parameters.get("rollingFileSizeMB") != null && parameters.get("rollingFileSizeMB").get() != null) {
            convertedParameters.put("rollingFileSizeMB", parameters.get("rollingFileSizeMB").get());
            if (convertedParameters.get("rollingFileSizeMB") != null && !(convertedParameters.get("rollingFileSizeMB") instanceof Number)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "rollingFileSizeMB", "Number", parameters.get("rollingFileSizeMB").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.containsKey("rollingFileSizeMB")) {
            try {
                if (convertedParameters.get("rollingFileSizeMB") != null && !((Double)convertedParameters.get("rollingFileSizeMB") > 0.0)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.GreaterThan", "rollingFileSizeMB", "0"));
                }
            }
            catch (ClassCastException e) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "rollingFileSizeMB", "Number", convertedParameters.get("rollingFileSizeMB").getClass().getSimpleName()));
            }
            catch (NullPointerException e) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "rollingFileSizeMB"));
            }
        }
        try {
            Optional<Value> result = Optional.ofNullable(command.start((String)convertedParameters.get("logLevelsAndFileOption"), (String)convertedParameters.get("logFilePath"), (String)convertedParameters.get("infoLogFilePath"), (String)convertedParameters.get("warnLogFilePath"), (String)convertedParameters.get("errorLogFilePath"), (Number)convertedParameters.get("rollingFileSizeMB")));
            return logger.traceExit(result);
        }
        catch (ClassCastException e) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.IllegalParameters", "start"));
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
