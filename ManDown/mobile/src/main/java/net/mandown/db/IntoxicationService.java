package net.mandown.db;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import net.mandown.ml.PredictionException;
import net.mandown.ml.RealtimePrediction;
import net.mandown.sensors.SensorSample;

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
            // If the last timestamp is 0, get the current timestamp as the most recent and exit
            if (mLastTimestamp == 0) {
                mVarLock.lock();
                mLastTimestamp = System.currentTimeMillis();
                mIntoxicationLevel = 0f;
                mVarLock.unlock();
                return;
            }

            // Grab the recent data to get a prediction from
            List<SensorSample> accel = DBService.getAccelDataSince(mLastTimestamp);
            List<SensorSample> gyro = DBService.getGyroDataSince(mLastTimestamp);
            List<SensorSample> magnet = DBService.getMagnetDataSince(mLastTimestamp);
            List<Integer> reactions = DBService.getReactionTimesSince(mLastTimestamp);

            // Package the samples into a Map
            Map<String, String> classifySamples = new HashMap<String, String>();
            // TODO package times into map

            String resultLabel;
            float resultScore = 0f;

            // Create and connect the predictor
            try {
                predictor = new RealtimePrediction();
                predictor.connect();
                // Create a prediction from samples
                predictor.predict(classifySamples);
                resultLabel = predictor.getPredictedLabel();
                resultScore = predictor.getPredictedScore(resultLabel);
            } catch (PredictionException pe) {
                Log.e("IntoxicationService", "Error in prediction: " pe.getStackTrace());
                return;
            }

            mVarLock.lock();
            mIntoxicationLevel = resultScore;
            mVarLock.unlock();

            // TODO broadcast a notification to the user if above a certain level

        }
    }

}
