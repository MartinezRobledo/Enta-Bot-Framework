package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.*;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.Helpers;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import java.sql.Connection;
import java.util.LinkedHashMap;

@BotCommand
@CommandPkg(
        name = "workqueue_get_last_item",
        label = "Get Last Item",
        description = "Get the most recently created item (any status) without modifying it",
        group_label = "Workqueues",
        icon = "workqueue.svg",
        return_label = "Assign item to",
        return_type = DataType.DICTIONARY,
        return_required = true
)
public class GetLastItem {
    @Execute
    public DictionaryValue get(
            @Idx(index = "1", type = AttributeType.FILE)
            @Pkg(label = "Path to file Access")
            @NotEmpty
            String filePath
    ) {
        return AccessExecutor.executeWithConnection(filePath, conn -> {
            WorkqueueItemDao dao = new WorkqueueItemDao((Connection)conn);
            WorkqueueItemDao.WorkItem item = dao.getLastItem();
            if (item != null) {
                return Helpers.toDictionary(item);
            }
            return new DictionaryValue(new LinkedHashMap<String, Value>());
        });
    }
}
