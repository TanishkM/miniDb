package org.example.commands;

import org.example.catalog.Catalog;
import org.example.catalog.TableSchema;
import org.example.storage.Index;
import org.example.storage.Page;
import org.example.storage.Record;
import org.example.storage.TableFile;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class CreateIndexCommand implements Command {
    private final Catalog catalog;
    private final String tableName;
    private final String columnName;

    public CreateIndexCommand(Catalog c, String table, String col) {
        this.catalog = c;
        this.tableName = table;
        this.columnName = col;
    }

    @Override
    public void execute() throws Exception {
        TableSchema s = catalog.getTable(tableName);
        if (s == null) throw new RuntimeException("No such table");

        int colIdx = new ArrayList<>(s.columns.keySet()).indexOf(columnName);
        if (colIdx == -1) throw new RuntimeException("No such column");

        TableFile tf = new TableFile(tableName);
        Index idx = new Index(tableName, columnName);

        // Build index from existing data
        for (int pid = 0; pid < tf.pf.numPages(); pid++) {
            Page p = tf.pf.readPage(pid);
            ByteBuffer bb = ByteBuffer.wrap(p.getData());
            int used = bb.getInt(0);
            if (used < 8) continue;

            int pos = 4;
            while (pos + 4 <= used) { // ensure we can read the length prefix
                int recStart = pos;       // position of the length prefix
                bb.position(pos);         // set ByteBuffer position to recStart
                int len = bb.getInt();    // read record length (advances pos by 4)

                // Validate length to avoid reading past 'used'
                if (len <= 0 || recStart + 4 + len > used) {
                    System.err.println("Warning: truncated/corrupt record on page " + pid + " offset " + recStart + " (len=" + len + ", used=" + used + ")");
                    break; // give up on this page
                }

                byte[] rec = new byte[len];
                bb.get(rec);             // read the record bytes (advances pos by len)
                pos = bb.position();     // update pos to new buffer position

                try {
                    Object[] fields = Record.deserialize(rec);
                    if (colIdx >= 0 && colIdx < fields.length) {
                        String key = String.valueOf(fields[colIdx]);
                        idx.add(key, pid, recStart);
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Skipped invalid record on page " + pid + " offset " + recStart + " — " + e.getMessage());
                    // continue scanning page
                }
            }
        }

        idx.save();
        System.out.println("Index created on " + tableName + "(" + columnName + ")");
        tf.close();
    }
}
