package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;

import static com.automationanywhere.botcommand.utilities.workqueue.Helpers.toDictionary;

@BotCommand
@CommandPkg(
        label = "Set Item Status",
        name = "workqueue_set_item_status",
        description = "Actualiza StatusWorkflow y StepWorkflow de un ítem en estado WORKING.",
        group_label = "Workqueues",
        icon = "workqueue.svg",
        return_type = DataType.DICTIONARY,
        return_required = true,
        return_label = "Asignar metadatos del item a"
)
public class SetItemStatus {

    @Execute
    public DictionaryValue set(
            @Idx(index = "1", type = AttributeType.FILE)
            @NotEmpty
            @Pkg(label = "Path to file Access") String filePath,

            @Idx(index = "2", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "2.1", pkg = @Pkg(label = "By Id", value = "id")),
                    @Idx.Option(index = "2.2", pkg = @Pkg(label = "By Key", value = "key"))
            })
            @Pkg(label = "Select item by", default_value = "id", default_value_type = DataType.STRING)
            @NotEmpty
            String identifyBy,

            @Idx(index = "2.1.1", type = AttributeType.NUMBER)
            @Pkg(label = "Item Id (workqueue.Id)")
            @NotEmpty
            Double itemId,

            @Idx(index = "2.2.1", type = AttributeType.TEXT)
            @Pkg(label = "Item Key (workqueue.Key)")
            @NotEmpty
            String itemKey,

            @Idx(index = "3", type = AttributeType.NUMBER)
            @Pkg(label = "Status Workflow")
            @NotEmpty
            Double statusWorkflow,

            @Idx(index = "4", type = AttributeType.TEXT)
            @Pkg(label = "Step Workflow")
            @NotEmpty
            String stepWorkflow
    ) {
        try {
            return AccessExecutor.executeWithConnection(filePath, conn -> {
                WorkqueueItemDao dao = new WorkqueueItemDao(conn);
                WorkqueueItemDao.WorkItem item = null;

                if ("id".equalsIgnoreCase(identifyBy)) {
                    if (itemId == null) throw new BotCommandException("Debe indicar el Item Id.");
                    item = dao.updateWorkflowById(itemId.longValue(), statusWorkflow.longValue(), stepWorkflow);
                } else if ("key".equalsIgnoreCase(identifyBy)) {
                    if (itemKey == null || itemKey.isBlank())
                        throw new BotCommandException("Debe indicar el Item Key.");
                    item = dao.updateWorkflowByKey(itemKey, statusWorkflow.longValue(), stepWorkflow);
                } else {
                    throw new BotCommandException("Valor inválido en 'Identificar ítem por': " + identifyBy);
                }


                if (item == null) {
                    // Por si el DAO pudiera retornar null (en nuestro refactor lanza excepción si no actualiza)
                    throw new BotCommandException("El ítem no existe o no se pudo actualizar el workflow.");
                }

                return toDictionary(item);

            });

        } catch (Exception e) {
            throw new BotCommandException("SetItemStatus falló: " + e.getMessage(), e);
        }
    }
}
