package com.tonicartos.widgets.stickygridheaders;

public class SimpleStickyGridHeaderData {
    protected int count;
    protected String header;

    public SimpleStickyGridHeaderData(String header, int count) {
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