package net.mandown.db;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import net.mandown.R;
import net.mandown.db.PassiveDataReaderContract.PassiveDataEntry;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DBService extends IntentService {

    private SQLiteDatabase mDb;
    private ManDownDbHelper mDbHelper;
    private int row_id = 1;

    public DBService() {
        super("DBService");
        mDbHelper = new ManDownDbHelper(this);
    }

    @Override
    public void onDestroy() {
        // Close database connection
        mDbHelper.close();
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

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            // Put passive sensor data reading
            if (getString(R.string.put_passive).equals(action)) {
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

    }
}
