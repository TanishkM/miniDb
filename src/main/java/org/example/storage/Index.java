package org.example.storage;

import java.io.*;
import java.util.*;

public class Index implements Serializable {
    private final String tableName;
    private final String columnName;
    private final Map<String, List<RecordPointer>> map = new HashMap<>();

    public Index(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public void add(String key, int pageId, int offset) {
        map.computeIfAbsent(key, k -> new ArrayList<>())
                .add(new RecordPointer(pageId, offset));
    }

    public List<RecordPointer> find(String key) {
        return map.getOrDefault(key, Collections.emptyList());
    }

    public void remove(String key, int pageId, int offset) {
        List<RecordPointer> lst = map.get(key);
        if (lst != null) {
            lst.removeIf(rp -> rp.pageId == pageId && rp.offset == offset);
            if (lst.isEmpty()) map.remove(key);
        }
    }

    public void save() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(indexFile()))) {
            oos.writeObject(this);
        }
    }

    public static Index load(String table, String column) throws IOException, ClassNotFoundException {
        File f = new File(indexFilePath(table, column));
        if (!f.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (Index) ois.readObject();
        }
    }

    private static String indexFilePath(String table, String column) {
        return "index_" + table + "_" + column + ".idx";
    }

    private File indexFile() {
        return new File(indexFilePath(tableName, columnName));
    }
}
