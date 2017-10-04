package org.psylo.sensgraph;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by bdance on 9/21/17.
 */

public class FileDb {

    private static final String TAG = "FileDb"; //dev
    private static final String DB_FILE_NAME = "sensgraph.data";
    private static final String VALUE_SEP = ";"; //separator used separate db entry values
    private static final int DB_ENTRY_SIZE = 7; //max elements per entry (with ID)
    private static File dbFile;
    private static RandomAccessFile dbRandomAccessFile;

    public static String[] getEntry(Context context, int entryId) {

        if (!dbFileExist(context)) {
            return null;
        }

        long[] linesPositions = getLinePositions(entryId);
        try {
            if ((linesPositions[0] != 0) && (linesPositions[1] != 0)) {
                dbRandomAccessFile.seek(linesPositions[0]);
                String readString = dbRandomAccessFile.readLine();
                return readString.split((VALUE_SEP));
            }
        }catch (IOException e) {
            DevTools.logE(TAG, e.toString());
        }

        return null;
    }

    @NonNull
    public static boolean saveEntry(Context context, String... values) {

        if (!dbFileExist(context)) {
            return false;
        }

        DevTools.log(TAG, "saveEntry called entryId", "values", values);

        if (values.length != DB_ENTRY_SIZE) {
            DevTools.logE(TAG, "saveEntry method failed because entry has different element number" +
                    " than db accepts, DB_ENTRY_SIZE=", DB_ENTRY_SIZE, "exLineLen of entry=", values.length);
            return false;
        }

        String newLine = makeValueStringFromArray(values);
        return replaceExistingFileString(getLinePositions(Integer.parseInt(values[0])), newLine);
    }

    public static boolean deleteEntry(Context context, int entryId) {
        if (!dbFileExist(context)) {
            return false;
        }
        return replaceExistingFileString(getLinePositions(entryId), "");
    }

    public static boolean replaceExistingFileString(long[] positions, String newString) {
        try {
            if ((positions[0] != 0) && (positions[1] != 0)) {
                long exLineEnd = positions[1];
                long exLineLen = exLineEnd - positions[0];
                long lenDiff = newString.length() - exLineLen;

                //read everything till the end to buffer
                dbRandomAccessFile.seek(positions[1]);
                byte[] buff = new byte[(int) (dbRandomAccessFile.length() - exLineEnd)];
                int read = dbRandomAccessFile.read(buff);
                if (read > -1) {
                    dbRandomAccessFile.seek(exLineEnd - exLineLen);//goes back to exLine start
                }
                dbRandomAccessFile.setLength(dbRandomAccessFile.length() + lenDiff);
                dbRandomAccessFile.writeBytes(newString); //new line
                dbRandomAccessFile.write(buff, 0, buff.length); //rest of file from buffer
            } else {
                dbRandomAccessFile.writeBytes(newString);
            }
            return true;
        } catch (IOException e) {
            DevTools.logE(TAG, e.toString());
        }
        return false;
    }

    /**
     *
     * @param entryId entry to get
     * @return  returns long[2] with lines start and end positions
     *      returns [0,0] if entry was not found and leaves pointer at the end of file
     */
    public static long[] getLinePositions(int entryId) {
        String readString;
        String[] strArr;
        long prevLineEnd = 0;
        long[] positions;

        try {
            dbRandomAccessFile.seek(0);
            while ((readString = dbRandomAccessFile.readLine()) != null) {
                strArr = readString.split((VALUE_SEP));
                if (Integer.parseInt(strArr[0]) == entryId) { //entry found
                    positions = new long[2];
                    positions[0] = prevLineEnd;
                    positions[1] = dbRandomAccessFile.getFilePointer();
                    return positions;
                }
                prevLineEnd = dbRandomAccessFile.getFilePointer();
            }
        } catch (IOException e) {
            DevTools.logE(TAG, e.toString());
        }

        positions = new long[2];
        positions[0] = 0;
        positions[1] = 0;
        return positions;

    }

    public static String makeValueStringFromArray(String... strArr) {
        StringBuilder sb = new StringBuilder();

        if ((strArr == null) || (strArr.length == 0)) {
            return "";
        }

        if (strArr.length > 1) {
            for (int i = 0; i < strArr.length - 1; i++) {
                sb.append(String.valueOf(strArr[i]));
                sb.append(VALUE_SEP);
            }
        }
        sb.append(String.valueOf(strArr[strArr.length - 1]));
        sb.append("\n");

        return sb.toString();
    }

    /**
     *
     * @param context android context
     * @return boolean if db file exist (or was successfully created)
     */
    public static boolean dbFileExist(Context context) {

        try {
            dbFile = new File(context.getFilesDir(), DB_FILE_NAME);
            if (dbFile.exists()) {
                if (dbRandomAccessFile == null) {
                    dbRandomAccessFile = new RandomAccessFile(dbFile, "rw");
                }
            } else {
                try {
                    dbRandomAccessFile = new RandomAccessFile(dbFile, "rw");
                    dbRandomAccessFile.writeBytes("");
                } catch (IOException ex) {
                    DevTools.logE(TAG, ex.toString());
                    return false;
                }
            }
        } catch (IOException e) {
            DevTools.logE(TAG, e.toString());
            return false;
        }
        return dbFile.exists();
    }

    public static String readDbFile(Context context) {

        if (!dbFileExist(context)) {
            return "Error!!Db file does not exist!!";
        }

        StringBuilder stringBuilder = new StringBuilder();
        String readString;
        try {
            dbRandomAccessFile.seek(0);
            while ((readString = dbRandomAccessFile.readLine()) != null) {
                stringBuilder.append(readString);
                stringBuilder.append("\n");
            }
        } catch (IOException e) {
            DevTools.logE(TAG, e.toString());
        }

        String resultString = stringBuilder.toString();
        if (resultString.length() == 0) {
            return "";
        } else {
            return resultString.substring(0, resultString.length()-1);
        }
    }

    public static void deleteDbFile(Context context) {
        dbFile = new File(context.getFilesDir(), DB_FILE_NAME);
        if (dbFile.exists()) {
            if (dbFile.delete()) {
                DevTools.log(TAG, "dbFile was deleted successfully");
            } else {
                DevTools.logE(TAG, "Error occured deleting dbFile");
            }
        }

    }

}
