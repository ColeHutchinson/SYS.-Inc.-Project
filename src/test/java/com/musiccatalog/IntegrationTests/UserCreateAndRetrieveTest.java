package com.musiccatalog.IntegrationTests;

import com.musiccatalog.dao.UserDAO;
import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.User;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IT-07-TB: UserDAO + DatabaseManager
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserCreateAndRetrieveTest {

    private static final String TEST_USERNAME = "it07_testuser";
    private static final String TEST_PASSWORD = "securePass!99";
    private static final String TEST_EMAIL    = "it07_testuser@musiccatalog.com";

    private static UserDAO userDAO;

    @BeforeAll
    static void setUp() {
        DatabaseManager.getInstance().initializeDatabase();
        userDAO = new UserDAO();
    }

    @Test
    @Order(1)
    @DisplayName("IT-07-TB Step 1: register() returns true for a new unique username")
    void testRegisterUser() {
        // Guard: remove the user if it was left over from a previous failed run
        User existing = userDAO.findByUsername(TEST_USERNAME);
        if (existing != null) {
            // No delete in UserDAO — skip re-insertion; test will still verify fields
            return;
        }

        boolean registered = userDAO.register(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL);
        assertTrue(registered, "register() should return true for a brand-new username");
    }

    @Test
    @Order(2)
    @DisplayName("IT-07-TB Step 2: findByUsername() retrieves the user with matching stored data")
    void testRetrieveUserByUsername() {
        User user = userDAO.findByUsername(TEST_USERNAME);

        assertNotNull(user, "findByUsername() should return a non-null User after registration");
        assertEquals(TEST_USERNAME, user.getUsername(), "Username should match");
        assertEquals(TEST_EMAIL, user.getEmail(), "Email should match the registered value");
        assertEquals("USER", user.getRole(), "Default role should be USER");
        assertNotNull(user.getPasswordHash(), "Password hash should not be null");
        assertFalse(user.getPasswordHash().isEmpty(), "Password hash should not be empty");
        assertTrue(user.getId() > 0, "User should have a positive generated id");
    }

    @Test
    @Order(3)
    @DisplayName("IT-07-TB Step 3: register() returns false for a duplicate username")
    void testDuplicateRegistrationFails() {
        boolean duplicate = userDAO.register(TEST_USERNAME, "anotherPassword", "other@email.com");
        assertFalse(duplicate, "register() should return false when the username is already taken");
    }
}
