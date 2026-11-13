/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.Helpers;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import java.sql.Connection;
import java.util.LinkedHashMap;

public class GetNextItem {
    public DictionaryValue get(String filePath) {
        return AccessExecutor.executeWithConnection(filePath, conn -> {
            WorkqueueItemDao dao = new WorkqueueItemDao((Connection)conn);
            WorkqueueItemDao.WorkItem item = dao.takeNextPendingAndSetWorking();
            if (item != null) {
                return Helpers.toDictionary(item);
            }
            return new DictionaryValue(new LinkedHashMap<String, Value>());
        });
    }
}
