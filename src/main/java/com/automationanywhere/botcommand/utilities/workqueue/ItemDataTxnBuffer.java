// src/main/java/com/automationanywhere/botcommand/utilities/workqueue/ItemDataTxnBuffer.java
package com.automationanywhere.botcommand.utilities.workqueue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ItemDataTxnBuffer {

    private static final Map<ScopeKey, LinkedHashMap<String,String>> PENDING = new ConcurrentHashMap<>();

    public static ScopeKey scopeById(String filePath, long itemId) {
        return new ScopeKey(ItemDataTxnBuffer.normalize(filePath), "id", itemId, null);
    }

    public static ScopeKey scopeByKey(String filePath, String itemKey) {
        return new ScopeKey(ItemDataTxnBuffer.normalize(filePath), "key", null, itemKey);
    }

    // Agrega una inserción (falla si la clave ya estaba staged)
    public static void addInsert(ScopeKey scope, String dataKey, String dataValue) {
        if (dataKey == null || dataKey.isBlank()) {
            throw new IllegalArgumentException("La clave de Item Data es obligatoria.");
        }
        PENDING.compute(scope, (k, map) -> {
            if (map == null) {
                map = new LinkedHashMap<String, String>();
            }
            if (map.containsKey(dataKey)) {
                throw new IllegalStateException("La clave '" + dataKey + "' ya fue agregada en staging.");
            }
            map.put(dataKey, dataValue);
            return map;
        });
    }

    // Devuelve una copia inmutable de lo staged (puede ser vacío)
    public static Map<String,String> peek(ScopeKey scope) {
        Map m = PENDING.get(scope);
        return m == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap(m));
    }

    // Toma y limpia (para Commit)
    public static Map<String,String> drain(ScopeKey scope) {
        LinkedHashMap<String, String> m = PENDING.remove(scope);
        return m == null ? Collections.emptyMap() : m;
    }

    // Cancela (limpia)
    public static void cancel(ScopeKey scope) {
        PENDING.remove(scope);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim();
    }

    // --- identificador de alcance ---
    public static final class ScopeKey {
        public final String filePath;
        public final String identifyBy; // "id" | "key"
        public final Long itemId;
        public final String itemKey;

        private ScopeKey(String filePath, String identifyBy, Long itemId, String itemKey) {
            this.filePath = filePath;
            this.identifyBy = identifyBy;
            this.itemId = itemId;
            this.itemKey = itemKey;
        }

        @Override public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ScopeKey)) {
                return false;
            }
            ScopeKey that = (ScopeKey)o;
            return Objects.equals(this.filePath, that.filePath) && Objects.equals(this.identifyBy, that.identifyBy) && Objects.equals(this.itemId, that.itemId) && Objects.equals(this.itemKey, that.itemKey);
        }

        @Override public int hashCode() {
            return Objects.hash(this.filePath, this.identifyBy, this.itemId, this.itemKey);
        }

        @Override public String toString() {
            return "ScopeKey{" + this.identifyBy + "=" + String.valueOf(this.itemId != null ? this.itemId : this.itemKey) + ", db=" + this.filePath + "}";
        }
    }

    public static void addUpsert(ScopeKey scope, String dataKey, String dataValue) {
        if (dataKey == null || dataKey.isBlank()) {
            throw new IllegalArgumentException("La clave de Item Data es obligatoria.");
        }
        PENDING.compute(scope, (k, map) -> {
            if (map == null) {
                map = new LinkedHashMap<String, String>();
            }
            map.put(dataKey, dataValue);
            return map;
        });
    }
}
