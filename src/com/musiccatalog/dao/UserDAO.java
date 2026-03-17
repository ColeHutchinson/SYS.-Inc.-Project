package com.musiccatalog.dao;

import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.User;
import com.musiccatalog.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User operations.
 */
public class UserDAO {

    private Connection getConn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        return null;
    }

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by id: " + e.getMessage());
        }
        return null;
    }

    /**
     * Authenticate a user. Returns the User object on success, null on failure.
     */
    public User authenticate(String username, String password) {
        User user = findByUsername(username);
        if (user != null && PasswordUtil.verify(password, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    /**
     * Register a new user. Returns true on success.
     */
    public boolean register(String username, String password, String email) {
        if (findByUsername(username) != null) {
            return false; // Username taken
        }
        String sql = "INSERT INTO users (username, password_hash, email, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            ps.setString(3, email);
            ps.setString(4, "USER");
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }
        return users;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("email"),
            rs.getString("created_at"),
            rs.getString("role")
        );
    }
}
