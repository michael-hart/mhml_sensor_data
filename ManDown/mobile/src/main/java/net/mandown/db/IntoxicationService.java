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

            // Grab the recent data to get a prediction from
            List<Long> reactions = DBService.getMostRecentReactionTime();
            int score = DBService.getMostRecentWhackABeerScore();
            if (reactions == null) {
                Log.w("IntoxicationService", "Early return due to null value");
                return;
            }

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
            List<SensorSample> current = null;

            // Calculate a mean over all the samples in reactions
            long meanRT = 0;
            for (Long rt : reactions) {
                meanRT += rt;
            }
            meanRT = meanRT / reactions.size();

            Map<String, String> classifySamples = new HashMap<>();
            classifySamples.put("score", Integer.toString(score));
            classifySamples.put("rt", Long.toString(meanRT));

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
    }
}
