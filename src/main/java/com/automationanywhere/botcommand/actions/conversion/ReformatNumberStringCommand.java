/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.conversion;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.BotCommand;
import com.automationanywhere.botcommand.actions.conversion.ReformatNumberString;
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

public final class ReformatNumberStringCommand
implements BotCommand {
    private static final Logger logger = LogManager.getLogger(ReformatNumberStringCommand.class);
    private static final Messages MESSAGES_GENERIC = MessagesFactory.getMessages("com.automationanywhere.commandsdk.generic.messages");

    @Override
    @Deprecated
    public Optional<Value> execute(Map<String, Value> parameters, Map<String, Object> sessionMap) {
        return this.execute(null, parameters, sessionMap);
    }

    @Override
    public Optional<Value> execute(GlobalSessionContext globalSessionContext, Map<String, Value> parameters, Map<String, Object> sessionMap) {
        logger.traceEntry(() -> parameters != null ? parameters.entrySet().stream().filter(en -> !Arrays.asList(new String[0]).contains(en.getKey()) && en.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString() : null, () -> sessionMap != null ? sessionMap.toString() : null);
        ReformatNumberString command = new ReformatNumberString();
        HashMap convertedParameters = new HashMap();
        if (parameters.containsKey("amount") && parameters.get("amount") != null && parameters.get("amount").get() != null) {
            convertedParameters.put("amount", parameters.get("amount").get());
            if (convertedParameters.get("amount") != null && !(convertedParameters.get("amount") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "amount", "String", parameters.get("amount").get().getClass().getSimpleName()));
            }
        }
        if (parameters.containsKey("targetFormat") && parameters.get("targetFormat") != null && parameters.get("targetFormat").get() != null) {
            convertedParameters.put("targetFormat", parameters.get("targetFormat").get());
            if (convertedParameters.get("targetFormat") != null && !(convertedParameters.get("targetFormat") instanceof String)) {
                throw new BotCommandException(MESSAGES_GENERIC.getString("generic.UnexpectedTypeReceived", "targetFormat", "String", parameters.get("targetFormat").get().getClass().getSimpleName()));
            }
        }
        if (convertedParameters.get("targetFormat") != null) {
            switch ((String)convertedParameters.get("targetFormat")) {
                case "d.ddd,dd": {
                    break;
                }
                case "d,ddd.dd": {
                    break;
                }
                case "dddd.dd": {
                    break;
                }
                case "dddd,dd": {
                    break;
                }
                default: {
                    throw new BotCommandException(MESSAGES_GENERIC.getString("generic.InvalidOption", "targetFormat"));
                }
            }
        }
        try {
            Optional<Value> result = Optional.ofNullable(command.action((String)convertedParameters.get("amount"), (String)convertedParameters.get("targetFormat")));
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
