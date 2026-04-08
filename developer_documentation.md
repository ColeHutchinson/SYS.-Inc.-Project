**Music Catalog System**

Developer Documentation

*Technical Reference & Architecture Guide*

**1. Overview**

Music Catalog System is a Java Swing desktop application providing a
user-authenticated music catalog browser with full CRUD operations,
search, filtering, and sorting capabilities. It uses SQLite for local
persistence via the JDBC driver.

**2. System Requirements**

  -------------------------- --------------------------------------------
  **Requirement**            **Version / Notes**

  Java JDK                   17 or higher

  SQLite JDBC Driver         3.51.x (e.g. sqlite-jdbc-3.51.2.0.jar)

  Apache Ant                 Any recent version

  Operating System           Windows (build.bat provided); cross-platform
                             via Ant
  -------------------------- --------------------------------------------

**3. Dependencies**

The project has a single external runtime dependency:

-   **sqlite-jdbc-3.51.2.0.jar ---** SQLite JDBC Driver

*Download from:*

> https://github.com/xerial/sqlite-jdbc/releases/download/3.51.2.0/sqlite-jdbc-3.51.2.0.jar

Place the JAR in the lib/ directory at the project root. This path is
referenced by both the Ant build file and the runtime classpath.

**4. Project Structure**

> MusicCatalog/
>
> ├── src/
>
> │ └── com/musiccatalog/
>
> │ ├── Main.java ← Entry point
>
> │ ├── model/
>
> │ │ ├── User.java
>
> │ │ └── Song.java
>
> │ ├── db/
>
> │ │ └── DatabaseManager.java ← SQLite init & seed data
>
> │ ├── dao/
>
> │ │ ├── UserDAO.java ← Auth & registration queries
>
> │ │ └── SongDAO.java ← Search, sort, CRUD queries
>
> │ └── ui/
>
> │ ├── LoginDialog.java ← Login & register dialog
>
> │ ├── CatalogWindow.java ← Main application window
>
> │ ├── SongTableModel.java ← JTable model
>
> │ └── SongFormDialog.java ← Add / Edit song form
>
> ├── lib/ ← External JARs (sqlite-jdbc)
>
> ├── out/ ← Compiled output (auto-generated)
>
> ├── build.xml ← Apache Ant build configuration
>
> ├── build.bat ← Windows build & run script
>
> └── README.md

**5. Architecture**

**5.1 Layer Overview**

The application follows a layered architecture with clear separation
between UI, data access, and persistence:

  ---------------- ------------------------ -------------------------------
  **Layer**        **Package**              **Responsibility**

  UI Layer         com.musiccatalog.ui      Swing components, dialogs,
                                            table models

  Model Layer      com.musiccatalog.model   Plain Java objects: User, Song

  DAO Layer        com.musiccatalog.dao     SQL queries, result mapping

  Database Layer   com.musiccatalog.db      Connection management, schema
                                            init, seed data
  ---------------- ------------------------ -------------------------------

**5.2 Key Classes**

**Main.java**

Application entry point. Instantiates the LoginDialog and, on successful
authentication, launches the CatalogWindow.

**DatabaseManager.java**

Handles SQLite connection lifecycle, schema creation on first launch,
and seeding the catalog with 30 pre-loaded songs. The database file
music_catalog.db is created automatically in the working directory.

**UserDAO.java**

Provides authenticate(username, password) and register(username,
password, email) methods. Passwords are stored as hashes --- plain-text
comparison is never performed.

**SongDAO.java**

Implements all song-related queries: full-text search across title,
artist, album, and genre; sort by any column (ascending/descending);
genre filtering; and CRUD operations (insert, update, delete).

**SongTableModel.java**

Extends AbstractTableModel to back the JTable in CatalogWindow. Holds an
in-memory list of Song objects and delegates mutations back to SongDAO.

**CatalogWindow.java**

Main application window. Hosts the JTable, toolbar, search field, genre
dropdown, and status bar. Wires UI events to DAO calls and refreshes the
table model on data changes.

**LoginDialog.java**

Modal dialog presenting Login and Register tabs. Calls UserDAO for
authentication and passes the authenticated User object back to Main on
success.

**SongFormDialog.java**

Reusable modal form for Add and Edit operations. Validates input fields
before invoking SongDAO.

**6. Database Schema**

The SQLite database is initialised automatically by DatabaseManager on
first launch. The schema includes two tables that are active and two
that are scaffolded for future playlist functionality.

> \-- Active tables
>
> users (
>
> id INTEGER PRIMARY KEY AUTOINCREMENT,
>
> username TEXT NOT NULL UNIQUE,
>
> password_hash TEXT NOT NULL,
>
> email TEXT,
>
> created_at DATETIME DEFAULT CURRENT_TIMESTAMP
>
> );
>
> songs (
>
> id INTEGER PRIMARY KEY AUTOINCREMENT,
>
> title TEXT NOT NULL,
>
> artist TEXT NOT NULL,
>
> album TEXT,
>
> duration_seconds INTEGER,
>
> genre TEXT,
>
> release_year INTEGER,
>
> added_at DATETIME DEFAULT CURRENT_TIMESTAMP
>
> );
>
> \-- Ready for future use
>
> playlists (
>
> id INTEGER PRIMARY KEY AUTOINCREMENT,
>
> user_id INTEGER REFERENCES users(id),
>
> name TEXT NOT NULL,
>
> description TEXT,
>
> created_at DATETIME DEFAULT CURRENT_TIMESTAMP
>
> );
>
> playlist_songs (
>
> playlist_id INTEGER REFERENCES playlists(id),
>
> song_id INTEGER REFERENCES songs(id),
>
> position INTEGER,
>
> added_at DATETIME DEFAULT CURRENT_TIMESTAMP
>
> );

**7. Build & Run**

**7.1 Adding the JDBC Driver in IntelliJ IDEA**

1.  Go to File → Project Structure

2.  Click Libraries, then + → Java

3.  Select the downloaded sqlite-jdbc-3.51.2.0.jar

4.  Click OK and rebuild the project

**7.2 Windows Build Script**

Run the following from the project root to clean, compile, and launch
the application in one step:

> .\\ build.bat

**7.3 Manual Ant Targets**

  ---------------------- ------------------------------------------------
  **Command**            **Description**

  ant clean              Remove previous build output from out/

  ant compile            Compile all source files into out/

  ant run                Compile (if needed) and launch the application

  ant jar                Package the application into MusicCatalog.jar
  ---------------------- ------------------------------------------------

**7.4 Running the Packaged JAR**

> java -jar MusicCatalog.jar

Ensure the SQLite JDBC JAR is on the classpath, or bundled in the
manifest, when running the standalone JAR.

**8. Default Login Credentials**

The following accounts are seeded by DatabaseManager on first launch:

  ------------------ ------------------ ---------------------------------
  **Username**       **Password**       **Notes**

  admin              admin123           Full administrative account

  demo               demo               Demonstration / testing account
  ------------------ ------------------ ---------------------------------

Passwords are stored as hashes. These defaults should be changed before
any production deployment.

**9. Extending the Application**

**9.1 Implementing Playlists**

The database schema already includes the playlists and playlist_songs
tables. To activate this feature:

-   Create a PlaylistDAO class in com.musiccatalog.dao with methods for
    create, rename, delete, addSong, and removeSong.

-   Add a Playlist model class in com.musiccatalog.model.

-   Extend CatalogWindow with a playlist panel or sidebar, wiring it to
    PlaylistDAO.

**9.2 Adding New Sort Columns**

SongDAO.getSongs(sortColumn, sortDirection, searchQuery, genreFilter)
builds its ORDER BY clause dynamically. To add a new sortable column,
extend the Song model, update the songs table schema, and add the column
name to the sort column list in CatalogWindow.

**9.3 Password Hashing**

Review the hashing algorithm used in UserDAO. If bcrypt or Argon2 is not
already in use, consider replacing the current implementation before
shipping to production.

**10. Troubleshooting**

  ------------------------ --------------------- ------------------------------
  **Symptom**              **Likely Cause**      **Resolution**

  ClassNotFoundException   JDBC JAR not on       Verify sqlite-jdbc JAR is in
  on launch                classpath             lib/ and referenced in
                                                 build.xml

  Database not created     Insufficient write    Run the application from a
                           permissions in        directory where the process
                           working dir           has write access

  Build fails: cannot find Missing import or     Confirm JDK 17+ is active:
  symbol                   wrong JDK version     java -version

  Login always fails       Corrupted DB or wrong Delete music_catalog.db and
                           hash                  relaunch to re-seed
  ------------------------ --------------------- ------------------------------
