package net.mandown.history;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;

import net.mandown.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HistoryActivity extends AppCompatActivity  {

    // UI Controls
    private ImageButton btnBeerGlass;
    private ListView lvHistory;

    // List adapter for lvHistory
    private ListAdapter mHistoryAdapter;
    private ArrayList<IntoxicationRecord> mIntoxRecords;

    // Tools for formatting history
    private SimpleDateFormat mDateFormat, mTimeFormat;

    private Toolbar toolbar;
    private static final int REQUEST_PHONE_CALL = 1;

    private final String mDisclaimerText =
            "This app is distributed for the collection of accelerometer, gyroscope, and " +
                    "magnetometer data over time. The use of the app's games will collect information " +
                    "and store it online. The app does not accept responsibility for inaccurate readings " +
                    "or results for intoxication levels.\n\nIf you wish to opt out, please uninstall " +
                    "the application.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        //setting the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set up toolbar at the top
        toolbar = (Toolbar) findViewById(R.id.tool_bar_history);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("History");

        // Set up data for History View
        mIntoxRecords = new ArrayList<>();
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        mTimeFormat = new SimpleDateFormat("HH:mm:ss");

        Date now = new Date();
        String strToday = mDateFormat.format(now);
        String strNow = mTimeFormat.format(now);
        String level = "0.12% BAC";
        
        mIntoxRecords.add(new IntoxicationRecord(strToday, strNow, level));
        mIntoxRecords.add(new IntoxicationRecord(strToday, strNow, level));
        mIntoxRecords.add(new IntoxicationRecord(strToday, strNow, level));
        mIntoxRecords.add(new IntoxicationRecord(strToday, strNow, level));
        mIntoxRecords.add(new IntoxicationRecord(strToday, strNow, level));

        mHistoryAdapter = new RecordAdapter(this, mIntoxRecords);

        // Get reference to UI Controls
        btnBeerGlass = (ImageButton) findViewById(R.id.BeerGlass);
        lvHistory = (ListView) findViewById(R.id.lvHistoryView);
        lvHistory.setAdapter(mHistoryAdapter);

        // Set to fully drunk!
        update_drunk_level(2);

    }

        //Trying to pull data from database

//        //////reading from firebase
//        DatabaseReference mRef2= mRef.child("drunken");
//        // Read from the database
//        mRef2.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                Map<String,Long> value = (Map)dataSnapshot.getValue();
//                Log.d("Value is: " , String.valueOf(value.entrySet()));


        //update text

        // Set up a new handler to update the home textview with number of DB entries every 100ms
//        private final Handler mDbUpdateHandler = new Handler();
//        private Runnable mUpdateDBTxt = new Runnable() {
//            @Override
//            public void run() {
//                if (DBService.sInstance != null) {
//                    TextView txtDbInfo = (TextView) findViewById(R.id.txtDbView);
//                    txtDbInfo.setText(String.format("%d accel data readings",
//                            DBService.sInstance.getNumAccelReadings()));
//                    mDbUpdateHandler.postDelayed(mUpdateDBTxt, 100);
//                }
//            }
//        };



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

                return true;
            case R.id.emergency:
                new AlertDialog.Builder(this)
                        .setTitle("Contact Emergency Help")
                        .setMessage("Are you sure you want to send a distress call")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with action
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "I'm Drunk"));
                                if (ContextCompat.checkSelfPermission(HistoryActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(HistoryActivity.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
                                }
                                startActivity(intent);
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


}
