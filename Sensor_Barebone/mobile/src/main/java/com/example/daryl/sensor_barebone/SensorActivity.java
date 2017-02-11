package com.example.daryl.sensor_barebone;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Bundle;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;

import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.util.StringTokenizer;


/**
 * Created by Daryl on 11/02/2017.
 */

public class SensorActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private TextView axex;
    private TextView axey;
    private TextView axez;
    private TextView mtime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, 5000000);
        //last integer is sampling period in 0.1 microseconds (apparently!), delibrately slow to test data storage
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, 5000000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


//    SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//    Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    @Override
    public void onSensorChanged(SensorEvent event) {

//        float x = event.values[0];
//        float y = event.values[1];
//        float z = event.values[2];
        String x = Float.toString(event.values[0]);
        String y = Float.toString(event.values[1]);
        String z = Float.toString(event.values[2]);
//        double total = Math.sqrt(x * x + y * y + z * z);


        axex =(TextView) findViewById(R.id.textView1);
        axex.setText("X "+x+"ms^2");
        axey=(TextView) findViewById(R.id.textView2);
        axey.setText("Y "+y+"ms^2");
        axez=(TextView) findViewById(R.id.textView3);
        axez.setText("Z "+z+"ms^2");

        //Get local system time
        mtime = (TextView) findViewById(R.id.textView5);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSSS z");
        String timestr = dateFormat.format(date);
        mtime.setText("Time: " + timestr);


        //Write to file
//        String filename = "myfile";
//        String string = timestr + ' ' + x + ' ' + y + ' ' + z;
//        FileOutputStream outputStream;
//
//        try {
//            outputStream = openFileOutput(filename, this.MODE_PRIVATE);
//            outputStream.write(string.getBytes());
//            outputStream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    public void start(View view) {
        mSensorManager.registerListener(this, mAccelerometer, 5000000);
    }

    public void stop(View view) {
        mSensorManager.unregisterListener(this);
    }

    public void history(View view) {
        Intent intent = new Intent(this, ShowHist.class);
        startActivity(intent);
    }

}