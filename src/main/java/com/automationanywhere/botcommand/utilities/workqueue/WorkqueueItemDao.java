package com.automationanywhere.botcommand.utilities.workqueue;

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


    public WorkqueueItemDao(Connection conn) { this.conn = conn; }

    private Timestamp nowTs() {
        return Timestamp.from(java.time.Instant.now()); // UTC
    }

    // ---------- Utilidades para mensajes claros ----------
    private ModState fetchStateByKey(String key) throws SQLException {
        String sql = "SELECT " + COL_STATUS + "," + COL_DEFER_UNTIL + " FROM " + TBL_WQ + " WHERE " + COL_KEY + "=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new ModState(rs.getString(1), rs.getTimestamp(2));
            }
        }
    }
    private ModState fetchStateById(long id) throws SQLException {
        String sql = "SELECT " + COL_STATUS + "," + COL_DEFER_UNTIL + " FROM " + TBL_WQ + " WHERE " + COL_ID + "=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new ModState(rs.getString(1), rs.getTimestamp(2));
            }
        }
    }
    private static final class ModState {
        final String status; final Timestamp deferUntil;
        ModState(String s, Timestamp d) { status=s; deferUntil=d; }
    }
    private SQLException explainNotUpdatableByKey(String key, ModState st, Timestamp now) {
        if (st == null) return new SQLException("Item no encontrado (Key): " + key);
        boolean deferred = st.deferUntil != null && st.deferUntil.after(now);
        if (deferred) return new SQLException("Item '" + key + "' diferido hasta " + st.deferUntil + ".");
        return new SQLException("Item '" + key + "' no es modificable en estado " + st.status + ".");
    }
    private SQLException explainNotUpdatableById(long id, ModState st, Timestamp now) {
        if (st == null) return new SQLException("Item no encontrado (Id): " + id);
        boolean deferred = st.deferUntil != null && st.deferUntil.after(now);
        if (deferred) return new SQLException("Item Id=" + id + " diferido hasta " + st.deferUntil + ".");
        return new SQLException("Item Id=" + id + " no es modificable en estado " + st.status + ".");
    }

    // ==========================================================
    //                    GET NEXT ITEM (PENDING → WORKING)
    // ==========================================================
    public WorkItem takeNextPendingAndSetWorking() throws SQLException {
        boolean prevAuto = conn.getAutoCommit();
        try {
            final Timestamp now = nowTs();
            Long id = null; String key = null; Long statusWf = null; String stepWf = null;

            String sel = "SELECT TOP 1 " + COL_ID + "," + COL_KEY + "," + COL_STATUS_WF + "," + COL_STEP_WF +
                    " FROM " + TBL_WQ +
                    " WHERE " + COL_STATUS + "=? AND (" + COL_DEFER_UNTIL + " IS NULL OR " + COL_DEFER_UNTIL + " <= ?)" +
                    " ORDER BY " + COL_PRIORITY + " ASC, " + COL_CREATED + " ASC, " + COL_ID + " ASC";
            try (PreparedStatement ps = conn.prepareStatement(sel)) {
                ps.setString(1, ST_PENDING);
                ps.setTimestamp(2, now);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        id       = rs.getLong(1);
                        key      = rs.getString(2);
                        statusWf = rs.getLong(3);
                        stepWf   = rs.getString(4);
                    } else {
                        conn.commit();
                        return null; // no hay elegibles
                    }
                }
            }

            String upd = "UPDATE " + TBL_WQ + " SET " + COL_STATUS + "=?, " + COL_UPDATED + "=? " +
                    "WHERE " + COL_ID + "=? AND " + COL_STATUS + "=? AND (" + COL_DEFER_UNTIL + " IS NULL OR " + COL_DEFER_UNTIL + " <= ?)";
            try (PreparedStatement ps = conn.prepareStatement(upd)) {
                ps.setString(1, ST_WORKING);
                ps.setTimestamp(2, now);
                ps.setLong(3, id);
                ps.setString(4, ST_PENDING);
                ps.setTimestamp(5, now);
                if (ps.executeUpdate() == 0) { // carrera: otro worker lo tomó
                    conn.commit();
                    return null;
                }
            }
            conn.commit();
            Map<String,String> data = getItemDataById(id);
            return new WorkItem(id, key, statusWf, stepWf, data);
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(prevAuto);
        }
    }

    // ==========================================================
    //                WORKING → COMPLETED
    // ==========================================================
    public WorkItem completeFromWorkingByKey(String key) throws SQLException {
        final Timestamp now = nowTs();
        boolean prevAuto = conn.getAutoCommit();
        try {
            String sql = "UPDATE " + TBL_WQ + " SET " +
                    COL_STATUS + "=?," + COL_UPDATED + "=? " +
                    "WHERE " + COL_KEY + "=? AND " + COL_STATUS + "=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ST_COMPLETED);
                ps.setTimestamp(2, now);
                ps.setString(3, key);
                ps.setString(4, ST_WORKING);
                if (ps.executeUpdate() == 0) {
                    throw explainNotUpdatableByKey(key, fetchStateByKey(key), now);
                }
            }
            conn.commit();
            return buildWorkItemByKey(key);
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(prevAuto);
        }
    }

    public WorkItem completeFromWorkingById(long itemId) throws SQLException {
        final Timestamp now = nowTs();
        boolean prevAuto = conn.getAutoCommit();
        try {
            String sql = "UPDATE " + TBL_WQ + " SET " +
                    COL_STATUS + "=?," + COL_UPDATED + "=? " +
                    "WHERE " + COL_ID + "=? AND " + COL_STATUS + "=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ST_COMPLETED);
                ps.setTimestamp(2, now);
                ps.setLong(3, itemId);
                ps.setString(4, ST_WORKING);
                if (ps.executeUpdate() == 0) {
                    throw explainNotUpdatableById(itemId, fetchStateById(itemId), now);
                }
            }
            conn.commit();
            return buildWorkItemById(itemId);
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(prevAuto);
        }
    }

    // ==========================================================
    //                WORKING → EXCEPTION
    // ==========================================================
    public WorkItem exceptionFromWorkingByKey(String key, String reason) throws SQLException {
        final Timestamp now = nowTs();
        boolean prevAuto = conn.getAutoCommit();
        try {
            String sql = "UPDATE " + TBL_WQ + " SET " +
                    COL_STATUS + "=?," + COL_EXC_REASON + "=?," + COL_UPDATED + "=? " +
                    "WHERE " + COL_KEY + "=? AND " + COL_STATUS + "=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ST_EXCEPTION);
                ps.setString(2, reason);
                ps.setTimestamp(3, now);
                ps.setString(4, key);
                ps.setString(5, ST_WORKING);
                if (ps.executeUpdate() == 0) {
                    throw explainNotUpdatableByKey(key, fetchStateByKey(key), now);
                }
            }
            conn.commit();
            return buildWorkItemByKey(key);
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(prevAuto);
        }
    }

    public WorkItem exceptionFromWorkingById(long itemId, String reason) throws SQLException {
        final Timestamp now = nowTs();
        boolean prevAuto = conn.getAutoCommit();
        try {
            String sql = "UPDATE " + TBL_WQ + " SET " +
                    COL_STATUS + "=?," + COL_EXC_REASON + "=?," + COL_UPDATED + "=? " +
                    "WHERE " + COL_ID + "=? AND " + COL_STATUS + "=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ST_EXCEPTION);
                ps.setString(2, reason);
                ps.setTimestamp(3, now);
                ps.setLong(4, itemId);
                ps.setString(5, ST_WORKING);
                if (ps.executeUpdate() == 0) {
                    throw explainNotUpdatableById(itemId, fetchStateById(itemId), now);
                }
            }
            conn.commit();
            return buildWorkItemById(itemId);
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(prevAuto);
        }
    }

    // ==========================================================
    //                     WORKING → PENDING (DEFER)
    // ==========================================================
    public WorkItem deferFromWorkingByKey(String key, Timestamp deferUntil) throws SQLException {
        if (deferUntil == null) throw new SQLException("Defer Until es obligatorio.");
        final Timestamp now = nowTs();
        if (!deferUntil.after(now)) throw new SQLException("Defer Until debe ser futuro.");

        boolean prevAuto = conn.getAutoCommit();
        try {
            String sql = "UPDATE " + TBL_WQ + " SET " +
                    COL_DEFER_UNTIL + "=?," + COL_UPDATED + "=? " +
                    "WHERE " + COL_KEY + "=? AND " + COL_STATUS + "=? " +
                    "AND (" + COL_DEFER_UNTIL + " IS NULL OR " + COL_DEFER_UNTIL + " <= ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, deferUntil);
                ps.setTimestamp(2, now);
                ps.setString(3, key);
                ps.setString(4, ST_WORKING);
                ps.setTimestamp(5, now);
                if (ps.executeUpdate() == 0) {
                    throw explainNotUpdatableByKey(key, fetchStateByKey(key), now);
                }
            }
            conn.commit();
            return buildWorkItemByKey(key);
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(prevAuto);
        }
    }

    public WorkItem deferFromWorkingById(long itemId, Timestamp deferUntil) throws SQLException {
        if (deferUntil == null) throw new SQLException("Defer Until es obligatorio.");
        final Timestamp now = nowTs();
        if (!deferUntil.after(now)) throw new SQLException("Defer Until debe ser futuro.");

        boolean prevAuto = conn.getAutoCommit();
        try {
            String sql = "UPDATE " + TBL_WQ + " SET " +
                    COL_DEFER_UNTIL + "=?," + COL_UPDATED + "=? " +
                    "WHERE " + COL_ID + "=? AND " + COL_STATUS + "=? " +
                    "AND (" + COL_DEFER_UNTIL + " IS NULL OR " + COL_DEFER_UNTIL + " <= ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, deferUntil);
                ps.setTimestamp(2, now);
                ps.setLong(3, itemId);
                ps.setString(4, ST_WORKING);
                ps.setTimestamp(5, now);
                if (ps.executeUpdate() == 0) {
                    throw explainNotUpdatableById(itemId, fetchStateById(itemId), now);
                }
            }
            conn.commit();
            return buildWorkItemById(itemId);
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(prevAuto);
        }
    }

    // ==========================================================
    //                     WORKING → PENDING (RELEASE)
    // ==========================================================
    public WorkItem releaseFromWorkingByKey(String key) throws SQLException {
        final Timestamp now = nowTs();
        boolean prevAuto = conn.getAutoCommit();
        try {
            String sql = "UPDATE " + TBL_WQ + " SET " +
                    COL_STATUS + "=?," + COL_UPDATED + "=? " +
                    "WHERE " + COL_KEY + "=? AND " + COL_STATUS + "=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ST_PENDING);
                ps.setTimestamp(2, now);
                ps.setString(3, key);
                ps.setString(4, ST_WORKING);
                if (ps.executeUpdate() == 0) {
                    throw explainNotUpdatableByKey(key, fetchStateByKey(key), now);
                }
            }
            conn.commit();
            return buildWorkItemByKey(key);
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(prevAuto);
        }
    }

    public WorkItem releaseFromWorkingById(long itemId) throws SQLException {
        final Timestamp now = nowTs();
        boolean prevAuto = conn.getAutoCommit();
        try {
            String sql = "UPDATE " + TBL_WQ + " SET " +
                    COL_STATUS + "=?," + COL_UPDATED + "=? " +
                    "WHERE " + COL_ID + "=? AND " + COL_STATUS + "=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ST_PENDING);
                ps.setTimestamp(2, now);
                ps.setLong(3, itemId);
                ps.setString(4, ST_WORKING);
                if (ps.executeUpdate() == 0) {
                    throw explainNotUpdatableById(itemId, fetchStateById(itemId), now);
                }
            }
            conn.commit();
            return buildWorkItemById(itemId);
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(prevAuto);
        }
    }

    // ==========================================================
    //                        GET ITEM DATA
    // ==========================================================
    /** Devuelve el diccionario Key/Value de [Item Data] por Item Id. */
    public Map<String,String> getItemDataById(long itemId) throws SQLException {
        String sql = "SELECT " + COL_D_KEY + "," + COL_D_VALUE + " FROM " + TBL_DATA +
                " WHERE " + COL_D_ITEM_ID + "=? ORDER BY " + COL_D_ID + " ASC";
        Map<String,String> map = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
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
        String sql = "SELECT d." + COL_D_KEY + ", d." + COL_D_VALUE +
                " FROM " + TBL_DATA + " d INNER JOIN " + TBL_WQ + " w ON d." + COL_D_ITEM_ID + " = w." + COL_ID +
                " WHERE w." + COL_KEY + "=? ORDER BY d." + COL_D_ID + " ASC";
        Map<String,String> map = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemKey);
            try (ResultSet rs = ps.executeQuery()) {
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
        final Timestamp now = nowTs();
        boolean prevAuto = conn.getAutoCommit();
        try {
            String sql = "UPDATE " + TBL_WQ + " SET " + COL_STATUS_WF + "=?, " + COL_STEP_WF + "=?, " + COL_UPDATED + "=? " +
                    "WHERE " + COL_ID + "=? AND " + COL_STATUS + "=?"; // sigue tu regla: solo en Working
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (statusWf != null) ps.setLong(1, statusWf); else ps.setNull(1, SQL_LONG_TYPE);
                if (stepWf   != null) ps.setString(2, stepWf);  else ps.setNull(2, Types.VARCHAR);
                ps.setTimestamp(3, now);
                ps.setLong(4, id);
                ps.setString(5, ST_WORKING);
                int rows = ps.executeUpdate();
                if (rows == 0) throw explainNotUpdatableById(id, fetchStateById(id), now);
            }
            conn.commit();
            return buildWorkItemById(id);
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(prevAuto);
        }
    }

    public WorkItem updateWorkflowByKey(String key, Long statusWf, String stepWf) throws SQLException {
        final Timestamp now = nowTs();
        String sql = "UPDATE " + TBL_WQ + " SET " + COL_STATUS_WF + "=?, " + COL_STEP_WF + "=?, " + COL_UPDATED + "=? " +
                "WHERE " + COL_KEY + "=? AND " + COL_STATUS + "=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (statusWf != null) ps.setLong(1, statusWf); else ps.setNull(1, SQL_LONG_TYPE);
            if (stepWf   != null) ps.setString(2, stepWf);  else ps.setNull(2, Types.VARCHAR);
            ps.setTimestamp(3, now);
            ps.setString(4, key);
            ps.setString(5, ST_WORKING);
            int rows = ps.executeUpdate();
            if (rows == 0) throw explainNotUpdatableByKey(key, fetchStateByKey(key), now);
        }
        return buildWorkItemByKey(key);
    }



    //============================================================
    //                  HELPERS
    //============================================================
    // Carga WorkItem por ID (sin exponer [Status])
    private WorkItem buildWorkItemById(long id) throws SQLException {
        String sql = "SELECT " + COL_KEY + "," + COL_STATUS_WF + "," + COL_STEP_WF +
                " FROM " + TBL_WQ + " WHERE " + COL_ID + "=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String key = rs.getString(1);

                long wf = rs.getLong(2);
                Long statusWf = rs.wasNull() ? null : wf; // <-- manejo de null

                String stepWf = rs.getString(3);

                Map<String,String> data = getItemDataById(id);
                return new WorkItem(id, key, statusWf, stepWf, data);
            }
        }
    }

    private WorkItem buildWorkItemByKey(String key) throws SQLException {
        String sql = "SELECT " + COL_ID + "," + COL_STATUS_WF + "," + COL_STEP_WF +
                " FROM " + TBL_WQ + " WHERE " + COL_KEY + "=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                long id = rs.getLong(1);

                long wf = rs.getLong(2);
                Long statusWf = rs.wasNull() ? null : wf; // <-- manejo de null

                String stepWf = rs.getString(3);

                Map<String,String> data = getItemDataById(id);
                return new WorkItem(id, key, statusWf, stepWf, data);
            }
        }
    }


}
