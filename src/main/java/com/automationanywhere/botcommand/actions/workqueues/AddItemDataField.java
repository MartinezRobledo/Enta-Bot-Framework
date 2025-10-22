// src/main/java/com/automationanywhere/botcommand/actions/workqueues/AddItemDataField.java
package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import com.automationanywhere.botcommand.utilities.workqueue.ItemDataTxnBuffer;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.automationanywhere.botcommand.utilities.workqueue.Helpers.toDictionary;

@BotCommand
@CommandPkg(
        label = "Add Item Data Field",
        name = "workqueue_add_item_data_field",
        description = "Encola (staging) un nuevo campo en [Item Data] del ítem (se aplicará con Commit).",
        group_label = "Workqueues",
        icon = "workqueue.svg",
        return_type = DataType.DICTIONARY,
        return_label = "Asignar metadatos del item a"
)
public class AddItemDataField {

    @Execute
    public DictionaryValue add(
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
            // 1) Encolar en memoria (no impacta BD)
            ItemDataTxnBuffer.ScopeKey scope;
            boolean byId;
            if ("id".equalsIgnoreCase(identifyBy)) {
                if (itemId == null) throw new BotCommandException("Debe indicar el Item Id.");
                byId = true;
                scope = ItemDataTxnBuffer.scopeById(filePath, itemId.longValue());
            } else if ("key".equalsIgnoreCase(identifyBy)) {
                if (itemKey == null || itemKey.isBlank()) throw new BotCommandException("Debe indicar el Item Key.");
                byId = false;
                scope = ItemDataTxnBuffer.scopeByKey(filePath, itemKey);
            } else {
                throw new BotCommandException("Valor inválido en 'Identificar ítem por': " + identifyBy);
            }

            try {
                ItemDataTxnBuffer.addInsert(scope, dataKey, dataValue);
            } catch (IllegalStateException | IllegalArgumentException ex) {
                throw new BotCommandException(ex.getMessage(), ex);
            }

            // 2) Leer desde BD y devolver "previsualización" (BD + staging)
            boolean finalById = byId;
            return AccessExecutor.executeWithConnection(filePath, conn -> {
                WorkqueueItemDao dao = new WorkqueueItemDao(conn);
                WorkqueueItemDao.WorkItem base = finalById
                        ? dao.readWorkItemById(itemId.longValue())
                        : dao.readWorkItemByKey(itemKey);

                if (base == null) throw new BotCommandException("El ítem no existe.");

                Map<String,String> effective = new LinkedHashMap<>();
                if (base.data != null) effective.putAll(base.data);
                ItemDataTxnBuffer.peek(scope).forEach(effective::put);

                WorkqueueItemDao.WorkItem preview = new WorkqueueItemDao.WorkItem(
                        base.id, base.key, base.statusWorkflow, base.stepWorkflow, effective
                );
                return toDictionary(preview);
            });

        } catch (Exception e) {
            throw new BotCommandException("AddItemDataField (staging) falló: " + e.getMessage(), e);
        }
    }
}