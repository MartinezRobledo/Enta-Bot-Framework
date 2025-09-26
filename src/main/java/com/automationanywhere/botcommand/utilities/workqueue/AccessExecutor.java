package com.automationanywhere.botcommand.utilities.workqueue;

import java.sql.Connection;
import java.sql.DriverManager;

public class AccessExecutor {

    private static final String DRIVER = "net.ucanaccess.jdbc.UcanaccessDriver";

    public static <T> T executeWithConnection(String filePath, SqlFunction<Connection, T> action) {
        try {
            Class.forName(DRIVER);
            String url = "jdbc:ucanaccess://" + filePath + ";immediatelyReleaseResources=true";
            try (Connection conn = DriverManager.getConnection(url)) {
                conn.setAutoCommit(true); // simplifica
                return action.apply(conn);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error ejecutando acción sobre Access: " + e.getMessage(), e);
        }
    }

    @FunctionalInterface
    public interface SqlFunction<C, R> {
        R apply(C conn) throws Exception;
    }

    public static void executeVoidWithConnection(String filePath, SqlConsumer<Connection> action) {
        executeWithConnection(filePath, conn -> { action.accept(conn); return null; });
    }

    @FunctionalInterface
    public interface SqlConsumer<C> {
        void accept(C conn) throws Exception;
    }

}
