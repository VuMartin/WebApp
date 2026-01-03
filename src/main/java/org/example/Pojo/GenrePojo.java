package main.java.org.example.Pojo;

import com.google.gson.JsonObject;

public class GenrePojo {

    private String id;
    private String name;

    private GenrePojo() {}

    public static GenrePojo builder() {
        return new GenrePojo();
    }

    public GenrePojo setId(String id) {
        this.id = id;
        return this;
    }

    public GenrePojo setName(String name) {
        this.name = name;
        return this;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("name", name);
        return obj;
    }
}