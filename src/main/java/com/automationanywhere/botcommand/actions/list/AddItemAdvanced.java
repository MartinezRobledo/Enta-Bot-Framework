package com.automationanywhere.botcommand.actions.list;

import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.*;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.ListValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import java.util.ArrayList;
import java.util.List;

@BotCommand
@CommandPkg(
        name = "list_add_item_advanced",
        label = "Add Item",
        description = "Adds an item at the beginning, end, or a specific index of a list.",
        node_label = "Add {{itemType}} to {{inputList}}",
        group_label = "Lists",
        icon = "list.svg",
        return_label = "Updated List",
        return_type = DataType.LIST,
        return_required = false
)
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

    @Execute
    public ListValue action(
            @Idx(index = "1", type = AttributeType.LIST)
            @Pkg(label = "Input List", description = "Target list")
            @NotEmpty
            List<Value> inputList,

            @Idx(index = "2", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "2.1", pkg = @Pkg(label = "Beginning", value = "BEGINNING")),
                    @Idx.Option(index = "2.2", pkg = @Pkg(label = "End", value = "END")),
                    @Idx.Option(index = "2.3", pkg = @Pkg(label = "Index", value = "INDEX"))
            })
            @Pkg(label = "Insert Position", default_value = "END", default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes("SINGLE_SELECT")
            String insertPosition,

            @Idx(index = "2.3.1", type = AttributeType.NUMBER)
            @Pkg(label = "Index", description = "Position where the item will be inserted")
            Double index,

            @Idx(index = "3", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "3.1", pkg = @Pkg(label = "String", value = "STRING")),
                    @Idx.Option(index = "3.2", pkg = @Pkg(label = "Number", value = "NUMBER")),
                    @Idx.Option(index = "3.3", pkg = @Pkg(label = "Boolean", value = "BOOLEAN")),
                    @Idx.Option(index = "3.4", pkg = @Pkg(label = "Dictionary", value = "DICTIONARY")),
                    @Idx.Option(index = "3.5", pkg = @Pkg(label = "List", value = "LIST")),
                    @Idx.Option(index = "3.6", pkg = @Pkg(label = "DateTime", value = "DATETIME"))
            })
            @Pkg(label = "Item Type", default_value = "STRING", default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes("SINGLE_SELECT")
            String itemType,

            @Idx(index = "3.1.1", type = AttributeType.VARIABLE)
            @Pkg(label = "String Item")
            @VariableType(DataType.STRING)
            Value stringItem,

            @Idx(index = "3.2.1", type = AttributeType.VARIABLE)
            @Pkg(label = "Number Item")
            @VariableType(DataType.NUMBER)
            Value numberItem,

            @Idx(index = "3.3.1", type = AttributeType.VARIABLE)
            @Pkg(label = "Boolean Item")
            @VariableType(DataType.BOOLEAN)
            Value booleanItem,

            @Idx(index = "3.4.1", type = AttributeType.VARIABLE)
            @Pkg(label = "Dictionary Item")
            @VariableType(DataType.DICTIONARY)
            Value dictionaryItem,

            @Idx(index = "3.5.1", type = AttributeType.VARIABLE)
            @Pkg(label = "List Item")
            @VariableType(DataType.LIST)
            Value listItem,

            @Idx(index = "3.6.1", type = AttributeType.VARIABLE)
            @Pkg(label = "DateTime Item")
            @VariableType(DataType.DATETIME)
            Value dateTimeItem
    ) {
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
