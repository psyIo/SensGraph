package org.psylo.sensgraph;

//import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by psylo on 17.3.23.
 */

public class SimpleDb {
    final static String TAG = "SimpleDb";
    List ids;
    private int numberOfFields;
    private int currEntryIdx;
    private List entries;
    private Entry currEntry;
    DevTools dt = new DevTools(); //dev


//    public SimpleDb() {
//        this.ids = new ArrayList<>(); //widget id
//        this.entries = new ArrayList<>(); //entries for widget
//        this.numberOfFields = 10;
//    }

    public SimpleDb(int size) {
        this.ids = new ArrayList<>(); //widget id
        this.entries = new ArrayList<>(); //entries for widget
        this.numberOfFields = size; //
    }

    public Boolean getCurrEntry(int id) {
        int idx = ids.indexOf(id);
        if (idx > -1) {
            currEntryIdx = idx;
            currEntry = (Entry) entries.get(idx);
//            dt.logV("getCurrEntry currEntry", currEntry);
            return true;
        } else {
            return false;
        }
    }

    public Boolean createEntry(int id) {
        if (getCurrEntry(id)) {
            return false;
        }
        ids.add(id);
        entries.add(new Entry());
        getCurrEntry(id);
        return true;
    }

    public Boolean deleteEntry(int id) {
        if (getCurrEntry(id)) {
            ids.remove(currEntryIdx);
            entries.remove(currEntryIdx);
            currEntryIdx = -1;
            return true;
        } else {
            return false;
        }
    }

    public Boolean deleteCurrEntry() {
        try {
            ids.remove(currEntryIdx);
            entries.remove(currEntryIdx);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Object getField(int fieldIdx) {
        if (fieldIdx < numberOfFields) {
            return currEntry.fields[fieldIdx];
        } else {
            return null;
        }
    }

    public Boolean setField(int fieldIdx, Object valueToSet) {
        if (fieldIdx < numberOfFields) {
            currEntry.fields[fieldIdx] = valueToSet;
            return true;
        }
        return false;
    }

    public class Entry {
        private Object[] fields;

        public Entry() {
            this.fields = new Object[numberOfFields];
        }
    }
}
