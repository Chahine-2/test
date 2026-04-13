import java.util.Optional;

public class AuthUseCase {
    private final UserRepository userRepository;

    public AuthUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void seedDefaultsIfEmpty() {
        if (userRepository.countAll() > 0) {
            return;
        }

        userRepository.create("admin", "admin@educore.com", "admin123", Role.ADMIN);
        userRepository.create("teacher", "teacher@educore.com", "teacher123", Role.TEACHER);
        userRepository.create("student", "student@educore.com", "student123", Role.STUDENT);
    }

    public Optional<User> authenticate(String login, String password) {
        return userRepository.findByLogin(login)
                .filter(user -> user.getPassword().equals(password));
    }

    public Optional<User> register(String username, String email, String password, Role role) {
        return userRepository.create(username, email, password, role);
    }

    public boolean updateAvatarPath(String username, String avatarPath) {
        return userRepository.updateAvatarPath(username, avatarPath);
    }
}

