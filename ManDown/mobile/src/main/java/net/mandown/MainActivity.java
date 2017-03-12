package net.mandown;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
//import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import net.mandown.db.DBService;
import net.mandown.games.GameMenuActivity;
import net.mandown.history.HistoryActivity;
import net.mandown.journal.JournalActivity;
import net.mandown.sensors.SensorService;

import net.mandown.R;

import net.mandown.sensors.SensorSample;
import net.mandown.sensors.SensorService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
		DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final int REQUEST_PHONE_CALL = 1;

    private final String mDisclaimerText =
            "This app is distributed for the collection of accelerometer, gyroscope, and " +
            "magnetometer data over time. The use of the app's games will collect information " +
            "and store it online. The app does not accept responsibility for inaccurate readings " +
            "or results for intoxication levels.\n\nIf you wish to opt out, please uninstall " +
            "the application.";

    // Set up a new handler to update the home textview with number of DB entries every 100ms
    private final Handler mDbUpdateHandler = new Handler();
    private Runnable mUpdateDBTxt = new Runnable() {
        @Override
        public void run() {
            if (DBService.sInstance != null) {
                TextView txtDbInfo = (TextView) findViewById(R.id.txtDbView);
                txtDbInfo.setText(String.format("%d accel data readings",
                        DBService.sInstance.getNumAccelReadings()));
                mDbUpdateHandler.postDelayed(mUpdateDBTxt, 100);
            }
        }
    };

    private ImageButton btnGamePlay;
    private ImageButton btnBeerGlass;
 //   private ImageButton btnJournal;
    private ImageButton btnHistory;
    private ImageButton btnOptions;
    private ImageButton btnEmergency;

    //Communication variables
    private GoogleApiClient mGoogleApiClient;
    private static final String BEER_KEY = "net.mandown.key.beer";
    private static final String WINE_KEY = "net.mandown.key.wine";
    private static final String COCKTAIL_KEY = "net.mandown.key.cocktail";
    private static final String SHOT_KEY = "net.mandown.key.shot";
    private static final String WATCH_RX_KEY = "net.mandown.key.watchrx";
    private static final String WATCH_TX_FLOAT_KEY = "net.mandown.key.watchtxfloat";
    private static final String WATCH_TX_LONG_KEY = "net.mandown.key.watchtxlong";
    private static final long CONNECTION_TIME_OUT_MS = 100;
    private TextView beerview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setting the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //getting the button

        btnGamePlay = (ImageButton) findViewById(R.id.JoyStick);
        btnBeerGlass = (ImageButton) findViewById(R.id.BeerGlass);
    //    btnJournal  = (ImageButton) findViewById(R.id.Journal);
        btnHistory  = (ImageButton) findViewById(R.id.History);
        btnOptions  = (ImageButton) findViewById(R.id.OptionLevers);
        btnEmergency= (ImageButton) findViewById(R.id.DrunkMan);

        //adding a click listener

        btnGamePlay.setOnClickListener(this);
        btnBeerGlass.setOnClickListener(this);
     //   btnJournal.setOnClickListener(this);
        btnHistory.setOnClickListener(this);
        btnOptions.setOnClickListener(this);
        btnEmergency.setOnClickListener(this);

        // Reset the database on initialisation
    //    DBService.startActionResetDatabase(this);

        // Start the sensor service to collect data
        if (this != null) {
            startService(new Intent(this, SensorService.class));
        }
        // Post event to handler to begin DB updates
        mDbUpdateHandler.postDelayed(mUpdateDBTxt, 100);

        update_drunk_level(0);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        beerview = (TextView) findViewById(R.id.watchtext);
    }

    private void update_drunk_level(int d_lvl){

        int drunk_level = d_lvl;

        if(drunk_level==0){
            btnBeerGlass.setImageResource(R.drawable.empty_beer_glass);
        }else if(drunk_level==1){
            btnBeerGlass.setImageResource(R.drawable.glass_beer);
        } else if(drunk_level==2){
            btnBeerGlass.setImageResource(R.drawable.full_glass_beer);
        }
    }

    @Override
    public void onClick(View v) {

        if (v == btnGamePlay) {
            //starting game menu activity
            startActivity(new Intent(this, GameMenuActivity.class));
        }
        if (v == btnHistory) {
            //starting game activity
            startActivity(new Intent(this, HistoryActivity.class));
        }
//        if (v == btnJournal) {
//            //starting game activity
//            startActivity(new Intent(this, JournalActivity.class));
//        }
     //   if (v == btnBeerGlass) {
            //starting sensor activity
          //  DBService.startActionPutPassive(getApplicationContext(), 0, 0, 0);
     //   }
        if (v == btnEmergency) {
            new AlertDialog.Builder(this)
                    .setTitle("Contact Emergency Help")
                    .setMessage("Are you sure you want to send a distress call")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with action

                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "12345"));

                            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
                            }
                            else
                            {
                                startActivity(intent);
                            }

                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        // Create a disclaimer window using AlertDialog
        AlertDialog dialog = (new AlertDialog.Builder(this))
                .setTitle("ManDown Disclaimer")
                .setMessage(mDisclaimerText)
                .setPositiveButton("I understand", null)
                .create();
        dialog.show();
    }
	
	public void goJournal(View view){
        Intent intent = new Intent(this, JournalActivity.class);
        startActivity(intent);
	}

    //Communications code below
    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Log.i("listener Connected","Mobile Connected!");
    }

    @Override
    public void onConnectionSuspended(int cause){
        Log.d("Connection suspended", "onConnectionSuspended: " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d("connect failed", "onConnectionFailed: " + result);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        Log.i("RConnected","ResumeConnected!");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        Log.i("dis","Disconnected");
    }


    private boolean watchbool = false;
    //Tell watch to start measuring
    public void startwatchaccel(View v){
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/watchaccel");
        putDataMapReq.getDataMap().putBoolean(WATCH_RX_KEY, watchbool);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        putDataReq.setUrgent();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        Log.d("watchaccel","TRIGGER WATCH!");
        //Start sensorservice here instead of constantly starting in background and make it last approx 11seconds
        watchbool = !watchbool;
    }

    //In case of for user input from watch
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.i("data","data CHANGED!");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/beer") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    ((TextView) findViewById(R.id.watchtext)).setText(dataMap.getString(BEER_KEY));
                    //dataMap.remove(BEER_KEY); //Delete item so the next write is a "new" entry to trigger onDataChanged
                    dataMap.putString(BEER_KEY, "unknown");
                }
                else if (item.getUri().getPath().compareTo("/wine") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    ((TextView) findViewById(R.id.watchtext)).setText(dataMap.getString(WINE_KEY));
                    //dataMap.remove(WINE_KEY);
                    dataMap.putString(WINE_KEY, "unknown");
                }
                else if (item.getUri().getPath().compareTo("/cocktail") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    ((TextView) findViewById(R.id.watchtext)).setText(dataMap.getString(COCKTAIL_KEY));
                    dataMap.remove(COCKTAIL_KEY);
                }
                else if (item.getUri().getPath().compareTo("/shot") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    ((TextView) findViewById(R.id.watchtext)).setText(dataMap.getString(SHOT_KEY));
                    dataMap.remove(SHOT_KEY);
                }
                else if (item.getUri().getPath().compareTo("/watchdata") == 0) {
                    Log.d("datachanged", "GOT WATCH DATA");
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    long[] watchTimeStamps = dataMap.getLongArray(WATCH_TX_LONG_KEY);
                    float[] watchAccelValues = dataMap.getFloatArray(WATCH_TX_FLOAT_KEY);

                    ((TextView) findViewById(R.id.watchsamplenum)).setText(Integer.toString(watchTimeStamps.length) + ' ' + Integer.toString(watchAccelValues.length));

                    List<SensorSample> watchCombinedData = new ArrayList<SensorSample>(watchTimeStamps.length);

                    for (int i=0; i<watchTimeStamps.length; i++) {
                        watchCombinedData.add(new SensorSample(watchTimeStamps[i], watchAccelValues[3*i], watchAccelValues[3*i+1], watchAccelValues[3*i+2]));
                    }
                    Log.d("datachanged", Integer.toString(watchCombinedData.size()));
                    //Send watch data to database here
                }

            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }
}

