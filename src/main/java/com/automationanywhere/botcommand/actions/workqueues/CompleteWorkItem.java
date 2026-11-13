/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import java.sql.Connection;

public class CompleteWorkItem {
    public void execute(String filePath, String identifyBy, Double itemId, String itemKey) {
        try {
            AccessExecutor.executeVoidWithConnection(filePath, conn -> {
                WorkqueueItemDao dao = new WorkqueueItemDao((Connection)conn);
                if ("id".equalsIgnoreCase(identifyBy)) {
                    if (itemId == null) {
                        throw new BotCommandException("Debe indicar el Item Id.");
                    }
                    dao.completeFromWorkingById(itemId.longValue());
                } else if ("key".equalsIgnoreCase(identifyBy)) {
                    if (itemKey == null || itemKey.isBlank()) {
                        throw new BotCommandException("Debe indicar el Item Key.");
                    }
                    dao.completeFromWorkingByKey(itemKey);
                } else {
                    throw new BotCommandException("Valor inv\u00e1lido en 'Identificar \u00edtem por': " + identifyBy);
                }
            });
        }
        catch (Exception e) {
            throw new BotCommandException("Complete Work Item: " + e.getMessage());
        }
    }
}
