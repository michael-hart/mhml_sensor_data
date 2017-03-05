package net.mandown.sensors;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import net.mandown.R;
import net.mandown.db.DBService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class SensorService extends Service implements AlarmManager.OnAlarmListener {

    // Define constants for use in Service
//    public static final int DEFAULT_POLL_RATE_US = 100000; // 100ms
    public static final int DEFAULT_POLL_RATE_US = 100; // 100us
    public static final int DEFAULT_POLL_PERIOD_S = 2; // 30s
    public static final int DEFAULT_POLL_INTERVAL_S = 1; // 10 minutes

    // Binder given to clients
    private final IBinder mBinder = new SensorBinder();

    // Member variables
    private int mPollRate, mPollPeriod, mPollInterval;
    private boolean mRunning = false;
    private Lock mVarLock;
    private AlarmManager mAlarmManager;

    // Sensor member variables
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mMagnetometer;

    // Constructor replacement method
    @Override
    public void onCreate() {
        super.onCreate();
        // Create member objects
        mVarLock = new ReentrantLock();
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Get alarm manager from context
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("SensorService", "Received start id " + startId + ": " + intent);

        // Check if there are any extras in the intent
        mVarLock.lock();
        mPollRate = intent.getIntExtra(getString(R.string.sensor_poll_rate),
                DEFAULT_POLL_RATE_US);
        mPollInterval = intent.getIntExtra(getString(R.string.sensor_poll_interval),
                DEFAULT_POLL_INTERVAL_S);
        mPollPeriod = intent.getIntExtra(getString(R.string.sensor_poll_period),
                DEFAULT_POLL_PERIOD_S);
        mVarLock.unlock();

        // Start the data collection
        startPollingNow();

        // Return not sticky, as service will restart itself after an interval using alarm
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mVarLock.lock();
        mRunning = false;
        mVarLock.unlock();
    }

    /**
     * Class used for client Binder. Use threads with locks for safe multi-threaded access.
     */
    public class SensorBinder extends Binder {
        SensorService getService() {
            return SensorService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onAlarm()
    {
        startPollingNow();
    }

    /* Threaded runnable to take data */
    private class SensorDataCollector implements Runnable, SensorEventListener {

        private List<AccelerometerSample> accelSamples;

        @Override
        public void run() {
            // Lock variables for thread setup
            mVarLock.lock();
            long pollPeriod = mPollPeriod * 1000;
            long startTime = SystemClock.elapsedRealtime();
            int pollInterval = mPollInterval;
            // Attach listeners to objects
            mSensorManager.registerListener(this, mAccelerometer, mPollRate);
            // Set state to recording
            mRunning = true;

            // Create new objects
            accelSamples = new ArrayList<AccelerometerSample>((int)(pollPeriod / mPollRate));

            // Critical work done, so unlock
            mVarLock.unlock();

            // Repeatedly wait until our work is done
            while (SystemClock.elapsedRealtime() - startTime < pollPeriod) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Log.e("SensorDataCollector", "Interrupted while waiting for execution: "
                                                 + ie.toString());
                }
            }

            mVarLock.lock();
            // Reset running variable as not taking data
            mRunning = false;
            // Remove listeners
            mSensorManager.unregisterListener(this, mAccelerometer);
            mVarLock.unlock();

            DBService.startActionPutAccelList(getApplicationContext(), accelSamples);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (!mRunning) return;
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    accelSamples.add(new AccelerometerSample(event.timestamp, event.values[0],
                                                             event.values[1], event.values[2]));
                    break;
                default:
                    Log.w("SensorDataCollector", "Unknown sensor type received");
            }
        }
    }

    /* Methods for clients to use */

    /**
     * Set the poll rate to use on the next sampling period
     * @param pollMicro - Time in microseconds between each reading
     */
    void setPollRate(int pollMicro) {
        mVarLock.lock();
        mPollRate = pollMicro;
        mVarLock.unlock();
    }

    int getPollRate() {
        return mPollRate;
    }

    void setPollPeriod(int pollPeriod) {
        mVarLock.lock();
        mPollPeriod = pollPeriod;
        mVarLock.unlock();
    }

    int getPollPeriod() {
        return mPollPeriod;
    }

    void setPollInterval(int pollInterval) {
        mVarLock.lock();
        mPollInterval = pollInterval;
        mVarLock.unlock();
    }

    int getPollInterval() {
        return mPollInterval;
    }

    void startPollingNow() {
        mVarLock.lock();
        // Check and remove pre-existing alarms
         mAlarmManager.cancel(this);

        // Set an alarm to trigger service to restart
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                          SystemClock.elapsedRealtime() + (mPollPeriod + mPollInterval) * 1000,
                          "SensorService Start Collection", this, null);

        // Only start running if not already running
        if (!mRunning) {
            // Spawn a thread to do the work
            (new Thread(new SensorDataCollector())).start();
        }

        mVarLock.unlock();
    }

}
