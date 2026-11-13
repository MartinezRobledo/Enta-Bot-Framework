/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.utilities.workqueue;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.data.impl.NumberValue;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class Helpers {
    public static DictionaryValue toDictionary(WorkqueueItemDao.WorkItem item) {
        LinkedHashMap<String, Value> root = new LinkedHashMap<String, Value>();
        root.put("Id", new NumberValue(BigDecimal.valueOf(item.id)));
        Helpers.putString(root, "Key", item.key);
        Helpers.putString(root, "StepWorkflow", item.stepWorkflow);
        if (item.statusWorkflow != null) {
            root.put("StatusWorkflow", new NumberValue(BigDecimal.valueOf(item.statusWorkflow)));
        }
        LinkedHashMap<String, Value> dataMap = new LinkedHashMap<String, Value>();
        if (item.data != null) {
            item.data.forEach((k, v) -> dataMap.put((String)k, new StringValue(v == null ? "" : v)));
        }
        root.put("Data", new DictionaryValue(dataMap));
        return new DictionaryValue(root);
    }

    private static void putString(Map<String, Value> m, String k, String v) {
        m.put(k, new StringValue(v == null ? "" : v));
    }
}
