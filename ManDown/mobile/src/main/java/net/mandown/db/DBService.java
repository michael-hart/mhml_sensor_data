package net.mandown.db;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.mandown.R;
import net.mandown.sensors.SensorSample;
import net.mandown.sensors.SensorType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread. TODO Implement separate thread for service work.
 */
public class DBService extends IntentService {

    // Track latest instance with static variable to allow static calls
    public static DBService sInstance;

    // Keep a reference to the database
    DatabaseReference mRef;

    // User reference
    private String mAndroidId;

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

        // Get unique Android ID
        mAndroidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Instantiate reference to database
        mRef = (FirebaseDatabase.getInstance()).getReference("Users").child(mAndroidId);

    }

    @Override
    public void onDestroy() {
    }

    /**
     * Put an ArrayList of reaction times into the Firebase Database. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPutReactionTimes(Context context, List<Long> rtList) {
        Intent intent = new Intent(context, DBService.class);
        long[] reactionTimes = new long[rtList.size()];
        int count = 0;
        for (Long i : rtList) {
            reactionTimes[count++] = i;
        }

        intent.setAction(context.getString(R.string.put_whackabeer_rt));
        intent.putExtra(context.getString(R.string.rt_arr), reactionTimes);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Put Sensor List with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPutSensorList(Context context, List<SensorSample> list,
                                                SensorType type) {
        Intent intent = new Intent(context, DBService.class);
        // Split accelerometer samples into arrays
        long[] timestamps = new long[list.size()];
        float[] x = new float[list.size()];
        float[] y = new float[list.size()];
        float[] z = new float[list.size()];

        int i = 0;
        for (SensorSample ss : list) {
            timestamps[i] = ss.mTimestamp;
            x[i] = ss.mX;
            y[i] = ss.mY;
            z[i] = ss.mZ;
            i++;
        }

        switch (type)
        {
            case ACCELEROMETER:
                intent.setAction(context.getString(R.string.put_accel_list));
                intent.putExtra(context.getString(R.string.accel_timestamp_arr), timestamps);
                intent.putExtra(context.getString(R.string.accel_x_arr), x);
                intent.putExtra(context.getString(R.string.accel_y_arr), y);
                intent.putExtra(context.getString(R.string.accel_z_arr), z);
                break;
            case GYROSCOPE:
                intent.setAction(context.getString(R.string.put_gyro_list));
                intent.putExtra(context.getString(R.string.gyro_timestamp_arr), timestamps);
                intent.putExtra(context.getString(R.string.gyro_x_arr), x);
                intent.putExtra(context.getString(R.string.gyro_y_arr), y);
                intent.putExtra(context.getString(R.string.gyro_z_arr), z);
                break;
            case MAGNETOMETER:
                intent.setAction(context.getString(R.string.put_magnet_list));
                intent.putExtra(context.getString(R.string.magnet_timestamp_arr), timestamps);
                intent.putExtra(context.getString(R.string.magnet_x_arr), x);
                intent.putExtra(context.getString(R.string.magnet_y_arr), y);
                intent.putExtra(context.getString(R.string.magnet_z_arr), z);
                break;
        }

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

            if (getString(R.string.put_whackabeer_score).equals(action)) {
                // TODO handle put whack-a-beer score
            } else if (getString(R.string.put_whackabeer_rt).equals(action)) {
                long[] reactionTimes = intent.getLongArrayExtra(getString(R.string.rt_arr));
                handleActionPutReactionTimes(reactionTimes);
            } else if (getString(R.string.put_accel_list).equals(action)) {
                long[] timestamps =
                        intent.getLongArrayExtra(getString(R.string.accel_timestamp_arr));
                float[] x = intent.getFloatArrayExtra(getString(R.string.accel_x_arr));
                float[] y = intent.getFloatArrayExtra(getString(R.string.accel_y_arr));
                float[] z = intent.getFloatArrayExtra(getString(R.string.accel_z_arr));
                handleActionPutAccelList(timestamps, x, y, z);
            } else if (getString(R.string.put_gyro_list).equals(action)) {
                long[] timestamps =
                        intent.getLongArrayExtra(getString(R.string.gyro_timestamp_arr));
                float[] x = intent.getFloatArrayExtra(getString(R.string.gyro_x_arr));
                float[] y = intent.getFloatArrayExtra(getString(R.string.gyro_y_arr));
                float[] z = intent.getFloatArrayExtra(getString(R.string.gyro_z_arr));
                handleActionPutGyroList(timestamps, x, y, z);
            } else if (getString(R.string.put_magnet_list).equals(action)) {
                long[] timestamps =
                        intent.getLongArrayExtra(getString(R.string.magnet_timestamp_arr));
                float[] x = intent.getFloatArrayExtra(getString(R.string.magnet_x_arr));
                float[] y = intent.getFloatArrayExtra(getString(R.string.magnet_y_arr));
                float[] z = intent.getFloatArrayExtra(getString(R.string.magnet_z_arr));
                handleActionPutMagnetList(timestamps, x, y, z);
            }
        }
    }

    private void handleActionPutReactionTimes(long[] times) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());
        List<Long> listTimes = new ArrayList<Long>();
        for (int i = 0; i < times.length; i++) {
            listTimes.add(times[i]);
        }
        mRef.child("reaction").child(format).setValue(listTimes);
    }

    private void handleActionPutAccelList(long[] timestamp, float[] acc_x, float[] acc_y,
                                          float[] acc_z)
    {
        // TODO change timestamp to real time string

        // Convert all the parameters to lists
        List<Long> stamps = new ArrayList<Long>();
        List<Float> x = new ArrayList<Float>();
        List<Float> y = new ArrayList<Float>();
        List<Float> z = new ArrayList<Float>();

        for (int i = 0; i < timestamp.length; i++) {
            stamps.add(timestamp[i]);
            x.add(acc_x[i]);
            y.add(acc_y[i]);
            z.add(acc_z[i]);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());
        mRef.child("accelerometer").child(format).child("timestamp").setValue(stamps);
        mRef.child("accelerometer").child(format).child("x").setValue(x);
        mRef.child("accelerometer").child(format).child("y").setValue(y);
        mRef.child("accelerometer").child(format).child("z").setValue(z);
    }

    private void handleActionPutGyroList(long[] timestamp, float[] gyr_x, float[] gyr_y,
                                         float[] gyr_z)
    {
        // TODO change timestamp to real time string

        // Convert all the parameters to lists
        List<Long> stamps = new ArrayList<Long>();
        List<Float> x = new ArrayList<Float>();
        List<Float> y = new ArrayList<Float>();
        List<Float> z = new ArrayList<Float>();

        for (int i = 0; i < timestamp.length; i++) {
            stamps.add(timestamp[i]);
            x.add(gyr_x[i]);
            y.add(gyr_y[i]);
            z.add(gyr_z[i]);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());
        mRef.child("gyro").child(format).child("timestamp").setValue(stamps);
        mRef.child("gyro").child(format).child("x").setValue(x);
        mRef.child("gyro").child(format).child("y").setValue(y);
    }

    private void handleActionPutMagnetList(long[] timestamp, float[] mag_x, float[] mag_y,
                                           float[] mag_z)
    {
        // TODO change timestamp to real time string

        // Convert all the parameters to lists
        List<Long> stamps = new ArrayList<Long>();
        List<Float> x = new ArrayList<Float>();
        List<Float> y = new ArrayList<Float>();
        List<Float> z = new ArrayList<Float>();

        for (int i = 0; i < timestamp.length; i++) {
            stamps.add(timestamp[i]);
            x.add(mag_x[i]);
            y.add(mag_y[i]);
            z.add(mag_z[i]);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());
        mRef.child("magnetometer").child(format).child("timestamp").setValue(stamps);
        mRef.child("magnetometer").child(format).child("x").setValue(x);
        mRef.child("magnetometer").child(format).child("y").setValue(y);
        mRef.child("magnetometer").child(format).child("z").setValue(z);
    }

    /**
     * Return all currently valid user names in a string array
     * TODO return string array and implement null checks
     */
    public List<String> getUserNames() {
        // Instantiate return object
        List<String> names = new ArrayList<String>();
        // FIXME

        return names;
    }

    /**
     * Temporary function to return a count of the number of passive readings in the SQLite database
     * @return number of passive readings in database
     */
    public int getNumPassiveReadings() {

        int cnt = -1;
        // FIXME

        return cnt;
    }

    public int getNumAccelReadings() {
        // FIXME
        int cnt = -1;

        return cnt;
    }
}
