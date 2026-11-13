/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.list;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.ListValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import java.util.ArrayList;
import java.util.List;

public class AddItemAdvanced {
    private static final String POSITION_BEGINNING = "BEGINNING";
    private static final String POSITION_END = "END";
    private static final String POSITION_INDEX = "INDEX";
    private static final String TYPE_STRING = "STRING";
    private static final String TYPE_NUMBER = "NUMBER";
    private static final String TYPE_BOOLEAN = "BOOLEAN";
    private static final String TYPE_DICTIONARY = "DICTIONARY";
    private static final String TYPE_LIST = "LIST";
    private static final String TYPE_DATETIME = "DATETIME";

    public ListValue action(List<Value> inputList, String insertPosition, Double index, String itemType, Value stringItem, Value numberItem, Value booleanItem, Value dictionaryItem, Value listItem, Value dateTimeItem) {
        Value item;
        if (inputList == null) {
            throw new BotCommandException("Input list cannot be null.");
        }
        switch (itemType) {
            case "STRING": {
                item = stringItem;
                break;
            }
            case "NUMBER": {
                item = numberItem;
                break;
            }
            case "BOOLEAN": {
                item = booleanItem;
                break;
            }
            case "DICTIONARY": {
                item = dictionaryItem;
                break;
            }
            case "LIST": {
                item = listItem;
                break;
            }
            case "DATETIME": {
                item = dateTimeItem;
                break;
            }
            default: {
                throw new BotCommandException("Invalid item type.");
            }
        }
        if (item == null) {
            throw new BotCommandException("Item cannot be null.");
        }
        ArrayList<Value> result = new ArrayList<Value>(inputList);
        switch (insertPosition) {
            case "BEGINNING": {
                result.add(0, item);
                break;
            }
            case "END": {
                result.add(item);
                break;
            }
            case "INDEX": {
                if (index == null) {
                    throw new BotCommandException("Index is required.");
                }
                int idx = index.intValue();
                if (idx < 0 || idx > result.size()) {
                    throw new BotCommandException("Index out of range. Valid range: 0 to " + result.size());
                }
                result.add(idx, item);
                break;
            }
            default: {
                throw new BotCommandException("Invalid insert position.");
            }
        }
        ListValue output = new ListValue();
        output.set((List<Value>)result);
        return output;
    }
}
