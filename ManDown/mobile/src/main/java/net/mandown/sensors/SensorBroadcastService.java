package net.mandown.sensors;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import net.mandown.R;

public class SensorBroadcastService extends Service implements SensorEventListener {

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
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

    private void publishResults(long ts, float x, float y, float z) {

        Intent intent = new Intent(getString(R.string.accel_broadcast));
        intent.putExtra("ts", ts);
        intent.putExtra("x", x);
        intent.putExtra("y", y);
        intent.putExtra("z", z);

        sendBroadcast(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // If sensor is unreliable, then just return
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
        {
            return;
        }

        publishResults(event.timestamp, event.values[0], event.values[1], event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
