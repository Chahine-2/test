import java.util.List;

public class ArchitectureSmokeTest {
    public static void main(String[] args) {
        DatabaseManager databaseManager = new DatabaseManager("jdbc:sqlite:educore.db");
        databaseManager.initializeSchema();

        AuthService authService = new AuthService();
        System.out.println("Auth admin login: " + authService.authenticate("admin", "admin123").isPresent());

        CoursRepository coursRepository = new SQLiteCoursRepository(databaseManager);

        Cours java = coursRepository.add(new Cours("Java", "Java basics"));
        Cours db = coursRepository.add(new Cours("Databases", "Relational model"));

        java = new Cours(java.getId(), java.getNom(), "Java fundamentals and OOP");
        coursRepository.update(java);

        coursRepository.delete(db.getId());

        List<Cours> all = coursRepository.getAll();
        System.out.println("Cours in DB: " + all.size());
        for (Cours cours : all) {
            System.out.println(cours);
        }
    }
}

