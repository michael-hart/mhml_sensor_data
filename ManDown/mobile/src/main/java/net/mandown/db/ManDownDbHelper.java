package net.mandown.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.mandown.db.UserNameReaderContract.UserNameEntry;
import net.mandown.db.PassiveDataReaderContract.PassiveDataEntry;
import net.mandown.db.AccelDataReaderContract.AccelDataEntry;
import net.mandown.db.GyroDataReaderContract.GyroDataEntry;
import net.mandown.db.MagnetDataReaderContract.MagnetDataEntry;

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
                    PassiveDataEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PassiveDataEntry.COLUMN_NAME_ID + " INTEGER, " +
                    // Set ID to be foreign id FIXME
//                    "FOREIGN KEY ("+PassiveDataEntry.COLUMN_NAME_ID+") REFERENCES " +
//                    UserNameEntry.TABLE_NAME + "(" + UserNameEntry._ID + ")," +
                    // Back to column names
                    PassiveDataEntry.COLUMN_NAME_ACCEL + " REAL," +
                    PassiveDataEntry.COLUMN_NAME_GYRO + " REAL," +
                    PassiveDataEntry.COLUMN_NAME_MAGNET + " REAL);";

    private static final String SQL_CREATE_ACCEL_DATA =
            "CREATE TABLE " + AccelDataEntry.TABLE_NAME + " (" +
                    AccelDataEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    AccelDataEntry.COLUMN_NAME_ID + " INTEGER, " +
                    AccelDataEntry.COLUMN_NAME_TS + " INTEGER, " +
                    AccelDataEntry.COLUMN_NAME_ACCEL_X + " REAL," +
                    AccelDataEntry.COLUMN_NAME_ACCEL_Y + " REAL," +
                    AccelDataEntry.COLUMN_NAME_ACCEL_Z + " REAL);";

    private static final String SQL_CREATE_GYRO_DATA =
            "CREATE TABLE " + GyroDataEntry.TABLE_NAME + " (" +
                    GyroDataEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    GyroDataEntry.COLUMN_NAME_ID + " INTEGER, " +
                    GyroDataEntry.COLUMN_NAME_TS + " INTEGER, " +
                    GyroDataEntry.COLUMN_NAME_GYRO_X + " REAL," +
                    GyroDataEntry.COLUMN_NAME_GYRO_Y + " REAL," +
                    GyroDataEntry.COLUMN_NAME_GYRO_Z + " REAL);";

    private static final String SQL_CREATE_MAGNET_DATA =
            "CREATE TABLE " + MagnetDataEntry.TABLE_NAME + " (" +
                    MagnetDataEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MagnetDataEntry.COLUMN_NAME_ID + " INTEGER, " +
                    MagnetDataEntry.COLUMN_NAME_TS + " INTEGER, " +
                    MagnetDataEntry.COLUMN_NAME_MAGNET_X + " REAL," +
                    MagnetDataEntry.COLUMN_NAME_MAGNET_Y + " REAL," +
                    MagnetDataEntry.COLUMN_NAME_MAGNET_Z + " REAL);";

    private static final String SQL_DELETE_USER_NAME =
            "DROP TABLE IF EXISTS " + UserNameEntry.TABLE_NAME;

    private static final String SQL_DELETE_PASSIVE_DATA =
            "DROP TABLE IF EXISTS " + PassiveDataEntry.TABLE_NAME;

    private static final String SQL_DELETE_ACCEL_DATA =
            "DROP TABLE IF EXISTS " + AccelDataEntry.TABLE_NAME;

    private static final String SQL_DELETE_GYRO_DATA =
            "DROP TABLE IF EXISTS " + GyroDataEntry.TABLE_NAME;

    private static final String SQL_DELETE_MAGNET_DATA =
            "DROP TABLE IF EXISTS " + MagnetDataEntry.TABLE_NAME;

    public ManDownDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        Log.d("ManDownDbHelper names", SQL_CREATE_USER_NAME);
        Log.d("ManDownDbHelper passive", SQL_CREATE_PASSIVE_DATA);
        Log.d("ManDownDbHelper accel", SQL_CREATE_ACCEL_DATA);
        Log.d("ManDownDbHelper gyro", SQL_CREATE_GYRO_DATA);
        Log.d("ManDownDbHelper magnet", SQL_CREATE_MAGNET_DATA);

        db.execSQL(SQL_ENABLE_FOREIGN);
        db.execSQL(SQL_CREATE_USER_NAME);
        db.execSQL(SQL_CREATE_PASSIVE_DATA);
        db.execSQL(SQL_CREATE_ACCEL_DATA);
        db.execSQL(SQL_CREATE_GYRO_DATA);
        db.execSQL(SQL_CREATE_MAGNET_DATA);

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
        db.execSQL(SQL_DELETE_MAGNET_DATA);
        db.execSQL(SQL_DELETE_GYRO_DATA);
        db.execSQL(SQL_DELETE_ACCEL_DATA);
        db.execSQL(SQL_DELETE_PASSIVE_DATA);
        db.execSQL(SQL_DELETE_USER_NAME);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
