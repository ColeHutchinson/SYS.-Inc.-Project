package com.musiccatalog.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the SQLite database connection.
 * Uses a singleton pattern so only one connection exists.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:music_catalog.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC driver not found. Make sure sqlite-jdbc.jar is in your classpath.", e);
            }
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Creates all tables and seeds sample data on first run.
     */
    public void initializeDatabase() {
        try (Statement stmt = getConnection().createStatement()) {

            // Users table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    created_at TEXT DEFAULT (datetime('now'))
                )
            """);

            // Songs table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS songs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    artist TEXT NOT NULL,
                    album TEXT,
                    duration_seconds INTEGER NOT NULL,
                    genre TEXT,
                    release_year INTEGER,
                    added_at TEXT DEFAULT (datetime('now'))
                )
            """);

            // Playlists table (for future use)
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS playlists (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT,
                    created_at TEXT DEFAULT (datetime('now')),
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            // Playlist_songs junction table (for future use)
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS playlist_songs (
                    playlist_id INTEGER NOT NULL,
                    song_id INTEGER NOT NULL,
                    position INTEGER NOT NULL,
                    added_at TEXT DEFAULT (datetime('now')),
                    PRIMARY KEY (playlist_id, song_id),
                    FOREIGN KEY (playlist_id) REFERENCES playlists(id),
                    FOREIGN KEY (song_id) REFERENCES songs(id)
                )
            """);

            seedSampleData(stmt);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seedSampleData(Statement stmt) throws SQLException {
        // Only seed if songs table is empty
        var rs = stmt.executeQuery("SELECT COUNT(*) FROM songs");
        if (rs.getInt(1) > 0) return;

        String insertSongs = """
            INSERT INTO songs (title, artist, album, duration_seconds, genre, release_year) VALUES
            ('Bohemian Rhapsody', 'Queen', 'A Night at the Opera', 354, 'Rock', 1975),
            ('Hotel California', 'Eagles', 'Hotel California', 391, 'Rock', 1977),
            ('Stairway to Heaven', 'Led Zeppelin', 'Led Zeppelin IV', 482, 'Rock', 1971),
            ('Smells Like Teen Spirit', 'Nirvana', 'Nevermind', 301, 'Grunge', 1991),
            ('Billie Jean', 'Michael Jackson', 'Thriller', 294, 'Pop', 1982),
            ('Like a Rolling Stone', 'Bob Dylan', 'Highway 61 Revisited', 369, 'Folk Rock', 1965),
            ('Imagine', 'John Lennon', 'Imagine', 187, 'Pop', 1971),
            ('Purple Haze', 'Jimi Hendrix', 'Are You Experienced', 170, 'Rock', 1967),
            ('Johnny B. Goode', 'Chuck Berry', 'Chuck Berry Is on Top', 162, 'Rock and Roll', 1958),
            ('What''s Going On', 'Marvin Gaye', 'What''s Going On', 234, 'Soul', 1971),
            ('Respect', 'Aretha Franklin', 'I Never Loved a Man', 147, 'Soul', 1967),
            ('Good Vibrations', 'The Beach Boys', 'Smiley Smile', 215, 'Pop', 1966),
            ('Johnny 99', 'Bruce Springsteen', 'Nebraska', 243, 'Folk Rock', 1982),
            ('Superstition', 'Stevie Wonder', 'Talking Book', 245, 'R&B', 1972),
            ('Born to Run', 'Bruce Springsteen', 'Born to Run', 270, 'Rock', 1975),
            ('Waterloo Sunset', 'The Kinks', 'Something Else', 213, 'Rock', 1967),
            ('Lose Yourself', 'Eminem', '8 Mile Soundtrack', 326, 'Hip-Hop', 2002),
            ('God Save the Queen', 'Sex Pistols', 'Never Mind the Bollocks', 206, 'Punk', 1977),
            ('Blinding Lights', 'The Weeknd', 'After Hours', 200, 'Synth-Pop', 2020),
            ('Shape of You', 'Ed Sheeran', 'Divide', 234, 'Pop', 2017),
            ('Rolling in the Deep', 'Adele', '21', 228, 'Soul', 2010),
            ('Uptown Funk', 'Mark Ronson ft. Bruno Mars', 'Uptown Special', 270, 'Funk', 2014),
            ('Happy', 'Pharrell Williams', 'Despicable Me 2', 233, 'Pop', 2013),
            ('Stay With Me', 'Sam Smith', 'In the Lonely Hour', 172, 'Soul', 2014),
            ('Thinking Out Loud', 'Ed Sheeran', 'x', 281, 'Pop', 2014),
            ('Let Her Go', 'Passenger', 'All the Little Lights', 253, 'Folk Pop', 2012),
            ('Someone Like You', 'Adele', '21', 285, 'Pop', 2011),
            ('Counting Stars', 'OneRepublic', 'Native', 257, 'Pop Rock', 2013),
            ('Radioactive', 'Imagine Dragons', 'Night Visions', 187, 'Rock', 2012),
            ('Demons', 'Imagine Dragons', 'Night Visions', 177, 'Pop Rock', 2012)
        """;

        stmt.executeUpdate(insertSongs);

        // Default admin user (password: "admin123")
        stmt.executeUpdate("""
            INSERT OR IGNORE INTO users (username, password_hash, email)
            VALUES ('admin', '0192023a7bbd73250516f069df18b500', 'admin@musiccatalog.com')
        """);

        // Default demo user (password: "demo")
        stmt.executeUpdate("""
            INSERT OR IGNORE INTO users (username, password_hash, email)
            VALUES ('demo', 'fe01ce2a7fbac8fafaed7c982a04e229', 'demo@musiccatalog.com')
        """);

        System.out.println("Sample data seeded.");
    }
}
