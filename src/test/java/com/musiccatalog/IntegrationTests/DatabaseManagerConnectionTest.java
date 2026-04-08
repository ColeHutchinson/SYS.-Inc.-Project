package com.musiccatalog.IntegrationTests;

import com.musiccatalog.db.DatabaseManager;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IT-08-CB: DatabaseManager + SQLite JDBC
 */
public class DatabaseManagerConnectionTest {

    @BeforeEach
    void initDb() {
        DatabaseManager.getInstance().initializeDatabase();
    }

    @Test
    @DisplayName("IT-08-CB: connection opens, simple COUNT(*) query executes, connection closes without error")
    void testConnectionOpenQueryClose() throws SQLException {
        DatabaseManager manager = DatabaseManager.getInstance();

        // Step 1 — open connection
        Connection connection = assertDoesNotThrow(
                manager::getConnection,
                "getConnection() should not throw"
        );
        assertNotNull(connection, "Connection should not be null");
        assertFalse(connection.isClosed(), "Connection should be open after getConnection()");

        // Step 2 — execute a simple query and verify expected result
        int songCount;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM songs")) {
            assertTrue(rs.next(), "ResultSet should have at least one row");
            songCount = rs.getInt(1);
        }
        assertTrue(songCount > 0, "Seeded songs table should contain at least one row; got " + songCount);

        // Step 3 — close connection without error
        assertDoesNotThrow(
                manager::closeConnection,
                "closeConnection() should not throw"
        );

        // Re-obtain connection to verify isClosed() state (singleton may reconnect on next call)
        // We verify the close was called cleanly by checking no exception was thrown above.
    }

    @Test
    @DisplayName("IT-08-CB: getConnection() reopens a closed connection transparently")
    void testConnectionReopensAfterClose() throws SQLException {
        DatabaseManager manager = DatabaseManager.getInstance();
        manager.closeConnection();

        // Should transparently reconnect
        Connection connection = assertDoesNotThrow(
                manager::getConnection,
                "getConnection() should reopen the connection after it was closed"
        );
        assertNotNull(connection);
        assertFalse(connection.isClosed(), "Re-opened connection should not be closed");
    }
}
