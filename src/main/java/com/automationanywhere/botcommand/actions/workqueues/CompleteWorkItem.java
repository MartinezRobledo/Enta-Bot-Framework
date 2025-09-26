package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueSession;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.annotations.rules.SelectModes;
import com.automationanywhere.commandsdk.annotations.rules.SessionObject;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;

@BotCommand
@CommandPkg(
        label = "Complete Work Item",
        name = "workqueue_complete_item",
        description = "Marca un item como Completed",
        group_label = "Workqueues",
        icon = "workqueue.svg"
)
public class CompleteWorkItem {

    @Execute
    public void execute(
            @Idx(index = "1", type = AttributeType.FILE)
            @NotEmpty
            @Pkg(label = "Path to file Access") String filePath,

            // ---- Selector Id/Key ----
            @Idx(index = "2", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "2.1", pkg = @Pkg(label = "By Id",  value = "id")),
                    @Idx.Option(index = "2.2", pkg = @Pkg(label = "By Key", value = "key"))
            })
            @Pkg(label = "Select item by", default_value = "id", default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes
            String identifyBy,

            @Idx(index = "2.1.1", type = AttributeType.NUMBER)
            @Pkg(label = "Item Id (workqueue.Id)")
            @NotEmpty
            Double itemId, // Usamos Double porque NUMBER del SDK llega como Double

            @Idx(index = "2.2.1", type = AttributeType.TEXT)
            @Pkg(label = "Item Key (workqueue.Key)")
            @NotEmpty
            String itemKey
    ) {
        try {
            AccessExecutor.executeVoidWithConnection(filePath, conn -> {
                WorkqueueItemDao dao = new WorkqueueItemDao(conn);
                if ("id".equalsIgnoreCase(identifyBy)) {
                    if (itemId == null) throw new BotCommandException("Debe indicar el Item Id.");
                    dao.completeFromWorkingById(itemId.longValue());
                } else if ("key".equalsIgnoreCase(identifyBy)) {
                    if (itemKey == null || itemKey.isBlank())
                        throw new BotCommandException("Debe indicar el Item Key.");
                    dao.completeFromWorkingByKey(itemKey);
                } else {
                    throw new BotCommandException("Valor inválido en 'Identificar ítem por': " + identifyBy);
                }
            });
        } catch (Exception e) {
            throw new BotCommandException("Complete Work Item: " + e.getMessage());
        }
    }
}
