/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Date;

public class MaintainQueue {
    public void purge(String filePath, Date cutoffDate) {
        try {
            if (cutoffDate == null) {
                throw new BotCommandException("La fecha umbral es obligatoria.");
            }
            Timestamp cutoff = new Timestamp(cutoffDate.getTime());
            AccessExecutor.executeVoidWithConnection(filePath, conn -> {
                WorkqueueItemDao dao = new WorkqueueItemDao((Connection)conn);
                WorkqueueItemDao.DeletedStats stats = dao.purgeOldItemsByCreatedDate(cutoff);
            });
        }
        catch (Exception e) {
            throw new BotCommandException("Maintain Queue fall\u00f3: " + e.getMessage(), e);
        }
    }
}
