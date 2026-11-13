package com.automationanywhere.botcommand.utilities.workqueue;

public enum ItemIdentifierType {
    ID, KEY;

    public static ItemIdentifierType from(String raw) {
        if (raw == null) return null;
        switch (raw.trim().toUpperCase()) {
            case "ID": return ID;
            case "KEY": return KEY;
            default: throw new IllegalArgumentException("IdentifierType inválido: " + raw);
        }
    }
}
