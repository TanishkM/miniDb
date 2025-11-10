package org.example.storage;

import java.io.*;

public class PageFile {
    private final RandomAccessFile raf;
    private final int pageSize = Page.PAGE_SIZE;

    public PageFile(File f) throws IOException {
        this.raf = new RandomAccessFile(f, "rw");
    }

    public int numPages() throws IOException {
        return (int) (raf.length() / pageSize);
    }

    public Page readPage(int pageNo) throws IOException {
        Page p = new Page(pageNo);
        raf.seek((long) pageNo * pageSize);
        raf.readFully(p.getData());
        return p;
    }

    public void writePage(Page p) throws IOException {
        raf.seek((long) p.getId() * pageSize);
        raf.write(p.getData());
    }

    public Page appendEmptyPage() throws IOException {
        int newPageId = numPages();
        Page p = new Page(newPageId);
        raf.seek((long) newPageId * pageSize);
        raf.write(p.getData());
        return p;
    }

    public void close() throws IOException {
        raf.close();
    }
}
