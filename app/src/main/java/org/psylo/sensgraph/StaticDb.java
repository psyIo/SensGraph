package org.psylo.sensgraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by psylo on 17.7.22.
 * StaticDb to use everywhere anytime
 */

public class StaticDb {
    final static private String TAG = "StaticDb";
    static List<Object> ids = new ArrayList<>(); //widget id;
    private static int numberOfFields = 10;
    private static int currEntryIdx;
    private static List<Object> entries = new ArrayList<>(); //entries for widget;
    static DbEntry currEntry;
    private static DbEntry emptyEntry;

    public StaticDb() {
        emptyEntry = new DbEntry();
    }

    static Boolean getCurrEntry(int id) {
        DevTools.logE(TAG, "getCurrEntry called with par id ", id, "List ids", ids);
        int idx = ids.indexOf(id);
        if (idx > -1) {
            currEntryIdx = idx;
            currEntry = (DbEntry) entries.get(idx);
            DevTools.logE(TAG, "currEntry", currEntry, "ids", ids, "entries", entries);
            return true;
        } else {
            return false;
        }
    }

    static Boolean createEntry(int id, DbEntry entry) {
        if (getCurrEntry(id)) {
            return false;
        }
        ids.add(id);
//        entries.add(new Entry());
        entries.add(entry);
        DevTools.logE(TAG, "entries", entries, "entry", entry);
        getCurrEntry(id);
        return true;
    }

    static Boolean deleteCurrEntry() {
        try {
            ids.remove(currEntryIdx);
            entries.remove(currEntryIdx);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    static Object getField(int fieldIdx) {
        DevTools.logE(TAG, "getField currEntry", currEntry, "currEntry.getField1(0)");
        if (fieldIdx < numberOfFields) {
            return currEntry.fields[fieldIdx];
        } else {
            return null;
        }
    }

    static Boolean setField(int fieldIdx, Object valueToSet) {
//        DevTools.logE(TAG, "setField", fieldIdx, valueToSet, numberOfFields, currEntry);
        if (fieldIdx < numberOfFields) {
            currEntry.fields[fieldIdx] = valueToSet;
            DevTools.logE(TAG, "setField currEntry.fields", currEntry.fields, "currEntry.fields[fieldIdx]", currEntry.fields[fieldIdx]);
            return true;
        }
        return false;
    }


//    public class Entry {
//        private Object[] fields;
//
//        Entry() {
//            this.fields = new Object[numberOfFields];
//        }
//    }


}
