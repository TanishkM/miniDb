package org.example.storage;

import java.nio.*;
import java.util.*;

public class Record {
    public static byte[] serialize(Object[] fields) {
        List<byte[]> parts = new ArrayList<>();
        int total = 4; // num fields
        for (Object f : fields) {
            if (f instanceof Integer) {
                ByteBuffer bb = ByteBuffer.allocate(5);
                bb.put((byte)1); // type int
                bb.putInt((Integer)f);
                parts.add(bb.array());
                total += 5;
            } else if (f instanceof String) {
                byte[] b = ((String)f).getBytes();
                ByteBuffer bb = ByteBuffer.allocate(1+4+b.length);
                bb.put((byte)2).putInt(b.length).put(b);
                parts.add(bb.array());
                total += 1+4+b.length;
            }
        }
        ByteBuffer out = ByteBuffer.allocate(total);
        out.putInt(fields.length);
        for (byte[] p : parts) out.put(p);
        return out.array();
    }

    public static Object[] deserialize(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        int n = bb.getInt();
        Object[] vals = new Object[n];
        for (int i=0;i<n;i++) {
            byte type = bb.get();
            if (type==1) vals[i]=bb.getInt();
            else if (type==2) {
                int len=bb.getInt();
                byte[] b=new byte[len];
                bb.get(b);
                vals[i]=new String(b);
            }
        }
        return vals;
    }
}
