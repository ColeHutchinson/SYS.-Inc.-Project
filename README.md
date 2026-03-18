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

| Requirement | Version |
|-------------|---------|
| Java JDK    | 17+     |
| SQLite JDBC | 3.51.x   |

---

## Quick Start

### 1. Download the SQLite JDBC Driver

Download `sqlite-jdbc-3.51.2.0.jar` from:

```
https://github.com/xerial/sqlite-jdbc/releases/download/3.51.2.0/sqlite-jdbc-3.51.2.0.jar
```
Go to File → Project Structure

Click Libraries

Click +

Select Java

Choose the downloaded file: (sqlite-jdbc-3.51.2.0.jar)


## Default Login Credentials

| Username | Password  |
|----------|-----------|
| admin    | admin123  |
| demo     | demo      |

The SQLite database file (`music_catalog.db`) is created automatically on first launch in the working directory.

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
├── build.xml                          ← Apache Ant build file
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

![Class Diagram](resource%20root/ClassDiagram.png)

![Sequence Diagram](resource%20root/SequenceDiagram.png)

![Sequence Diagram](resource%20root/StateChartDiagram.png)


---
