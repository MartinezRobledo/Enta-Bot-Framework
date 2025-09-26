package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;

@BotCommand
@CommandPkg(
        label = "Release Work Item",
        name = "workqueue_release_item",
        description = "Libera un item WORKING a PENDING (por Id o Key).",
        group_label = "Workqueues",
        icon = "workqueue.svg"
)
public class ReleaseWorkItem {

    @Execute
    public void execute(
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
            @NotEmpty
            @Pkg(label = "Item Id") Double itemId,

            @Idx(index = "2.2.1", type = AttributeType.TEXT)
            @NotEmpty
            @Pkg(label = "Item Key") String itemKey
    ) {
        AccessExecutor.executeWithConnection(filePath, conn -> {
            WorkqueueItemDao dao = new WorkqueueItemDao(conn);
            if ("id".equalsIgnoreCase(identifyBy)) {
                dao.releaseFromWorkingById(itemId.longValue());
            } else {
                dao.releaseFromWorkingByKey(itemKey);
            }
            return null;
        });
    }

}
