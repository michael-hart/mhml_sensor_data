package net.mandown;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import net.mandown.sensors.GetSensor;
import net.mandown.R;

public class MainActivity extends Activity {
    private TextView mTextView;
    public TextView axview;
    private float acc_x = 0.0f;
    private float acc_y = 0.0f;
    private float acc_z = 0.0f;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                //mTextView = (TextView) stub.findViewById(R.id.text);
                axview = (TextView) stub.findViewById(R.id.acc_reading);
                axview.setText("hello");
            }
        });


        intent = new Intent(this, GetSensor.class);

        //Start sensors
//        this.startService(intent);
//       // Log.i("intent", "Service Started");
//        this.registerReceiver(br, new IntentFilter("accel"));
       // Log.i("receiver", "Receiver Started");
    }

    private BroadcastReceiver br = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("broadreceived",Float.toString(acc_x));
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                acc_x = bundle.getFloat("x");
                acc_y = bundle.getFloat("y");
                acc_z = bundle.getFloat("z");

                ((TextView) findViewById(R.id.acc_reading)).setText(Float.toString(acc_x) + ' ' + Float.toString(acc_y) + ' ' + Float.toString(acc_z));

            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        this.startService(intent);
        this.registerReceiver(br, new IntentFilter("accel"));
    }

    @Override
    public void onPause() {
        super.onPause();
        this.stopService(intent);
        this.unregisterReceiver(br);
    }

//    @Override
//    public void onStop() {
//        super.onStop();
//        this.stopService(intent);
//        this.unregisterReceiver(br);
//    }
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        mTextView = (TextView) findViewById(R.id.text);
//    }
}
