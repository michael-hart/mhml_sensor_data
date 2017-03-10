package net.mandown;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import net.mandown.sensors.SensorBroadcast;
import net.mandown.R;

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
    private static final String BEER_KEY = "net.mandown.key.beer";

    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                axview = (TextView) stub.findViewById(R.id.acc_reading);
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
            //Log.i("broadreceived",Float.toString(acc_x));
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

        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.stopService(intent);
        this.unregisterReceiver(br);

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
    public void onDataChanged(DataEventBuffer dataEvents) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause){
        Log.d("Connection suspended", "onConnectionSuspended: " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d("connect failed", "onConnectionFailed: " + result);
    }

    //Send message
    public void drankbeer(View v) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/beer");
        putDataMapReq.getDataMap().putString(BEER_KEY, "Beer!");
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        putDataReq.setUrgent();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        Log.d("beer","SENT BEER!");
    }
}
