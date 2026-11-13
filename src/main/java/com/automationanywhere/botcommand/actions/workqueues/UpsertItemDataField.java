package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.*;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;

import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.Helpers;
import com.automationanywhere.botcommand.utilities.workqueue.ItemDataTxnBuffer;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import java.sql.Connection;
import java.util.LinkedHashMap;

@BotCommand
@CommandPkg(
        name = "workqueue_upsert_item_data_field",
        label = "Upsert Item Data Field",
        description = "Actualiza el valor de un campo en [Item Data]. Si no existe, lo crea (staging, requiere Commit).",
        group_label = "Workqueues",
        icon = "workqueue.svg",
        return_label = "Asignar metadatos del item a",
        return_type = DataType.DICTIONARY,
        return_required = false
)
public class UpsertItemDataField {
    @Execute
    public DictionaryValue upsert(
            @Idx(index = "1", type = AttributeType.FILE)
            @Pkg(label = "Path to file Access")
            @NotEmpty
            String filePath,

            @Idx(index = "2", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "2.1", pkg = @Pkg(label = "By Id", value = "id")),
                    @Idx.Option(index = "2.2", pkg = @Pkg(label = "By Key", value = "key"))
            })
            @Pkg(label = "Select item by", default_value = "id", default_value_type = DataType.STRING)
            String identifyBy,

            @Idx(index = "2.1.1", type = AttributeType.NUMBER)
            @Pkg(label = "Item Id (workqueue.Id)")
            Double itemId,

            @Idx(index = "2.2.1", type = AttributeType.TEXT)
            @Pkg(label = "Item Key (workqueue.Key)")
            String itemKey,

            @Idx(index = "3", type = AttributeType.TEXT)
            @Pkg(label = "Data Key ([Item Data].[Key])")
            @NotEmpty
            String dataKey,

            @Idx(index = "4", type = AttributeType.TEXT)
            @Pkg(label = "Data Value ([Item Data].[Value])")
            @NotEmpty
            String dataValue
    ) {
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
                throw new BotCommandException("Modo inválido: " + identifyBy);
            }
            ItemDataTxnBuffer.addUpsert(scope, dataKey, dataValue);
            boolean finalById = byId;
            return AccessExecutor.executeWithConnection(filePath, conn -> {
                WorkqueueItemDao.WorkItem base;
                WorkqueueItemDao dao = new WorkqueueItemDao((Connection)conn);
                WorkqueueItemDao.WorkItem workItem = base = finalById ? dao.readWorkItemById(itemId.longValue()) : dao.readWorkItemByKey(itemKey);
                if (base == null) {
                    throw new BotCommandException("El ítem no existe.");
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
            throw new BotCommandException("UpsertItemDataField (staging) falló: " + e.getMessage(), e);
        }
    }
}
