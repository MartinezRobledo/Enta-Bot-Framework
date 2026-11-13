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
import java.util.LinkedHashMap;

public class AddItemDataField {
    public DictionaryValue add(String filePath, String identifyBy, Double itemId, String itemKey, String dataKey, String dataValue) {
        try {
            ItemDataTxnBuffer.ScopeKey scope;
            boolean byId;
            if ("id".equalsIgnoreCase(identifyBy)) {
                if (itemId == null) {
                    throw new BotCommandException("Debe indicar el Item Id.");
                }
                byId = true;
                scope = ItemDataTxnBuffer.scopeById(filePath, itemId.longValue());
            } else if ("key".equalsIgnoreCase(identifyBy)) {
                if (itemKey == null || itemKey.isBlank()) {
                    throw new BotCommandException("Debe indicar el Item Key.");
                }
                byId = false;
                scope = ItemDataTxnBuffer.scopeByKey(filePath, itemKey);
            } else {
                throw new BotCommandException("Valor inv\u00e1lido en 'Identificar \u00edtem por': " + identifyBy);
            }
            try {
                ItemDataTxnBuffer.addInsert(scope, dataKey, dataValue);
            }
            catch (IllegalArgumentException | IllegalStateException ex) {
                throw new BotCommandException(ex.getMessage(), ex);
            }
            boolean finalById = byId;
            return AccessExecutor.executeWithConnection(filePath, conn -> {
                WorkqueueItemDao.WorkItem base;
                WorkqueueItemDao dao = new WorkqueueItemDao((Connection)conn);
                WorkqueueItemDao.WorkItem workItem = base = finalById ? dao.readWorkItemById(itemId.longValue()) : dao.readWorkItemByKey(itemKey);
                if (base == null) {
                    throw new BotCommandException("El \u00edtem no existe.");
                }
                LinkedHashMap<String, String> effective = new LinkedHashMap<String, String>();
                if (base.data != null) {
                    effective.putAll(base.data);
                }
                ItemDataTxnBuffer.peek(scope).forEach(effective::put);
                WorkqueueItemDao.WorkItem preview = new WorkqueueItemDao.WorkItem(base.id, base.key, base.statusWorkflow, base.stepWorkflow, effective);
                return Helpers.toDictionary(preview);
            });
        }
        catch (Exception e) {
            throw new BotCommandException("AddItemDataField (staging) fall\u00f3: " + e.getMessage(), e);
        }
    }
}
