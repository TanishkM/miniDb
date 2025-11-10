package org.example.commands;


import org.example.catalog.Catalog;
import org.example.catalog.TableSchema;
import org.example.storage.TableFile;

public class CreateTableCommand implements Command {
    private final Catalog catalog;
    private final TableSchema schema;

    public CreateTableCommand(Catalog cat, TableSchema s) {
        this.catalog = cat;
        this.schema = s;
    }

    @Override
    public void execute() throws Exception {
        if (catalog.hasTable(schema.name)) {
            System.out.println("Table already exists.");
            return;
        }
        catalog.addTable(schema);
        catalog.save(); // <-- persist schema metadata immediately
        new TableFile(schema.name); // create data file
        System.out.println("Table created: " + schema.name);
    }
}
