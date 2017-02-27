package net.mandown.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

import net.mandown.db.UserNameReaderContract.UserNameEntry;
import net.mandown.db.PassiveDataReaderContract.PassiveDataEntry;

/**
 * SQLite Database helper for ManDown
 */
public class ManDownDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ManDown.db";

    private static final String SQL_ENABLE_FOREIGN =
            "PRAGMA foreign_keys = 1;";

    private static final String SQL_CREATE_USER_NAME =
            "CREATE TABLE " + UserNameEntry.TABLE_NAME + " (" +
                    UserNameEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    UserNameEntry.COLUMN_NAME_USER_NAME + " TEXT NOT NULL);";

    private static final String SQL_CREATE_PASSIVE_DATA =
            "CREATE TABLE " + PassiveDataEntry.TABLE_NAME + " (" +
                    PassiveDataEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    PassiveDataEntry.COLUMN_NAME_ID + " INTEGER," +
                    // Set ID to be foreign id
                    "FOREIGN KEY ("+PassiveDataEntry.COLUMN_NAME_ID+") REFERENCES " +
                    UserNameEntry.TABLE_NAME + "(" + UserNameEntry._ID + ")," +
                    // Back to column names
                    PassiveDataEntry.COLUMN_NAME_ACCEL + " REAL," +
                    PassiveDataEntry.COLUMN_NAME_GYRO + " REAL," +
                    PassiveDataEntry.COLUMN_NAME_MAGNET + " REAL);";

    private static final String SQL_DELETE_USER_NAME =
            "DROP TABLE IF EXISTS " + UserNameEntry.TABLE_NAME;

    private static final String SQL_DELETE_PASSIVE_DATA =
            "DROP TABLE IF EXISTS " + PassiveDataEntry.TABLE_NAME;

    public ManDownDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_ENABLE_FOREIGN);
        db.execSQL(SQL_CREATE_USER_NAME);
        db.execSQL(SQL_CREATE_PASSIVE_DATA);

        // Bung in a default ID of 1 with a user name
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(UserNameEntry.COLUMN_NAME_USER_NAME, "Joe Bloggs");

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(UserNameEntry.TABLE_NAME, null, values);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_PASSIVE_DATA);
        db.execSQL(SQL_DELETE_USER_NAME);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
