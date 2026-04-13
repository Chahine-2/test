import java.util.Optional;

public interface UserRepository {
    Optional<User> findByLogin(String login);

    Optional<User> create(String username, String email, String password, Role role);

    boolean updateAvatarPath(String username, String avatarPath);

    long countAll();
}

