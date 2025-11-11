package org.example.storage;

import java.io.Serializable;

public class RecordPointer implements Serializable {
    public final int pageId;
    public final int offset;

    public RecordPointer(int pageId, int offset) {
        this.pageId = pageId;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "(" + pageId + ", " + offset + ")";
    }
}
