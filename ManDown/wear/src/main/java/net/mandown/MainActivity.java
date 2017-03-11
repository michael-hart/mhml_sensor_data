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
    private static final String WINE_KEY = "net.mandown.key.wine";
    private static final String COCKTAIL_KEY = "net.mandown.key.cocktail";
    private static final String SHOT_KEY = "net.mandown.key.shot";

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

    private String beertext = "beer";
    private String winetext = "wine";
    private String cocktailtext = "cocktail";
    private String shottext = "shot";

    //Send message
    public void drankbeer(View v) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/beer");
        putDataMapReq.getDataMap().putString(BEER_KEY, beertext);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        putDataReq.setUrgent();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        Log.d("beer","SENT BEER!");
        if (beertext == "beer")
        {
            beertext = "notbeer";
        }
        else
        {
            beertext = "beer";
        }
    }

//    private void msgBeer(View v) {
//        Wearable.MessageApi.sendMessage(googleApiClient, transcriptionNodeId,
//                VOICE_TRANSCRIPTION_MESSAGE_PATH, voiceData).setResultCallback(
//                new ResultCallback() {
//                    @Override
//                    public void onResult(SendMessageResult sendMessageResult) {
//                        if (!sendMessageResult.getStatus().isSuccess()) {
//                            // Failed to send message
//                        }
//                    }
//                }
//        );
//    }

    public void drankwine(View v) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/wine");
        putDataMapReq.getDataMap().putString(WINE_KEY, winetext);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        putDataReq.setUrgent();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        Log.d("wine","SENT WINE!");
        if (winetext == "wine")
        {
            winetext = "notwine";
        }
        else
        {
            winetext = "wine";
        }
    }

    public void drankcocktail(View v) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/cocktail");
        putDataMapReq.getDataMap().putString(COCKTAIL_KEY, cocktailtext);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        putDataReq.setUrgent();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        Log.d("cocktail","SENT COCKTAIL!");
        if (cocktailtext == "cocktail")
        {
            cocktailtext = "notcocktail";
        }
        else
        {
            cocktailtext = "cocktail";
        }
    }

    public void drankshot(View v) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/shot");
        putDataMapReq.getDataMap().putString(SHOT_KEY, shottext);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        putDataReq.setUrgent();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        Log.d("shot","SENT SHOT!");
        if (shottext == "shot")
        {
            shottext = "notshot";
        }
        else
        {
            shottext = "shot";
        }
    }
}
