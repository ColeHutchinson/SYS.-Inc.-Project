# Music Catalog System

A Java Swing desktop application for browsing and managing a music catalog with user authentication.

---

## Features

- **Login & Registration** — Secure login with hashed passwords. Register new accounts.
- **Song Catalog** — Browse 30 pre-loaded songs across multiple genres.
- **Sorting** — Sort by Title, Artist, Album, Duration, Genre, Year, or Date Added (ascending/descending).
- **Search** — Live search across title, artist, album, and genre.
- **Genre Filter** — Filter catalog by genre using a dropdown.
- **Add / Edit / Delete Songs** — Full CRUD support via form dialogs (double-click or right-click).
- **Status Bar** — Shows total songs and current filter count.
- **Playlist-Ready Schema** — Database tables for playlists and playlist_songs are already created for future use.

---

## Requirements

| Requirement | Version  |
|-------------|----------|
| Java JDK    | 17+      |
| SQLite JDBC | 3.51.x   |
| Apache Ant  | Any      |

---

## Quick Start

### 1. Download the SQLite JDBC Driver

Download `sqlite-jdbc-3.51.2.0.jar` from:

```
https://github.com/xerial/sqlite-jdbc/releases/download/3.51.2.0/sqlite-jdbc-3.51.2.0.jar
```

Then add it to your project in IntelliJ IDEA:

1. Go to **File → Project Structure**
2. Click **Libraries**
3. Click **+**
4. Select **Java**
5. Choose the downloaded file: `sqlite-jdbc-3.51.2.0.jar`

> **Note:** If using Ant, place the JAR in the `lib/` directory instead.

---

## Default Login Credentials

| Username | Password |
|----------|----------|
| admin    | admin123 |
| demo     | demo     |

The SQLite database file (`music_catalog.db`) is created automatically on first launch in the working directory.

---

## Build Files and Scripts

This project uses **Apache Ant** as its build automation tool. The main build configuration is defined in `build.xml`, which handles compiling source files, running the application, and packaging the project into a runnable JAR file.

A **Windows build script** (`build.bat`) is also included to simplify the build and execution process by automating common tasks such as cleaning, compiling, and running the application.

### Running the Build Script

From the project root directory, run:

```bat
.\build.bat
```

Alternatively, you can double-click the file in Windows Explorer. The script will automatically:

1. Clean previous build artifacts
2. Compile all source files
3. Run the application

### Manual Build Commands (Apache Ant)

If you prefer to run Ant targets manually:

| Command       | Description                  |
|---------------|------------------------------|
| `ant clean`   | Remove previous build output |
| `ant compile` | Compile all source files     |
| `ant run`     | Run the application          |
| `ant jar`     | Package into a runnable JAR  |

To run the generated JAR directly:

```bash
java -jar MusicCatalog.jar
```

---

## New Developer Setup Guide

Follow these steps to get up and running from scratch:

1. **Install prerequisites:**
    - Java JDK 17 or higher
    - Apache Ant

2. **Clone or download** the project repository.

3. **Add dependencies:** Place the SQLite JDBC driver JAR in the `lib/` directory.

4. **Build and run** from the project root:

```bat
build.bat
```

This will automatically compile and launch the application.

### Notes

- The main entry point is `com.musiccatalog.Main`
- All external dependencies must be located in the `lib/` folder
- The `out/` directory is generated during compilation — do not modify it manually
- If database-related errors occur, verify that the SQLite JDBC JAR is present in `lib/`

---

## Project Structure

```
MusicCatalog/
├── src/
│   └── com/musiccatalog/
│       ├── Main.java                  ← Entry point
│       ├── model/
│       │   ├── User.java
│       │   └── Song.java
│       ├── db/
│       │   └── DatabaseManager.java   ← SQLite init & seed data
│       ├── dao/
│       │   ├── UserDAO.java           ← User queries (auth, register)
│       │   └── SongDAO.java           ← Song queries (search, sort, CRUD)
│       └── ui/
│           ├── LoginDialog.java       ← Login & Register dialog
│           ├── CatalogWindow.java     ← Main window
│           ├── SongTableModel.java    ← JTable model
│           └── SongFormDialog.java    ← Add / Edit song form
├── lib/                               ← External JARs (e.g. sqlite-jdbc)
├── out/                               ← Compiled output (auto-generated)
├── build.xml                          ← Apache Ant build file
├── build.bat                          ← Windows build & run script
└── README.md
```

---

## Database Schema

```sql
users          — id, username, password_hash, email, created_at
songs          — id, title, artist, album, duration_seconds, genre, release_year, added_at
playlists      — id, user_id, name, description, created_at        (ready for future use)
playlist_songs — playlist_id, song_id, position, added_at          (ready for future use)
```

---

## Diagrams

![Class Diagram](resource%20root/ClassDiagram.png)

![Sequence Diagram](resource%20root/SequenceDiagram.png)

![State Chart Diagram](resource%20root/StateChartDiagram.png)