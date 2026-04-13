public class User {
    private final String username;
    private final String email;
    private final String password;
    private final Role role;
    private String avatarPath;

    public User(String username, String password, Role role) {
        this(username, null, password, role, null);
    }

    public User(String username, String email, String password, Role role) {
        this(username, email, password, role, null);
    }

    public User(String username, String email, String password, Role role, String avatarPath) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.avatarPath = avatarPath;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
}

