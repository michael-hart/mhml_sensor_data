package net.mandown.sensors;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import net.mandown.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SensorBroadcast extends Service implements SensorEventListener {

    //Sensors
    private static SensorManager mSensorManager;
    private static Sensor mAccelerometer;

    // Member variables
    private int mPollRate, mPollPeriod;
    private boolean mRunning = false;
    private Lock mVarLock;

    // Define constants for use in Service
    public static final int DEFAULT_POLL_RATE_US = 100000; // 100ms
    public static final int DEFAULT_POLL_PERIOD_S = 5; // 5s
   // public static final int DEFAULT_POLL_INTERVAL_S = 600; // 10 minutes

    private static List<SensorSample> accelSamples;

    // Constructor replacement method
    @Override
    public void onCreate() {
        super.onCreate();
        // Create member objects
        mVarLock = new ReentrantLock();
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.i("create", "CREATED");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.i("SensorService", "Received start id " + startId + ": " + intent);
        //Log.i("started", "sensor STARTED");
        mVarLock.lock();
        mPollRate = intent.getIntExtra(getString(R.string.sensor_poll_rate),
                DEFAULT_POLL_RATE_US);
       // mPollInterval = intent.getIntExtra(getString(R.string.sensor_poll_interval),
       //         DEFAULT_POLL_INTERVAL_S);
        mPollPeriod = intent.getIntExtra(getString(R.string.sensor_poll_period),
                DEFAULT_POLL_PERIOD_S);

        // Create new objects
        accelSamples = new ArrayList<SensorSample>((int)(mPollPeriod / mPollRate));

        mVarLock.unlock();
        mSensorManager.registerListener(this, mAccelerometer, mPollPeriod);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this, mAccelerometer);
    }

    private void publishResults(long time, float x, float y, float z) {

        Intent intent = new Intent("accel");
        intent.putExtra("t", time);
        intent.putExtra("x", x);
        intent.putExtra("y", y);
        intent.putExtra("z", z);

        sendBroadcast(intent);
       // Log.i("broadcast", "BROADCAST " + Float.toString(x) + ' ' + Float.toString(y));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
      //  Log.i("well well well", Float.toString(event.values[0]) + ' ' + Float.toString(event.values[1]));
        // If sensor is unreliable, then just return
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
        {
            //Log.i("sensor error", "ERRORRRRRR");
            return;
        }

        accelSamples.add(new SensorSample(event.timestamp, event.values[0], event.values[1], event.values[2]));
       publishResults(event.timestamp, event.values[0], event.values[1], event.values[2]);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

//    public static List<SensorSample> getMeasuredValues() {
//        pausesensing();
//        return accelSamples;
//    }
//
//    private void pausesensing() {
//        mSensorManager.unregisterListener(this, mAccelerometer);
//    }

}
