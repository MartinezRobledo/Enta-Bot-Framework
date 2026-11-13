package com.automationanywhere.botcommand.utilities.workqueue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class WorkqueueItemDao {

    // --- Tablas / columnas (Access usa corchetes por espacios/reservadas) ---
    private static final String TBL_WQ          = "[workqueue]";
    private static final String COL_ID          = "[Id]";
    private static final String COL_KEY         = "[Key]";
    private static final String COL_STATUS      = "[Status]";             // Pending | Working | Completed | Exception
    private static final String COL_STATUS_WF   = "[Status Workflow]";
    private static final String COL_STEP_WF   = "[Step Workflow]";
    private static final String COL_CREATED     = "[Created Date]";
    private static final String COL_UPDATED     = "[Last Updated]";
    private static final String COL_EXC_REASON  = "[Exception Reason]";
    private static final String COL_PRIORITY    = "[Priority]";
    private static final String COL_DEFER_UNTIL = "[Defer Until]";

    private static final String TBL_DATA        = "[Item Data]";
    private static final String COL_D_ID        = "[Id]";
    private static final String COL_D_ITEM_ID   = "[Item ID]";
    private static final String COL_D_KEY       = "[Key]";
    private static final String COL_D_VALUE     = "[Value]";

    // Estados
    private static final String ST_PENDING   = "Pending";
    private static final String ST_WORKING   = "Working";
    private static final String ST_COMPLETED = "Completed";
    private static final String ST_EXCEPTION = "Exception";

    private final Connection conn;

    // Según el tipo de columna en Access:
    private static final int SQL_LONG_TYPE = java.sql.Types.INTEGER; // si es Long Integer (32-bit)
    // private static final int SQL_LONG_TYPE = java.sql.Types.BIGINT; // si es Large Number (BigInt 64-bit)


    //=====================================================
    //                  DTO
    //=====================================================
    public static final class WorkItem {
        public final long id;
        public final String key;
        public final Long statusWorkflow;  // [Status Workflow]
        public final String stepWorkflow;    // [Step Workflow]
        public final Map<String,String> data; // [Item Data] como diccionario

        public WorkItem(long id, String key,
                        Long statusWorkflow, String stepWorkflow,
                        Map<String,String> data) {
            this.id = id;
            this.key = key;
            this.statusWorkflow = statusWorkflow;
            this.stepWorkflow = stepWorkflow;
            this.data = data;
        }
    }


    public WorkqueueItemDao(Connection conn) {
        this.conn = conn;
    }

    private Timestamp nowTs() {
        return Timestamp.from(Instant.now());
    }

    // ---------- Utilidades para mensajes claros ----------
    private ModState fetchStateByKey(String key) throws SQLException {
        String sql = "SELECT [Status],[Defer Until] FROM [workqueue] WHERE [Key]=?";
        try (PreparedStatement ps = this.conn.prepareStatement(sql);){
            ModState modState;
            block16: {
                ResultSet rs;
                block14: {
                    ModState modState2;
                    block15: {
                        ps.setString(1, key);
                        rs = ps.executeQuery();
                        try {
                            if (rs.next()) break block14;
                            modState2 = null;
                            if (rs == null) break block15;
                        }
                        catch (Throwable throwable) {
                            if (rs != null) {
                                try {
                                    rs.close();
                                }
                                catch (Throwable throwable2) {
                                    throwable.addSuppressed(throwable2);
                                }
                            }
                            throw throwable;
                        }
                        rs.close();
                    }
                    return modState2;
                }
                modState = new ModState(rs.getString(1), rs.getTimestamp(2));
                if (rs == null) break block16;
                rs.close();
            }
            return modState;
        }
    }
    private ModState fetchStateById(long id) throws SQLException {
        String sql = "SELECT [Status],[Defer Until] FROM [workqueue] WHERE [Id]=?";
        try (PreparedStatement ps = this.conn.prepareStatement(sql);){
            ModState modState;
            block16: {
                ResultSet rs;
                block14: {
                    ModState modState2;
                    block15: {
                        ps.setLong(1, id);
                        rs = ps.executeQuery();
                        try {
                            if (rs.next()) break block14;
                            modState2 = null;
                            if (rs == null) break block15;
                        }
                        catch (Throwable throwable) {
                            if (rs != null) {
                                try {
                                    rs.close();
                                }
                                catch (Throwable throwable2) {
                                    throwable.addSuppressed(throwable2);
                                }
                            }
                            throw throwable;
                        }
                        rs.close();
                    }
                    return modState2;
                }
                modState = new ModState(rs.getString(1), rs.getTimestamp(2));
                if (rs == null) break block16;
                rs.close();
            }
            return modState;
        }
    }
    private static final class ModState {
        final String status; final Timestamp deferUntil;
        ModState(String s, Timestamp d) {
            this.status = s;
            this.deferUntil = d;
        }
    }
    private SQLException explainNotUpdatableByKey(String key, ModState st, Timestamp now) {
        boolean deferred;
        if (st == null) {
            return new SQLException("Item no encontrado (Key): " + key);
        }
        boolean bl = deferred = st.deferUntil != null && st.deferUntil.after(now);
        if (deferred) {
            return new SQLException("Item '" + key + "' diferido hasta " + String.valueOf(st.deferUntil) + ".");
        }
        return new SQLException("Item '" + key + "' no es modificable en estado " + st.status + ".");
    }
    private SQLException explainNotUpdatableById(long id, ModState st, Timestamp now) {
        boolean deferred;
        if (st == null) {
            return new SQLException("Item no encontrado (Id): " + id);
        }
        boolean bl = deferred = st.deferUntil != null && st.deferUntil.after(now);
        if (deferred) {
            return new SQLException("Item Id=" + id + " diferido hasta " + String.valueOf(st.deferUntil) + ".");
        }
        return new SQLException("Item Id=" + id + " no es modificable en estado " + st.status + ".");
    }

    // ==========================================================
    //                    GET NEXT ITEM (PENDING → WORKING)
    // ==========================================================
    public WorkItem takeNextPendingAndSetWorking() throws SQLException {
        boolean prevAuto = this.conn.getAutoCommit();
        try {
            String stepWf;
            Long statusWf;
            String key;
            Long id;
            Timestamp now;
            block28: {
                now = this.nowTs();
                id = null;
                key = null;
                statusWf = null;
                stepWf = null;
                String sel = "SELECT TOP 1 [Id],[Key],[Status Workflow],[Step Workflow] FROM [workqueue] WHERE [Status]=? AND ([Defer Until] IS NULL OR [Defer Until] <= ?) ORDER BY [Priority] ASC, [Created Date] ASC, [Id] ASC";
                try (PreparedStatement ps = this.conn.prepareStatement(sel);){
                    ps.setString(1, ST_PENDING);
                    ps.setTimestamp(2, now);
                    try (ResultSet rs = ps.executeQuery();){
                        if (rs.next()) {
                            id = rs.getLong(1);
                            key = rs.getString(2);
                            statusWf = rs.getLong(3);
                            stepWf = rs.getString(4);
                            break block28;
                        }
                        this.conn.commit();
                        WorkItem workItem = null;
                        return workItem;
                    }
                }
            }
            String upd = "UPDATE [workqueue] SET [Status]=?, [Last Updated]=? WHERE [Id]=? AND [Status]=? AND ([Defer Until] IS NULL OR [Defer Until] <= ?)";
            try (PreparedStatement ps = this.conn.prepareStatement(upd);){
                ps.setString(1, ST_WORKING);
                ps.setTimestamp(2, now);
                ps.setLong(3, id);
                ps.setString(4, ST_PENDING);
                ps.setTimestamp(5, now);
                if (ps.executeUpdate() == 0) {
                    this.conn.commit();
                    WorkItem workItem = null;
                    return workItem;
                }
            }
            this.conn.commit();
            Map<String, String> data = this.getItemDataById(id);
            WorkItem workItem = new WorkItem(id, key, statusWf, stepWf, data);
            return workItem;
        }
        catch (SQLException ex) {
            this.conn.rollback();
            throw ex;
        }
        finally {
            this.conn.setAutoCommit(prevAuto);
        }
    }

    // ==========================================================
    //                WORKING → COMPLETED
    // ==========================================================
    public WorkItem completeFromWorkingByKey(String key) throws SQLException {
        Timestamp now = this.nowTs();
        boolean prevAuto = this.conn.getAutoCommit();
        try {
            String sql = "UPDATE [workqueue] SET [Status]=?,[Last Updated]=? WHERE [Key]=? AND [Status]=?";
            try (PreparedStatement ps = this.conn.prepareStatement(sql);){
                ps.setString(1, ST_COMPLETED);
                ps.setTimestamp(2, now);
                ps.setString(3, key);
                ps.setString(4, ST_WORKING);
                if (ps.executeUpdate() == 0) {
                    throw this.explainNotUpdatableByKey(key, this.fetchStateByKey(key), now);
                }
            }
            this.conn.commit();
            WorkItem workItem = this.buildWorkItemByKey(key);
            return workItem;
        }
        catch (SQLException ex) {
            this.conn.rollback();
            throw ex;
        }
        finally {
            this.conn.setAutoCommit(prevAuto);
        }
    }

    public WorkItem completeFromWorkingById(long itemId) throws SQLException {
        Timestamp now = this.nowTs();
        boolean prevAuto = this.conn.getAutoCommit();
        try {
            String sql = "UPDATE [workqueue] SET [Status]=?,[Last Updated]=? WHERE [Id]=? AND [Status]=?";
            try (PreparedStatement ps = this.conn.prepareStatement(sql);){
                ps.setString(1, ST_COMPLETED);
                ps.setTimestamp(2, now);
                ps.setLong(3, itemId);
                ps.setString(4, ST_WORKING);
                if (ps.executeUpdate() == 0) {
                    throw this.explainNotUpdatableById(itemId, this.fetchStateById(itemId), now);
                }
            }
            this.conn.commit();
            WorkItem workItem = this.buildWorkItemById(itemId);
            return workItem;
        }
        catch (SQLException ex) {
            this.conn.rollback();
            throw ex;
        }
        finally {
            this.conn.setAutoCommit(prevAuto);
        }
    }

    // ==========================================================
    //                WORKING → EXCEPTION
    // ==========================================================
    public WorkItem exceptionFromWorkingByKey(String key, String reason) throws SQLException {
        Timestamp now = this.nowTs();
        boolean prevAuto = this.conn.getAutoCommit();
        try {
            String sql = "UPDATE [workqueue] SET [Status]=?,[Exception Reason]=?,[Last Updated]=? WHERE [Key]=? AND [Status]=?";
            try (PreparedStatement ps = this.conn.prepareStatement(sql);){
                ps.setString(1, ST_EXCEPTION);
                ps.setString(2, reason);
                ps.setTimestamp(3, now);
                ps.setString(4, key);
                ps.setString(5, ST_WORKING);
                if (ps.executeUpdate() == 0) {
                    throw this.explainNotUpdatableByKey(key, this.fetchStateByKey(key), now);
                }
            }
            this.conn.commit();
            WorkItem workItem = this.buildWorkItemByKey(key);
            return workItem;
        }
        catch (SQLException ex) {
            this.conn.rollback();
            throw ex;
        }
        finally {
            this.conn.setAutoCommit(prevAuto);
        }
    }

    public WorkItem exceptionFromWorkingById(long itemId, String reason) throws SQLException {
        Timestamp now = this.nowTs();
        boolean prevAuto = this.conn.getAutoCommit();
        try {
            String sql = "UPDATE [workqueue] SET [Status]=?,[Exception Reason]=?,[Last Updated]=? WHERE [Id]=? AND [Status]=?";
            try (PreparedStatement ps = this.conn.prepareStatement(sql);){
                ps.setString(1, ST_EXCEPTION);
                ps.setString(2, reason);
                ps.setTimestamp(3, now);
                ps.setLong(4, itemId);
                ps.setString(5, ST_WORKING);
                if (ps.executeUpdate() == 0) {
                    throw this.explainNotUpdatableById(itemId, this.fetchStateById(itemId), now);
                }
            }
            this.conn.commit();
            WorkItem workItem = this.buildWorkItemById(itemId);
            return workItem;
        }
        catch (SQLException ex) {
            this.conn.rollback();
            throw ex;
        }
        finally {
            this.conn.setAutoCommit(prevAuto);
        }
    }

    // ==========================================================
    //                     WORKING → PENDING (DEFER)
    // ==========================================================
    public WorkItem deferFromWorkingByKey(String key, Timestamp deferUntil) throws SQLException {
        if (deferUntil == null) {
            throw new SQLException("Defer Until es obligatorio.");
        }
        Timestamp now = this.nowTs();
        if (!deferUntil.after(now)) {
            throw new SQLException("Defer Until debe ser futuro.");
        }
        boolean prevAuto = this.conn.getAutoCommit();
        try {
            String sql = "UPDATE [workqueue] SET [Defer Until]=?,[Last Updated]=?, [Status]=? WHERE [Key]=? AND [Status]=? AND ([Defer Until] IS NULL OR [Defer Until] <= ?)";
            try (PreparedStatement ps = this.conn.prepareStatement(sql);){
                ps.setTimestamp(1, deferUntil);
                ps.setTimestamp(2, now);
                ps.setString(3, key);
                ps.setString(4, ST_WORKING);
                ps.setTimestamp(5, now);
                if (ps.executeUpdate() == 0) {
                    throw this.explainNotUpdatableByKey(key, this.fetchStateByKey(key), now);
                }
            }
            this.conn.commit();
            WorkItem workItem = this.buildWorkItemByKey(key);
            return workItem;
        }
        catch (SQLException ex) {
            this.conn.rollback();
            throw ex;
        }
        finally {
            this.conn.setAutoCommit(prevAuto);
        }
    }

    public WorkItem deferFromWorkingById(long itemId, Timestamp deferUntil) throws SQLException {
        if (deferUntil == null) {
            throw new SQLException("Defer Until es obligatorio.");
        }
        Timestamp now = this.nowTs();
        if (!deferUntil.after(now)) {
            throw new SQLException("Defer Until debe ser futuro.");
        }
        boolean prevAuto = this.conn.getAutoCommit();
        try {
            String sql = "UPDATE [workqueue] SET [Defer Until]=?, [Last Updated]=?, [Status]=? WHERE [Id]=? AND [Status]=? AND ([Defer Until] IS NULL OR [Defer Until] <= ?)";
            try (PreparedStatement ps = this.conn.prepareStatement(sql);){
                ps.setTimestamp(1, deferUntil);
                ps.setTimestamp(2, now);
                ps.setString(3, ST_PENDING);
                ps.setLong(4, itemId);
                ps.setString(5, ST_WORKING);
                ps.setTimestamp(6, now);
                if (ps.executeUpdate() == 0) {
                    throw this.explainNotUpdatableById(itemId, this.fetchStateById(itemId), now);
                }
            }
            this.conn.commit();
            WorkItem workItem = this.buildWorkItemById(itemId);
            return workItem;
        }
        catch (SQLException ex) {
            this.conn.rollback();
            throw ex;
        }
        finally {
            this.conn.setAutoCommit(prevAuto);
        }
    }

    // ==========================================================
    //                     WORKING → PENDING (RELEASE)
    // ==========================================================
    public WorkItem releaseFromWorkingByKey(String key) throws SQLException {
        Timestamp now = this.nowTs();
        boolean prevAuto = this.conn.getAutoCommit();
        try {
            String sql = "UPDATE [workqueue] SET [Status]=?,[Last Updated]=? WHERE [Key]=? AND [Status]=?";
            try (PreparedStatement ps = this.conn.prepareStatement(sql);){
                ps.setString(1, ST_PENDING);
                ps.setTimestamp(2, now);
                ps.setString(3, key);
                ps.setString(4, ST_WORKING);
                if (ps.executeUpdate() == 0) {
                    throw this.explainNotUpdatableByKey(key, this.fetchStateByKey(key), now);
                }
            }
            this.conn.commit();
            WorkItem workItem = this.buildWorkItemByKey(key);
            return workItem;
        }
        catch (SQLException ex) {
            this.conn.rollback();
            throw ex;
        }
        finally {
            this.conn.setAutoCommit(prevAuto);
        }
    }

    public WorkItem releaseFromWorkingById(long itemId) throws SQLException {
        Timestamp now = this.nowTs();
        boolean prevAuto = this.conn.getAutoCommit();
        try {
            String sql = "UPDATE [workqueue] SET [Status]=?,[Last Updated]=? WHERE [Id]=? AND [Status]=?";
            try (PreparedStatement ps = this.conn.prepareStatement(sql);){
                ps.setString(1, ST_PENDING);
                ps.setTimestamp(2, now);
                ps.setLong(3, itemId);
                ps.setString(4, ST_WORKING);
                if (ps.executeUpdate() == 0) {
                    throw this.explainNotUpdatableById(itemId, this.fetchStateById(itemId), now);
                }
            }
            this.conn.commit();
            WorkItem workItem = this.buildWorkItemById(itemId);
            return workItem;
        }
        catch (SQLException ex) {
            this.conn.rollback();
            throw ex;
        }
        finally {
            this.conn.setAutoCommit(prevAuto);
        }
    }

    // ==========================================================
    //                        GET ITEM DATA
    // ==========================================================
    /** Devuelve el diccionario Key/Value de [Item Data] por Item Id. */
    public Map<String,String> getItemDataById(long itemId) throws SQLException {
        String sql = "SELECT [Key],[Value] FROM [Item Data] WHERE [Item ID]=? ORDER BY [Id] ASC";
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        try (PreparedStatement ps = this.conn.prepareStatement(sql);){
            ps.setLong(1, itemId);
            try (ResultSet rs = ps.executeQuery();){
                while (rs.next()) {
                    String k = rs.getString(1);
                    String v = rs.getString(2);
                    map.put(k, v);
                }
            }
        }
        return map;
    }

    /** Devuelve Key/Value por Item Key (join con workqueue). */
    public Map<String,String> getItemDataByKey(String itemKey) throws SQLException {
        String sql = "SELECT d.[Key], d.[Value] FROM [Item Data] d INNER JOIN [workqueue] w ON d.[Item ID] = w.[Id] WHERE w.[Key]=? ORDER BY d.[Id] ASC";
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        try (PreparedStatement ps = this.conn.prepareStatement(sql);){
            ps.setString(1, itemKey);
            try (ResultSet rs = ps.executeQuery();){
                while (rs.next()) {
                    String k = rs.getString(1);
                    String v = rs.getString(2);
                    map.put(k, v);
                }
            }
        }
        return map;
    }


    //============================================================
    //                   UPDATE WORKFLOW (SET STATUS)
    //============================================================
    public WorkItem updateWorkflowById(long id, Long statusWf, String stepWf) throws SQLException {
        Timestamp now = this.nowTs();
        boolean prevAuto = this.conn.getAutoCommit();
        try {
            String sql = "UPDATE [workqueue] SET [Status Workflow]=?, [Step Workflow]=?, [Last Updated]=? WHERE [Id]=? AND [Status]=?";
            try (PreparedStatement ps = this.conn.prepareStatement(sql);){
                if (statusWf != null) {
                    ps.setLong(1, statusWf);
                } else {
                    ps.setNull(1, 4);
                }
                if (stepWf != null) {
                    ps.setString(2, stepWf);
                } else {
                    ps.setNull(2, 12);
                }
                ps.setTimestamp(3, now);
                ps.setLong(4, id);
                ps.setString(5, ST_WORKING);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw this.explainNotUpdatableById(id, this.fetchStateById(id), now);
                }
            }
            this.conn.commit();
            WorkItem workItem = this.buildWorkItemById(id);
            return workItem;
        }
        catch (SQLException ex) {
            this.conn.rollback();
            throw ex;
        }
        finally {
            this.conn.setAutoCommit(prevAuto);
        }
    }

    public WorkItem updateWorkflowByKey(String key, Long statusWf, String stepWf) throws SQLException {
        Timestamp now = this.nowTs();
        String sql = "UPDATE [workqueue] SET [Status Workflow]=?, [Step Workflow]=?, [Last Updated]=? WHERE [Key]=? AND [Status]=?";
        try (PreparedStatement ps = this.conn.prepareStatement(sql);){
            if (statusWf != null) {
                ps.setLong(1, statusWf);
            } else {
                ps.setNull(1, 4);
            }
            if (stepWf != null) {
                ps.setString(2, stepWf);
            } else {
                ps.setNull(2, 12);
            }
            ps.setTimestamp(3, now);
            ps.setString(4, key);
            ps.setString(5, ST_WORKING);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw this.explainNotUpdatableByKey(key, this.fetchStateByKey(key), now);
            }
        }
        return this.buildWorkItemByKey(key);
    }



    //============================================================
    //      Modificadores de Item Data en Stagging
    //============================================================

    //
    // --- Inserciones en bloque de Item Data ---
    //   Reglas: WORKING + no diferido + falla si clave existe
    //
    public WorkItem insertDataFieldsById(long itemId, Map<String,String> dataToInsert) throws SQLException {
        if (dataToInsert == null || dataToInsert.isEmpty()) {
            return this.buildWorkItemById(itemId);
        }
        Timestamp now = this.nowTs();
        boolean prevAuto = this.conn.getAutoCommit();
        try {
            this.conn.setAutoCommit(false);
            ModState st = this.fetchStateById(itemId);
            if (st == null || !ST_WORKING.equals(st.status) || st.deferUntil != null && st.deferUntil.after(now)) {
                throw this.explainNotUpdatableById(itemId, st, now);
            }
            String existsSql = "SELECT [Key] FROM [Item Data] WHERE [Item ID]=?";
            try (PreparedStatement ps = this.conn.prepareStatement(existsSql);){
                ps.setLong(1, itemId);
                try (ResultSet rs = ps.executeQuery();){
                    HashSet<String> existing = new HashSet<String>();
                    while (rs.next()) {
                        existing.add(rs.getString(1));
                    }
                    for (String k : dataToInsert.keySet()) {
                        if (!existing.contains(k)) continue;
                        throw new SQLException("El campo '" + k + "' ya existe para el ítem Id=" + itemId + ".");
                    }
                }
            }
            String ins = "INSERT INTO [Item Data] ([Item ID],[Key],[Value]) VALUES (?,?,?)";
            try (PreparedStatement ps = this.conn.prepareStatement(ins);){
                for (Map.Entry entry : dataToInsert.entrySet()) {
                    ps.setLong(1, itemId);
                    ps.setString(2, (String)entry.getKey());
                    ps.setString(3, (String)entry.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            String upd = "UPDATE [workqueue] SET [Last Updated]=? WHERE [Id]=?";
            try (PreparedStatement ps = this.conn.prepareStatement(upd);){
                ps.setTimestamp(1, now);
                ps.setLong(2, itemId);
                ps.executeUpdate();
            }
            this.conn.commit();
            WorkItem workItem = this.buildWorkItemById(itemId);
            return workItem;
        }
        catch (SQLException ex) {
            this.conn.rollback();
            throw ex;
        }
        finally {
            this.conn.setAutoCommit(prevAuto);
        }
    }

    public WorkItem insertDataFieldsByKey(String itemKey, Map<String,String> dataToInsert) throws SQLException {
        if (itemKey == null || itemKey.isBlank()) {
            throw new SQLException("Item Key es obligatorio.");
        }
        if (dataToInsert == null || dataToInsert.isEmpty()) {
            return this.buildWorkItemByKey(itemKey);
        }
        Timestamp now = this.nowTs();
        boolean prevAuto = this.conn.getAutoCommit();
        try {
            boolean deferred;
            this.conn.setAutoCommit(false);
            Long id = null;
            String status = null;
            Timestamp deferUntil = null;
            String sel = "SELECT [Id],[Status],[Defer Until] FROM [workqueue] WHERE [Key]=?";
            try (PreparedStatement ps = this.conn.prepareStatement(sel);){
                ps.setString(1, itemKey);
                try (ResultSet rs = ps.executeQuery();){
                    if (!rs.next()) {
                        throw new SQLException("Item no encontrado (Key): " + itemKey);
                    }
                    id = rs.getLong(1);
                    status = rs.getString(2);
                    deferUntil = rs.getTimestamp(3);
                }
            }
            boolean bl = deferred = deferUntil != null && deferUntil.after(now);
            if (!ST_WORKING.equals(status) || deferred) {
                throw this.explainNotUpdatableByKey(itemKey, new ModState(status, deferUntil), now);
            }
            String existsSql = "SELECT [Key] FROM [Item Data] WHERE [Item ID]=?";
            try (PreparedStatement ps = this.conn.prepareStatement(existsSql);){
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery();){
                    HashSet<String> existing = new HashSet<String>();
                    while (rs.next()) {
                        existing.add(rs.getString(1));
                    }
                    for (String k : dataToInsert.keySet()) {
                        if (!existing.contains(k)) continue;
                        throw new SQLException("El campo '" + k + "' ya existe para el ítem Key='" + itemKey + "'.");
                    }
                }
            }
            String ins = "INSERT INTO [Item Data] ([Item ID],[Key],[Value]) VALUES (?,?,?)";
            try (PreparedStatement ps = this.conn.prepareStatement(ins);){
                for (Map.Entry entry : dataToInsert.entrySet()) {
                    ps.setLong(1, id);
                    ps.setString(2, (String)entry.getKey());
                    ps.setString(3, (String)entry.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            String upd = "UPDATE [workqueue] SET [Last Updated]=? WHERE [Id]=?";
            try (PreparedStatement ps = this.conn.prepareStatement(upd);){
                ps.setTimestamp(1, now);
                ps.setLong(2, id);
                ps.executeUpdate();
            }
            this.conn.commit();
            WorkItem workItem = this.buildWorkItemByKey(itemKey);
            return workItem;
        }
        catch (SQLException ex) {
            this.conn.rollback();
            throw ex;
        }
        finally {
            this.conn.setAutoCommit(prevAuto);
        }
    }

    //===========================================================
    //              Maintain Queue
    //===========================================================

    // --- Resultado de la purga ---
    public static final class DeletedStats {
        public final int itemsDeleted;
        public final int dataRowsDeleted;
        public final Timestamp cutoff;
        public DeletedStats(int itemsDeleted, int dataRowsDeleted, Timestamp cutoff) {
            this.itemsDeleted = itemsDeleted;
            this.dataRowsDeleted = dataRowsDeleted;
            this.cutoff = cutoff;
        }
    }

    /**
     * Borra ítems y su [Item Data] con [Created Date] < cutoff.
     * - Primero borra [Item Data] (JOIN con [workqueue]),
     * - luego borra de [workqueue].
     */
    public DeletedStats purgeOldItemsByCreatedDate(Timestamp cutoff) throws SQLException {
        if (cutoff == null) {
            throw new SQLException("La fecha de corte es obligatoria.");
        }
        boolean prevAuto = this.conn.getAutoCommit();
        try {
            int itemsDeleted;
            int dataRowsDeleted;
            this.conn.setAutoCommit(false);
            String delData = "DELETE d.* FROM [Item Data] AS d INNER JOIN [workqueue] AS w ON d.[Item ID] = w.[Id] WHERE w.[Created Date] < ?";
            try (PreparedStatement ps = this.conn.prepareStatement(delData);){
                ps.setTimestamp(1, cutoff);
                dataRowsDeleted = ps.executeUpdate();
            }
            String delItems = "DELETE FROM [workqueue] WHERE [Created Date] < ?";
            try (PreparedStatement ps = this.conn.prepareStatement(delItems);){
                ps.setTimestamp(1, cutoff);
                itemsDeleted = ps.executeUpdate();
            }
            this.conn.commit();
            DeletedStats deletedStats = new DeletedStats(itemsDeleted, dataRowsDeleted, cutoff);
            return deletedStats;
        }
        catch (SQLException ex) {
            this.conn.rollback();
            throw ex;
        }
        finally {
            this.conn.setAutoCommit(prevAuto);
        }
    }




    //============================================================
    //                  HELPERS
    //============================================================
    //
    // --- Lecturas públicas, reusando builders privados ---
    //
    public WorkItem readWorkItemById(long id) throws SQLException {
        return this.buildWorkItemById(id);
    }

    public WorkItem readWorkItemByKey(String key) throws SQLException {
        return this.buildWorkItemByKey(key);
    }

    // Carga WorkItem por ID (sin exponer [Status])
    private WorkItem buildWorkItemById(long id) throws SQLException {
        String sql = "SELECT [Key],[Status Workflow],[Step Workflow] FROM [workqueue] WHERE [Id]=?";
        try (PreparedStatement ps = this.conn.prepareStatement(sql);){
            WorkItem workItem;
            block16: {
                ResultSet rs;
                block14: {
                    WorkItem workItem2;
                    block15: {
                        ps.setLong(1, id);
                        rs = ps.executeQuery();
                        try {
                            if (rs.next()) break block14;
                            workItem2 = null;
                            if (rs == null) break block15;
                        }
                        catch (Throwable throwable) {
                            if (rs != null) {
                                try {
                                    rs.close();
                                }
                                catch (Throwable throwable2) {
                                    throwable.addSuppressed(throwable2);
                                }
                            }
                            throw throwable;
                        }
                        rs.close();
                    }
                    return workItem2;
                }
                String key = rs.getString(1);
                long wf = rs.getLong(2);
                Long statusWf = rs.wasNull() ? null : Long.valueOf(wf);
                String stepWf = rs.getString(3);
                Map<String, String> data = this.getItemDataById(id);
                workItem = new WorkItem(id, key, statusWf, stepWf, data);
                if (rs == null) break block16;
                rs.close();
            }
            return workItem;
        }
    }

    private WorkItem buildWorkItemByKey(String key) throws SQLException {
        String sql = "SELECT [Id],[Status Workflow],[Step Workflow] FROM [workqueue] WHERE [Key]=?";
        try (PreparedStatement ps = this.conn.prepareStatement(sql);){
            WorkItem workItem;
            block16: {
                ResultSet rs;
                block14: {
                    WorkItem workItem2;
                    block15: {
                        ps.setString(1, key);
                        rs = ps.executeQuery();
                        try {
                            if (rs.next()) break block14;
                            workItem2 = null;
                            if (rs == null) break block15;
                        }
                        catch (Throwable throwable) {
                            if (rs != null) {
                                try {
                                    rs.close();
                                }
                                catch (Throwable throwable2) {
                                    throwable.addSuppressed(throwable2);
                                }
                            }
                            throw throwable;
                        }
                        rs.close();
                    }
                    return workItem2;
                }
                long id = rs.getLong(1);
                long wf = rs.getLong(2);
                Long statusWf = rs.wasNull() ? null : Long.valueOf(wf);
                String stepWf = rs.getString(3);
                Map<String, String> data = this.getItemDataById(id);
                workItem = new WorkItem(id, key, statusWf, stepWf, data);
                if (rs == null) break block16;
                rs.close();
            }
            return workItem;
        }
    }



    public WorkItem getLastItem() throws SQLException {
        boolean prevAuto = this.conn.getAutoCommit();
        try {
            String stepWf;
            Long statusWf;
            String key;
            Long id;
            block20: {
                id = null;
                key = null;
                statusWf = null;
                stepWf = null;
                String sel = "SELECT TOP 1 [Id],[Key],[Status Workflow],[Step Workflow] FROM [workqueue] ORDER BY [Created Date] DESC, [Id] DESC";
                try (PreparedStatement ps = this.conn.prepareStatement(sel);
                     ResultSet rs = ps.executeQuery();){
                    if (rs.next()) {
                        id = rs.getLong(1);
                        key = rs.getString(2);
                        statusWf = rs.getLong(3);
                        stepWf = rs.getString(4);
                        break block20;
                    }
                    this.conn.commit();
                    WorkItem workItem = null;
                    return workItem;
                }
            }
            this.conn.commit();
            Map<String, String> data = this.getItemDataById(id);
            WorkItem workItem = new WorkItem(id, key, statusWf, stepWf, data);
            return workItem;
        }
        catch (SQLException ex) {
            this.conn.rollback();
            throw ex;
        }
        finally {
            this.conn.setAutoCommit(prevAuto);
        }
    }

    public WorkItem upsertDataFieldsById(long itemId, Map<String, String> data) throws SQLException {
        if (data == null || data.isEmpty()) {
            return this.buildWorkItemById(itemId);
        }
        Timestamp now = this.nowTs();
        boolean prevAuto = this.conn.getAutoCommit();
        try {
            this.conn.setAutoCommit(false);
            ModState st = this.fetchStateById(itemId);
            if (st == null || !ST_WORKING.equals(st.status) || st.deferUntil != null && st.deferUntil.after(now)) {
                throw this.explainNotUpdatableById(itemId, st, now);
            }
            HashSet<String> existing = new HashSet<String>();
            String sel = "SELECT [Key] FROM [Item Data] WHERE [Item ID]=?";
            try (PreparedStatement ps = this.conn.prepareStatement(sel);){
                ps.setLong(1, itemId);
                try (ResultSet rs = ps.executeQuery();){
                    while (rs.next()) {
                        existing.add(rs.getString(1));
                    }
                }
            }
            String upd = "UPDATE [Item Data] SET [Value]=? WHERE [Item ID]=? AND [Key]=?";
            try (PreparedStatement ps = this.conn.prepareStatement(upd);){
                for (Map.Entry<String, String> e : data.entrySet()) {
                    if (!existing.contains(e.getKey())) continue;
                    ps.setString(1, e.getValue());
                    ps.setLong(2, itemId);
                    ps.setString(3, e.getKey());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            String ins = "INSERT INTO [Item Data] ([Item ID],[Key],[Value]) VALUES (?,?,?)";
            try (PreparedStatement ps = this.conn.prepareStatement(ins);){
                for (Map.Entry<String, String> e : data.entrySet()) {
                    if (existing.contains(e.getKey())) continue;
                    ps.setLong(1, itemId);
                    ps.setString(2, e.getKey());
                    ps.setString(3, e.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            PreparedStatement ps = this.conn.prepareStatement("UPDATE [workqueue] SET [Last Updated]=? WHERE [Id]=?");
            try {
                ps.setTimestamp(1, now);
                ps.setLong(2, itemId);
                ps.executeUpdate();
            }
            finally {
                if (ps != null) {
                    ps.close();
                }
            }
            this.conn.commit();
            WorkItem workItem = this.buildWorkItemById(itemId);
            return workItem;
        }
        catch (SQLException ex) {
            this.conn.rollback();
            throw ex;
        }
        finally {
            this.conn.setAutoCommit(prevAuto);
        }
    }

    public WorkItem upsertDataFieldsByKey(String itemKey, Map<String, String> data) throws SQLException {
        if (itemKey == null || itemKey.isBlank()) {
            throw new SQLException("Item Key es obligatorio.");
        }
        if (data == null || data.isEmpty()) {
            return this.buildWorkItemByKey(itemKey);
        }
        Timestamp now = this.nowTs();
        boolean prevAuto = this.conn.getAutoCommit();
        try {
            boolean deferred;
            this.conn.setAutoCommit(false);
            Long itemId = null;
            String status = null;
            Timestamp deferUntil = null;
            String selItem = "SELECT [Id],[Status],[Defer Until] FROM [workqueue] WHERE [Key]=?";
            try (PreparedStatement ps = this.conn.prepareStatement(selItem);){
                ps.setString(1, itemKey);
                try (ResultSet rs = ps.executeQuery();){
                    if (!rs.next()) {
                        throw new SQLException("Item no encontrado (Key): " + itemKey);
                    }
                    itemId = rs.getLong(1);
                    status = rs.getString(2);
                    deferUntil = rs.getTimestamp(3);
                }
            }
            boolean bl = deferred = deferUntil != null && deferUntil.after(now);
            if (!ST_WORKING.equals(status) || deferred) {
                throw this.explainNotUpdatableByKey(itemKey, new ModState(status, deferUntil), now);
            }
            HashSet<String> existing = new HashSet<String>();
            String selKeys = "SELECT [Key] FROM [Item Data] WHERE [Item ID]=?";
            try (PreparedStatement ps = this.conn.prepareStatement(selKeys);){
                ps.setLong(1, itemId);
                try (ResultSet rs = ps.executeQuery();){
                    while (rs.next()) {
                        existing.add(rs.getString(1));
                    }
                }
            }
            String upd = "UPDATE [Item Data] SET [Value]=? WHERE [Item ID]=? AND [Key]=?";
            try (PreparedStatement ps = this.conn.prepareStatement(upd);){
                for (Map.Entry<String, String> e : data.entrySet()) {
                    if (!existing.contains(e.getKey())) continue;
                    ps.setString(1, e.getValue());
                    ps.setLong(2, itemId);
                    ps.setString(3, (String)e.getKey());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            String ins = "INSERT INTO [Item Data] ([Item ID],[Key],[Value]) VALUES (?,?,?)";
            try (PreparedStatement ps = this.conn.prepareStatement(ins);){
                for (Map.Entry entry : data.entrySet()) {
                    if (existing.contains(entry.getKey())) continue;
                    ps.setLong(1, itemId);
                    ps.setString(2, (String)entry.getKey());
                    ps.setString(3, (String)entry.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            String updWq = "UPDATE [workqueue] SET [Last Updated]=? WHERE [Id]=?";
            try (PreparedStatement ps = this.conn.prepareStatement(updWq);){
                ps.setTimestamp(1, now);
                ps.setLong(2, itemId);
                ps.executeUpdate();
            }
            this.conn.commit();
            WorkItem workItem = this.buildWorkItemByKey(itemKey);
            return workItem;
        }
        catch (SQLException ex) {
            this.conn.rollback();
            throw ex;
        }
        finally {
            this.conn.setAutoCommit(prevAuto);
        }
    }
}
