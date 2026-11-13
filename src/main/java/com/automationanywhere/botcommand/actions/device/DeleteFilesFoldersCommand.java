/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.device;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.BotCommand;
import com.automationanywhere.botcommand.actions.device.DeleteFilesFolders;
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

public final class DeleteFilesFoldersCommand
implements BotCommand {
    private static final Logger logger = LogManager.getLogger(DeleteFilesFoldersCommand.class);
    private static final Messages MESSAGES_GENERIC = MessagesFactory.getMessages("com.automationanywhere.commandsdk.generic.messages");

    @Override
    @Deprecated
    public Optional<Value> execute(Map<String, Value> parameters, Map<String, Object> sessionMap) {
        return this.execute(null, parameters, sessionMap);
    }

    @Override
    public Optional<Value> execute(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        logger.traceEntry(() -> parameters != null ? parameters.entrySet().stream().filter(en -> !Arrays.asList(new String[0]).contains(en.getKey()) && en.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString() : null, () -> sessionMap != null ? sessionMap.toString() : null);
        DeleteFilesFolders command = new DeleteFilesFolders();
        HashMap convertedParameters = new HashMap();
        if (parameters.containsKey("inputFolderPath") && parameters.get("inputFolderPath") != null && parameters.get("inputFolderPath").get() != null) {
            convertedParameters.put("inputFolderPath", parameters.get("inputFolderPath").get());
            if (convertedParameters.get("inputFolderPath") != null && !(convertedParameters.get("inputFolderPath") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "inputFolderPath", "String", parameters.get("inputFolderPath").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("inputFolderPath") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "inputFolderPath"));
        }
        if (parameters.containsKey("selectMethod") && parameters.get("selectMethod") != null && parameters.get("selectMethod").get() != null) {
            convertedParameters.put("selectMethod", parameters.get("selectMethod").get());
            if (convertedParameters.get("selectMethod") != null && !(convertedParameters.get("selectMethod") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "selectMethod", "String", parameters.get("selectMethod").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("selectMethod") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "selectMethod"));
        }
        if (convertedParameters.get("selectMethod") != null) {
            switch ((String)convertedParameters.get("selectMethod")) {
                case "ALL": {
                    break;
                }
                case "FILE": {
                    break;
                }
                default: {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.InvalidOption", "selectMethod"));
                }
            }
        }
        if (parameters.containsKey("recursive") && parameters.get("recursive") != null && parameters.get("recursive").get() != null) {
            convertedParameters.put("recursive", parameters.get("recursive").get());
            if (convertedParameters.get("recursive") != null && !(convertedParameters.get("recursive") instanceof Boolean)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "recursive", "Boolean", parameters.get("recursive").get().getClass().getSimpleName()));
            }
        }
        if (parameters.containsKey("thresholdNumber") && parameters.get("thresholdNumber") != null && parameters.get("thresholdNumber").get() != null) {
            convertedParameters.put("thresholdNumber", parameters.get("thresholdNumber").get());
            if (convertedParameters.get("thresholdNumber") != null && !(convertedParameters.get("thresholdNumber") instanceof Number)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "thresholdNumber", "Number", parameters.get("thresholdNumber").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("thresholdNumber") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "thresholdNumber"));
        }
        if (convertedParameters.containsKey("thresholdNumber")) {
            try {
                if (convertedParameters.get("thresholdNumber") != null && !((Double)convertedParameters.get("thresholdNumber") >= 0.0)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.GreaterThanEqualTo", "thresholdNumber", "0"));
                }
            }
            catch (ClassCastException e) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "thresholdNumber", "Number", convertedParameters.get("thresholdNumber").getClass().getSimpleName()));
            }
            catch (NullPointerException e) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "thresholdNumber"));
            }
            if (convertedParameters.get("thresholdNumber") != null && !(convertedParameters.get("thresholdNumber") instanceof Number)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "thresholdNumber", "Number", convertedParameters.get("thresholdNumber").getClass().getSimpleName()));
            }
        }
        if (parameters.containsKey("thresholdUnit") && parameters.get("thresholdUnit") != null && parameters.get("thresholdUnit").get() != null) {
            convertedParameters.put("thresholdUnit", parameters.get("thresholdUnit").get());
            if (convertedParameters.get("thresholdUnit") != null && !(convertedParameters.get("thresholdUnit") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "thresholdUnit", "String", parameters.get("thresholdUnit").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("thresholdUnit") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "thresholdUnit"));
        }
        if (convertedParameters.get("thresholdUnit") != null) {
            switch ((String)convertedParameters.get("thresholdUnit")) {
                case "DAY": {
                    break;
                }
                case "HOUR": {
                    break;
                }
                case "MINUTE": {
                    break;
                }
                case "SECOND": {
                    break;
                }
                default: {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.InvalidOption", "thresholdUnit"));
                }
            }
        }
        if (parameters.containsKey("thresholdCriteria") && parameters.get("thresholdCriteria") != null && parameters.get("thresholdCriteria").get() != null) {
            convertedParameters.put("thresholdCriteria", parameters.get("thresholdCriteria").get());
            if (convertedParameters.get("thresholdCriteria") != null && !(convertedParameters.get("thresholdCriteria") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "thresholdCriteria", "String", parameters.get("thresholdCriteria").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("thresholdCriteria") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "thresholdCriteria"));
        }
        if (convertedParameters.get("thresholdCriteria") != null) {
            switch ((String)convertedParameters.get("thresholdCriteria")) {
                case "CREATION": {
                    break;
                }
                case "MODIFICATION": {
                    break;
                }
                default: {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.InvalidOption", "thresholdCriteria"));
                }
            }
        }
        if (parameters.containsKey("skipFolders") && parameters.get("skipFolders") != null && parameters.get("skipFolders").get() != null) {
            convertedParameters.put("skipFolders", parameters.get("skipFolders").get());
            if (convertedParameters.get("skipFolders") != null && !(convertedParameters.get("skipFolders") instanceof Boolean)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "skipFolders", "Boolean", parameters.get("skipFolders").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("skipFolders") != null && ((Boolean)convertedParameters.get("skipFolders")).booleanValue()) {
            if (parameters.containsKey("skipFolderPathPattern") && parameters.get("skipFolderPathPattern") != null && parameters.get("skipFolderPathPattern").get() != null) {
                convertedParameters.put("skipFolderPathPattern", parameters.get("skipFolderPathPattern").get());
                if (convertedParameters.get("skipFolderPathPattern") != null && !(convertedParameters.get("skipFolderPathPattern") instanceof String)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "skipFolderPathPattern", "String", parameters.get("skipFolderPathPattern").get().getClass().getSimpleName()));
                }
            }
            if (convertedParameters.get("skipFolderPathPattern") == null) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "skipFolderPathPattern"));
            }
        }
        if (parameters.containsKey("skipFiles") && parameters.get("skipFiles") != null && parameters.get("skipFiles").get() != null) {
            convertedParameters.put("skipFiles", parameters.get("skipFiles").get());
            if (convertedParameters.get("skipFiles") != null && !(convertedParameters.get("skipFiles") instanceof Boolean)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "skipFiles", "Boolean", parameters.get("skipFiles").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("skipFiles") != null && ((Boolean)convertedParameters.get("skipFiles")).booleanValue()) {
            if (parameters.containsKey("skipFilePathPattern") && parameters.get("skipFilePathPattern") != null && parameters.get("skipFilePathPattern").get() != null) {
                convertedParameters.put("skipFilePathPattern", parameters.get("skipFilePathPattern").get());
                if (convertedParameters.get("skipFilePathPattern") != null && !(convertedParameters.get("skipFilePathPattern") instanceof String)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "skipFilePathPattern", "String", parameters.get("skipFilePathPattern").get().getClass().getSimpleName()));
                }
            }
            if (convertedParameters.get("skipFilePathPattern") == null) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "skipFilePathPattern"));
            }
        }
        if (parameters.containsKey("unableToDeleteBehavior") && parameters.get("unableToDeleteBehavior") != null && parameters.get("unableToDeleteBehavior").get() != null) {
            convertedParameters.put("unableToDeleteBehavior", parameters.get("unableToDeleteBehavior").get());
            if (convertedParameters.get("unableToDeleteBehavior") != null && !(convertedParameters.get("unableToDeleteBehavior") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "unableToDeleteBehavior", "String", parameters.get("unableToDeleteBehavior").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("unableToDeleteBehavior") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "unableToDeleteBehavior"));
        }
        if (convertedParameters.get("unableToDeleteBehavior") != null) {
            switch ((String)convertedParameters.get("unableToDeleteBehavior")) {
                case "THROW": {
                    break;
                }
                case "IGNORE": {
                    break;
                }
                default: {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.InvalidOption", "unableToDeleteBehavior"));
                }
            }
        }
        try {
            command.action((String)convertedParameters.get("inputFolderPath"), (String)convertedParameters.get("selectMethod"), (Boolean)convertedParameters.get("recursive"), (Number)convertedParameters.get("thresholdNumber"), (String)convertedParameters.get("thresholdUnit"), (String)convertedParameters.get("thresholdCriteria"), (Boolean)convertedParameters.get("skipFolders"), (String)convertedParameters.get("skipFolderPathPattern"), (Boolean)convertedParameters.get("skipFiles"), (String)convertedParameters.get("skipFilePathPattern"), (String)convertedParameters.get("unableToDeleteBehavior"));
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
