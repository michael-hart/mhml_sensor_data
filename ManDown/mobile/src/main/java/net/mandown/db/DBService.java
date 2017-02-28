package net.mandown.db;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.mandown.R;
import net.mandown.db.PassiveDataReaderContract.PassiveDataEntry;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread. TODO Implement separate thread for service work.
 */
public class DBService extends IntentService {

    private ManDownDbHelper mDbHelper;
    // Default row ID until we can sign a user into a server and get the User ID from there
    private int row_id = 1;
    // Track latest instance with static variable to allow static calls
    public static DBService sInstance;

    public DBService() {
        super("DBService");
        Log.i("DBService", "DBService created");
        sInstance = this;
    }

    @Override
    public void onDestroy() {
        // Close database connection
        mDbHelper.close();
    }

    public static void startActionResetDatabase(Context context) {
        Intent intent = new Intent(context, DBService.class);
        intent.setAction(context.getString(R.string.reset_db));
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Put Passive with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPutPassive(Context context,
                                             float accel,
                                             float gyro,
                                             float magnet) {
        Intent intent = new Intent(context, DBService.class);
        intent.setAction(context.getString(R.string.put_passive));
        intent.putExtra(context.getString(R.string.passive_accel),   accel);
        intent.putExtra(context.getString(R.string.passive_gyro),    gyro);
        intent.putExtra(context.getString(R.string.passive_magnet),  magnet);
        context.startService(intent);
    }

    /**
     * Overrides method to handle the intent, analysing the intent action and calling the
     * appropriate handler task
     * @param intent - the Intent object used to start the service
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("DBService", "onHandleIntent called");
        if (mDbHelper == null)
        {
            mDbHelper = new ManDownDbHelper(getApplicationContext());
        }
        if (intent != null) {
            final String action = intent.getAction();

            // Put passive sensor data reading
            if (getString(R.string.reset_db).equals(action)) {
                mDbHelper.onUpgrade(mDbHelper.getWritableDatabase(), 1, 1);
            } else if (getString(R.string.put_passive).equals(action)) {
                final float accel  = intent.getFloatExtra(getString(R.string.passive_accel),  0f);
                final float gyro   = intent.getFloatExtra(getString(R.string.passive_gyro),   0f);
                final float magnet = intent.getFloatExtra(getString(R.string.passive_magnet), 0f);
                handleActionPutPassive(accel, gyro, magnet);
            } else if (getString(R.string.put_whackabeer_score).equals(action)) {
                // TODO handle put whack-a-beer score
            } else if (getString(R.string.put_whackabeer_rt).equals(action)) {
                // TODO handle put whack-a-beer reaction time
            }
        }
    }

    /**
     * Handle action Put Passive in the provided background thread with the provided
     * parameters. Puts passive sensor data in the SQLite database.
     */
    private void handleActionPutPassive(float accel, float gyro, float magnet) {
        // Get a reference to the writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Bung in a default ID of 1 with a user name
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PassiveDataEntry.COLUMN_NAME_ID, row_id);
        values.put(PassiveDataEntry.COLUMN_NAME_ACCEL, accel);
        values.put(PassiveDataEntry.COLUMN_NAME_GYRO, gyro);
        values.put(PassiveDataEntry.COLUMN_NAME_MAGNET, magnet);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(PassiveDataEntry.TABLE_NAME, null, values);
        Log.i("DBService", String.format("Inserted (accel,gyro,magnet)=(%f,%f,%f) as row ID %d",
                                         accel, gyro, magnet, row_id));
    }

    /**
     * Return all currently valid user names in a string array
     * TODO return string array and implement null checks
     */
    public void getUserNames() {
        // Get a reference to database
        SQLiteDatabase r_db = mDbHelper.getReadableDatabase();

        // Get the IDs and user names currently in the database
        String countQuery = "SELECT * FROM " + UserNameReaderContract.UserNameEntry.TABLE_NAME;
        Cursor cursor = r_db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
    }

    /**
     * Temporary function to return a count of the number of passive readings in the SQLite database
     * @return number of passive readings in database
     */
    public int getNumPassiveReadings() {
        if (mDbHelper == null)
        {
            return -1;
        }
        // Get a reference to database
        SQLiteDatabase r_db = mDbHelper.getReadableDatabase();

        int cnt = -1;

        if (r_db != null) {
            // Get the IDs and user names currently in the database
            String countQuery = "SELECT * FROM " + PassiveDataEntry.TABLE_NAME;
            Cursor cursor = r_db.rawQuery(countQuery, null);
            cnt = cursor.getCount();
            cursor.close();
        }
        return cnt;
    }
}
