package net.mandown;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
//import android.support.v7.app.AlertDialog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.support.v7.app.ActionBarActivity;

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
import net.mandown.db.IntoxicationService;
import net.mandown.games.GameMenuActivity;
import net.mandown.history.HistoryActivity;
import net.mandown.journal.JournalActivity;
import net.mandown.sensors.SensorService;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import net.mandown.sensors.SensorSample;
import net.mandown.sensors.SensorService;
import net.mandown.sensors.SensorType;

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
    private Toolbar toolbar;
    // Set up a new handler to update the home textview with number of DB entries every 100ms
//    private final Handler mDbUpdateHandler = new Handler();
//    private Runnable mUpdateDBTxt = new Runnable() {
//        @Override
//        public void run() {
//            if (DBService.sInstance != null) {
//                TextView txtDbInfo = (TextView) findViewById(R.id.txtDbView);
//                txtDbInfo.setText(String.format("%d accel data readings",
//                        DBService.sInstance.getNumAccelReadings()));
//                mDbUpdateHandler.postDelayed(mUpdateDBTxt, 100);
//            }
//        }
//    };

    private ImageButton btnGamePlay;
    private ImageButton btnBeerGlass;
 //   private ImageButton btnJournal;
    private ImageButton btnHistory;




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

    private BroadcastReceiver mIntoxReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            float intoxLevel = intent.getFloatExtra("ml", -1);
            if (intoxLevel >= 0) {
                update_drunk_level(Math.round(intoxLevel));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setting the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        //getting toolbar
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);


        if (isFirstTime()) {
            AlertDialog dialog = (new AlertDialog.Builder(this))
                    .setTitle("ManDown Disclaimer")
                    .setMessage(mDisclaimerText)
                    .setPositiveButton("I understand", null)
                    .create();
            dialog.show();
        }

        btnGamePlay = (ImageButton) findViewById(R.id.JoyStick);
        btnBeerGlass = (ImageButton) findViewById(R.id.BeerGlass);
        btnHistory = (ImageButton) findViewById(R.id.History);

        // Add all click listeners
        btnGamePlay.setOnClickListener(this);
        btnBeerGlass.setOnClickListener(this);
        btnHistory.setOnClickListener(this);

        // Start the sensor service to collect data
        if (getApplicationContext() != null) {
            startService(new Intent(this, SensorService.class));
            startService(new Intent(this, IntoxicationService.class));
        }

        update_drunk_level(0);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        beerview = (TextView) findViewById(R.id.watchtext);
    }

    private boolean isFirstTime()
    {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("RanBefore", false);
        if (!ranBefore) {
            // first time
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("RanBefore", true);
            editor.commit();
        }
        return !ranBefore;


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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu1:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_menu2:
                AlertDialog dialog = (new AlertDialog.Builder(this))
                .setTitle("ManDown Disclaimer")
                .setMessage(mDisclaimerText)
                .setPositiveButton("I understand", null)
                .create();
        dialog.show();
                return true;
            case R.id.action_menu3:
                startwatchaccel();
                return true;
            case R.id.emergency:
                new AlertDialog.Builder(this)
                        .setTitle("Contact Emergency Help")
                        .setMessage("Are you sure you want to send a distress call")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with action

                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "I'm Drunk"));

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
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

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
        registerReceiver(mIntoxReceiver, new IntentFilter(getString(R.string.intox_broadcast)));
        Log.i("RConnected","ResumeConnected!");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        unregisterReceiver(mIntoxReceiver);
        mGoogleApiClient.disconnect();
        Log.i("dis","Disconnected");
    }


    private boolean watchbool = false;
    //Tell watch to start measuring
    public void startwatchaccel(){
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
                    DBService.startPutActionWatchAccel(getApplicationContext(), watchCombinedData, SensorType.ACCELEROMETER);
                }

            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }
}

