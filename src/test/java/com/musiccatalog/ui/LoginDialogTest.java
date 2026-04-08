package com.musiccatalog.ui;

import com.musiccatalog.dao.UserDAO;
import com.musiccatalog.model.User;
import com.musiccatalog.ui.LoginDialog;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LoginDialog.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoginDialogTest {

    // --- Stubs ---

    /**
     * In-memory stub for UserDAO.
     * authenticate() returns a preset User or null.
     * register() returns a preset boolean and records call arguments.
     */
    static class StubUserDAO extends UserDAO {
        User authenticateResult = null;
        boolean registerResult = true;
        boolean registerWasCalled = false;
        String lastRegisteredUsername = null;
        String lastRegisteredEmail = null;

        @Override
        public User authenticate(String username, String password) {
            return authenticateResult;
        }

        @Override
        public boolean register(String username, String password, String email) {
            registerWasCalled = true;
            lastRegisteredUsername = username;
            lastRegisteredEmail = email;
            return registerResult;
        }
    }

    // --- Test fields ---

    private StubUserDAO stubUserDAO;
    private LoginDialog dialog;

    @BeforeEach
    public void setup() throws Exception {
        stubUserDAO = new StubUserDAO();
        SwingUtilities.invokeAndWait(() ->
                dialog = new LoginDialog(null)
        );
        injectUserDAO();
    }

    @AfterEach
    public void teardown() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            if (dialog != null) dialog.dispose();
        });
    }

    // --- getAuthenticatedUser ---

    @Test
    public void testGetAuthenticatedUserIsNullByDefault() {
        assertNull(dialog.getAuthenticatedUser(), "Authenticated user should be null before any login attempt");
    }

    // --- doLogin: empty field validation ---

    @Test
    public void testLoginWithEmptyUsernameShowsError() throws Exception {
        setLoginFields("", "somepassword");
        invokeDoLogin();

        assertEquals("Please enter username and password.", getLoginStatusText(),
                "Should show error when username is empty");
        assertNull(dialog.getAuthenticatedUser(), "No user should be authenticated");
    }

    @Test
    public void testLoginWithEmptyPasswordShowsError() throws Exception {
        setLoginFields("someuser", "");
        invokeDoLogin();

        assertEquals("Please enter username and password.", getLoginStatusText(),
                "Should show error when password is empty");
        assertNull(dialog.getAuthenticatedUser(), "No user should be authenticated");
    }

    @Test
    public void testLoginWithBothFieldsEmptyShowsError() throws Exception {
        setLoginFields("", "");
        invokeDoLogin();

        assertEquals("Please enter username and password.", getLoginStatusText(),
                "Should show error when both fields are empty");
        assertNull(dialog.getAuthenticatedUser(), "No user should be authenticated");
    }

    // --- doLogin: authentication outcomes ---

    @Test
    public void testLoginWithValidCredentialsSetsAuthenticatedUser() throws Exception {
        User user = new User(1, "alice", "hashed", "alice@example.com", "2024-01-01", "USER");
        stubUserDAO.authenticateResult = user;

        setLoginFields("alice", "password123");
        invokeDoLogin();

        assertNotNull(dialog.getAuthenticatedUser(), "Authenticated user should be set on successful login");
        assertEquals("alice", dialog.getAuthenticatedUser().getUsername());
    }

    @Test
    public void testLoginWithInvalidCredentialsShowsError() throws Exception {
        stubUserDAO.authenticateResult = null;

        setLoginFields("alice", "wrongpassword");
        invokeDoLogin();

        assertEquals("Invalid username or password.", getLoginStatusText(),
                "Should show error on failed authentication");
        assertNull(dialog.getAuthenticatedUser(), "No user should be authenticated on failure");
    }

    @Test
    public void testLoginFailureClearsPasswordField() throws Exception {
        stubUserDAO.authenticateResult = null;

        setLoginFields("alice", "wrongpassword");
        invokeDoLogin();

        JPasswordField passwordField = getField("loginPasswordField", JPasswordField.class);
        SwingUtilities.invokeAndWait(() -> {});
        assertEquals("", new String(passwordField.getPassword()),
                "Password field should be cleared after failed login");
    }

    // --- doRegister: empty field validation ---

    @Test
    public void testRegisterWithEmptyUsernameShowsError() throws Exception {
        setRegisterFields("", "test@example.com", "pass1", "pass1");
        invokeDoRegister();

        assertEquals("All fields are required.", getRegStatusText(),
                "Should show error when username is empty");
        assertFalse(stubUserDAO.registerWasCalled, "register() should not be called with empty fields");
    }

    @Test
    public void testRegisterWithEmptyEmailShowsError() throws Exception {
        setRegisterFields("alice", "", "pass1", "pass1");
        invokeDoRegister();

        assertEquals("All fields are required.", getRegStatusText(),
                "Should show error when email is empty");
        assertFalse(stubUserDAO.registerWasCalled, "register() should not be called with empty fields");
    }

    @Test
    public void testRegisterWithEmptyPasswordShowsError() throws Exception {
        setRegisterFields("alice", "alice@example.com", "", "");
        invokeDoRegister();

        assertEquals("All fields are required.", getRegStatusText(),
                "Should show error when password is empty");
        assertFalse(stubUserDAO.registerWasCalled, "register() should not be called with empty fields");
    }

    // --- doRegister: email validation ---

    @Test
    public void testRegisterWithInvalidEmailShowsError() throws Exception {
        setRegisterFields("alice", "notanemail", "pass1", "pass1");
        invokeDoRegister();

        assertEquals("Please enter a valid email address.", getRegStatusText(),
                "Should show error for email without @");
        assertFalse(stubUserDAO.registerWasCalled, "register() should not be called with invalid email");
    }

    // --- doRegister: password validation ---

    @Test
    public void testRegisterWithShortPasswordShowsError() throws Exception {
        setRegisterFields("alice", "alice@example.com", "abc", "abc");
        invokeDoRegister();

        assertEquals("Password must be at least 4 characters.", getRegStatusText(),
                "Should show error when password is under 4 characters");
        assertFalse(stubUserDAO.registerWasCalled, "register() should not be called with short password");
    }

    @Test
    public void testRegisterWithMismatchedPasswordsShowsError() throws Exception {
        setRegisterFields("alice", "alice@example.com", "password1", "password2");
        invokeDoRegister();

        assertEquals("Passwords do not match.", getRegStatusText(),
                "Should show error when passwords do not match");
        assertFalse(stubUserDAO.registerWasCalled, "register() should not be called when passwords mismatch");
    }

    // --- doRegister: success and failure outcomes ---

    @Test
    public void testRegisterSuccessShowsConfirmationAndClearsFields() throws Exception {
        stubUserDAO.registerResult = true;
        setRegisterFields("newuser", "new@example.com", "pass1234", "pass1234");
        invokeDoRegister();

        assertEquals("Account created! You can now log in.", getRegStatusText(),
                "Should show success message on successful registration");
        assertTrue(stubUserDAO.registerWasCalled, "register() should have been called");
        assertEquals("newuser",         stubUserDAO.lastRegisteredUsername);
        assertEquals("new@example.com", stubUserDAO.lastRegisteredEmail);

        // Verify fields are cleared
        SwingUtilities.invokeAndWait(() -> {});
        assertEquals("", getField("regUsernameField", JTextField.class).getText(), "Username field should be cleared");
        assertEquals("", getField("regEmailField",    JTextField.class).getText(), "Email field should be cleared");
        assertEquals("", new String(getField("regPasswordField", JPasswordField.class).getPassword()),
                "Password field should be cleared");
        assertEquals("", new String(getField("regConfirmField",  JPasswordField.class).getPassword()),
                "Confirm field should be cleared");
    }

    @Test
    public void testRegisterFailureShowsDuplicateUsernameError() throws Exception {
        stubUserDAO.registerResult = false;
        setRegisterFields("takenuser", "user@example.com", "pass1234", "pass1234");
        invokeDoRegister();

        assertEquals("Username already exists. Choose another.", getRegStatusText(),
                "Should show error when username is already taken");
    }

    // --- Reflection helpers ---

    private void injectUserDAO() throws Exception {
        Field f = LoginDialog.class.getDeclaredField("userDAO");
        f.setAccessible(true);
        f.set(dialog, stubUserDAO);
    }

    private <T> T getField(String name, Class<T> type) {
        try {
            Field f = LoginDialog.class.getDeclaredField(name);
            f.setAccessible(true);
            return type.cast(f.get(dialog));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setLoginFields(String username, String password) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            getField("loginUsernameField", JTextField.class).setText(username);
            getField("loginPasswordField", JPasswordField.class).setText(password);
        });
    }

    private void setRegisterFields(String username, String email, String password, String confirm) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            getField("regUsernameField",  JTextField.class).setText(username);
            getField("regEmailField",     JTextField.class).setText(email);
            getField("regPasswordField",  JPasswordField.class).setText(password);
            getField("regConfirmField",   JPasswordField.class).setText(confirm);
        });
    }

    private void invokeDoLogin() throws Exception {
        Method m = LoginDialog.class.getDeclaredMethod("doLogin");
        m.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try {
                m.invoke(dialog);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void invokeDoRegister() throws Exception {
        Method m = LoginDialog.class.getDeclaredMethod("doRegister");
        m.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try {
                m.invoke(dialog);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String getLoginStatusText() throws Exception {
        final String[] text = new String[1];
        SwingUtilities.invokeAndWait(() ->
                text[0] = getField("loginStatusLabel", JLabel.class).getText()
        );
        return text[0];
    }

    private String getRegStatusText() throws Exception {
        final String[] text = new String[1];
        SwingUtilities.invokeAndWait(() ->
                text[0] = getField("regStatusLabel", JLabel.class).getText()
        );
        return text[0];
    }
}