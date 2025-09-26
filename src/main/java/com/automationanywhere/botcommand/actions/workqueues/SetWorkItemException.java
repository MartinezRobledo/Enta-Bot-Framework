package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueSession;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.annotations.rules.SelectModes;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;
import com.automationanywhere.commandsdk.annotations.rules.SessionObject;

@BotCommand
@CommandPkg(
        label = "Set Work Item Exception",
        name = "workqueue_set_item_exception",
        description = "Marca un item como Exception con razón",
        group_label = "Workqueues",
        icon = "workqueue.svg"
)
public class SetWorkItemException {

    @Execute
    public void execute(
            @Idx(index = "1", type = AttributeType.FILE)
            @NotEmpty
            @Pkg(label = "Path to file Access") String filePath,

            @Idx(index = "2", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "2.1", pkg = @Pkg(label = "By Id",  value = "id")),
                    @Idx.Option(index = "2.2", pkg = @Pkg(label = "By Key", value = "key"))
            })
            @Pkg(label = "Identify item by", default_value = "id", default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes
            String identifyBy,

            @Idx(index = "2.1.1", type = AttributeType.NUMBER)
            @Pkg(label = "Item Id (workqueue.Id)")
            @NotEmpty
            Double itemId,

            @Idx(index = "2.2.1", type = AttributeType.TEXT)
            @Pkg(label = "Item Key (workqueue.Key)")
            @NotEmpty
            String itemKey,

            @Idx(index = "3", type = AttributeType.TEXT)
            @Pkg(label = "Exception Reason")
            @NotEmpty
            String reason
    ) {
        try {
            AccessExecutor.executeVoidWithConnection(filePath, conn -> {
                WorkqueueItemDao dao = new WorkqueueItemDao(conn);
                if ("id".equalsIgnoreCase(identifyBy)) {
                    if (itemId == null) throw new BotCommandException("Debe indicar el Item Id.");
                    dao.exceptionFromWorkingById(itemId.longValue(), reason);
                } else if ("key".equalsIgnoreCase(identifyBy)) {
                    if (itemKey == null || itemKey.isBlank())
                        throw new BotCommandException("Debe indicar el Item Key.");
                    dao.exceptionFromWorkingByKey(itemKey, reason);
                } else {
                    throw new BotCommandException("Valor inválido en 'Identificar ítem por': " + identifyBy);
                }
            });
        } catch (Exception e) {
            throw new BotCommandException("Set Work Item Exception: " + e.getMessage());
        }
    }
}
