import com.automationanywhere.botcommand.actions.conversion.FileToBase64;
import com.automationanywhere.botcommand.actions.list.AddItemAdvanced;
import com.automationanywhere.botcommand.actions.workqueues.UpsertItemDataField;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.utilities.logger.HTMLGenerator;
import com.automationanywhere.botcommand.utilities.workqueue.ItemDataTxnBuffer;
import java.nio.file.*;
import java.util.*;

public class RecoverySmoke {
    private static int checks;
    static void equal(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) throw new AssertionError(expected + " != " + actual);
        checks++;
    }
    static void rejected(Runnable action) {
        try { action.run(); } catch (RuntimeException expected) { checks++; return; }
        throw new AssertionError("Expected rejection");
    }
    public static void main(String[] args) throws Exception {
        Path temp = Files.createTempFile("bot-recovery-test-", ".bin");
        try {
            byte[] bytes = new byte[]{0, 1, 2, -1, 10};
            Files.write(temp, bytes);
            equal(Base64.getEncoder().encodeToString(bytes), new FileToBase64().action(temp.toString()).get());
            rejected(() -> new FileToBase64().action(""));
            rejected(() -> new FileToBase64().action(temp + ".missing"));
        } finally { Files.deleteIfExists(temp); }
        List<Value> input = new ArrayList<>(Arrays.asList(new StringValue("a"), new StringValue("b")));
        AddItemAdvanced add = new AddItemAdvanced();
        for (String position : Arrays.asList("BEGINNING", "END", "INDEX")) {
            List<Value> result = add.action(input, position, 1.0, "STRING", new StringValue("x"), null, null, null, null, null).get();
            int expectedIndex = position.equals("BEGINNING") ? 0 : position.equals("END") ? 2 : 1;
            equal("x", result.get(expectedIndex).get());
            equal(3, result.size());
        }
        equal(2, input.size());
        rejected(() -> add.action(input, "INDEX", 9.0, "STRING", new StringValue("x"), null, null, null, null, null));
        rejected(() -> add.action(null, "END", null, "STRING", new StringValue("x"), null, null, null, null, null));
        Map<String, Value> vars = new LinkedHashMap<>();
        equal("", HTMLGenerator.generateHTML(vars));
        vars.put("a", new StringValue("a"));
        equal("<span class='variable-count'>1 variable</span>", HTMLGenerator.generateHTML(vars));
        vars.put("b", new StringValue("b"));
        equal("<span class='variable-count'>2 variables</span>", HTMLGenerator.generateHTML(vars));
        ItemDataTxnBuffer.ScopeKey scope = ItemDataTxnBuffer.scopeById("recovery-test", 1);
        ItemDataTxnBuffer.addUpsert(scope, "key", "first");
        ItemDataTxnBuffer.addUpsert(scope, "key", "second");
        equal("second", ItemDataTxnBuffer.peek(scope).get("key"));
        equal(1, ItemDataTxnBuffer.drain(scope).size());
        equal(0, ItemDataTxnBuffer.peek(scope).size());
        rejected(() -> new UpsertItemDataField().upsert("unused", "id", null, null, "k", "v"));
        rejected(() -> new UpsertItemDataField().upsert("unused", "key", null, "", "k", "v"));
        System.out.println("PASS: " + checks + " checks");
    }
}
