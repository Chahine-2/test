import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SQLiteUserRepository implements UserRepository {
    private final DatabaseManager databaseManager;

    public SQLiteUserRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public Optional<User> findByLogin(String login) {
        String normalized = normalize(login);
        if (normalized.isBlank()) {
            return Optional.empty();
        }

        String sql = "SELECT username, email, password, role, avatar_path FROM users WHERE lower(username) = ? OR lower(email) = ? LIMIT 1";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalized);
            statement.setString(2, normalized);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                Role role = Role.valueOf(resultSet.getString("role"));
                User user = new User(
                        resultSet.getString("username"),
                        resultSet.getString("email"),
                        resultSet.getString("password"),
                        role,
                        resultSet.getString("avatar_path")
                );
                return Optional.of(user);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to query user by login", ex);
        }
    }

    @Override
    public Optional<User> create(String username, String email, String password, Role role) {
        String cleanedUsername = username == null ? "" : username.trim();
        String cleanedEmail = email == null ? "" : email.trim();
        if (cleanedUsername.isBlank() || cleanedEmail.isBlank() || password == null || password.isBlank() || role == null) {
            return Optional.empty();
        }

        String sql = "INSERT INTO users(username, email, password, role) VALUES(?, ?, ?, ?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cleanedUsername);
            statement.setString(2, cleanedEmail);
            statement.setString(3, password);
            statement.setString(4, role.name());
            statement.executeUpdate();
            return Optional.of(new User(cleanedUsername, cleanedEmail, password, role));
        } catch (SQLException ex) {
            // Constraint errors are expected when username/email already exists.
            if (isUniqueConstraintViolation(ex)) {
                return Optional.empty();
            }
            throw new IllegalStateException("Failed to create user", ex);
        }
    }

    @Override
    public long countAll() {
        String sql = "SELECT COUNT(*) AS total FROM users";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getLong("total") : 0L;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to count users", ex);
        }
    }

    @Override
    public boolean updateAvatarPath(String username, String avatarPath) {
        String cleanedUsername = username == null ? "" : username.trim();
        if (cleanedUsername.isBlank()) {
            return false;
        }

        String sql = "UPDATE users SET avatar_path = ? WHERE username = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, avatarPath);
            statement.setString(2, cleanedUsername);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update avatar path", ex);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private boolean isUniqueConstraintViolation(SQLException ex) {
        String message = ex.getMessage();
        return message != null && message.toLowerCase().contains("unique");
    }
}

