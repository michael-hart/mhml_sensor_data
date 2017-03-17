package net.mandown.db;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import net.mandown.R;
import net.mandown.ml.PredictionException;
import net.mandown.ml.RealtimePrediction;
import net.mandown.sensors.SensorSample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class IntoxicationService extends Service {

    // Constant definitions
    private static final int INTOX_CHECK_PERIOD_S = 600; // 10 minutes
    private static final float DRUNK_LEVEL = 1.5f; // Class 2 drunk or higher is too drunk

    private static final String SCORE_WHACKABEER = "score";
    private static final String VARIANCE_REACTION_TIME = "vrt";
    private static final String MEAN_REACTION_TIME = "mrt";

    private static final String VARIANCE_WALK_GYRO_X = "vargx";
    private static final String VARIANCE_WALK_GYRO_Y = "vargy";
    private static final String VARIANCE_WALK_GYRO_Z = "vargz";

    private static final String VARIANCE_WALK_ACCEL_X = "varax";
    private static final String VARIANCE_WALK_ACCEL_Y = "varay";
    private static final String VARIANCE_WALK_ACCEL_Z = "varaz";

    private static final String VARIANCE_SGAME_X = "varxs";
    private static final String VARIANCE_SGAME_Y = "varys";
    private static final String VARIANCE_SGAME_Z = "varzs";


    // Member variables
    private Timer mScheduleTimer;
    private boolean mTimerIsCancelled = false;
    private int mCheckPeriod = INTOX_CHECK_PERIOD_S;
    private Lock mVarLock;
    private boolean mRunning;

    private long mLastTimestamp = 0;
    private float mIntoxicationLevel = 0f;

    // Binder for allowing activities to bind to service
    private final IBinder mBinder = new IntoxBinder();
    public static IntoxicationService sInstance;


    /**
     * Class used for client Binder. Use threads with locks for safe multi-threaded access.
     */
    public class IntoxBinder extends Binder {
        public IntoxicationService getService() {
            return IntoxicationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    /*
     * Service creation/destruction
     */

    // Constructor replacement method
    @Override
    public void onCreate() {
        super.onCreate();
        // Create member objects
        mScheduleTimer = new Timer();
        mVarLock = new ReentrantLock();
        sInstance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("IntoxicationService", "Received start id " + startId + ": " + intent);

        // Start the scheduled service
        rescheduleIntoxChecker();

        // Return sticky, as service must stay alive to use the timer interval
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mVarLock.lock();
        mRunning = false;
        mVarLock.unlock();
    }


    /*
     * Public Getters/Setters
     */
    public int getIntoxCheckPeriod() {
        return mCheckPeriod;
    }

    public void setIntoxCheckPeriod(int period) {
        mVarLock.lock();
        mCheckPeriod = period;
        mVarLock.unlock();
        // Adjust timer period
        rescheduleIntoxChecker();
    }

    public float getIntoxLevel() {
        return mIntoxicationLevel;
    }

    public float getLastTimestamp() {
        return mLastTimestamp;
    }


    /*
     * Service tasks
     */

    public void stopPolling() {
        if (!mTimerIsCancelled) {
            mVarLock.lock();
            mScheduleTimer.cancel();
            mScheduleTimer = new Timer();
            mTimerIsCancelled = true;
            mVarLock.unlock();
        }
    }

    private void rescheduleIntoxChecker() {
        stopPolling();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Only start running if not already running
                if (!mRunning) {
                    // Spawn a thread to do the work
                    (new Thread(new IntoxChecker())).start();
                }
            }
        };
        mScheduleTimer.scheduleAtFixedRate(task, (mCheckPeriod) * 1000, (mCheckPeriod) * 1000);
        mTimerIsCancelled = false;
    }


    /*
     * Threaded task to check intoxication
     */
    private class IntoxChecker implements Runnable {
        private RealtimePrediction predictor;
        @Override
        public void run() {

            // Create the predictor object
            RealtimePrediction predictor;
            try {
                predictor = new RealtimePrediction();
                predictor.connect();
            } catch (PredictionException pe) {
                Log.e("IntoxicationService", "Error in ML connection: " + pe.getStackTrace());
                return;
            }

            // Create data map
            Map<String, String> classifySamples = new HashMap<>();

            // Extract and pack all data
            putWhackABeerData(classifySamples);
            putWalkGyroData(classifySamples);
            putWalkAccelData(classifySamples);
            putSensorGameData(classifySamples);

            // Return if there is no valid data
            if (classifySamples.size() == 0) {
                Log.w("IntoxicationService", "No valid data found during classification attempt");
                return;
            }

            float result = 0f;
            float conf = 0f;

            try {
                predictor.predict(classifySamples);
                String intox = predictor.getPredictedLabel();
                conf = predictor.getPredictedScore(intox);
                result = Float.parseFloat(intox);
            } catch (PredictionException pe) {
                Log.e("IntoxicationService", "Error in ML connection: " + pe.getStackTrace());
            }

            // Record data in member variables
            mVarLock.lock();
            mIntoxicationLevel = result;
            mLastTimestamp = System.currentTimeMillis();
            mVarLock.unlock();

            // Insert new value into database
            DBService.startActionPutML(getApplicationContext(), Float.toString(result));

            // Send a broadcast to any other listener in the system
            Intent intent = new Intent(getString(R.string.intox_broadcast));
            intent.putExtra("ml", result);
            intent.putExtra("conf", conf);
            intent.putExtra("ts", mLastTimestamp);
            sendBroadcast(intent);

            // Create a notification if too drunk
            if (result > DRUNK_LEVEL) {
                NotificationManager nm = (NotificationManager) getSystemService(
                        Service.NOTIFICATION_SERVICE);
                Notification notif = (new Notification.Builder(getApplicationContext()))
                        .setContentTitle("ManDown Intoxication Alert")
                        .setContentText("ManDown has detected that you are too intoxicated. " +
                                "Please take a break for your own safety!")
                        .setSmallIcon(R.drawable.beer_glass)
                        .build();
                nm.notify(1, notif);
            }

        }

        private void putWhackABeerData(Map<String, String> map) {
            // Grab the recent data to get a prediction from
            List<Long> reactions = DBService.getMostRecentReactionTime();
            int score = DBService.getMostRecentWhackABeerScore();

            // Extract features
            double meanRT = IntoxAlgs.LongAlgs.mean(reactions);
            double varRT = IntoxAlgs.LongAlgs.variance(reactions);

            // Only put the data in the map if it is valid and useful
            if (score > 0) {
                map.put(SCORE_WHACKABEER, Integer.toString(score));
            }

            if (meanRT >= 0) {
                map.put(MEAN_REACTION_TIME, Double.toString(score));
            }

            if (varRT >= 0) {
                map.put(VARIANCE_REACTION_TIME, Double.toString(score));
            }

        }

        private void putWalkGyroData(Map<String, String> map) {
            // Get recent data for prediction
            List<SensorSample> gyro = DBService.getMostRecentGyro();

            // Separate into x,y,z
            List<Float> gyroX = IntoxAlgs.getSensorX(gyro);
            List<Float> gyroY = IntoxAlgs.getSensorY(gyro);
            List<Float> gyroZ = IntoxAlgs.getSensorZ(gyro);

            // Extract features
            double varX = IntoxAlgs.FloatAlgs.variance(gyroX);
            double varY = IntoxAlgs.FloatAlgs.variance(gyroY);
            double varZ = IntoxAlgs.FloatAlgs.variance(gyroZ);

            // Only pack the data if it is valid
            if (varX >= 0) {
                map.put(VARIANCE_WALK_GYRO_X, Double.toString(varX));
            }
            if (varY >= 0) {
                map.put(VARIANCE_WALK_GYRO_Y, Double.toString(varY));
            }
            if (varZ >= 0) {
                map.put(VARIANCE_WALK_GYRO_Z, Double.toString(varZ));
            }
        }

        private void putWalkAccelData(Map<String, String> map) {
            // Get recent data for prediction
            List<SensorSample> phoneAccel = DBService.getMostRecentWalkAccel();
            List<SensorSample> watchAccel = DBService.getMostRecentWatchAccel();

            // Separate into x, y, z for all
            List<Float> phoneX = IntoxAlgs.getSensorX(phoneAccel);
            List<Float> phoneY = IntoxAlgs.getSensorY(phoneAccel);
            List<Float> phoneZ = IntoxAlgs.getSensorZ(phoneAccel);

            List<Float> watchX = IntoxAlgs.getSensorX(watchAccel);
            List<Float> watchY = IntoxAlgs.getSensorY(watchAccel);
            List<Float> watchZ = IntoxAlgs.getSensorZ(watchAccel);

            // Extract features
            double varX = -1;
            double varY = -1;
            double varZ = -1;

            double varPhoneX = IntoxAlgs.FloatAlgs.variance(phoneX);
            double varPhoneY = IntoxAlgs.FloatAlgs.variance(phoneY);
            double varPhoneZ = IntoxAlgs.FloatAlgs.variance(phoneZ);

            double varWatchX = IntoxAlgs.FloatAlgs.variance(watchX);
            double varWatchY = IntoxAlgs.FloatAlgs.variance(watchY);
            double varWatchZ = IntoxAlgs.FloatAlgs.variance(watchZ);

            // Take average and pack if useful
            if (varPhoneX >= 0 && varWatchX >= 0) {
                varX = (varPhoneX + varWatchX) / 2;
                map.put(VARIANCE_WALK_ACCEL_X, Double.toString(varX));
            }

            if (varPhoneY >= 0 && varWatchY >= 0) {
                varY = (varPhoneY + varWatchY) / 2;
                map.put(VARIANCE_WALK_ACCEL_Y, Double.toString(varY));
            }

            if (varPhoneZ >= 0 && varWatchZ >= 0) {
                varZ = (varPhoneZ + varWatchZ) / 2;
                map.put(VARIANCE_WALK_ACCEL_Z, Double.toString(varZ));
            }
        }

        private void putSensorGameData(Map<String, String> map) {
            // Get recent data for prediction
            List<SensorSample> sgAccel = DBService.getMostRecentSensorGameData();

            // Separate into x, y, z
            List<Float> sgX = IntoxAlgs.getSensorX(sgAccel);
            List<Float> sgY = IntoxAlgs.getSensorY(sgAccel);
            List<Float> sgZ = IntoxAlgs.getSensorZ(sgAccel);

            // Extract features
            double varX = IntoxAlgs.FloatAlgs.variance(sgX);
            double varY = IntoxAlgs.FloatAlgs.variance(sgY);
            double varZ = IntoxAlgs.FloatAlgs.variance(sgZ);

            // Only pack the data if it is valid
            if (varX >= 0) {
                map.put(VARIANCE_SGAME_X, Double.toString(varX));
            }
            if (varY >= 0) {
                map.put(VARIANCE_SGAME_Y, Double.toString(varY));
            }
            if (varZ >= 0) {
                map.put(VARIANCE_SGAME_Z, Double.toString(varZ));
            }
        }

    }
}
