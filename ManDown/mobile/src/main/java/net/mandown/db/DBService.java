package net.mandown.db;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.mandown.R;
import net.mandown.sensors.SensorSample;
import net.mandown.sensors.SensorType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class DBService extends IntentService {

    // Track latest instance with static variable to allow static calls
    public static DBService sInstance;

    /*
     * Create lists for most recent readings
     */
    private static ArrayList<Long> mRecentReaction;
    private static List<SensorSample> mRecentAccel, mRecentGyro, mRecentMag, mRecentWearAccel;


    // Keep a reference to the database
    DatabaseReference mRef;

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
        String androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

        // Instantiate reference to database
        mRef = (FirebaseDatabase.getInstance()).getReference("Users").child(androidId);

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
    public static void startActionPutReactionTimes(Context context, ArrayList<Long> rtList) {
        Intent intent = new Intent(context, DBService.class);
        intent.setAction(context.getString(R.string.put_whackabeer_rt));
        intent.putExtra(context.getString(R.string.rt_arr), rtList);
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
        // Split samples into separate lists
        ArrayList<Long> timestamps = new ArrayList<>();
        ArrayList<Float> x = new ArrayList<>();
        ArrayList<Float> y = new ArrayList<>();
        ArrayList<Float> z = new ArrayList<>();

        for (SensorSample s : list) {
            timestamps.add(s.mTimestamp);
            x.add(s.mX);
            y.add(s.mY);
            z.add(s.mZ);
        }

        switch (type)
        {
            case ACCELEROMETER:
                intent.setAction(context.getString(R.string.put_accel_list));
                intent.putExtra(context.getString(R.string.accel_timestamp_arr), timestamps);
                intent.putExtra(context.getString(R.string.accel_x_arr), x);
                intent.putExtra(context.getString(R.string.accel_y_arr), y);
                intent.putExtra(context.getString(R.string.accel_z_arr), z);
                mRecentAccel = list;
                break;
            case GYROSCOPE:
                intent.setAction(context.getString(R.string.put_gyro_list));
                intent.putExtra(context.getString(R.string.gyro_timestamp_arr), timestamps);
                intent.putExtra(context.getString(R.string.gyro_x_arr), x);
                intent.putExtra(context.getString(R.string.gyro_y_arr), y);
                intent.putExtra(context.getString(R.string.gyro_z_arr), z);
                mRecentGyro = list;
                break;
            case MAGNETOMETER:
                intent.setAction(context.getString(R.string.put_magnet_list));
                intent.putExtra(context.getString(R.string.magnet_timestamp_arr), timestamps);
                intent.putExtra(context.getString(R.string.magnet_x_arr), x);
                intent.putExtra(context.getString(R.string.magnet_y_arr), y);
                intent.putExtra(context.getString(R.string.magnet_z_arr), z);
                mRecentMag = list;
                break;
        }

        context.startService(intent);
    }

    public static void startPutActionWatchAccel(Context context, List<SensorSample> list,
                                                SensorType type) {
        Intent intent = new Intent(context, DBService.class);
        // Split samples into separate lists
        ArrayList<Long> timestamps = new ArrayList<>();
        ArrayList<Float> x = new ArrayList<>();
        ArrayList<Float> y = new ArrayList<>();
        ArrayList<Float> z = new ArrayList<>();

        for (SensorSample s : list) {
            timestamps.add(s.mTimestamp);
            x.add(s.mX);
            y.add(s.mY);
            z.add(s.mZ);
        }

        switch (type)
        {
            case ACCELEROMETER:
                intent.setAction(context.getString(R.string.put_watch_accel));
                intent.putExtra(context.getString(R.string.watch_timestamp_arr), timestamps);
                intent.putExtra(context.getString(R.string.watch_x_arr), x);
                intent.putExtra(context.getString(R.string.watch_y_arr), y);
                intent.putExtra(context.getString(R.string.watch_z_arr), z);
                mRecentWearAccel = list;
                break;
        }

        context.startService(intent);
    }



    public static ArrayList<Long> getMostRecentReactionTime() {
        return mRecentReaction;
    }

    public static List<SensorSample> getMostRecentAccel() {
        return mRecentAccel;
    }
    public static List<SensorSample> getMostRecentMagn() {
        return mRecentMag;
    }
    public static List<SensorSample> getMostRecentGyro() {
        return mRecentGyro;
    }

    public static List<SensorSample> getMostRecentWatchAccel() {
        return mRecentWearAccel;
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
                ArrayList<Long> reactionTimes = (ArrayList<Long>)
                        intent.getSerializableExtra(getString(R.string.rt_arr));
            } else if (getString(R.string.put_accel_list).equals(action)) {
                ArrayList<Long> timestamps = (ArrayList<Long>)
                        intent.getSerializableExtra(getString(R.string.accel_timestamp_arr));
                ArrayList<Float> x = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.accel_x_arr));
                ArrayList<Float> y = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.accel_y_arr));
                ArrayList<Float> z = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.accel_z_arr));
                handleActionPutAccelList(timestamps, x, y, z);
            } else if (getString(R.string.put_gyro_list).equals(action)) {
                ArrayList<Long> timestamps = (ArrayList<Long>)
                        intent.getSerializableExtra(getString(R.string.gyro_timestamp_arr));
                ArrayList<Float> x = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.gyro_x_arr));
                ArrayList<Float> y = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.gyro_y_arr));
                ArrayList<Float> z = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.gyro_z_arr));
                handleActionPutGyroList(timestamps, x, y, z);
            } else if (getString(R.string.put_magnet_list).equals(action)) {
                ArrayList<Long> timestamps = (ArrayList<Long>)
                        intent.getSerializableExtra(getString(R.string.magnet_timestamp_arr));
                ArrayList<Float> x = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.magnet_x_arr));
                ArrayList<Float> y = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.magnet_y_arr));
                ArrayList<Float> z = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.magnet_z_arr));
                handleActionPutMagnetList(timestamps, x, y, z);
            } else if (getString(R.string.put_watch_accel).equals(action)) {
                ArrayList<Long> timestamps = (ArrayList<Long>)
                        intent.getSerializableExtra(getString(R.string.watch_timestamp_arr));
                ArrayList<Float> x = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.watch_x_arr));
                ArrayList<Float> y = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.watch_y_arr));
                ArrayList<Float> z = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.watch_z_arr));
                handleActionPutWatchList(timestamps, x, y, z);
            }
        }
    }

    private void handleActionPutReactionTimes(ArrayList<Long> times) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());

        mRef.child("reaction").child(format).setValue(times);

        //////reading from firebase
        DatabaseReference mRef2= mRef.child("reaction");
        // Read from the database
        mRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Map<String,Long> value = (Map)dataSnapshot.getValue();
                Log.d("Value is: " , String.valueOf(value.entrySet()));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Failed to read value.", error.toException());
            }
        });
        ///////////////////
    }

    private void handleActionPutAccelList(ArrayList<Long> timestamps, ArrayList<Float> acc_x,
                                          ArrayList<Float> acc_y, ArrayList<Float> acc_z)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());
        mRef.child("accelerometer").child(format).child("timestamp").setValue(timestamps);
        mRef.child("accelerometer").child(format).child("x").setValue(acc_x);
        mRef.child("accelerometer").child(format).child("y").setValue(acc_y);
        mRef.child("accelerometer").child(format).child("z").setValue(acc_z);
    }

    private void handleActionPutGyroList(ArrayList<Long> timestamps, ArrayList<Float> gyr_x,
                                          ArrayList<Float> gyr_y, ArrayList<Float> gyr_z)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());
        mRef.child("accelerometer").child(format).child("timestamp").setValue(timestamps);
        mRef.child("accelerometer").child(format).child("x").setValue(gyr_x);
        mRef.child("accelerometer").child(format).child("y").setValue(gyr_y);
        mRef.child("accelerometer").child(format).child("z").setValue(gyr_z);
    }

    private void handleActionPutMagnetList(ArrayList<Long> timestamps, ArrayList<Float> mag_x,
                                         ArrayList<Float> mag_y, ArrayList<Float> mag_z)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());
        mRef.child("accelerometer").child(format).child("timestamp").setValue(timestamps);
        mRef.child("accelerometer").child(format).child("x").setValue(mag_x);
        mRef.child("accelerometer").child(format).child("y").setValue(mag_y);
        mRef.child("accelerometer").child(format).child("z").setValue(mag_z);
    }

    private void handleActionPutGyroList(ArrayList<Long> timestamps, ArrayList<Float> gyr_x,
                                         ArrayList<Float> gyr_y, ArrayList<Float> gyr_z)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());
        mRef.child("gyroscope").child(format).child("timestamp").setValue(timestamps);
        mRef.child("gyroscope").child(format).child("x").setValue(gyr_x);
        mRef.child("gyroscope").child(format).child("y").setValue(gyr_y);
        mRef.child("gyroscope").child(format).child("z").setValue(gyr_z);
    }

    private void handleActionPutMagnetList(ArrayList<Long> timestamps, ArrayList<Float> mag_x,
                                           ArrayList<Float> mag_y, ArrayList<Float> mag_z)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());
        mRef.child("magnetometer").child(format).child("timestamp").setValue(timestamps);
        mRef.child("magnetometer").child(format).child("x").setValue(mag_x);
        mRef.child("magnetometer").child(format).child("y").setValue(mag_y);
        mRef.child("magnetometer").child(format).child("z").setValue(mag_z);
    }

}