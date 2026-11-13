/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.config;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.BotCommand;
import com.automationanywhere.botcommand.actions.config.ExcelReader;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.i18n.Messages;
import com.automationanywhere.commandsdk.i18n.MessagesFactory;
import com.automationanywhere.core.security.SecureString;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ExcelReaderCommand
implements BotCommand {
    private static final Logger logger = LogManager.getLogger(ExcelReaderCommand.class);
    private static final Messages MESSAGES_GENERIC = MessagesFactory.getMessages("com.automationanywhere.commandsdk.generic.messages");

    @Override
    @Deprecated
    public Optional<Value> execute(Map<String, Value> parameters, Map<String, Object> sessionMap) {
        return this.execute(null, parameters, sessionMap);
    }

    @Override
    public Optional<Value> execute(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        logger.traceEntry(() -> parameters != null ? parameters.entrySet().stream().filter(en -> !Arrays.asList(new String[0]).contains(en.getKey()) && en.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString() : null, () -> sessionMap != null ? sessionMap.toString() : null);
        ExcelReader command = new ExcelReader();
        HashMap convertedParameters = new HashMap();
        if (parameters.containsKey("inputFilePath") && parameters.get("inputFilePath") != null && parameters.get("inputFilePath").get() != null) {
            convertedParameters.put("inputFilePath", parameters.get("inputFilePath").get());
            if (convertedParameters.get("inputFilePath") != null && !(convertedParameters.get("inputFilePath") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "inputFilePath", "String", parameters.get("inputFilePath").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("inputFilePath") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "inputFilePath"));
        }
        if (convertedParameters.containsKey("inputFilePath")) {
            String filePath = (String)convertedParameters.get("inputFilePath");
            int lastIndxDot = filePath.lastIndexOf(".");
            if (lastIndxDot == -1 || lastIndxDot >= filePath.length()) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.FileExtension", "inputFilePath", "xlsm"));
            }
            String fileExtension = filePath.substring(lastIndxDot + 1);
            if (!Arrays.stream("xlsm".split(",")).anyMatch(fileExtension::equalsIgnoreCase)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.FileExtension", "inputFilePath", "xlsm"));
            }
        }
        if (parameters.containsKey("isPasswordProtected") && parameters.get("isPasswordProtected") != null && parameters.get("isPasswordProtected").get() != null) {
            convertedParameters.put("isPasswordProtected", parameters.get("isPasswordProtected").get());
            if (convertedParameters.get("isPasswordProtected") != null && !(convertedParameters.get("isPasswordProtected") instanceof Boolean)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "isPasswordProtected", "Boolean", parameters.get("isPasswordProtected").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("isPasswordProtected") != null && ((Boolean)convertedParameters.get("isPasswordProtected")).booleanValue() && parameters.containsKey("filePassword") && parameters.get("filePassword") != null && parameters.get("filePassword").get() != null) {
            convertedParameters.put("filePassword", parameters.get("filePassword").get());
            if (convertedParameters.get("filePassword") != null && !(convertedParameters.get("filePassword") instanceof SecureString)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "filePassword", "SecureString", parameters.get("filePassword").get().getClass().getSimpleName()));
            }
        }
        if (parameters.containsKey("isTrimValues") && parameters.get("isTrimValues") != null && parameters.get("isTrimValues").get() != null) {
            convertedParameters.put("isTrimValues", parameters.get("isTrimValues").get());
            if (convertedParameters.get("isTrimValues") != null && !(convertedParameters.get("isTrimValues") instanceof Boolean)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "isTrimValues", "Boolean", parameters.get("isTrimValues").get().getClass().getSimpleName()));
            }
        }
        try {
            Optional<Value> result = Optional.ofNullable(command.action((String)convertedParameters.get("inputFilePath"), (Boolean)convertedParameters.get("isPasswordProtected"), (SecureString)convertedParameters.get("filePassword"), (Boolean)convertedParameters.get("isTrimValues")));
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
