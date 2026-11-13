/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.utilities.workqueue;

import com.automationanywhere.toolchain.runtime.session.CloseableSessionObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorkqueueSession
implements CloseableSessionObject {
    public static final Map<String, WorkqueueSession> SESSION_BY_FILE = new ConcurrentHashMap<String, WorkqueueSession>();
    private final String sessionId = UUID.randomUUID().toString();
    private final String filePath;
    private Connection connection;

    public WorkqueueSession(String filePath) throws SQLException, ClassNotFoundException {
        this.filePath = filePath;
        this.openConnection();
        SESSION_BY_FILE.put(filePath, this);
    }

    private void openConnection() throws SQLException, ClassNotFoundException {
        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        String url = "jdbc:ucanaccess://" + this.filePath + ";immediatelyReleaseResources=true";
        this.connection = DriverManager.getConnection(url);
        this.connection.setAutoCommit(true);
    }

    public Connection getConnection() {
        return this.connection;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    @Override
    public void close() {
        try {
            if (!this.isClosed()) {
                this.connection.close();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isClosed() {
        try {
            return this.connection == null || this.connection.isClosed();
        }
        catch (SQLException e) {
            return true;
        }
    }
}
