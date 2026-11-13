package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;

import java.util.LinkedHashMap;

import static com.automationanywhere.botcommand.utilities.workqueue.Helpers.toDictionary;

@BotCommand
@CommandPkg(
        label = "Get Next Item",
        name = "workqueue_get_next_item",
        description = "Get next 'Pending' item and check it as 'Working'",
        group_label = "Workqueues",
        icon = "workqueue.svg",
        return_type = DataType.DICTIONARY,
        return_required = true,
        return_label = "Assign item to"
)
public class GetNextItem {

    @Execute
    public DictionaryValue get(
            @Idx(index = "1", type = AttributeType.FILE)
            @NotEmpty
            @Pkg(label = "Path to file Access") String filePath
    ) {
        return AccessExecutor.executeWithConnection(filePath, conn -> {
            WorkqueueItemDao dao = new WorkqueueItemDao(conn);
            WorkqueueItemDao.WorkItem item = dao.takeNextPendingAndSetWorking();

            if (item != null)
                return toDictionary(item);
            else
                return new DictionaryValue(new LinkedHashMap<>());
        });
    }

}
