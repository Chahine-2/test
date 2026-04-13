public class Cours {
    private Integer id;
    private String nom;
    private String description;

    public Cours(Integer id, String nom, String description) {
        this.id = id;
        this.nom = nom;
        this.description = description;
    }

    public Cours(String nom, String description) {
        this(null, nom, description);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Cours{id=" + id + ", nom='" + nom + "', description='" + description + "'}";
    }
}

