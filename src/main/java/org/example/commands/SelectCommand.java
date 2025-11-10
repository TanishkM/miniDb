package org.example.commands;


import org.example.catalog.Catalog;
import org.example.catalog.TableSchema;
import org.example.storage.TableFile;

import java.util.List;

public class SelectCommand implements Command {
    private final Catalog catalog;
    private final String table;

    public SelectCommand(Catalog c, String t) {
        this.catalog = c;
        this.table = t;
    }

    @Override
    public void execute() throws Exception {
        TableSchema s = catalog.getTable(table);
        if (s == null) throw new RuntimeException("No such table");
        TableFile tf = new TableFile(table);
        List<Object[]> rows = tf.readAll();
        tf.close();
        System.out.println(String.join(" | ", s.columns.keySet()));
        for (Object[] r : rows) {
            for (int i = 0; i < r.length; i++) {
                System.out.print(r[i]);
                if (i < r.length - 1) System.out.print(" | ");
            }
            System.out.println();
        }
        System.out.println(rows.size() + " row(s).");
    }
}
