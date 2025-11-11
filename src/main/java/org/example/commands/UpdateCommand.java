package org.example.commands;

import org.example.catalog.Catalog;
import org.example.catalog.TableSchema;
import org.example.storage.TableFile;

import java.util.ArrayList;
import java.util.Map;

public class UpdateCommand implements Command {
    private final Catalog catalog;
    private final String tableName;
    private final Map<String, String> updates;
    private final String whereColumn;
    private final String whereValue;

    public UpdateCommand(Catalog catalog, String tableName, Map<String, String> updates, String whereColumn, String whereValue) {
        this.catalog = catalog;
        this.tableName = tableName;
        this.updates = updates;
        this.whereColumn = whereColumn;
        this.whereValue = whereValue;
    }


    @Override
    public void execute() throws Exception {
        TableSchema schema = catalog.getTable(tableName);
        if (schema == null) throw new RuntimeException("No such table: " + tableName);
        TableFile tableFile = new TableFile(tableName);
        int updated = tableFile.update(whereColumn, whereValue, updates, new ArrayList<>(schema.columns.keySet()), schema.columns);
        tableFile.close();
        System.out.println(updated + " row(s) updated.");

    }
}
