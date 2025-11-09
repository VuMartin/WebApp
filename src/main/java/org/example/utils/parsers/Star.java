package main.java.org.example.utils.parsers;

public class Star {
    private String id;        // Generated star ID (nm...)
    private String name;      // From <stagename>
    private Integer birthYear; // From <dob> (can be null)

    public Star() {}

    public Star(String name, Integer birthYear) {
        this.name = name;
        this.birthYear = birthYear;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getBirthYear() { return birthYear; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }

    @Override
    public String toString() {
        return "Star [id=" + id + ", name=" + name +
                ", birthYear=" + birthYear + "]";
    }
}