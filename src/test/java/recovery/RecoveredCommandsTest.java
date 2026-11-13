package recovery;

import com.automationanywhere.botcommand.actions.conversion.FileToBase64Command;
import com.automationanywhere.botcommand.actions.list.AddItemAdvancedCommand;
import com.automationanywhere.botcommand.conditional.KeyNotExistsConditionCondition;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.data.impl.ListValue;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Exercises adapters generated from annotations, rather than calling action methods directly. */
public class RecoveredCommandsTest {
    @Test
    public void base64AdapterConvertsFile() throws Exception {
        Path file = Files.createTempFile("base64-command-", ".bin");
        byte[] bytes = {0, 1, -1, 10};
        try {
            Files.write(file, bytes);
            Map<String, Value> parameters = new HashMap<>();
            parameters.put("filePath", new StringValue(file.toString()));
            Value result = new FileToBase64Command().execute(null, parameters, Collections.emptyMap()).get();
            Assert.assertEquals(result.get(), Base64.getEncoder().encodeToString(bytes));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test(expectedExceptions = BotCommandException.class)
    public void base64AdapterRequiresFileParameter() {
        new FileToBase64Command().execute(null, Collections.emptyMap(), Collections.emptyMap());
    }

    @Test
    public void listAdapterPreservesValueWrapper() {
        Map<String, Value> parameters = new HashMap<>();
        List<Value> initial = new ArrayList<>();
        initial.add(new StringValue("first"));
        ListValue input = new ListValue();
        input.set(initial);
        parameters.put("inputList", input);
        parameters.put("insertPosition", new StringValue("END"));
        parameters.put("itemType", new StringValue("STRING"));
        parameters.put("stringItem", new StringValue("last"));
        ListValue result = (ListValue) new AddItemAdvancedCommand()
                .execute(null, parameters, Collections.emptyMap()).get();
        List<Value> values = result.get();
        Assert.assertEquals(values.size(), 2);
        Assert.assertEquals(values.get(1).get(), "last");
        Assert.assertEquals(initial.size(), 1);
    }

    @Test
    public void generatedConditionReceivesDictionaryAndKey() {
        Map<String, Value> dictionary = new HashMap<>();
        dictionary.put("present", new StringValue("value"));
        Map<String, Value> parameters = new HashMap<>();
        parameters.put("dictionary", new DictionaryValue(dictionary));
        parameters.put("key", new StringValue("present"));
        KeyNotExistsConditionCondition condition = new KeyNotExistsConditionCondition();
        Assert.assertFalse(condition.test(null, parameters, Collections.emptyMap()));
        parameters.put("key", new StringValue("missing"));
        Assert.assertTrue(condition.test(null, parameters, Collections.emptyMap()));
    }
}
