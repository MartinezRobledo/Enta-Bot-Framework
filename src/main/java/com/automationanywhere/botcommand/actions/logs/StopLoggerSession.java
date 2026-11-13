/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.logs;

import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.logger.CustomLogger;

public class StopLoggerSession {
    public void stop(CustomLogger session) {
        if (session.isClosed()) {
            throw new BotCommandException("Logger session not found");
        }
        session.close();
    }
}
