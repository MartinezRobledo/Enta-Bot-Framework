/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.logs;

import com.automationanywhere.botcommand.data.impl.SessionValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.logger.CustomLogger;
import java.util.HashMap;
import java.util.UUID;
import org.apache.logging.log4j.Level;

public class StartLoggerSession {
    private static final String COMMON_FILE_ALL_LEVEL = "COMMON_FILE";
    private static final String CONFIGURABLE_FILE_ALL_LEVEL = "CONFIGURABLE_FILE";

    public SessionValue start(String logLevelsAndFileOption, String logFilePath, String infoLogFilePath, String warnLogFilePath, String errorLogFilePath, Number rollingFileSizeMB) {
        try {
            CustomLogger customLogger;
            switch (logLevelsAndFileOption) {
                case "COMMON_FILE": {
                    customLogger = new CustomLogger("CustomLogger_" + String.valueOf(UUID.randomUUID()), logFilePath, rollingFileSizeMB.longValue());
                    break;
                }
                case "CONFIGURABLE_FILE": {
                    HashMap<Level, String> levelFilePathMap = new HashMap<Level, String>();
                    levelFilePathMap.put(Level.INFO, infoLogFilePath);
                    levelFilePathMap.put(Level.WARN, warnLogFilePath);
                    levelFilePathMap.put(Level.ERROR, errorLogFilePath);
                    customLogger = new CustomLogger("CustomLogger_" + String.valueOf(UUID.randomUUID()), levelFilePathMap, rollingFileSizeMB.longValue());
                    break;
                }
                default: {
                    throw new BotCommandException("Invalid log level and file option");
                }
            }
            return SessionValue.builder().withSessionObject(customLogger).build();
        }
        catch (Exception e) {
            throw new BotCommandException("Error occurred while creating new session: " + e.getMessage());
        }
    }
}
