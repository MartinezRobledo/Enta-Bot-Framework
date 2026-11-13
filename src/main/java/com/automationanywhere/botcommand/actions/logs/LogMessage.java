/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.logs;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.logger.CustomLogger;
import com.automationanywhere.botcommand.utilities.logger.DataConversion;
import com.automationanywhere.botcommand.utilities.screen.CaptureScreen;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class LogMessage {
    private static final String LEVEL_INFO = "INFO";
    private static final String LEVEL_ERROR = "ERROR";
    private static final String LEVEL_WARN = "WARN";
    private static final String LOG_VARIABLE = "YES";
    private static final String DO_NOT_LOG_VARIABLE = "NO";
    private String colName;
    private Value colValue;
    private GlobalSessionContext globalSessionContext;
    private String testBotUri;

    public void setTestBotUri(String testBotUri) {
        this.testBotUri = testBotUri;
    }

    public void setGlobalSessionContext(GlobalSessionContext globalSessionContext) {
        this.globalSessionContext = globalSessionContext;
    }

    public void action(CustomLogger session, String logLevel, String logMessage, Boolean captureScreenshot, String logVariable, List<Value> list, Map<String, Value> sourceMap) {
        try {
            Map<String, Value> variableValues = null;
            String screenshotPath = "";
            Level log4jLevel = this.convertToLog4jLevel(logLevel);
            if (logVariable.equalsIgnoreCase(LOG_VARIABLE)) {
                variableValues = sourceMap != null && !sourceMap.isEmpty() ? DataConversion.getMergedDictionary(list, sourceMap) : DataConversion.getMergedDictionary(list);
            }
            if (captureScreenshot.booleanValue()) {
                String screenshotFolder = session.getScreenshotFolderPath(log4jLevel);
                screenshotPath = LogMessage.generateRandomScreenshotPath(screenshotFolder, logLevel);
                CaptureScreen.captureDesktop(screenshotPath, true);
            }
            HashMap<String, Object> message = new HashMap<String, Object>();
            message.put("Message", logMessage);
            message.put("Screenshot", screenshotPath);
            message.put("Variables", variableValues);
            message.put("Source", this.getFormattedBotUri());
            if (variableValues != null && !variableValues.isEmpty()) {
                message.put("variablesFolderPath", session.getVariablesFolderPath(log4jLevel));
            }
            Logger logger = session.getLogger();
            switch (logLevel) {
                case "INFO": {
                    logger.info(message);
                    break;
                }
                case "WARN": {
                    logger.warn(message);
                    break;
                }
                case "ERROR": {
                    logger.error(message);
                    break;
                }
                default: {
                    throw new BotCommandException("Invalid log level");
                }
            }
        }
        catch (Exception e) {
            throw new BotCommandException("Error occurred: " + e.getMessage());
        }
    }

    private Level convertToLog4jLevel(String logLevel) {
        switch (logLevel) {
            case "INFO": {
                return Level.INFO;
            }
            case "WARN": {
                return Level.WARN;
            }
            case "ERROR": {
                return Level.ERROR;
            }
        }
        return Level.INFO;
    }

    public static String generateRandomScreenshotPath(String folderPath, String logLevel) {
        String prefix = "info_";
        if (logLevel.equals(LEVEL_WARN)) {
            prefix = "warn_";
        } else if (logLevel.equals(LEVEL_ERROR)) {
            prefix = "error_";
        }
        String fileName = prefix + String.valueOf(UUID.randomUUID()) + ".png";
        Path path = Paths.get(folderPath, fileName);
        return path.toAbsolutePath().toString();
    }

    public String getFormattedBotUri() {
        if (this.testBotUri != null) {
            return this.testBotUri;
        }
        String botUri = this.globalSessionContext.getBotUri();
        botUri = URLDecoder.decode(botUri, StandardCharsets.UTF_8);
        botUri = botUri.substring(botUri.indexOf("Automation Anywhere") + "Automation Anywhere".length(), botUri.indexOf(63));
        botUri = botUri.replace("/", "\\");
        return botUri;
    }
}
