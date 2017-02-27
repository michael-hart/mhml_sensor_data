package net.mandown.db;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import net.mandown.R;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DBService extends IntentService {

    public DBService() {
        super("DBService");
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
        // TODO: Handle action Put Passive
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
