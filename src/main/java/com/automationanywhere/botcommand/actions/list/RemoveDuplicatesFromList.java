/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.list;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.ListValue;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class RemoveDuplicatesFromList {
    public ListValue action(List<Value> inputList) {
        try {
            if (inputList == null) {
                throw new BotCommandException("La lista no puede ser nula.");
            }
            ArrayList<StringValue> output = new ArrayList<StringValue>();
            LinkedHashSet<String> seen = new LinkedHashSet<String>();
            for (Value v : inputList) {
                String raw = v == null || v.get() == null ? "" : v.get().toString();
                String trimmed = raw.trim();
                if (!seen.add(trimmed)) continue;
                output.add(new StringValue(trimmed));
            }
            ListValue lv = new ListValue();
            lv.set((List<Value>)new ArrayList<Value>(output));
            return lv;
        }
        catch (Exception e) {
            throw new BotCommandException(e.getMessage());
        }
    }
}
