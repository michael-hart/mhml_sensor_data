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
    private static final float DRUNK_LEVEL = 2.0f; // Class 2 drunk or higher is too drunk

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


    /**
     * Class used for client Binder. Use threads with locks for safe multi-threaded access.
     */
    public class IntoxBinder extends Binder {
        IntoxicationService getService() {
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

            // Grab the recent data to get a prediction from
            List<SensorSample> accel = DBService.GetMostRecentAccel();
            List<SensorSample> gyro = DBService.GetMostRecentGyro();
            List<SensorSample> magnet = DBService.GetMostRecentMagn();
            List<Long> reactions = DBService.GetMostRecentReactionTime();

            List<Float[]> intoxLevels = new ArrayList<>();

            // Create the predictor object
            RealtimePrediction predictor;
            try {
                predictor = new RealtimePrediction();
                predictor.connect();
            } catch (PredictionException pe) {
                Log.e("IntoxicationService", "Error in ML connection: " + pe.getStackTrace());
                return;
            }

            // Initialise to prevent errors during loop
            List<SensorSample> current = new ArrayList<>();

            // Check three sensor arrays with readings
            for (int i = 0; i < 3; i++) {
                String[] xyz = {"", "", ""};
                switch(i) {
                    case 0:
                        current = accel;
                        xyz[0] = "ax";
                        xyz[1] = "ay";
                        xyz[2] = "az";
                        break;
                    case 1:
                        current = gyro;
                        xyz[0] = "gx";
                        xyz[1] = "gy";
                        xyz[2] = "gz";
                        break;
                    case 2:
                        current = magnet;
                        xyz[0] = "mx";
                        xyz[1] = "my";
                        xyz[2] = "mz";
                        break;
                }
                for (SensorSample ss : current) {
                    SensorSample recent = null;
                    for (SensorSample dSs : accel) {
                        // Check the timestamp difference
                        long dTS = ss.mTimestamp - dSs.mTimestamp;
                        // If the sample is too recent, we are too far in the last, so quit
                        if (dTS < 950) {
                            break;
                        }
                        // If the sample is 1s older, +/- 50ms, use as a sample
                        if ((dTS < 1050) && (dTS >= 950)) {
                            recent = dSs;
                            break;
                        }
                    }

                    // If recent isn't null, we found a sample of the correct age
                    if (recent != null) {
                        Map<String, String> classifySamples = new HashMap<String, String>();
                        classifySamples.put(xyz[0], Float.toString(ss.mX - recent.mX));
                        classifySamples.put(xyz[1], Float.toString(ss.mY - recent.mY));
                        classifySamples.put(xyz[2], Float.toString(ss.mZ - recent.mZ));

                        try {
                            predictor.predict(classifySamples);
                            String intox = predictor.getPredictedLabel();
                            float conf = predictor.getPredictedScore(intox);
                            Float[] result = {Float.parseFloat(intox), conf};
                            intoxLevels.add(result);
                        } catch (PredictionException pe) {
                            Log.e("IntoxicationService", "Error in ML connection: " +
                                    pe.getStackTrace());
                        }
                    }
                }
            }

            for (Long l : reactions) {
                Map<String, String> classifySamples = new HashMap<>();
                classifySamples.put("rt", Double.toString(l));

                try {
                    predictor.predict(classifySamples);
                    String intox = predictor.getPredictedLabel();
                    float conf = predictor.getPredictedScore(intox);
                    Float[] result = {Float.parseFloat(intox), conf};
                    intoxLevels.add(result);
                } catch (PredictionException pe) {
                    Log.e("IntoxicationService", "Error in ML connection: " + pe.getStackTrace());
                }
            }

            // Based on the results just obtained, calculate a final weight mean
            if (intoxLevels.size() == 0) {
                Log.w("IntoxicationService", "No results during intoxication check!");
                return;
            }

            float finalIntox = 0f;
            float intoxWeights = 0f;
            for (Float[] result : intoxLevels) {
                finalIntox += result[0] * result[1];
                intoxWeights += result[1];
            }
            finalIntox = finalIntox / intoxWeights;

            // Record data in member variables
            mVarLock.lock();
            mIntoxicationLevel = finalIntox;
            mLastTimestamp = System.currentTimeMillis();
            mVarLock.unlock();

            // Insert new value into database
            DBService.startActionPutML(getApplicationContext(), Float.toString(finalIntox));

            // Send a broadcast to any other listener in the system
            Intent intent = new Intent(getString(R.string.intox_broadcast));
            intent.putExtra("ml", finalIntox);
            intent.putExtra("ts", mLastTimestamp);
            sendBroadcast(intent);

            // Create a notification if too drunk
            NotificationManager nm = (NotificationManager) getSystemService(
                    Service.NOTIFICATION_SERVICE);
            Notification notif = (new Notification.Builder(getApplicationContext()))
                    .setContentTitle("ManDown Intoxication Alert")
                    .setContentText("ManDown has detected that you are too intoxicated. " +
                                    "Please take a break for your own safety!")
                    .build();
            nm.notify(1, notif);

        }
    }

}
