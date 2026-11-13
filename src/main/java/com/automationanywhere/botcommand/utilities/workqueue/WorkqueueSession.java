package com.automationanywhere.botcommand.utilities.workqueue;

import com.automationanywhere.toolchain.runtime.session.CloseableSessionObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorkqueueSession implements CloseableSessionObject {

    public static final Map<String, WorkqueueSession> SESSION_BY_FILE = new ConcurrentHashMap<>();

    private final String sessionId;
    private final String filePath;
    private Connection connection;

    public WorkqueueSession(String filePath) throws SQLException, ClassNotFoundException {
        this.sessionId = UUID.randomUUID().toString();
        this.filePath = filePath;
        openConnection();
        SESSION_BY_FILE.put(filePath, this);
    }

    private void openConnection() throws SQLException, ClassNotFoundException {
        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

        String url = "jdbc:ucanaccess://" + filePath
                + ";immediatelyReleaseResources=true";

        this.connection = DriverManager.getConnection(url);
        this.connection.setAutoCommit(true); // <<— clave para simplificar

    }

    public Connection getConnection() {
        return connection;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void close() {
        try {
            if (!isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace(); // O usar un logger si lo integrás
        }
    }

    @Override
    public boolean isClosed() {
        try {
            return connection == null || connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }
}
