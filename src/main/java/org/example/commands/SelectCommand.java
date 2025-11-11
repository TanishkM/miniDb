package org.example.commands;


import org.example.catalog.Catalog;
import org.example.catalog.TableSchema;
import org.example.storage.TableFile;

import java.util.ArrayList;
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

        List<String> columns = new ArrayList<>(s.columns.keySet());
        int colCount = columns.size();

        // Step 1: Calculate max width per column
        int[] widths = new int[colCount];
        for (int i = 0; i < colCount; i++) {
            widths[i] = columns.get(i).length(); // header width
        }

        for (Object[] row : rows) {
            for (int i = 0; i < colCount; i++) {
                widths[i] = Math.max(widths[i], String.valueOf(row[i]).length());
            }
        }

        // Step 2: Build table borders
        StringBuilder border = new StringBuilder("+");
        for (int w : widths) border.append("-".repeat(w + 2)).append("+");
        String borderLine = border.toString();

        // Step 3: Print header
        System.out.println(borderLine);
        System.out.print("|");
        for (int i = 0; i < colCount; i++) {
            System.out.printf(" %-"+widths[i]+"s |", columns.get(i));
        }
        System.out.println();
        System.out.println(borderLine);

        // Step 4: Print rows
        for (Object[] row : rows) {
            System.out.print("|");
            for (int i = 0; i < colCount; i++) {
                System.out.printf(" %-"+widths[i]+"s |", row[i]);
            }
            System.out.println();
        }

        // Step 5: Footer
        System.out.println(borderLine);
        System.out.println(rows.size() + " row(s).");
    }

}
