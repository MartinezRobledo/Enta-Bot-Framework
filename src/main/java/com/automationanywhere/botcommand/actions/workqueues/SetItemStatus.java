/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.Helpers;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import java.sql.Connection;

public class SetItemStatus {
    public DictionaryValue set(String filePath, String identifyBy, Double itemId, String itemKey, Double statusWorkflow, String stepWorkflow) {
        try {
            return AccessExecutor.executeWithConnection(filePath, conn -> {
                WorkqueueItemDao dao = new WorkqueueItemDao((Connection)conn);
                WorkqueueItemDao.WorkItem item = null;
                if ("id".equalsIgnoreCase(identifyBy)) {
                    if (itemId == null) {
                        throw new BotCommandException("Debe indicar el Item Id.");
                    }
                    item = dao.updateWorkflowById(itemId.longValue(), statusWorkflow.longValue(), stepWorkflow);
                } else if ("key".equalsIgnoreCase(identifyBy)) {
                    if (itemKey == null || itemKey.isBlank()) {
                        throw new BotCommandException("Debe indicar el Item Key.");
                    }
                    item = dao.updateWorkflowByKey(itemKey, statusWorkflow.longValue(), stepWorkflow);
                } else {
                    throw new BotCommandException("Valor inv\u00e1lido en 'Identificar \u00edtem por': " + identifyBy);
                }
                if (item == null) {
                    throw new BotCommandException("El \u00edtem no existe o no se pudo actualizar el workflow.");
                }
                return Helpers.toDictionary(item);
            });
        }
        catch (Exception e) {
            throw new BotCommandException("SetItemStatus fall\u00f3: " + e.getMessage(), e);
        }
    }
}
