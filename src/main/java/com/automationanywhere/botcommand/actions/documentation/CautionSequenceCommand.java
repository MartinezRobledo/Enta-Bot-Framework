/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.documentation;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.BotCommand;
import com.automationanywhere.botcommand.actions.documentation.CautionSequence;
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

public final class CautionSequenceCommand
implements BotCommand {
    private static final Logger logger = LogManager.getLogger(CautionSequenceCommand.class);
    private static final Messages MESSAGES_GENERIC = MessagesFactory.getMessages("com.automationanywhere.commandsdk.generic.messages");

    @Override
    @Deprecated
    public Optional<Value> execute(Map<String, Value> parameters, Map<String, Object> sessionMap) {
        return this.execute(null, parameters, sessionMap);
    }

    @Override
    public Optional<Value> execute(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        logger.traceEntry(() -> parameters != null ? parameters.entrySet().stream().filter(en -> !Arrays.asList(new String[0]).contains(en.getKey()) && en.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString() : null, () -> sessionMap != null ? sessionMap.toString() : null);
        CautionSequence command = new CautionSequence();
        HashMap convertedParameters = new HashMap();
        if (parameters.containsKey("cautionSequenceTitle") && parameters.get("cautionSequenceTitle") != null && parameters.get("cautionSequenceTitle").get() != null) {
            convertedParameters.put("cautionSequenceTitle", parameters.get("cautionSequenceTitle").get());
            if (convertedParameters.get("cautionSequenceTitle") != null && !(convertedParameters.get("cautionSequenceTitle") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "cautionSequenceTitle", "String", parameters.get("cautionSequenceTitle").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("cautionSequenceTitle") == null) {
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.validation.notEmpty", "cautionSequenceTitle"));
        }
        try {
            command.setCautionSequenceTitle((String)convertedParameters.get("cautionSequenceTitle"));
        }
        catch (ClassCastException e) {
            Object[] objectArray = new Object[3];
            objectArray[0] = "cautionSequenceTitle";
            objectArray[1] = "String";
            objectArray[2] = parameters.get("cautionSequenceTitle") != null ? (parameters.get("cautionSequenceTitle").get() != null ? parameters.get("cautionSequenceTitle").get().getClass().toString() : "null") : "null";
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
        }
        if (parameters.containsKey("cautionSequenceDescription") && parameters.get("cautionSequenceDescription") != null && parameters.get("cautionSequenceDescription").get() != null) {
            convertedParameters.put("cautionSequenceDescription", parameters.get("cautionSequenceDescription").get());
            if (convertedParameters.get("cautionSequenceDescription") != null && !(convertedParameters.get("cautionSequenceDescription") instanceof Map)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "cautionSequenceDescription", "Map", parameters.get("cautionSequenceDescription").get().getClass().getSimpleName()));
            }
        }
        try {
            command.setCautionSequenceDescription((Map)convertedParameters.get("cautionSequenceDescription"));
        }
        catch (ClassCastException e) {
            Object[] objectArray = new Object[3];
            objectArray[0] = "cautionSequenceDescription";
            objectArray[1] = "Map<String, Value>";
            objectArray[2] = parameters.get("cautionSequenceDescription") != null ? (parameters.get("cautionSequenceDescription").get() != null ? parameters.get("cautionSequenceDescription").get().getClass().toString() : "null") : "null";
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
        }
        if (parameters.containsKey("cautionSequenceCheckBox1") && parameters.get("cautionSequenceCheckBox1") != null && parameters.get("cautionSequenceCheckBox1").get() != null) {
            convertedParameters.put("cautionSequenceCheckBox1", parameters.get("cautionSequenceCheckBox1").get());
            if (convertedParameters.get("cautionSequenceCheckBox1") != null && !(convertedParameters.get("cautionSequenceCheckBox1") instanceof Boolean)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "cautionSequenceCheckBox1", "Boolean", parameters.get("cautionSequenceCheckBox1").get().getClass().getSimpleName()));
            }
        }
        try {
            command.setCautionSequenceCheckBox1((Boolean)convertedParameters.get("cautionSequenceCheckBox1"));
        }
        catch (ClassCastException e) {
            Object[] objectArray = new Object[3];
            objectArray[0] = "cautionSequenceCheckBox1";
            objectArray[1] = "Boolean";
            objectArray[2] = parameters.get("cautionSequenceCheckBox1") != null ? (parameters.get("cautionSequenceCheckBox1").get() != null ? parameters.get("cautionSequenceCheckBox1").get().getClass().toString() : "null") : "null";
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
        }
        if (convertedParameters.get("cautionSequenceCheckBox1") != null && ((Boolean)convertedParameters.get("cautionSequenceCheckBox1")).booleanValue()) {
            if (parameters.containsKey("cautionSequenceText1") && parameters.get("cautionSequenceText1") != null && parameters.get("cautionSequenceText1").get() != null) {
                convertedParameters.put("cautionSequenceText1", parameters.get("cautionSequenceText1").get());
                if (convertedParameters.get("cautionSequenceText1") != null && !(convertedParameters.get("cautionSequenceText1") instanceof Image)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "cautionSequenceText1", "Image", parameters.get("cautionSequenceText1").get().getClass().getSimpleName()));
                }
            }
            try {
                command.setCautionSequenceText1((Image)convertedParameters.get("cautionSequenceText1"));
            }
            catch (ClassCastException e) {
                Object[] objectArray = new Object[3];
                objectArray[0] = "cautionSequenceText1";
                objectArray[1] = "Image";
                objectArray[2] = parameters.get("cautionSequenceText1") != null ? (parameters.get("cautionSequenceText1").get() != null ? parameters.get("cautionSequenceText1").get().getClass().toString() : "null") : "null";
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
            }
            if (parameters.containsKey("cautionSequenceImage1") && parameters.get("cautionSequenceImage1") != null && parameters.get("cautionSequenceImage1").get() != null) {
                convertedParameters.put("cautionSequenceImage1", parameters.get("cautionSequenceImage1").get());
                if (convertedParameters.get("cautionSequenceImage1") != null && !(convertedParameters.get("cautionSequenceImage1") instanceof Image)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "cautionSequenceImage1", "Image", parameters.get("cautionSequenceImage1").get().getClass().getSimpleName()));
                }
            }
            try {
                command.setCautionSequenceImage1((Image)convertedParameters.get("cautionSequenceImage1"));
            }
            catch (ClassCastException e) {
                Object[] objectArray = new Object[3];
                objectArray[0] = "cautionSequenceImage1";
                objectArray[1] = "Image";
                objectArray[2] = parameters.get("cautionSequenceImage1") != null ? (parameters.get("cautionSequenceImage1").get() != null ? parameters.get("cautionSequenceImage1").get().getClass().toString() : "null") : "null";
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
            }
        }
        if (parameters.containsKey("cautionSequenceCheckBox2") && parameters.get("cautionSequenceCheckBox2") != null && parameters.get("cautionSequenceCheckBox2").get() != null) {
            convertedParameters.put("cautionSequenceCheckBox2", parameters.get("cautionSequenceCheckBox2").get());
            if (convertedParameters.get("cautionSequenceCheckBox2") != null && !(convertedParameters.get("cautionSequenceCheckBox2") instanceof Boolean)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "cautionSequenceCheckBox2", "Boolean", parameters.get("cautionSequenceCheckBox2").get().getClass().getSimpleName()));
            }
        }
        try {
            command.setCautionSequenceCheckBox2((Boolean)convertedParameters.get("cautionSequenceCheckBox2"));
        }
        catch (ClassCastException e) {
            Object[] objectArray = new Object[3];
            objectArray[0] = "cautionSequenceCheckBox2";
            objectArray[1] = "Boolean";
            objectArray[2] = parameters.get("cautionSequenceCheckBox2") != null ? (parameters.get("cautionSequenceCheckBox2").get() != null ? parameters.get("cautionSequenceCheckBox2").get().getClass().toString() : "null") : "null";
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
        }
        if (convertedParameters.get("cautionSequenceCheckBox2") != null && ((Boolean)convertedParameters.get("cautionSequenceCheckBox2")).booleanValue()) {
            if (parameters.containsKey("cautionSequenceText2") && parameters.get("cautionSequenceText2") != null && parameters.get("cautionSequenceText2").get() != null) {
                convertedParameters.put("cautionSequenceText2", parameters.get("cautionSequenceText2").get());
                if (convertedParameters.get("cautionSequenceText2") != null && !(convertedParameters.get("cautionSequenceText2") instanceof Image)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "cautionSequenceText2", "Image", parameters.get("cautionSequenceText2").get().getClass().getSimpleName()));
                }
            }
            try {
                command.setCautionSequenceText2((Image)convertedParameters.get("cautionSequenceText2"));
            }
            catch (ClassCastException e) {
                Object[] objectArray = new Object[3];
                objectArray[0] = "cautionSequenceText2";
                objectArray[1] = "Image";
                objectArray[2] = parameters.get("cautionSequenceText2") != null ? (parameters.get("cautionSequenceText2").get() != null ? parameters.get("cautionSequenceText2").get().getClass().toString() : "null") : "null";
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
            }
            if (parameters.containsKey("cautionSequenceImage2") && parameters.get("cautionSequenceImage2") != null && parameters.get("cautionSequenceImage2").get() != null) {
                convertedParameters.put("cautionSequenceImage2", parameters.get("cautionSequenceImage2").get());
                if (convertedParameters.get("cautionSequenceImage2") != null && !(convertedParameters.get("cautionSequenceImage2") instanceof Image)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "cautionSequenceImage2", "Image", parameters.get("cautionSequenceImage2").get().getClass().getSimpleName()));
                }
            }
            try {
                command.setCautionSequenceImage2((Image)convertedParameters.get("cautionSequenceImage2"));
            }
            catch (ClassCastException e) {
                Object[] objectArray = new Object[3];
                objectArray[0] = "cautionSequenceImage2";
                objectArray[1] = "Image";
                objectArray[2] = parameters.get("cautionSequenceImage2") != null ? (parameters.get("cautionSequenceImage2").get() != null ? parameters.get("cautionSequenceImage2").get().getClass().toString() : "null") : "null";
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
            }
        }
        if (parameters.containsKey("cautionSequenceCheckBox3") && parameters.get("cautionSequenceCheckBox3") != null && parameters.get("cautionSequenceCheckBox3").get() != null) {
            convertedParameters.put("cautionSequenceCheckBox3", parameters.get("cautionSequenceCheckBox3").get());
            if (convertedParameters.get("cautionSequenceCheckBox3") != null && !(convertedParameters.get("cautionSequenceCheckBox3") instanceof Boolean)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "cautionSequenceCheckBox3", "Boolean", parameters.get("cautionSequenceCheckBox3").get().getClass().getSimpleName()));
            }
        }
        try {
            command.setCautionSequenceCheckBox3((Boolean)convertedParameters.get("cautionSequenceCheckBox3"));
        }
        catch (ClassCastException e) {
            Object[] objectArray = new Object[3];
            objectArray[0] = "cautionSequenceCheckBox3";
            objectArray[1] = "Boolean";
            objectArray[2] = parameters.get("cautionSequenceCheckBox3") != null ? (parameters.get("cautionSequenceCheckBox3").get() != null ? parameters.get("cautionSequenceCheckBox3").get().getClass().toString() : "null") : "null";
            throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
        }
        if (convertedParameters.get("cautionSequenceCheckBox3") != null && ((Boolean)convertedParameters.get("cautionSequenceCheckBox3")).booleanValue()) {
            if (parameters.containsKey("cautionSequenceText3") && parameters.get("cautionSequenceText3") != null && parameters.get("cautionSequenceText3").get() != null) {
                convertedParameters.put("cautionSequenceText3", parameters.get("cautionSequenceText3").get());
                if (convertedParameters.get("cautionSequenceText3") != null && !(convertedParameters.get("cautionSequenceText3") instanceof Image)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "cautionSequenceText3", "Image", parameters.get("cautionSequenceText3").get().getClass().getSimpleName()));
                }
            }
            try {
                command.setCautionSequenceText3((Image)convertedParameters.get("cautionSequenceText3"));
            }
            catch (ClassCastException e) {
                Object[] objectArray = new Object[3];
                objectArray[0] = "cautionSequenceText3";
                objectArray[1] = "Image";
                objectArray[2] = parameters.get("cautionSequenceText3") != null ? (parameters.get("cautionSequenceText3").get() != null ? parameters.get("cautionSequenceText3").get().getClass().toString() : "null") : "null";
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.ClassCastException", objectArray), e);
            }
            if (parameters.containsKey("cautionSequenceImage3") && parameters.get("cautionSequenceImage3") != null && parameters.get("cautionSequenceImage3").get() != null) {
                convertedParameters.put("cautionSequenceImage3", parameters.get("cautionSequenceImage3").get());
                if (convertedParameters.get("cautionSequenceImage3") != null && !(convertedParameters.get("cautionSequenceImage3") instanceof Image)) {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "cautionSequenceImage3", "Image", parameters.get("cautionSequenceImage3").get().getClass().getSimpleName()));
                }
            }
            try {
                command.setCautionSequenceImage3((Image)convertedParameters.get("cautionSequenceImage3"));
            }
            catch (ClassCastException e) {
                Object[] objectArray = new Object[3];
                objectArray[0] = "cautionSequenceImage3";
                objectArray[1] = "Image";
                objectArray[2] = parameters.get("cautionSequenceImage3") != null ? (parameters.get("cautionSequenceImage3").get() != null ? parameters.get("cautionSequenceImage3").get().getClass().toString() : "null") : "null";
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
