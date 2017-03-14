package net.mandown.journal;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.*;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;


import net.mandown.R;
import net.mandown.db.DBService;
import net.mandown.games.GameMenuActivity;
import net.mandown.history.HistoryActivity;
import net.mandown.sensors.SensorService;


public class JournalActivity extends AppCompatActivity implements View.OnClickListener {


    private Button btnConfirm;
    private ImageButton btnBeer;
    private ImageButton btnWine;
    private ImageButton btnShot;
    EditText enterUnit;
    TextView displayUnit;

    private Toolbar toolbar;
    private static final int REQUEST_PHONE_CALL = 1;

    private ImageButton btnBeerGlass;


    private final String mDisclaimerText =
            "This app is distributed for the collection of accelerometer, gyroscope, and " +
                    "magnetometer data over time. The use of the app's games will collect information " +
                    "and store it online. The app does not accept responsibility for inaccurate readings " +
                    "or results for intoxication levels.\n\nIf you wish to opt out, please uninstall " +
                    "the application.";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        //setting the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        btnConfirm= (Button) findViewById(R.id.Confirm);
        btnBeer= (ImageButton) findViewById(R.id.BeerUnit);
        btnWine= (ImageButton) findViewById(R.id.WineUnit);
        btnShot= (ImageButton) findViewById(R.id.ShotUnit);
        btnConfirm.setOnClickListener(this);
        btnBeer.setOnClickListener(this);
        btnWine.setOnClickListener(this);
        btnShot.setOnClickListener(this);

        displayUnit=(TextView) findViewById(R.id.display);
        displayUnit.setMovementMethod(new ScrollingMovementMethod());


        //getting toolbar
        toolbar = (Toolbar) findViewById(R.id.tool_bar_journal);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Journal");


        btnBeerGlass = (ImageButton) findViewById(R.id.BeerGlass);
//
//        enterUnit = (EditText) findViewById(R.id.Unit_input);
//        String sUsername = enterUnit.getText().toString();
//        if (sUsername.matches("")) {
//            findViewById(R.id.Confirm).setVisibility(View.GONE);
//
//        }else{
//            findViewById(R.id.Confirm).setVisibility(View.VISIBLE);
//
//        }


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

                                if (ContextCompat.checkSelfPermission(JournalActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(JournalActivity.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
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


        if (v == btnConfirm){
            enterUnit=(EditText) findViewById(R.id.Unit_input);
            displayUnit.setText("Your input: "+enterUnit.getText().toString());
        }
        if (v == btnBeer){
            displayUnit.setText("Your input: 2");

        }
        if (v == btnWine){
            displayUnit.setText("Your input: 1.5");

        }
        if (v == btnShot){
            displayUnit.setText("Your input: 1");
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


}

