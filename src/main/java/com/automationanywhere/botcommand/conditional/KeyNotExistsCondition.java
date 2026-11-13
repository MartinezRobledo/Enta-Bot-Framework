/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.conditional;

import java.util.Map;

public class KeyNotExistsCondition {
    public Boolean keyNotExists(Map<String, Object> dictionary, String key) {
        if (key == null) {
            return true;
        }
        if (dictionary == null || dictionary.isEmpty()) {
            return true;
        }
        return !dictionary.containsKey(key);
    }
}
