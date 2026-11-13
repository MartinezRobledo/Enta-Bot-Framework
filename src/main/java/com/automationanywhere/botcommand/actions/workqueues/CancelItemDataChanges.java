// src/main/java/com/automationanywhere/botcommand/actions/workqueues/CancelItemDataChanges.java
package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.botcommand.utilities.workqueue.Helpers;
import java.sql.Connection;

import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import com.automationanywhere.botcommand.utilities.workqueue.ItemDataTxnBuffer;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;

import static com.automationanywhere.botcommand.utilities.workqueue.Helpers.toDictionary;

@BotCommand
@CommandPkg(
        label = "Cancel Item Data",
        name = "workqueue_cancel_item_data",
        description = "Descarta los cambios staged de [Item Data] (no impacta la base).",
        group_label = "Workqueues",
        icon = "workqueue.svg",
        return_type = DataType.DICTIONARY,
        return_label = "Asignar metadatos del item a"
)
public class CancelItemDataChanges {

    @Execute
    public DictionaryValue cancel(
            @Idx(index = "1", type = AttributeType.FILE)
            @NotEmpty
            @Pkg(label = "Path to file Access") String filePath,

            @Idx(index = "2", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "2.1", pkg = @Pkg(label = "By Id",  value = "id")),
                    @Idx.Option(index = "2.2", pkg = @Pkg(label = "By Key", value = "key"))
            })
            @Pkg(label = "Select item by", default_value = "id", default_value_type = DataType.STRING)
            @NotEmpty
            String identifyBy,

            @Idx(index = "2.1.1", type = AttributeType.NUMBER)
            @Pkg(label = "Item Id (workqueue.Id)")
            Double itemId,

            @Idx(index = "2.2.1", type = AttributeType.TEXT)
            @Pkg(label = "Item Key (workqueue.Key)")
            String itemKey
    ) {
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
                throw new BotCommandException("Valor inválido en 'Identificar ítem por': " + identifyBy);
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
                    throw new BotCommandException("El ítem no existe.");
                }
                return Helpers.toDictionary(item);
            });
        }
        catch (Exception e) {
            throw new BotCommandException("CancelItemDataChanges falló: " + e.getMessage(), e);
        }
    }
}
