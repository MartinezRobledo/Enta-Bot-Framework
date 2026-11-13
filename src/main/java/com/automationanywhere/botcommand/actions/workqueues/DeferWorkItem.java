/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DeferWorkItem {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void execute(String filePath, String identifyBy, Double itemId, String itemKey, String selectBy, ZonedDateTime deferUntil, Double deferOffset) {
        try {
            AccessExecutor.executeVoidWithConnection(filePath, conn -> {
                Timestamp ts;
                WorkqueueItemDao dao = new WorkqueueItemDao((Connection)conn);
                if ("offset".equalsIgnoreCase(selectBy)) {
                    if (deferOffset == null || deferOffset <= 0.0) {
                        throw new BotCommandException("El offset debe ser un n\u00famero mayor a 0.");
                    }
                    ts = Timestamp.from(Instant.now().plusSeconds((long)(deferOffset * 60.0)));
                } else {
                    if (deferUntil == null) {
                        throw new BotCommandException("Debe indicar la fecha/hora destino.");
                    }
                    ts = Timestamp.from(deferUntil.toInstant());
                }
                if ("id".equalsIgnoreCase(identifyBy)) {
                    if (itemId == null) {
                        throw new BotCommandException("Debe indicar el Item Id.");
                    }
                    dao.deferFromWorkingById(itemId.longValue(), ts);
                } else if ("key".equalsIgnoreCase(identifyBy)) {
                    if (itemKey == null || itemKey.isBlank()) {
                        throw new BotCommandException("Debe indicar el Item Key.");
                    }
                    dao.deferFromWorkingByKey(itemKey, ts);
                } else {
                    throw new BotCommandException("Valor inv\u00e1lido en 'Identificar \u00edtem por': " + identifyBy);
                }
            });
        }
        catch (Exception e) {
            throw new BotCommandException("Defer Work Item: " + e.getMessage(), e);
        }
    }
}
