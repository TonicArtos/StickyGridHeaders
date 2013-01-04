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

    /**
     * An array of sample (dummy) items.
     */
    public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();
    public static List<ArrayStickyGridHeaderData> HEADERS = new ArrayList<ArrayStickyGridHeaderData>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();
    public static Map<String, ArrayStickyGridHeaderData> HEADER_MAP = new HashMap<String, ArrayStickyGridHeaderData>();

    static {
        // Add sample items.
        addHeader("1", new ArrayStickyGridHeaderData("Section 1", 3));
        addItem(new DummyItem("1", "Item 1"));
        addItem(new DummyItem("2", "Item 2"));
        addItem(new DummyItem("3", "Item 3"));
        addHeader("2", new ArrayStickyGridHeaderData("Section 2", 4));
        addItem(new DummyItem("4", "Item 4"));
        addItem(new DummyItem("5", "Item 5"));
        addItem(new DummyItem("6", "Item 6"));
        addItem(new DummyItem("7", "Item 7"));
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static void addHeader(String id, ArrayStickyGridHeaderData item) {
        HEADERS.add(item);
        HEADER_MAP.put(id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public String id;
        public String content;

        public DummyItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }

    /**
     * A dummy item representing a section header.
     */
    public static class DummyHItem {
        public String id;
        public String content;
        public int count;

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
}
