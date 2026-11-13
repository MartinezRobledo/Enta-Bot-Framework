/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.documentation;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.BotCommand;
import com.automationanywhere.botcommand.actions.documentation.Comment;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.model.image.Image;
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

public final class CommentCommand
implements BotCommand {
    private static final Logger logger = LogManager.getLogger(CommentCommand.class);
    private static final Messages MESSAGES_GENERIC = MessagesFactory.getMessages("com.automationanywhere.commandsdk.generic.messages");

    @Override
    @Deprecated
    public Optional<Value> execute(Map<String, Value> parameters, Map<String, Object> sessionMap) {
        return this.execute(null, parameters, sessionMap);
    }

    @Override
    public Optional<Value> execute(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        logger.traceEntry(() -> parameters != null ? parameters.entrySet().stream().filter(en -> !Arrays.asList(new String[0]).contains(en.getKey()) && en.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString() : null, () -> sessionMap != null ? sessionMap.toString() : null);
        Comment command = new Comment();
        HashMap convertedParameters = new HashMap();
        if (parameters.containsKey("commentDescription") && parameters.get("commentDescription") != null && parameters.get("commentDescription").get() != null) {
            convertedParameters.put("commentDescription", parameters.get("commentDescription").get());
            if (convertedParameters.get("commentDescription") != null && !(convertedParameters.get("commentDescription") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "commentDescription", "String", parameters.get("commentDescription").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("commentDescription") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "commentDescription"));
        }
        try {
            command.setCommentDescription((String)convertedParameters.get("commentDescription"));
        }
        catch (ClassCastException e) {
            Object[] objectArray = new Object[3];
            objectArray[0] = "commentDescription";
            objectArray[1] = "String";
            objectArray[2] = parameters.get("commentDescription") != null ? (parameters.get("commentDescription").get() != null ? parameters.get("commentDescription").get().getClass().toString() : "null") : "null";
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
        }
        if (parameters.containsKey("commentImage") && parameters.get("commentImage") != null && parameters.get("commentImage").get() != null) {
            convertedParameters.put("commentImage", parameters.get("commentImage").get());
            if (convertedParameters.get("commentImage") != null && !(convertedParameters.get("commentImage") instanceof Image)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "commentImage", "Image", parameters.get("commentImage").get().getClass().getSimpleName()));
            }
        }
        try {
            command.setCommentImage((Image)convertedParameters.get("commentImage"));
        }
        catch (ClassCastException e) {
            Object[] objectArray = new Object[3];
            objectArray[0] = "commentImage";
            objectArray[1] = "Image";
            objectArray[2] = parameters.get("commentImage") != null ? (parameters.get("commentImage").get() != null ? parameters.get("commentImage").get().getClass().toString() : "null") : "null";
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
