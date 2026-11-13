/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.utilities.workqueue;

import java.sql.Connection;
import java.sql.DriverManager;

public class AccessExecutor {
    private static final String DRIVER = "net.ucanaccess.jdbc.UcanaccessDriver";

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static <T> T executeWithConnection(String filePath, SqlFunction<Connection, T> action) {
        try {
            Class.forName(DRIVER);
            String url = "jdbc:ucanaccess://" + filePath + ";immediatelyReleaseResources=true";
            try (Connection conn = DriverManager.getConnection(url);){
                conn.setAutoCommit(true);
                T t = action.apply(conn);
                return t;
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Error ejecutando acci\u00f3n sobre Access: " + e.getMessage(), e);
        }
    }

    public static void executeVoidWithConnection(String filePath, SqlConsumer<Connection> action) {
        AccessExecutor.executeWithConnection(filePath, conn -> {
            action.accept((Connection)conn);
            return null;
        });
    }

    @FunctionalInterface
    public static interface SqlConsumer<C> {
        public void accept(C var1) throws Exception;
    }

    @FunctionalInterface
    public static interface SqlFunction<C, R> {
        public R apply(C var1) throws Exception;
    }
}
