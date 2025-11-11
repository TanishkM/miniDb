package org.example.commands;

import org.example.catalog.Catalog;
import org.example.catalog.TableSchema;
import org.example.storage.TableFile;

import java.util.ArrayList;
import java.util.Map;

public class DeleteCommand implements Command {
    private final Catalog catalog;
    private final String tableName;
    private final String whereColumn;
    private final String whereValue;

    public DeleteCommand(Catalog catalog, String tableName, String whereColumn, String whereValue) {
        this.catalog = catalog;
        this.tableName = tableName;
        this.whereColumn = whereColumn;
        this.whereValue = whereValue;
    }

    @Override
    public void execute() throws Exception {
        TableSchema schema = catalog.getTable(tableName);
        if (schema == null) throw new RuntimeException("No such table: " + tableName);

        TableFile tableFile = new TableFile(tableName);
        int deleted = tableFile.delete(whereColumn, whereValue,
                new ArrayList<>(schema.columns.keySet()));
        tableFile.close();

        System.out.println(deleted + " row(s) deleted.");
    }
}
