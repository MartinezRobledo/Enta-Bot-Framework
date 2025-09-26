package com.automationanywhere.botcommand.utilities.workqueue;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.data.impl.NumberValue;
import com.automationanywhere.botcommand.data.impl.StringValue;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class Helpers {

    public static DictionaryValue toDictionary(WorkqueueItemDao.WorkItem item) {
        Map<String, Value> root = new LinkedHashMap<>();

        // Id
        root.put("Id", new NumberValue(BigDecimal.valueOf(item.id)));

        // Key / StepWorkflow como String
        putString(root, "Key", item.key);
        putString(root, "StepWorkflow", item.stepWorkflow);

        // StatusWorkflow como Number (Long). Si es null, no se agrega la clave.
        if (item.statusWorkflow != null) {
            root.put("StatusWorkflow",
                    new NumberValue(BigDecimal.valueOf(item.statusWorkflow)));
        }

        // Data: diccionario anidado
        Map<String, Value> dataMap = new LinkedHashMap<>();
        if (item.data != null) {
            item.data.forEach((k, v) -> dataMap.put(k, new StringValue(v == null ? "" : v)));
        }
        root.put("Data", new DictionaryValue(dataMap));

        return new DictionaryValue(root);
    }

    private static void putString(Map<String, Value> m, String k, String v) {
        m.put(k, new StringValue(v == null ? "" : v));
    }
}
