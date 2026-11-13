/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.config;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.BotCommand;
import com.automationanywhere.botcommand.actions.config.JSONReader;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
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

public final class JSONReaderCommand
implements BotCommand {
    private static final Logger logger = LogManager.getLogger(JSONReaderCommand.class);
    private static final Messages MESSAGES_GENERIC = MessagesFactory.getMessages("com.automationanywhere.commandsdk.generic.messages");

    @Override
    @Deprecated
    public Optional<Value> execute(Map<String, Value> parameters, Map<String, Object> sessionMap) {
        return this.execute(null, parameters, sessionMap);
    }

    @Override
    public Optional<Value> execute(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        logger.traceEntry(() -> parameters != null ? parameters.entrySet().stream().filter(en -> !Arrays.asList(new String[0]).contains(en.getKey()) && en.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString() : null, () -> sessionMap != null ? sessionMap.toString() : null);
        JSONReader command = new JSONReader();
        HashMap convertedParameters = new HashMap();
        if (parameters.containsKey("inputMethod") && parameters.get("inputMethod") != null && parameters.get("inputMethod").get() != null) {
            convertedParameters.put("inputMethod", parameters.get("inputMethod").get());
            if (convertedParameters.get("inputMethod") != null && !(convertedParameters.get("inputMethod") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "inputMethod", "String", parameters.get("inputMethod").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("inputMethod") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "inputMethod"));
        }
        if (convertedParameters.get("inputMethod") != null) {
            switch ((String)convertedParameters.get("inputMethod")) {
                case "FILE": {
                    if (parameters.containsKey("jsonFilePath") && parameters.get("jsonFilePath") != null && parameters.get("jsonFilePath").get() != null) {
                        convertedParameters.put("jsonFilePath", parameters.get("jsonFilePath").get());
                        if (convertedParameters.get("jsonFilePath") != null && !(convertedParameters.get("jsonFilePath") instanceof String)) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "jsonFilePath", "String", parameters.get("jsonFilePath").get().getClass().getSimpleName()));
                        }
                    }
                    if (convertedParameters.containsKey("jsonFilePath")) {
                        String filePath = (String)convertedParameters.get("jsonFilePath");
                        int lastIndxDot = filePath.lastIndexOf(".");
                        if (lastIndxDot == -1 || lastIndxDot >= filePath.length()) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.FileExtension", "jsonFilePath", "json"));
                        }
                        String fileExtension = filePath.substring(lastIndxDot + 1);
                        if (!Arrays.stream("json".split(",")).anyMatch(fileExtension::equalsIgnoreCase)) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.FileExtension", "jsonFilePath", "json"));
                        }
                    }
                    if (parameters.containsKey("charsetName") && parameters.get("charsetName") != null && parameters.get("charsetName").get() != null) {
                        convertedParameters.put("charsetName", parameters.get("charsetName").get());
                        if (convertedParameters.get("charsetName") != null && !(convertedParameters.get("charsetName") instanceof String)) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "charsetName", "String", parameters.get("charsetName").get().getClass().getSimpleName()));
                        }
                    }
                    if (convertedParameters.get("charsetName") != null) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "charsetName"));
                }
                case "TEXT": {
                    if (parameters.containsKey("jsonText") && parameters.get("jsonText") != null && parameters.get("jsonText").get() != null) {
                        convertedParameters.put("jsonText", parameters.get("jsonText").get());
                        if (convertedParameters.get("jsonText") != null && !(convertedParameters.get("jsonText") instanceof String)) {
                            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "jsonText", "String", parameters.get("jsonText").get().getClass().getSimpleName()));
                        }
                    }
                    if (convertedParameters.get("jsonText") != null) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "jsonText"));
                }
                default: {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.InvalidOption", "inputMethod"));
                }
            }
        }
        if (parameters.containsKey("parsingMethod") && parameters.get("parsingMethod") != null && parameters.get("parsingMethod").get() != null) {
            convertedParameters.put("parsingMethod", parameters.get("parsingMethod").get());
            if (convertedParameters.get("parsingMethod") != null && !(convertedParameters.get("parsingMethod") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "parsingMethod", "String", parameters.get("parsingMethod").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("parsingMethod") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "parsingMethod"));
        }
        if (convertedParameters.get("parsingMethod") != null) {
            switch ((String)convertedParameters.get("parsingMethod")) {
                case "ALL": {
                    break;
                }
                case "SPECIFIC": {
                    if (!parameters.containsKey("jsonPathSegments") || parameters.get("jsonPathSegments") == null || parameters.get("jsonPathSegments").get() == null) break;
                    convertedParameters.put("jsonPathSegments", parameters.get("jsonPathSegments").get());
                    if (convertedParameters.get("jsonPathSegments") == null || convertedParameters.get("jsonPathSegments") instanceof List) break;
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "jsonPathSegments", "List", parameters.get("jsonPathSegments").get().getClass().getSimpleName()));
                }
                default: {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.InvalidOption", "parsingMethod"));
                }
            }
        }
        if (parameters.containsKey("isTrimValues") && parameters.get("isTrimValues") != null && parameters.get("isTrimValues").get() != null) {
            convertedParameters.put("isTrimValues", parameters.get("isTrimValues").get());
            if (convertedParameters.get("isTrimValues") != null && !(convertedParameters.get("isTrimValues") instanceof Boolean)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "isTrimValues", "Boolean", parameters.get("isTrimValues").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("isTrimValues") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "isTrimValues"));
        }
        try {
            Optional<Value> result = Optional.ofNullable(command.action((String)convertedParameters.get("inputMethod"), (String)convertedParameters.get("jsonFilePath"), (String)convertedParameters.get("charsetName"), (String)convertedParameters.get("jsonText"), (String)convertedParameters.get("parsingMethod"), (List)convertedParameters.get("jsonPathSegments"), (Boolean)convertedParameters.get("isTrimValues")));
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
