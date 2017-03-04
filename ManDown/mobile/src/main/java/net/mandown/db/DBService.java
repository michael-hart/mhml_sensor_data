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
import net.mandown.db.AccelDataReaderContract.AccelDataEntry;
import net.mandown.sensors.AccelerometerSample;

import java.util.ArrayList;
import java.util.List;

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
        Log.d("DBService", "DBService created");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (sInstance == null) {
            sInstance = this;
        }
        if (mDbHelper == null)
        {
            mDbHelper = new ManDownDbHelper(getApplicationContext());
        }
    }

    @Override
    public void onDestroy() {
        if (mDbHelper != null) {
            // Close database connection
            mDbHelper.close();
        }
    }

    /**
     * Get the instantiated database helper
     * @return ManDownDbHelper object or null
     */
    public ManDownDbHelper getDbHelper() {
        return mDbHelper;
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
     * Starts this service to perform action Put Passive with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPutAccelList(Context context, List<AccelerometerSample> list) {
        Intent intent = new Intent(context, DBService.class);
        // Split accelerometer samples into arrays
        long[] timestamps = new long[list.size()];
        float[] x = new float[list.size()];
        float[] y = new float[list.size()];
        float[] z = new float[list.size()];

        int i = 0;
        for (AccelerometerSample as : list) {
            timestamps[i] = as.mTimestamp;
            x[i] = as.mX;
            y[i] = as.mY;
            z[i] = as.mZ;
            i++;
        }

        intent.setAction(context.getString(R.string.put_accel_list));
        intent.putExtra(context.getString(R.string.accel_timestamp_arr), timestamps);
        intent.putExtra(context.getString(R.string.accel_x_arr), x);
        intent.putExtra(context.getString(R.string.accel_y_arr), y);
        intent.putExtra(context.getString(R.string.accel_z_arr), z);

        context.startService(intent);
    }

    /**
     * Overrides method to handle the intent, analysing the intent action and calling the
     * appropriate handler task
     * @param intent - the Intent object used to start the service
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("DBService", "onHandleIntent called");
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
            } else if (getString(R.string.put_accel_list).equals(action)) {
                long[] timestamps =
                        intent.getLongArrayExtra(getString(R.string.accel_timestamp_arr));
                float[] x = intent.getFloatArrayExtra(getString(R.string.accel_x_arr));
                float[] y = intent.getFloatArrayExtra(getString(R.string.accel_y_arr));
                float[] z = intent.getFloatArrayExtra(getString(R.string.accel_z_arr));
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
        Log.d("DBService", String.format("Inserted (accel,gyro,magnet)=(%f,%f,%f) as row ID %d",
                                         accel, gyro, magnet, row_id));
    }

    private void handleActionPutAccelList(long[] timestamp, float[] acc_x, float[] acc_y,
                                          float[] acc_z)
    {
        // Get a reference to the writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        for (int i = 0; i < timestamp.length; i++) {
            // Bung in a default ID of 1 with a user name
            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(AccelDataEntry.COLUMN_NAME_ID, row_id);
            values.put(AccelDataEntry.COLUMN_NAME_TS, timestamp[i]);
            values.put(AccelDataEntry.COLUMN_NAME_ACCEL_X, acc_x[i]);
            values.put(AccelDataEntry.COLUMN_NAME_ACCEL_Y, acc_y[i]);
            values.put(AccelDataEntry.COLUMN_NAME_ACCEL_Z, acc_z[i]);

            long newRowId = db.insert(AccelDataEntry.TABLE_NAME, null, values);
        }
    }

    /**
     * Return all currently valid user names in a string array
     * TODO return string array and implement null checks
     */
    public List<String> getUserNames() {
        // Instantiate return object
        List<String> names = new ArrayList<String>();

        // Get a reference to database
        SQLiteDatabase r_db = mDbHelper.getReadableDatabase();

        // Get the IDs and user names currently in the database
        String countQuery = "SELECT * FROM " + UserNameReaderContract.UserNameEntry.TABLE_NAME;
        Cursor cursor = r_db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();

        return names;
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

    public int getNumAccelReadings() {
        if (mDbHelper == null)
        {
            return -1;
        }
        // Get a reference to database
        SQLiteDatabase r_db = mDbHelper.getReadableDatabase();

        int cnt = -1;

        if (r_db != null) {
            // Get the IDs and user names currently in the database
            String countQuery = "SELECT * FROM " + AccelDataEntry.TABLE_NAME;
            Cursor cursor = r_db.rawQuery(countQuery, null);
            cnt = cursor.getCount();
            cursor.close();
        }
        return cnt;
    }
}
