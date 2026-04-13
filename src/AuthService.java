import java.util.Optional;

public class AuthService {
    private final AuthUseCase authUseCase;

    public AuthService() {
        DatabaseManager databaseManager = new DatabaseManager("jdbc:sqlite:educore.db");
        databaseManager.initializeSchema();

        UserRepository userRepository = new SQLiteUserRepository(databaseManager);
        authUseCase = new AuthUseCase(userRepository);
        authUseCase.seedDefaultsIfEmpty();
    }

    public Optional<User> authenticate(String username, String password) {
        return authUseCase.authenticate(username, password);
    }

    public synchronized Optional<User> registerUser(String username, String email, String password, Role role) {
        return authUseCase.register(username, email, password, role);
    }

    public synchronized boolean updateAvatarPath(String username, String avatarPath) {
        return authUseCase.updateAvatarPath(username, avatarPath);
    }
}

