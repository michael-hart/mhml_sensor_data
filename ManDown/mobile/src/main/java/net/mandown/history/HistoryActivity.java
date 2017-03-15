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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;

import net.mandown.R;
import net.mandown.db.DBService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends AppCompatActivity  {

    // UI Controls
    private ImageButton btnBeerGlass;
    private ListView lvHistory;

    // List adapter for lvHistory
    private ListAdapter mHistoryAdapter;
    private ArrayList<IntoxicationRecord> mIntoxRecords;

    // Tools for formatting history
    private SimpleDateFormat mDateFormat, mTimeFormat, mFirebaseFormat;

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
        mFirebaseFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        mHistoryAdapter = new RecordAdapter(this, mIntoxRecords);

        // Get reference to UI Controls
        btnBeerGlass = (ImageButton) findViewById(R.id.BeerGlass);
        lvHistory = (ListView) findViewById(R.id.lvHistoryView);
        lvHistory.setAdapter(mHistoryAdapter);

        // Set to fully drunk!
        update_drunk_level(2);

    }


    @Override
    protected void onResume() {
        super.onResume();
        mIntoxRecords.clear();

        // Get intoxication readings and put into records
        List<String[]> results = DBService.getIntoxHistory();
        if (results != null) {
            for (String[] result : results) {
                Date record = null;
                try {
                    record = mFirebaseFormat.parse(result[0]);
                } catch (ParseException pe) {
                    Log.w("HistoryActivity", "Failed to parse string: " + result[0]);
                    continue;
                }
                mIntoxRecords.add(new IntoxicationRecord(mDateFormat.format(record),
                        mTimeFormat.format(record),
                        result[1]));
            }
        }

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
