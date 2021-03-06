package net.mandown;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.IntegerRes;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import net.mandown.sensors.SensorBroadcast;
import net.mandown.R;
import net.mandown.sensors.SensorSample;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class MainActivity extends Activity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private TextView mTextView;
    public TextView axview;
    private float acc_x = 0.0f;
    private float acc_y = 0.0f;
    private float acc_z = 0.0f;
    private Intent intent;

    //For watch-phone communication
    private static final long CONNECTION_TIME_OUT_MS = 100;
    private static final String WATCH_RX_KEY = "net.mandown.key.watchrx";
    private static final String WATCH_TX_FLOAT_KEY = "net.mandown.key.watchtxfloat";
    private static final String WATCH_TX_LONG_KEY = "net.mandown.key.watchtxlong";
    private static final String INTOX_KEY = "net.mandown.key.intox";

    private GoogleApiClient mGoogleApiClient;

    public static final int DEFAULT_POLL_RATE_US = 100000; // 100ms
    public static final int DEFAULT_POLL_PERIOD_US = 12000000; // 12s
    private List<Float> WatchAccelValues;
    private List<Long> WatchTimeValues;
    private Lock mVarLock;
    private int count = 0;

    private ImageButton btnBeerGlass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mVarLock = new ReentrantLock();
                mTextView = (TextView) stub.findViewById(R.id.text);
                axview = (TextView) stub.findViewById(R.id.acc_reading);
                btnBeerGlass = (ImageButton) stub.findViewById(R.id.drinkindicator);
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        intent = new Intent(this, SensorBroadcast.class);

        //Start sensors
//        this.startService(intent);
//       // Log.i("intent", "Service Started");
//        this.registerReceiver(br, new IntentFilter("accel"));
       // Log.i("receiver", "Receiver Started");
    }

    private BroadcastReceiver br = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("broadreceived",Integer.toString(++count));
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
//                acc_x = bundle.getFloat("x");
//                acc_y = bundle.getFloat("y");
//                acc_z = bundle.getFloat("z");

                //((TextView) findViewById(R.id.acc_reading)).setText(Float.toString(acc_x) + ' ' + Float.toString(acc_y) + ' ' + Float.toString(acc_z));

                //Assumption here is since poll is APPROX 10ms or 100Hz (which is still much too fast, body motion up to 20Hz => 40Hz polling enough)
                //Therefore 10ms is a long enough time that the broadcast is not flooded and values can be recorded and stored before the next sensor sample
                WatchTimeValues.add(bundle.getLong("t")); //Get Timestamp
                //Get x, y, z values
                WatchAccelValues.add(bundle.getFloat("x"));
                WatchAccelValues.add(bundle.getFloat("y"));
                WatchAccelValues.add(bundle.getFloat("z"));
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        //this.startService(intent);
        //this.registerReceiver(br, new IntentFilter("accel"));

        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        //this.stopService(intent);
        //this.unregisterReceiver(br);

        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
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

    //Communications code below
    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Log.d("connected","WEARABLE CONNECTED!");
    }

    @Override
    public void onConnectionSuspended(int cause){
        Log.d("Connection suspended", "onConnectionSuspended: " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d("connect failed", "onConnectionFailed: " + result);
    }

    //Listen for start signal
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        //Log.i("data","data CHANGED!");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/watchaccel") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    ((TextView) findViewById(R.id.phonetrigger)).setText(Boolean.toString(dataMap.getBoolean(WATCH_RX_KEY)));

                    //Log.d("datachanged", "SET BOOL TO STR");
                    mVarLock.lock();
                    long startTime = SystemClock.elapsedRealtime();
                    WatchAccelValues = new ArrayList<Float>((int)(3*DEFAULT_POLL_PERIOD_US / DEFAULT_POLL_RATE_US));
                    WatchTimeValues = new ArrayList<Long>((int)(DEFAULT_POLL_PERIOD_US / DEFAULT_POLL_RATE_US));
                    long pollPeriod = DEFAULT_POLL_PERIOD_US / 1000;
                    mVarLock.unlock();
                    //Log.d("datachanged", Long.toString(startTime));
                    startService(intent);
                    //Log.d("datachanged", "STARTED BACKGROUND SVC");
                    this.registerReceiver(br, new IntentFilter("accel"));
                    //Log.d("datachanged", "REGISTERED RECEIVER");

                    Timer timer1 = new Timer();
                    timer1.schedule(new stopTask(), pollPeriod);
                }/////////////////////////////////////////////////////////////////////////////////////////////////////////////
                else if (item.getUri().getPath().compareTo("/intoxication") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    int drunk_level = dataMap.getInt(INTOX_KEY);
                    update_drunk_level(drunk_level);
                }

            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    private void update_drunk_level(int drunk_level) {
        if (drunk_level == 0) {
            btnBeerGlass.setImageResource(R.drawable.empty_beer_glass);
            axview.setText("Sober");
        } else if (drunk_level == 1) {
            btnBeerGlass.setImageResource(R.drawable.glass_beer);
            axview.setText("Tipsy");
        } else if (drunk_level == 2) {
            btnBeerGlass.setImageResource(R.drawable.full_glass_beer);
            axview.setText("Drunk!");
        }
    }

    //SOS button
    public void callSOS(View v) {
        startActivity(new Intent(this, CallSOS.class));
    }

    //Drinks button
    public void inputDrink(View v) {
        startActivity(new Intent(this, InputDrinks.class));
    }

    private void sendmeasresults() {
        float[] floatArr = new float[WatchAccelValues.size()];
        long[] longArr = new long[WatchTimeValues.size()];
        int i = 0;
        //Convert java object types to primitives
        for (Float f : WatchAccelValues) {
            floatArr[i++] = (f != null ? f : Float.NaN); //Ensure that Float is not pointing to Null before passing in
        }
        i = 0; //Reset counter
        for (Long l : WatchTimeValues) {
            longArr[i++] = (l != null ? l : 0); //Ensure that Long is not pointing to Null before passing in, time epoch should never be 0 so it's safe
        }

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/watchdata");
        putDataMapReq.getDataMap().putFloatArray(WATCH_TX_FLOAT_KEY, floatArr);
        putDataMapReq.getDataMap().putLongArray(WATCH_TX_LONG_KEY, longArr);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        putDataReq.setUrgent();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        //Log.d("sendmeas","SENT ACCEL RESULTS!");
//        ((TextView) findViewById(R.id.phonetrigger)).setText(Integer.toString(WatchTimeValues.size())+ ' ' + Integer.toString(WatchAccelValues.size()));
    }

//    private float[] floatListtoArr() {
//        float[] floatArr = new float[WatchAccelValues.size()];
//        int i = 0;
//
//        for (Float f : WatchAccelValues) {
//            floatArr[i++] = (f != null ? f : Float.NaN); //Ensure that Float is not pointing to Null before passing in
//        }
//
//        return floatArr;
//    }
//
//    private long[] longListtoArr() {
//        long[] longArr = new long[WatchTimeValues.size()];
//        int i = 0;
//
//        for (Long l : WatchTimeValues) {
//            longArr[i++] = (l != null ? l : 0); //Ensure that Long is not pointing to Null before passing in, time epoch should never be 0 so it's safe
//        }
//
//        return longArr;
//    }

    class stopTask extends TimerTask {
        @Override
        public void run() {
            //Log.d("datachanged", "STOPPED SLEEP");
            stopService(intent);
            //Log.d("datachanged", "STOPPED SERVICE");
            unregisterReceiver(br);
            //Log.d("datachanged", "STOPPED BROADCAST");
            sendmeasresults();
           // Log.d("datachanged", "FINISHED FUNCTION");
        }
    }
}
