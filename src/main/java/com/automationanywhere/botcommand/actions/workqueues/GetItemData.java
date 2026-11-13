/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

public class GetItemData {
    public DictionaryValue get(String filePath, String identifyBy, Double itemId, String itemKey) {
        try {
            return AccessExecutor.executeWithConnection(filePath, conn -> {
                Map<String, String> rawDict;
                WorkqueueItemDao dao = new WorkqueueItemDao((Connection)conn);
                if ("id".equalsIgnoreCase(identifyBy)) {
                    if (itemId == null) {
                        throw new BotCommandException("Debe indicar el Item Id.");
                    }
                    rawDict = dao.getItemDataById(itemId.longValue());
                } else if ("key".equalsIgnoreCase(identifyBy)) {
                    if (itemKey == null || itemKey.isBlank()) {
                        throw new BotCommandException("Debe indicar el Item Key.");
                    }
                    rawDict = dao.getItemDataByKey(itemKey);
                } else {
                    throw new BotCommandException("Valor inv\u00e1lido en 'Identificar \u00edtem por': " + identifyBy);
                }
                LinkedHashMap<String, Value> dict = new LinkedHashMap<String, Value>();
                for (Map.Entry<String, String> entry : rawDict.entrySet()) {
                    dict.put(entry.getKey(), new StringValue(entry.getValue()));
                }
                return new DictionaryValue(dict);
            });
        }
        catch (Exception e) {
            throw new BotCommandException("Error al obtener Item Data para '" + itemKey + "': " + e.getMessage());
        }
    }
}
