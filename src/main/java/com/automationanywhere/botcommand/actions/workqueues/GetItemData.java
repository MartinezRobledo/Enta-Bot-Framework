package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueSession;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.annotations.rules.SessionObject;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

@BotCommand
@CommandPkg(
        label = "Get Item Data",
        name = "workqueue_get_item_data",
        description = "Obtiene el diccionario de datos (Key/Value) del ítem",
        group_label = "Workqueues",
        icon = "workqueue.svg",
        return_type = DataType.DICTIONARY,
        return_required = true,
        return_label = "Asignar a"
        //return_settings = {ReturnSettingsType.OUTPUT}
)
public class GetItemData {

    @Execute
    public DictionaryValue get(
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
            @NotEmpty
            Double itemId,

            @Idx(index = "2.2.1", type = AttributeType.TEXT)
            @Pkg(label = "Item Key (workqueue.Key)")
            @NotEmpty
            String itemKey
    ) {
        try {
            return AccessExecutor.executeWithConnection(filePath, conn -> {
                WorkqueueItemDao dao = new WorkqueueItemDao(conn);
                Map<String, String> rawDict;

                if ("id".equalsIgnoreCase(identifyBy)) {
                    if (itemId == null) throw new BotCommandException("Debe indicar el Item Id.");
                    rawDict = dao.getItemDataById(itemId.longValue());
                } else if ("key".equalsIgnoreCase(identifyBy)) {
                    if (itemKey == null || itemKey.isBlank())
                        throw new BotCommandException("Debe indicar el Item Key.");
                    rawDict = dao.getItemDataByKey(itemKey);
                } else {
                    throw new BotCommandException("Valor inválido en 'Identificar ítem por': " + identifyBy);
                }

                // convertir Map<String,String> → Map<String,Value>
                Map<String, Value> dict = new LinkedHashMap<>();
                for (Map.Entry<String, String> entry : rawDict.entrySet()) {
                    dict.put(entry.getKey(), new StringValue(entry.getValue()));
                }

                return new DictionaryValue(dict);
            });

        } catch (Exception e) {
            throw new BotCommandException("Error al obtener Item Data para '" + itemKey + "': " + e.getMessage());
        }
    }
}
