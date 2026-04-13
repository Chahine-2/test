import java.util.List;

public interface CoursRepository {
    Cours add(Cours cours);

    boolean update(Cours cours);

    boolean delete(int id);

    List<Cours> getAll();
}

