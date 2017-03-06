package net.mandown.journal;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import net.mandown.R;
import net.mandown.db.DBService;
import net.mandown.games.GameMenuActivity;
import net.mandown.sensors.SensorService;


public class JournalActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        //setting the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //getting the button
//        ImageButton btnGamePlay = (ImageButton) findViewById(R.id.JoyStick);
//        //adding a click listener
//        btnGamePlay.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                //starting game activity
//                startActivity(new Intent(getApplicationContext(), GameMenuActivity.class));
//            }
//        });

//        // Set Beer glass to manually insert new passive data entry into database
//        ImageButton btnBeerGlass = (ImageButton) findViewById(R.id.BeerGlass);
//        // Add the click listener
//        btnBeerGlass.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                // Insert new entry
//                DBService.startActionPutPassive(getApplicationContext(), 0, 0, 0);
//            }
//        });
//
//        // Reset the database on initialisation
//        DBService.startActionResetDatabase(this);
//
//        // Start the sensor service to collect data
//        startService(new Intent(this, SensorService.class));
//
//        // Post event to handler to begin DB updates
//        mDbUpdateHandler.postDelayed(mUpdateDBTxt, 100);

    }

}

