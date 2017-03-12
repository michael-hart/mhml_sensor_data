package net.mandown;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.support.v7.app.ActionBarActivity;
import net.mandown.db.DBService;
import net.mandown.games.GameMenuActivity;
import net.mandown.history.HistoryActivity;
import net.mandown.journal.JournalActivity;
import net.mandown.sensors.SensorService;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int REQUEST_PHONE_CALL = 1;

    private final String mDisclaimerText =
            "This app is distributed for the collection of accelerometer, gyroscope, and " +
            "magnetometer data over time. The use of the app's games will collect information " +
            "and store it online. The app does not accept responsibility for inaccurate readings " +
            "or results for intoxication levels.\n\nIf you wish to opt out, please uninstall " +
            "the application.";

    // Set up a new handler to update the home textview with number of DB entries every 100ms
    private final Handler mDbUpdateHandler = new Handler();
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



    private Toolbar toolbar;

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
            dialog.show();        }

        btnGamePlay = (ImageButton) findViewById(R.id.JoyStick);
        btnBeerGlass = (ImageButton) findViewById(R.id.BeerGlass);
    //    btnJournal  = (ImageButton) findViewById(R.id.Journal);
        btnHistory  = (ImageButton) findViewById(R.id.History);

        //adding a click listener

        btnGamePlay.setOnClickListener(this);
        btnBeerGlass.setOnClickListener(this);
     //   btnJournal.setOnClickListener(this);
        btnHistory.setOnClickListener(this);


        // Reset the database on initialisation
    //    DBService.startActionResetDatabase(this);


        // Start the sensor service to collect data
        startService(new Intent(this, SensorService.class));

        // Post event to handler to begin DB updates
        //mDbUpdateHandler.postDelayed(mUpdateDBTxt, 100);


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

}

