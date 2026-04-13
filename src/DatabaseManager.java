import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private final String jdbcUrl;

    public DatabaseManager(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }

    public void initializeSchema() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        email TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL,
                        avatar_path TEXT,
                        role TEXT NOT NULL,
                        created_at TEXT DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            // Backward-compatible migration for existing databases.
            try {
                statement.executeUpdate("ALTER TABLE users ADD COLUMN avatar_path TEXT");
            } catch (SQLException ignored) {
                // Column already exists in migrated databases.
            }

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS cours (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nom TEXT NOT NULL,
                        description TEXT NOT NULL
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS evaluations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        student_username TEXT NOT NULL,
                        cours_id INTEGER NOT NULL,
                        note TEXT,
                        commentaire TEXT,
                        created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(cours_id) REFERENCES cours(id)
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS presence (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        student_username TEXT NOT NULL,
                        cours_id INTEGER NOT NULL,
                        date_presence TEXT NOT NULL,
                        statut TEXT NOT NULL,
                        FOREIGN KEY(cours_id) REFERENCES cours(id)
                    )
                    """);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize SQLite schema", ex);
        }
    }
}

