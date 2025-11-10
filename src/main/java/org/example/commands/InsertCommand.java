package org.example.commands;



import org.example.catalog.Catalog;
import org.example.catalog.TableSchema;
import org.example.storage.TableFile;

import java.util.ArrayList;
import java.util.List;

public class InsertCommand implements Command {
    private final Catalog catalog;
    private final String table;
    private final List<String> values;

    public InsertCommand(Catalog c, String t, List<String> vals) {
        this.catalog = c;
        this.table = t;
        this.values = vals;
    }

    @Override
    public void execute() throws Exception {
        TableSchema s = catalog.getTable(table);
        if (s == null) throw new RuntimeException("No such table");

        List<String> schemaCols = new ArrayList<>(s.columns.keySet());
        if (values.size() != schemaCols.size()) {
            throw new RuntimeException("Expected " + schemaCols.size() + " values, got " + values.size());
        }

        Object[] row = new Object[schemaCols.size()];
        for (int i = 0; i < schemaCols.size(); i++) {
            String type = s.columns.get(schemaCols.get(i));
            String v = values.get(i);
            if (type.equalsIgnoreCase("INT")) row[i] = Integer.parseInt(v);
            else row[i] = v;
        }

        TableFile tf = new TableFile(table);
        tf.insert(row);
        tf.close();
        System.out.println("1 row inserted.");
    }

}
