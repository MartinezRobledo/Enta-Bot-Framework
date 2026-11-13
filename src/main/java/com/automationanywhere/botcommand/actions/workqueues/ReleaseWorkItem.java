/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import java.sql.Connection;

public class ReleaseWorkItem {
    public void execute(String filePath, String identifyBy, Double itemId, String itemKey) {
        AccessExecutor.executeWithConnection(filePath, conn -> {
            WorkqueueItemDao dao = new WorkqueueItemDao((Connection)conn);
            if ("id".equalsIgnoreCase(identifyBy)) {
                dao.releaseFromWorkingById(itemId.longValue());
            } else {
                dao.releaseFromWorkingByKey(itemKey);
            }
            return null;
        });
    }
}
