package com.tonicartos.widget.stickygridheaders;

public class ArrayStickyGridHeaderData {
    protected int count;
    protected String header;

    public ArrayStickyGridHeaderData(String header, int count) {
        this.header = header;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public String getHeader() {
        return header;
    }
}