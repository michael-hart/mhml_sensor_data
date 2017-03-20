package net.mandown.db;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class DBService extends IntentService {

    // Track latest instance with static variable to allow static calls
    public static DBService sInstance;

    /* Declare most recent variable array lists */
    public static ArrayList<Long> mRecentReactions;
    public static List<SensorSample> mRecentAccel, mRecentGyro, mRecentMagn, mRecentWatchAccel;
    public static List<SensorSample> mRecentWalkAccel, mRecentWalkGyro, mRecentWalkMagn;
    public static List<SensorSample> mRecentSensorGameData;
    public static List<String[]> mIntoxHistory;
    public static int mRecentWhackABeerScore = 0;

    /* Store database reference */
    DatabaseReference mRef;

    public DBService() {
        super("DBService");
        Log.d("DBService", "DBService created");
    }

    String uid;
    String name;
    @Override
    public void onCreate() {
        super.onCreate();
        if (sInstance == null) {
            sInstance = this;
        }


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            uid = user.getUid();
        }

        String androidId =  name;
        if(androidId == null){

            androidId = Settings.Secure.getString(getContentResolver(),
               Settings.Secure.ANDROID_ID);
        }
        androidId = androidId.replace(".", "");

        mRef = (FirebaseDatabase.getInstance()).getReference("Users").child(androidId);

        mIntoxHistory = new ArrayList<>();
        mRef.child("Drunkness").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mIntoxHistory.add(new String[] {dataSnapshot.getKey(),
                        dataSnapshot.getValue(String.class)});
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

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

        if(!rtList.isEmpty()) {
            mRecentReactions = rtList;
        }

        Intent intent = new Intent(context, DBService.class);
        intent.setAction(context.getString(R.string.put_whackabeer_rt));
        intent.putExtra(context.getString(R.string.rt_arr), rtList);
        context.startService(intent);

    }

    /**
     * Put a single Whack-A-Beer score into the Firebase Database. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPutWhackABeerScore(Context context, int score) {
        mRecentWhackABeerScore = score;
        Intent intent = new Intent(context, DBService.class);
        intent.setAction(context.getString(R.string.put_whackabeer_score));
        intent.putExtra(context.getString(R.string.whackabeer_score), score);
        context.startService(intent);
    }


    //function to put machine learning result to Firebase
    public static void startActionPutML(Context context, String MLvalues) {
        Intent intent = new Intent(context, DBService.class);
        intent.setAction(context.getString(R.string.put_ml_values));
        intent.putExtra(context.getString(R.string.classif), MLvalues);
        context.startService(intent);
    }

    // Can be used for TightropeWaiter if sensor dara needs to be placed in another colummn
    // (Sensor Game) in Firebase
    public static void startActionPutSensorGameData(Context context, List<SensorSample> list,
                                                    SensorType type) {
        mRecentSensorGameData = list;
        Intent intent = new Intent(context, DBService.class);

        intent.setAction(context.getString(R.string.put_tightropewaiter_sn));

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
                intent.setAction(context.getString(R.string.put_tightropewaiter_sn));
                intent.putExtra(context.getString(R.string.accel_timestamp_game), timestamps);
                intent.putExtra(context.getString(R.string.accel_x_game), x);
                intent.putExtra(context.getString(R.string.accel_y_game), y);
                intent.putExtra(context.getString(R.string.accel_z_game), z);

                break;
        }

        context.startService(intent);

    }

    /**
     * Starts this service to perform action Put Sensor List with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    //passive sensor data
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
                mRecentMagn = list;
                break;
        }

        context.startService(intent);
    }

    public static void startActionPutWalkSensor(Context context, List<SensorSample> list,
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
                intent.setAction(context.getString(R.string.put_walk_accel));
                intent.putExtra(context.getString(R.string.accel_timestamp_walk), timestamps);
                intent.putExtra(context.getString(R.string.accel_x_walk), x);
                intent.putExtra(context.getString(R.string.accel_y_walk), y);
                intent.putExtra(context.getString(R.string.accel_z_walk), z);
                mRecentWalkAccel = list;
                break;
            case GYROSCOPE:
                intent.setAction(context.getString(R.string.put_walk_gyro));
                intent.putExtra(context.getString(R.string.gyro_timestamp_walk), timestamps);
                intent.putExtra(context.getString(R.string.gyro_x_walk), x);
                intent.putExtra(context.getString(R.string.gyro_y_walk), y);
                intent.putExtra(context.getString(R.string.gyro_z_walk), z);
                mRecentWalkGyro = list;
                break;
            case MAGNETOMETER:
                intent.setAction(context.getString(R.string.put_walk_magnet));
                intent.putExtra(context.getString(R.string.magnet_timestamp_walk), timestamps);
                intent.putExtra(context.getString(R.string.magnet_x_walk), x);
                intent.putExtra(context.getString(R.string.magnet_y_walk), y);
                intent.putExtra(context.getString(R.string.magnet_z_walk), z);
                mRecentWalkMagn = list;
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
                mRecentWatchAccel = list;
                break;
        }

        context.startService(intent);
    }

    public static ArrayList<Long> getMostRecentReactionTime() {
        return mRecentReactions;
    }

    public static List<SensorSample> getMostRecentAccel() {

        return mRecentAccel;
    }
    public static List<SensorSample> getMostRecentMagn() {

        return mRecentMagn;
    }
    public static List<SensorSample> getMostRecentGyro() {

        return mRecentGyro;
    }
    //captured on walking call
    public static List<SensorSample> getMostRecentWalkAccel() {

        return mRecentWalkAccel;
    }
    public static List<SensorSample> getMostRecentWalkMagn() {

        return mRecentWalkMagn;
    }
    public static List<SensorSample> getMostRecentWalkGyro() {

        return mRecentWalkGyro;
    }
    public static List<SensorSample> getMostRecentWatchAccel() {

        return mRecentWatchAccel;
    }
    public static List<SensorSample> getMostRecentSensorGameData() {

        return mRecentSensorGameData;
    }

    public static List<String[]> getIntoxHistory() {
        return mIntoxHistory;
    }
    public static int getMostRecentWhackABeerScore() {
        return mRecentWhackABeerScore;
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

            if (getString(R.string.put_whackabeer_rt).equals(action)) {
                ArrayList<Long> reactionTimes = (ArrayList<Long>)
                        intent.getSerializableExtra(getString(R.string.rt_arr));
                handleActionPutReactionTimes(reactionTimes);
            } else if (getString(R.string.put_ml_values).equals(action)) {
                String mlValue = intent.getStringExtra(getString(R.string.classif));
                handleActionPutMLValues(mlValue);
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
            }  else if (getString(R.string.put_walk_accel).equals(action)) {
                ArrayList<Long> timestamps = (ArrayList<Long>)
                        intent.getSerializableExtra(getString(R.string.accel_timestamp_walk));
                ArrayList<Float> x = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.accel_x_walk));
                ArrayList<Float> y = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.accel_y_walk));
                ArrayList<Float> z = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.accel_z_walk));
                handleActionPutWalkAccelList(timestamps, x, y, z);
            } else if (getString(R.string.put_walk_gyro).equals(action)) {
                ArrayList<Long> timestamps = (ArrayList<Long>)
                        intent.getSerializableExtra(getString(R.string.gyro_timestamp_walk));
                ArrayList<Float> x = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.gyro_x_walk));
                ArrayList<Float> y = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.gyro_y_walk));
                ArrayList<Float> z = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.gyro_z_walk));
                handleActionPutWalkGyroList(timestamps, x, y, z);
            } else if (getString(R.string.put_walk_magnet).equals(action)) {
                ArrayList<Long> timestamps = (ArrayList<Long>)
                        intent.getSerializableExtra(getString(R.string.magnet_timestamp_walk));
                ArrayList<Float> x = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.magnet_x_walk));
                ArrayList<Float> y = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.magnet_y_walk));
                ArrayList<Float> z = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.magnet_z_walk));
                handleActionPutWalkMagnetList(timestamps, x, y, z);
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
            } else if (getString(R.string.put_tightropewaiter_sn).equals(action)) {
                ArrayList<Long> timestamps = (ArrayList<Long>)
                        intent.getSerializableExtra(getString(R.string.accel_timestamp_game));
                ArrayList<Float> x = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.accel_x_game));
                ArrayList<Float> y = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.accel_y_game));
                ArrayList<Float> z = (ArrayList<Float>)
                        intent.getSerializableExtra(getString(R.string.accel_z_game));
                handleActionPutSensorGamedata(timestamps, x, y, z);

            } else if (getString(R.string.put_whackabeer_score).equals(action)) {
                // Check for whack-a-beer put score

                int score = intent.getIntExtra(getString(R.string.whackabeer_score), 0);
                handleActionPutWhackABeerScore(score);

            }
        }
    }

    private void handleActionPutSensorGamedata(ArrayList<Long> timestamps, ArrayList<Float> acc_x,
                                               ArrayList<Float> acc_y, ArrayList<Float> acc_z) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());
        mRef.child("Sensorgame").child(format).child("timestamp").setValue(timestamps);
        mRef.child("Sensorgame").child(format).child("x").setValue(acc_x);
        mRef.child("Sensorgame").child(format).child("y").setValue(acc_y);
        mRef.child("Sensorgame").child(format).child("z").setValue(acc_z);

    }

    private void handleActionPutMLValues(String ml) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());
        mRef.child("Drunkness").child(format).setValue(ml);
    }

    private void handleActionPutReactionTimes(ArrayList<Long> rt) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());
        mRef.child("reaction").child(format).setValue(rt);
    }

    private void handleActionPutWatchList(ArrayList<Long> timestamps, ArrayList<Float> acc_x,
                                          ArrayList<Float> acc_y, ArrayList<Float> acc_z)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());
        mRef.child("watch").child(format).child("timestamp").setValue(timestamps);
        mRef.child("watch").child(format).child("x").setValue(acc_x);
        mRef.child("watch").child(format).child("y").setValue(acc_y);
        mRef.child("watch").child(format).child("z").setValue(acc_z);
    }

    /* Put Walking Sensor Values */
    private void handleActionPutWalkAccelList(ArrayList<Long> timestamps, ArrayList<Float> acc_x,
                                          ArrayList<Float> acc_y, ArrayList<Float> acc_z)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());
        mRef.child("Walk").child("accelerometer").child(format).child("timestamp").setValue(timestamps);
        mRef.child("Walk").child("accelerometer").child(format).child("x").setValue(acc_x);
        mRef.child("Walk").child("accelerometer").child(format).child("y").setValue(acc_y);
        mRef.child("Walk").child("accelerometer").child(format).child("z").setValue(acc_z);
    }

    private void handleActionPutWalkGyroList(ArrayList<Long> timestamps, ArrayList<Float> gyr_x,
                                         ArrayList<Float> gyr_y, ArrayList<Float> gyr_z)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());
        mRef.child("Walk").child("gyro").child(format).child("timestamp").setValue(timestamps);
        mRef.child("Walk").child("gyro").child(format).child("x").setValue(gyr_x);
        mRef.child("Walk").child("gyro").child(format).child("y").setValue(gyr_y);
        mRef.child("Walk").child("gyro").child(format).child("z").setValue(gyr_z);
    }

    private void handleActionPutWalkMagnetList(ArrayList<Long> timestamps, ArrayList<Float> mag_x,
                                           ArrayList<Float> mag_y, ArrayList<Float> mag_z)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());
        mRef.child("Walk").child("Magnetometer").child(format).child("timestamp").setValue(timestamps);
        mRef.child("Walk").child("Magnetometer").child(format).child("x").setValue(mag_x);
        mRef.child("Walk").child("Magnetometer").child(format).child("y").setValue(mag_y);
        mRef.child("Walk").child("Magnetometer").child(format).child("z").setValue(mag_z);
    }

    /* Passive Data */
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
        mRef.child("gyro").child(format).child("timestamp").setValue(timestamps);
        mRef.child("gyro").child(format).child("x").setValue(gyr_x);
        mRef.child("gyro").child(format).child("y").setValue(gyr_y);
        mRef.child("gyro").child(format).child("z").setValue(gyr_z);
    }

    private void handleActionPutMagnetList(ArrayList<Long> timestamps, ArrayList<Float> mag_x,
                                         ArrayList<Float> mag_y, ArrayList<Float> mag_z)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());
        mRef.child("Magnetometer").child(format).child("timestamp").setValue(timestamps);
        mRef.child("Magnetometer").child(format).child("x").setValue(mag_x);
        mRef.child("Magnetometer").child(format).child("y").setValue(mag_y);
        mRef.child("Magnetometer").child(format).child("z").setValue(mag_z);
    }

    private void handleActionPutWhackABeerScore(int score) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String format = dateFormat.format(new Date());
        mRef.child("Whack-A-Beer Score").child(format).setValue(score);
    }

}