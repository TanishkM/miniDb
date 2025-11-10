package org.example.storage;

public class Page {
    public static final int PAGE_SIZE = 4096;
    private final byte[] data = new byte[PAGE_SIZE];
    private final int pageId;

    public Page(int id) { this.pageId = id; }
    public int getId() { return pageId; }
    public byte[] getData() { return data; }
}
