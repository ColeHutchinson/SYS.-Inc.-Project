package com.musiccatalog.dao;

import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.User;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserDAO.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserDAOTest {

    private UserDAO dao;
    private Connection conn;

    @BeforeAll
    public void setupDatabase() throws SQLException {
        DatabaseManager.getInstance().initializeDatabase();
        conn = DatabaseManager.getInstance().getConnection();
        dao = new UserDAO();
    }

    @BeforeEach
    public void clearUsers() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM users");
        }
    }

    @AfterAll
    public void cleanup() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    // --- register ---

    @Test
    public void testRegister() {
        boolean result = dao.register("alice", "password123", "alice@example.com");
        assertTrue(result, "Registering a new user should succeed");

        User found = dao.findByUsername("alice");
        assertNotNull(found, "Registered user should be retrievable by username");
        assertEquals("alice", found.getUsername());
        assertEquals("alice@example.com", found.getEmail());
        assertEquals("USER", found.getRole());
    }

    @Test
    public void testRegisterPasswordIsHashed() {
        dao.register("bob", "securepass", "bob@example.com");

        User found = dao.findByUsername("bob");
        assertNotNull(found, "User should exist after registration");
        assertNotEquals("securepass", found.getPasswordHash(), "Password should be stored as a hash, not plaintext");
    }

    @Test
    public void testRegisterDuplicateUsernameReturnsFalse() {
        dao.register("charlie", "pass1", "charlie@example.com");

        boolean duplicate = dao.register("charlie", "pass2", "charlie2@example.com");
        assertFalse(duplicate, "Registering a duplicate username should return false");

        // Confirm only one user exists with that username
        List<User> users = dao.findAll();
        long count = users.stream().filter(u -> "charlie".equals(u.getUsername())).count();
        assertEquals(1, count, "Only one user should exist with the duplicate username");
    }

    // --- authenticate ---

    @Test
    public void testAuthenticateWithCorrectCredentials() {
        dao.register("dave", "mypassword", "dave@example.com");

        User authenticated = dao.authenticate("dave", "mypassword");
        assertNotNull(authenticated, "Authentication with correct credentials should succeed");
        assertEquals("dave", authenticated.getUsername());
    }

    @Test
    public void testAuthenticateWithWrongPassword() {
        dao.register("eve", "correctpass", "eve@example.com");

        User authenticated = dao.authenticate("eve", "wrongpass");
        assertNull(authenticated, "Authentication with wrong password should return null");
    }

    @Test
    public void testAuthenticateWithUnknownUsername() {
        User authenticated = dao.authenticate("ghost", "anypassword");
        assertNull(authenticated, "Authentication with unknown username should return null");
    }

    // --- findByUsername ---

    @Test
    public void testFindByUsername() {
        dao.register("frank", "pass", "frank@example.com");

        User found = dao.findByUsername("frank");
        assertNotNull(found, "Should find user by username");
        assertEquals("frank", found.getUsername());
    }

    @Test
    public void testFindByUsernameReturnsNullForUnknownUser() {
        User found = dao.findByUsername("nobody");
        assertNull(found, "Should return null for a username that does not exist");
    }

    // --- findById ---

    @Test
    public void testFindById() {
        dao.register("grace", "pass", "grace@example.com");

        User registered = dao.findByUsername("grace");
        assertNotNull(registered, "Should find user after registration");

        User found = dao.findById(registered.getId());
        assertNotNull(found, "Should find user by ID");
        assertEquals("grace", found.getUsername());
        assertEquals("grace@example.com", found.getEmail());
    }

    @Test
    public void testFindByIdReturnsNullForMissingId() {
        User found = dao.findById(99999);
        assertNull(found, "Should return null for a non-existent ID");
    }

    // --- findAll ---

    @Test
    public void testFindAll() {
        dao.register("henry", "pass", "henry@example.com");
        dao.register("iris", "pass", "iris@example.com");
        dao.register("jack", "pass", "jack@example.com");

        List<User> users = dao.findAll();
        assertEquals(3, users.size(), "Should return all registered users");
    }

    @Test
    public void testFindAllOrderedByUsernameAsc() {
        dao.register("zara", "pass", "zara@example.com");
        dao.register("adam", "pass", "adam@example.com");
        dao.register("mike", "pass", "mike@example.com");

        List<User> users = dao.findAll();
        assertEquals("adam", users.get(0).getUsername(), "First user should be alphabetically first");
        assertEquals("zara", users.get(users.size() - 1).getUsername(), "Last user should be alphabetically last");
    }

    @Test
    public void testFindAllReturnsEmptyWhenNoUsers() {
        List<User> users = dao.findAll();
        assertNotNull(users, "Result should not be null when no users exist");
        assertTrue(users.isEmpty(), "Should return empty list when no users are registered");
    }
}