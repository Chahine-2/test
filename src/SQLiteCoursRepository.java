import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SQLiteCoursRepository implements CoursRepository {
    private final DatabaseManager databaseManager;

    public SQLiteCoursRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public Cours add(Cours cours) {
        String sql = "INSERT INTO cours(nom, description) VALUES(?, ?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, cours.getNom());
            statement.setString(2, cours.getDescription());
            statement.executeUpdate();

            try (ResultSet generated = statement.getGeneratedKeys()) {
                if (generated.next()) {
                    cours.setId(generated.getInt(1));
                }
            }
            return cours;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to add cours", ex);
        }
    }

    @Override
    public boolean update(Cours cours) {
        if (cours.getId() == null) {
            return false;
        }

        String sql = "UPDATE cours SET nom = ?, description = ? WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cours.getNom());
            statement.setString(2, cours.getDescription());
            statement.setInt(3, cours.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update cours", ex);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM cours WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete cours", ex);
        }
    }

    @Override
    public List<Cours> getAll() {
        List<Cours> coursList = new ArrayList<>();
        String sql = "SELECT id, nom, description FROM cours ORDER BY id";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                coursList.add(new Cours(
                        resultSet.getInt("id"),
                        resultSet.getString("nom"),
                        resultSet.getString("description")
                ));
            }
            return coursList;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to list cours", ex);
        }
    }
}

