#!/bin/bash
# ============================================================
#  Music Catalog — Build & Run Script
#  Requirements: Java 17+ and sqlite-jdbc JAR in ./lib/
# ============================================================

set -e

SQLITE_JAR="lib/sqlite-jdbc-3.45.1.0.jar"
SRC_DIR="src"
OUT_DIR="out"
MAIN_CLASS="com.musiccatalog.Main"

# Check Java
if ! command -v javac &> /dev/null; then
    echo "ERROR: javac not found. Please install JDK 17+."
    exit 1
fi

# Download SQLite JDBC if not present
if [ ! -f "$SQLITE_JAR" ]; then
    echo "Downloading SQLite JDBC driver..."
    mkdir -p lib
    curl -L "https://github.com/xerial/sqlite-jdbc/releases/download/3.45.1.0/sqlite-jdbc-3.45.1.0.jar" \
         -o "$SQLITE_JAR"
    echo "Downloaded."
fi

echo "Compiling..."
mkdir -p "$OUT_DIR"
find "$SRC_DIR" -name "*.java" > sources.txt
javac -cp "$SQLITE_JAR" -d "$OUT_DIR" @sources.txt
rm sources.txt

echo "Compilation successful!"
echo "Launching Music Catalog..."
java -cp "$OUT_DIR:$SQLITE_JAR" "$MAIN_CLASS"
