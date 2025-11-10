package org.example.storage;

import java.io.*;
import java.nio.*;
import java.util.*;

public class TableFile {
    private final File file;
    private final PageFile pf;

    public TableFile(String name) throws IOException {
        this.file = new File("table_" + name + ".dat");
        this.pf = new PageFile(file);
        if (pf.numPages() == 0) pf.appendEmptyPage();
    }

    public void insert(Object[] fields) throws IOException {
        byte[] rec = Record.serialize(fields);
        // append simple: find page with space, else new page
        for (int pid=0; pid<pf.numPages(); pid++) {
            Page p = pf.readPage(pid);
            ByteBuffer bb = ByteBuffer.wrap(p.getData());
            int used = bb.getInt(0);
            if (used == 0) used = 4;
            if (used + rec.length + 4 <= Page.PAGE_SIZE) {
                // write record
                bb.position(used);
                bb.putInt(rec.length);
                bb.put(rec);
                used = bb.position();
                bb.putInt(0, used);
                pf.writePage(p);
                return;
            }
        }
        // no page has space, create new
        Page np = pf.appendEmptyPage();
        ByteBuffer bb = ByteBuffer.wrap(np.getData());
        bb.position(4);
        bb.putInt(rec.length);
        bb.put(rec);
        bb.putInt(0, bb.position());
        pf.writePage(np);
    }

    public List<Object[]> readAll() throws IOException {
        List<Object[]> out = new ArrayList<>();
        for (int pid=0; pid<pf.numPages(); pid++) {
            Page p = pf.readPage(pid);
            ByteBuffer bb = ByteBuffer.wrap(p.getData());
            int used = bb.getInt(0);
            if (used < 8) continue;
            bb.position(4);
            while (bb.position() < used) {
                int len = bb.getInt();
                byte[] rec = new byte[len];
                bb.get(rec);
                out.add(Record.deserialize(rec));
            }
        }
        return out;
    }

    public void close() throws IOException { pf.close(); }
}
