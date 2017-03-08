package net.mandown.sensors;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class GetSensor extends Service implements SensorEventListener {

//    public float acc_x = 0.0f;
//    public float acc_y = 0.0f;

    //Sensors
    private static SensorManager mSensorManager;
    private static Sensor mAccelerometer;

    // Constructor replacement method
    @Override
    public void onCreate() {
        super.onCreate();
        // Create member objects
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.i("create", "CREATED");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.i("SensorService", "Received start id " + startId + ": " + intent);
        //Log.i("started", "sensor STARTED");
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

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

    private void publishResults(float x, float y, float z) {

        Intent intent = new Intent("accel");
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
            Log.i("sensor error", "ERRORRRRRR");
            return;
        }

        publishResults(event.values[0], event.values[1], event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



}
