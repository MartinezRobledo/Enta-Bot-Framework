package com.automationanywhere.botcommand.actions.list;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.ListValue;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.annotations.BotCommand;
import com.automationanywhere.commandsdk.annotations.CommandPkg;
import com.automationanywhere.commandsdk.annotations.Execute;
import com.automationanywhere.commandsdk.annotations.Idx;
import com.automationanywhere.commandsdk.annotations.Pkg;
import com.automationanywhere.commandsdk.annotations.rules.ListType;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;

import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;

@BotCommand
@CommandPkg(
        label = "Remove Duplicates",
        node_label = "From {{inputList}}",
        description = "Devuelve una lista sin duplicados, trimmeando cada valor y preservando el orden.",
        icon = "list.svg",
        name = "list_remove_duplicates_trim",
        group_label = "Lists",
        return_type = DataType.LIST,
        return_label = "Clean List"
)
public class RemoveDuplicatesFromList {

    @Execute
    public ListValue action(
            @Idx(index = "1", type = AttributeType.LIST)
            @Pkg(label = "Input List", description = "Lista de valores a limpiar (se aplicará trim y se eliminarán duplicados)")
            @NotEmpty
            @ListType(DataType.STRING)
            List<Value> inputList
    ) {
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
