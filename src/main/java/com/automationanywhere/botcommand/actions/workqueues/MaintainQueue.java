package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.data.impl.NumberValue;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@BotCommand
@CommandPkg(
        label = "Maintain Queue",
        name = "workqueue_purge_before_date",
        description = "Elimina ítems con [Created Date] anterior a la fecha indicada, incluyendo su [Item Data].",
        group_label = "Workqueues",
        icon = "workqueue.svg"
)
public class MaintainQueue {

    @Execute
    public void purge(
            @Idx(index = "1", type = AttributeType.FILE)
            @NotEmpty
            @Pkg(label = "Path to file Access") String filePath,

            @Idx(index = "2", type = AttributeType.DATETIME)
            @NotEmpty
            @Pkg(label = "Fecha umbral", description = "Se eliminarán ítems con [Created Date] estrictamente menor a esta")
            Date cutoffDate
    ) {
        try {
            if (cutoffDate == null) throw new BotCommandException("La fecha umbral es obligatoria.");
            final Timestamp cutoff = new Timestamp(cutoffDate.getTime());

            AccessExecutor.executeVoidWithConnection(filePath, conn -> {
                WorkqueueItemDao dao = new WorkqueueItemDao(conn);
                WorkqueueItemDao.DeletedStats stats = dao.purgeOldItemsByCreatedDate(cutoff);
            });

        } catch (Exception e) {
            throw new BotCommandException("Maintain Queue falló: " + e.getMessage(), e);
        }
    }
}