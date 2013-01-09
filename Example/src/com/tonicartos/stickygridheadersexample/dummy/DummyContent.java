package com.tonicartos.stickygridheadersexample.dummy;

import com.tonicartos.widget.stickygridheaders.ArrayStickyGridHeaderData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {
    public static Map<String, ArrayStickyGridHeaderData> HEADER_MAP = new HashMap<String, ArrayStickyGridHeaderData>();
    public static List<ArrayStickyGridHeaderData> HEADERS = new ArrayList<ArrayStickyGridHeaderData>();

    public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();
    public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    static {
        // Add sample items.
        addHeader("1", new ArrayStickyGridHeaderData("Section 1", 3));
        addItem(new DummyItem("1", "Item 1 - la la la la la la la la la la la la la la la la la la la la la la la la"));
        addItem(new DummyItem("2", "Item 2"));
        addItem(new DummyItem("3", "Item 3"));
        addHeader("2", new ArrayStickyGridHeaderData("Section 2 - A long header view with much to say indeed! la la la la la la la la la la la la la la la la la la la la la la la la", 4));
        addItem(new DummyItem("4", "Item 4"));
        addItem(new DummyItem("5", "Item 5"));
        addItem(new DummyItem("6", "Item 6"));
        addItem(new DummyItem("7", "Item 7"));
        addHeader("2", new ArrayStickyGridHeaderData("Section 3", 8));
        addItem(new DummyItem("8", "Item 8"));
        addItem(new DummyItem("9", "Item 9"));
        addItem(new DummyItem("10", "Item 10"));
        addItem(new DummyItem("11", "Item 11"));
        addItem(new DummyItem("12", "Item 12 - la la la la la la la la la la la la la la la la la la la la la la la la"));
        addItem(new DummyItem("13", "Item 13"));
        addItem(new DummyItem("14", "Item 14 - la la la la"));
        addItem(new DummyItem("15", "Item 15"));
        addHeader("2", new ArrayStickyGridHeaderData("Section 4", 2));
        addItem(new DummyItem("16", "Item 16"));
        addItem(new DummyItem("17", "Item 17"));
        addHeader("2", new ArrayStickyGridHeaderData("Section 5 - A long header view with much to say indeed!!! la la la la", 5));
        addItem(new DummyItem("18", "Item 18"));
        addItem(new DummyItem("19", "Item 19"));
        addItem(new DummyItem("20", "Item 20"));
        addItem(new DummyItem("21", "Item 21"));
        addItem(new DummyItem("22", "Item 22"));
        addHeader("2", new ArrayStickyGridHeaderData("Section 6", 6));
        addItem(new DummyItem("23", "Item 23"));
        addItem(new DummyItem("24", "Item 24"));
        addItem(new DummyItem("25", "Item 25"));
        addItem(new DummyItem("26", "Item 26"));
        addItem(new DummyItem("27", "Item 27"));
        addItem(new DummyItem("28", "Item 28"));
        addHeader("2", new ArrayStickyGridHeaderData("Section 7", 4));
        addItem(new DummyItem("29", "Item 29"));
        addItem(new DummyItem("30", "Item 30"));
        addItem(new DummyItem("31", "Item 31"));
        addItem(new DummyItem("32", "Item 32"));
    }

    private static void addHeader(String id, ArrayStickyGridHeaderData item) {
        HEADERS.add(item);
        HEADER_MAP.put(id, item);
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a section header.
     */
    public static class DummyHItem {
        public String content;
        public int count;
        public String id;

        public DummyHItem(String id, String content, int count) {
            this.id = id;
            this.content = content;
            this.count = count;
        }

        @Override
        public String toString() {
            return content;
        }
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public String content;
        public String id;

        public DummyItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
