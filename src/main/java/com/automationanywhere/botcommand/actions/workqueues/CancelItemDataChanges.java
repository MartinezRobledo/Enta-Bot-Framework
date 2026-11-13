/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.Helpers;
import com.automationanywhere.botcommand.utilities.workqueue.ItemDataTxnBuffer;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import java.sql.Connection;

public class CancelItemDataChanges {
    public DictionaryValue cancel(String filePath, String identifyBy, Double itemId, String itemKey) {
        try {
            ItemDataTxnBuffer.ScopeKey scope;
            boolean byId;
            long id = 0L;
            String key = null;
            if ("id".equalsIgnoreCase(identifyBy)) {
                if (itemId == null) {
                    throw new BotCommandException("Debe indicar el Item Id.");
                }
                byId = true;
                id = itemId.longValue();
                scope = ItemDataTxnBuffer.scopeById(filePath, id);
            } else if ("key".equalsIgnoreCase(identifyBy)) {
                if (itemKey == null || itemKey.isBlank()) {
                    throw new BotCommandException("Debe indicar el Item Key.");
                }
                byId = false;
                key = itemKey;
                scope = ItemDataTxnBuffer.scopeByKey(filePath, key);
            } else {
                throw new BotCommandException("Valor inv\u00e1lido en 'Identificar \u00edtem por': " + identifyBy);
            }
            ItemDataTxnBuffer.cancel(scope);
            boolean finalById = byId;
            long finalId = id;
            String finalKey = key;
            return AccessExecutor.executeWithConnection(filePath, conn -> {
                WorkqueueItemDao.WorkItem item;
                WorkqueueItemDao dao = new WorkqueueItemDao((Connection)conn);
                WorkqueueItemDao.WorkItem workItem = item = finalById ? dao.readWorkItemById(finalId) : dao.readWorkItemByKey(finalKey);
                if (item == null) {
                    throw new BotCommandException("El \u00edtem no existe.");
                }
                return Helpers.toDictionary(item);
            });
        }
        catch (Exception e) {
            throw new BotCommandException("CancelItemDataChanges fall\u00f3: " + e.getMessage(), e);
        }
    }
}
