package org.example.storage;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        for (int pid = 0; pid < pf.numPages(); pid++) {
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
        for (int pid = 0; pid < pf.numPages(); pid++) {
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

    public int update(String whereCol, String whereVal,
                      Map<String, String> updates,
                      List<String> schemaCols,
                      Map<String, String> colTypes) throws IOException {
        int updated = 0;
        int whereIdx = -2;
        boolean updateAllRows = false;
        if (!Objects.equals(whereCol, "")) {
            whereIdx = schemaCols.indexOf(whereCol);
        } else {
            updateAllRows = true;
        }
        if (whereIdx == -1) throw new RuntimeException("No such column: " + whereCol);

        for (int pid = 0; pid < pf.numPages(); pid++) {
            Page p = pf.readPage(pid);
            ByteBuffer bb = ByteBuffer.wrap(p.getData());
            int used = bb.getInt(0);
            if (used < 8) continue;

            int pos = 4;

            while (pos < used) {
                int recLen = bb.getInt(pos);
                int recStart = pos;
                pos += 4;
                byte[] rec = new byte[recLen];
                bb.position(pos);
                bb.get(rec);
                pos += recLen;
                Object[] fields = Record.deserialize(rec);
                Object val = whereIdx > 0 ? fields[whereIdx] : null;
                if (updateAllRows || Objects.equals(val.toString(), whereVal)) {

                    for (Map.Entry<String, String> e : updates.entrySet()) {
                        int idx = schemaCols.indexOf(e.getKey());
                        if (idx == -1) throw new RuntimeException("No such column: " + e.getKey());
                        String type = colTypes.get(e.getKey());
                        String newVal = e.getValue();
                        if (type.equalsIgnoreCase("int")) {
                            fields[idx] = Integer.parseInt(newVal);
                        } else {
                            fields[idx] = newVal;
                        }
                    }
                    byte[] newRec = Record.serialize(fields);
                    if (newRec.length <= recLen) {
                        bb.position(pos - recLen);
                        bb.put(newRec);
                        if (newRec.length < recLen) {
                            bb.put(new byte[recLen - newRec.length]);
                        }
                        pf.writePage(p);
                    } else {
                        removeRecord(p, recStart, recLen + 4); // 4 bytes for length prefix
                        pf.writePage(p);
                        insert(fields);
                    }
                    updated++;
                }
            }
        }

        return updated;
    }

    public int delete(String whereCol, String whereVal,
                      List<String> schemaCols) throws IOException {
        int deleted = 0;
        int whereIdx = -2;
        boolean deleteAllRows = false;
        if(!Objects.equals(whereCol,"")){
            whereIdx = schemaCols.indexOf(whereCol);
        }
        else{
            deleteAllRows = true;
        }
        if (whereIdx == -1) throw new RuntimeException("No such column: " + whereCol);

        for (int pid = 0; pid < pf.numPages(); pid++) {
            Page p = pf.readPage(pid);
            ByteBuffer bb = ByteBuffer.wrap(p.getData());
            int used = bb.getInt(0);
            if (used < 8) continue;

            int pos = 4;
            while (pos < used) {
                int recStart = pos;
                int recLen = bb.getInt(pos);
                pos += 4;
                byte[] rec = new byte[recLen];
                bb.position(pos);
                bb.get(rec);
                pos += recLen;

                Object[] fields = Record.deserialize(rec);
                Object val = deleteAllRows ? null : fields[whereIdx] ;

                if (deleteAllRows || Objects.equals(val.toString(), whereVal)) {
                    removeRecord(p, recStart, recLen + 4);
                    used = bb.getInt(0);
                    pos = 4;
                    deleted++;
                }
            }
            pf.writePage(p);
        }

        return deleted;
    }

    private void removeRecord(Page p, int startOffset, int totalLen) {
        ByteBuffer bb = ByteBuffer.wrap(p.getData());
        int used = bb.getInt(0);

        byte[] data = p.getData();

        int srcPos = startOffset + totalLen;
        int destPos = startOffset;
        int remaining = used - srcPos;

        // Shift data left
        System.arraycopy(data, srcPos, data, destPos, remaining);

        // Zero out the old tail
        for (int i = used - totalLen; i < used; i++) {
            data[i] = 0;
        }

        // Update "used" marker
        int newUsed = used - totalLen;
        bb.putInt(0, newUsed);
    }

    public void close() throws IOException {
        pf.close();
    }
}
