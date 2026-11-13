package com.automationanywhere.botcommand.conditional;

import com.automationanywhere.commandsdk.annotations.BotCommand;
import com.automationanywhere.commandsdk.annotations.CommandPkg;
import com.automationanywhere.commandsdk.annotations.ConditionTest;
import com.automationanywhere.commandsdk.annotations.Idx;
import com.automationanywhere.commandsdk.annotations.Pkg;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.model.AttributeType;

import java.util.Map;

/**
 * Condición: devuelve TRUE si la clave NO existe en el diccionario.
 * Aparece en el panel de If/Loop como una condición seleccionable.
 */
@BotCommand(commandType = BotCommand.CommandType.Condition)
@CommandPkg(
        label = "Diccionario: clave NO existe",
        name = "KeyNotExists",
        description = "Retorna TRUE si la clave no existe en el diccionario.",
        node_label = "{{dictionary}} no contiene la clave {{key}}",
        icon = "dict_key_not_exists.svg" // opcional: ícono en resources
)
public class KeyNotExistsCondition {

    @ConditionTest
    public Boolean keyNotExists(
            @Idx(index = "1", type = AttributeType.DICTIONARY)
            @Pkg(label = "Diccionario")
            @NotEmpty
            Map<String, Object> dictionary,

            @Idx(index = "2", type = AttributeType.TEXT)
            @Pkg(label = "Clave")
            @NotEmpty
            String key
    ) {
        // Null o vacío => tratamos como “no existe”
        if (key == null) return true;
        if (dictionary == null || dictionary.isEmpty()) return true;

        // Por defecto A360 trata las claves de diccionario como sensibles a mayúsculas/minúsculas,
        // así que usamos containsKey tal cual (case-sensitive).
        // (Si quisieras ignorar mayúsculas, iterá keys y compará equalsIgnoreCase)
        return !dictionary.containsKey(key);
    }
}
