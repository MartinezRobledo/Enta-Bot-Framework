package recovery;

import com.automationanywhere.botcommand.actions.workqueues.CommitItemDataChanges;
import com.automationanywhere.botcommand.utilities.workqueue.ItemDataTxnBuffer;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Comprueba las diferencias recuperadas con una base Access temporal. */
public class WorkqueueDeltaTest {
    private Path directory;
    private Path database;
    private Connection conn;
    private String previousDateTimeType;

    @BeforeMethod
    public void setUp() throws Exception {
        // Jackcess 3 usa LocalDateTime para los valores de fecha de este fixture Java 11.
        previousDateTimeType = System.getProperty("com.healthmarketscience.jackcess.dateTimeType");
        System.setProperty("com.healthmarketscience.jackcess.dateTimeType", "LOCAL_DATE_TIME");
        directory = Files.createTempDirectory("workqueue-delta-");
        database = directory.resolve("queue.accdb");
        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        conn = DriverManager.getConnection("jdbc:ucanaccess://" + database + ";newDatabaseVersion=V2010;immediatelyReleaseResources=true");
        try (Statement statement = conn.createStatement()) {
            statement.execute("CREATE TABLE [workqueue] ([Id] COUNTER PRIMARY KEY, [Key] VARCHAR(255), [Status] VARCHAR(255), " +
                    "[Status Workflow] LONG, [Step Workflow] VARCHAR(255), [Created Date] DATETIME, [Last Updated] DATETIME, " +
                    "[Defer Until] DATETIME, [Priority] LONG, [Exception Reason] VARCHAR(255))");
            statement.execute("CREATE TABLE [Item Data] ([Id] COUNTER PRIMARY KEY, [Item ID] LONG, [Key] VARCHAR(255), [Value] VARCHAR(255))");
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws Exception {
        try {
            if (conn != null) conn.close();
            if (directory != null) FileUtils.deleteDirectory(directory.toFile());
        } finally {
            if (previousDateTimeType == null) System.clearProperty("com.healthmarketscience.jackcess.dateTimeType");
            else System.setProperty("com.healthmarketscience.jackcess.dateTimeType", previousDateTimeType);
        }
    }

    private long createItem(String key, String status, Instant created) throws SQLException {
        String sql = "INSERT INTO [workqueue] ([Key],[Status],[Status Workflow],[Step Workflow],[Created Date],[Priority]) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, key);
            ps.setString(2, status);
            ps.setLong(3, 1);
            ps.setString(4, "step");
            ps.setTimestamp(5, Timestamp.from(created));
            ps.setLong(6, 1);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                Assert.assertTrue(rs.next());
                return rs.getLong(1);
            }
        }
    }

    @Test
    public void getLastItemReadsNewestWithoutChangingStatus() throws Exception {
        createItem("older", "Pending", Instant.parse("2020-01-01T00:00:00Z"));
        long id = createItem("newest", "Completed", Instant.parse("2099-01-01T00:00:00Z"));
        WorkqueueItemDao.WorkItem item = new WorkqueueItemDao(conn).getLastItem();
        Assert.assertEquals(item.id, id);
        Assert.assertEquals(item.key, "newest");
        try (PreparedStatement ps = conn.prepareStatement("SELECT [Status] FROM [workqueue] WHERE [Id]=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                Assert.assertTrue(rs.next());
                Assert.assertEquals(rs.getString(1), "Completed");
            }
        }
    }

    @Test
    public void upsertUpdatesExistingAndInsertsNewFieldsByIdAndKey() throws Exception {
        long id = createItem("upsert", "Working", Instant.now());
        WorkqueueItemDao dao = new WorkqueueItemDao(conn);
        Map<String,String> data = new LinkedHashMap<>();
        data.put("existing", "first");
        dao.insertDataFieldsById(id, data);
        data.put("existing", "second");
        data.put("new", "created");
        Assert.assertEquals(dao.upsertDataFieldsById(id, data).data, data);
        data.put("existing", "third");
        data.put("newByKey", "created by key");
        Assert.assertEquals(dao.upsertDataFieldsByKey("upsert", data).data, data);
        Assert.assertTrue(conn.getAutoCommit());
    }

    @Test
    public void commitAppliesStagedChangesToExistingFields() throws Exception {
        long id = createItem("commit", "Working", Instant.now());
        Map<String,String> data = new LinkedHashMap<>();
        data.put("existing", "first");
        WorkqueueItemDao dao = new WorkqueueItemDao(conn);
        dao.insertDataFieldsById(id, data);
        ItemDataTxnBuffer.ScopeKey scope = ItemDataTxnBuffer.scopeById(database.toString(), id);
        try {
            ItemDataTxnBuffer.addUpsert(scope, "existing", "second");
            ItemDataTxnBuffer.addUpsert(scope, "new", "created");
            new CommitItemDataChanges().commit(database.toString(), "id", (double) id, null);
            data.put("existing", "second");
            data.put("new", "created");
            Assert.assertEquals(dao.getItemDataById(id), data);
            Assert.assertTrue(ItemDataTxnBuffer.peek(scope).isEmpty());
        } finally {
            ItemDataTxnBuffer.cancel(scope);
        }
    }
}
