/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.documentation;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.BotCommand;
import com.automationanywhere.botcommand.actions.documentation.Sequence;
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

public final class SequenceCommand
implements BotCommand {
    private static final Logger logger = LogManager.getLogger(SequenceCommand.class);
    private static final Messages MESSAGES_GENERIC = MessagesFactory.getMessages("com.automationanywhere.commandsdk.generic.messages");

    @Override
    @Deprecated
    public Optional<Value> execute(Map<String, Value> parameters, Map<String, Object> sessionMap) {
        return this.execute(null, parameters, sessionMap);
    }

    @Override
    public Optional<Value> execute(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        logger.traceEntry(() -> parameters != null ? parameters.entrySet().stream().filter(en -> !Arrays.asList(new String[0]).contains(en.getKey()) && en.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString() : null, () -> sessionMap != null ? sessionMap.toString() : null);
        Sequence command = new Sequence();
        HashMap convertedParameters = new HashMap();
        if (parameters.containsKey("sequenceTitle") && parameters.get("sequenceTitle") != null && parameters.get("sequenceTitle").get() != null) {
            convertedParameters.put("sequenceTitle", parameters.get("sequenceTitle").get());
            if (convertedParameters.get("sequenceTitle") != null && !(convertedParameters.get("sequenceTitle") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "sequenceTitle", "String", parameters.get("sequenceTitle").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("sequenceTitle") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "sequenceTitle"));
        }
        try {
            command.setSequenceTitle((String)convertedParameters.get("sequenceTitle"));
        }
        catch (ClassCastException e) {
            Object[] objectArray = new Object[3];
            objectArray[0] = "sequenceTitle";
            objectArray[1] = "String";
            objectArray[2] = parameters.get("sequenceTitle") != null ? (parameters.get("sequenceTitle").get() != null ? parameters.get("sequenceTitle").get().getClass().toString() : "null") : "null";
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
        }
        if (parameters.containsKey("sequenceDescription") && parameters.get("sequenceDescription") != null && parameters.get("sequenceDescription").get() != null) {
            convertedParameters.put("sequenceDescription", parameters.get("sequenceDescription").get());
            if (convertedParameters.get("sequenceDescription") != null && !(convertedParameters.get("sequenceDescription") instanceof Map)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "sequenceDescription", "Map", parameters.get("sequenceDescription").get().getClass().getSimpleName()));
            }
        }
        try {
            command.setSequenceDescription((Map)convertedParameters.get("sequenceDescription"));
        }
        catch (ClassCastException e) {
            Object[] objectArray = new Object[3];
            objectArray[0] = "sequenceDescription";
            objectArray[1] = "Map<String, Value>";
            objectArray[2] = parameters.get("sequenceDescription") != null ? (parameters.get("sequenceDescription").get() != null ? parameters.get("sequenceDescription").get().getClass().toString() : "null") : "null";
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
        }
        if (parameters.containsKey("sequenceCheckBox1") && parameters.get("sequenceCheckBox1") != null && parameters.get("sequenceCheckBox1").get() != null) {
            convertedParameters.put("sequenceCheckBox1", parameters.get("sequenceCheckBox1").get());
            if (convertedParameters.get("sequenceCheckBox1") != null && !(convertedParameters.get("sequenceCheckBox1") instanceof Boolean)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "sequenceCheckBox1", "Boolean", parameters.get("sequenceCheckBox1").get().getClass().getSimpleName()));
            }
        }
        try {
            command.setSequenceCheckBox1((Boolean)convertedParameters.get("sequenceCheckBox1"));
        }
        catch (ClassCastException e) {
            Object[] objectArray = new Object[3];
            objectArray[0] = "sequenceCheckBox1";
            objectArray[1] = "Boolean";
            objectArray[2] = parameters.get("sequenceCheckBox1") != null ? (parameters.get("sequenceCheckBox1").get() != null ? parameters.get("sequenceCheckBox1").get().getClass().toString() : "null") : "null";
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
        }
        if (convertedParameters.get("sequenceCheckBox1") != null && ((Boolean)convertedParameters.get("sequenceCheckBox1")).booleanValue()) {
            if (parameters.containsKey("sequenceImageText1") && parameters.get("sequenceImageText1") != null && parameters.get("sequenceImageText1").get() != null) {
                convertedParameters.put("sequenceImageText1", parameters.get("sequenceImageText1").get());
                if (convertedParameters.get("sequenceImageText1") != null && !(convertedParameters.get("sequenceImageText1") instanceof Image)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "sequenceImageText1", "Image", parameters.get("sequenceImageText1").get().getClass().getSimpleName()));
                }
            }
            try {
                command.setSequenceImageText1((Image)convertedParameters.get("sequenceImageText1"));
            }
            catch (ClassCastException e) {
                Object[] objectArray = new Object[3];
                objectArray[0] = "sequenceImageText1";
                objectArray[1] = "Image";
                objectArray[2] = parameters.get("sequenceImageText1") != null ? (parameters.get("sequenceImageText1").get() != null ? parameters.get("sequenceImageText1").get().getClass().toString() : "null") : "null";
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
            }
            if (parameters.containsKey("sequenceImage1") && parameters.get("sequenceImage1") != null && parameters.get("sequenceImage1").get() != null) {
                convertedParameters.put("sequenceImage1", parameters.get("sequenceImage1").get());
                if (convertedParameters.get("sequenceImage1") != null && !(convertedParameters.get("sequenceImage1") instanceof Image)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "sequenceImage1", "Image", parameters.get("sequenceImage1").get().getClass().getSimpleName()));
                }
            }
            try {
                command.setSequenceImage1((Image)convertedParameters.get("sequenceImage1"));
            }
            catch (ClassCastException e) {
                Object[] objectArray = new Object[3];
                objectArray[0] = "sequenceImage1";
                objectArray[1] = "Image";
                objectArray[2] = parameters.get("sequenceImage1") != null ? (parameters.get("sequenceImage1").get() != null ? parameters.get("sequenceImage1").get().getClass().toString() : "null") : "null";
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
            }
        }
        if (parameters.containsKey("sequenceCheckBox2") && parameters.get("sequenceCheckBox2") != null && parameters.get("sequenceCheckBox2").get() != null) {
            convertedParameters.put("sequenceCheckBox2", parameters.get("sequenceCheckBox2").get());
            if (convertedParameters.get("sequenceCheckBox2") != null && !(convertedParameters.get("sequenceCheckBox2") instanceof Boolean)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "sequenceCheckBox2", "Boolean", parameters.get("sequenceCheckBox2").get().getClass().getSimpleName()));
            }
        }
        try {
            command.setSequenceCheckBox2((Boolean)convertedParameters.get("sequenceCheckBox2"));
        }
        catch (ClassCastException e) {
            Object[] objectArray = new Object[3];
            objectArray[0] = "sequenceCheckBox2";
            objectArray[1] = "Boolean";
            objectArray[2] = parameters.get("sequenceCheckBox2") != null ? (parameters.get("sequenceCheckBox2").get() != null ? parameters.get("sequenceCheckBox2").get().getClass().toString() : "null") : "null";
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
        }
        if (convertedParameters.get("sequenceCheckBox2") != null && ((Boolean)convertedParameters.get("sequenceCheckBox2")).booleanValue()) {
            if (parameters.containsKey("sequenceImageText2") && parameters.get("sequenceImageText2") != null && parameters.get("sequenceImageText2").get() != null) {
                convertedParameters.put("sequenceImageText2", parameters.get("sequenceImageText2").get());
                if (convertedParameters.get("sequenceImageText2") != null && !(convertedParameters.get("sequenceImageText2") instanceof Image)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "sequenceImageText2", "Image", parameters.get("sequenceImageText2").get().getClass().getSimpleName()));
                }
            }
            try {
                command.setSequenceImageText2((Image)convertedParameters.get("sequenceImageText2"));
            }
            catch (ClassCastException e) {
                Object[] objectArray = new Object[3];
                objectArray[0] = "sequenceImageText2";
                objectArray[1] = "Image";
                objectArray[2] = parameters.get("sequenceImageText2") != null ? (parameters.get("sequenceImageText2").get() != null ? parameters.get("sequenceImageText2").get().getClass().toString() : "null") : "null";
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
            }
            if (parameters.containsKey("sequenceImage2") && parameters.get("sequenceImage2") != null && parameters.get("sequenceImage2").get() != null) {
                convertedParameters.put("sequenceImage2", parameters.get("sequenceImage2").get());
                if (convertedParameters.get("sequenceImage2") != null && !(convertedParameters.get("sequenceImage2") instanceof Image)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "sequenceImage2", "Image", parameters.get("sequenceImage2").get().getClass().getSimpleName()));
                }
            }
            try {
                command.setSequenceImage2((Image)convertedParameters.get("sequenceImage2"));
            }
            catch (ClassCastException e) {
                Object[] objectArray = new Object[3];
                objectArray[0] = "sequenceImage2";
                objectArray[1] = "Image";
                objectArray[2] = parameters.get("sequenceImage2") != null ? (parameters.get("sequenceImage2").get() != null ? parameters.get("sequenceImage2").get().getClass().toString() : "null") : "null";
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
            }
        }
        if (parameters.containsKey("sequenceCheckBox3") && parameters.get("sequenceCheckBox3") != null && parameters.get("sequenceCheckBox3").get() != null) {
            convertedParameters.put("sequenceCheckBox3", parameters.get("sequenceCheckBox3").get());
            if (convertedParameters.get("sequenceCheckBox3") != null && !(convertedParameters.get("sequenceCheckBox3") instanceof Boolean)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "sequenceCheckBox3", "Boolean", parameters.get("sequenceCheckBox3").get().getClass().getSimpleName()));
            }
        }
        try {
            command.setSequenceCheckBox3((Boolean)convertedParameters.get("sequenceCheckBox3"));
        }
        catch (ClassCastException e) {
            Object[] objectArray = new Object[3];
            objectArray[0] = "sequenceCheckBox3";
            objectArray[1] = "Boolean";
            objectArray[2] = parameters.get("sequenceCheckBox3") != null ? (parameters.get("sequenceCheckBox3").get() != null ? parameters.get("sequenceCheckBox3").get().getClass().toString() : "null") : "null";
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
        }
        if (convertedParameters.get("sequenceCheckBox3") != null && ((Boolean)convertedParameters.get("sequenceCheckBox3")).booleanValue()) {
            if (parameters.containsKey("sequenceImageText3") && parameters.get("sequenceImageText3") != null && parameters.get("sequenceImageText3").get() != null) {
                convertedParameters.put("sequenceImageText3", parameters.get("sequenceImageText3").get());
                if (convertedParameters.get("sequenceImageText3") != null && !(convertedParameters.get("sequenceImageText3") instanceof Image)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "sequenceImageText3", "Image", parameters.get("sequenceImageText3").get().getClass().getSimpleName()));
                }
            }
            try {
                command.setSequenceImageText3((Image)convertedParameters.get("sequenceImageText3"));
            }
            catch (ClassCastException e) {
                Object[] objectArray = new Object[3];
                objectArray[0] = "sequenceImageText3";
                objectArray[1] = "Image";
                objectArray[2] = parameters.get("sequenceImageText3") != null ? (parameters.get("sequenceImageText3").get() != null ? parameters.get("sequenceImageText3").get().getClass().toString() : "null") : "null";
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
            }
            if (parameters.containsKey("sequenceImage3") && parameters.get("sequenceImage3") != null && parameters.get("sequenceImage3").get() != null) {
                convertedParameters.put("sequenceImage3", parameters.get("sequenceImage3").get());
                if (convertedParameters.get("sequenceImage3") != null && !(convertedParameters.get("sequenceImage3") instanceof Image)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "sequenceImage3", "Image", parameters.get("sequenceImage3").get().getClass().getSimpleName()));
                }
            }
            try {
                command.setSequenceImage3((Image)convertedParameters.get("sequenceImage3"));
            }
            catch (ClassCastException e) {
                Object[] objectArray = new Object[3];
                objectArray[0] = "sequenceImage3";
                objectArray[1] = "Image";
                objectArray[2] = parameters.get("sequenceImage3") != null ? (parameters.get("sequenceImage3").get() != null ? parameters.get("sequenceImage3").get().getClass().toString() : "null") : "null";
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
            }
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
