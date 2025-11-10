package org.example.catalog;

import java.io.*;
import java.util.*;
import com.google.gson.*;

public class Catalog {
    private final File file;
    private final Map<String, TableSchema> tables = new HashMap<>();

    public Catalog(String path) throws IOException {
        this.file = new File(path);
        if (file.exists()) {

            // initiales the catalog (table metadata) from by creating table schemas from json file
            try (Reader r = new FileReader(file)) {
                JsonObject obj = JsonParser.parseReader(r).getAsJsonObject();
                for (String k : obj.keySet()) {
                    JsonObject t = obj.getAsJsonObject(k);
                    TableSchema schema = new TableSchema();
                    schema.name = k;
                    schema.columns = new LinkedHashMap<>();
                    for (Map.Entry<String, JsonElement> e : t.getAsJsonObject("columns").entrySet()) {
                        schema.columns.put(e.getKey(), e.getValue().getAsString());
                    }
                    tables.put(k, schema);
                }
            }
        }
    }

    public void save() throws IOException {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, TableSchema> e  : tables.entrySet()) {
            JsonObject t = new JsonObject();
            JsonObject cols = new JsonObject();
            e.getValue().columns.forEach(cols::addProperty);
            t.add("columns", cols);
            root.add(e.getKey(), t);
        }
        try (Writer w = new FileWriter(file)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(root, w);
        }
    }

    public void addTable(TableSchema schema) {
        tables.put(schema.name, schema);
    }

    public TableSchema getTable(String name) {
        return tables.get(name);
    }

    public boolean hasTable(String name) {
        return tables.containsKey(name);
    }
}
