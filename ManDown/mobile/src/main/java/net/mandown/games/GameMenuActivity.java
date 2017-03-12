package net.mandown.games;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import net.mandown.MainActivity;
import net.mandown.R;

public class GameMenuActivity extends AppCompatActivity implements View.OnClickListener{


    private ImageButton buttonWAB;
    private ImageButton buttonTRW;
    private ImageButton buttonRF;
    private ImageButton buttonWI;


    private static final int REQUEST_PHONE_CALL = 1;

    private final String mDisclaimerText =
            "This app is distributed for the collection of accelerometer, gyroscope, and " +
                    "magnetometer data over time. The use of the app's games will collect information " +
                    "and store it online. The app does not accept responsibility for inaccurate readings " +
                    "or results for intoxication levels.\n\nIf you wish to opt out, please uninstall " +
                    "the application.";
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menu);


        //setting the orientation to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //getting the button
        buttonWAB = (ImageButton) findViewById(R.id.Button_Whack_a_beer);
        buttonTRW = (ImageButton) findViewById(R.id.Button_Tightrope_Waiter);
        buttonRF = (ImageButton) findViewById(R.id.Button_Ring_of_Fire);
        buttonWI = (ImageButton) findViewById(R.id.Button_Who_am_I);

        //adding a click listener
        buttonWAB.setOnClickListener(this);
        buttonTRW.setOnClickListener(this);
        buttonRF.setOnClickListener(this);
        buttonWI.setOnClickListener(this);

        toolbar = (Toolbar) findViewById(R.id.tool_bar_history);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Games");
    }

    @Override
    public void onClick(View v) {

        if (v == buttonWAB) {
            //starting game activity
            startActivity(new Intent(this, WhackABeerMenuActivity.class));
        }
        if (v == buttonTRW) {
            //starting game activity
            startActivity(new Intent(this, TightropeWaiterMenuActivity.class));
        }
        if (v == buttonRF) {
            //starting game activity
            startActivity(new Intent(this, RingOfFireMenuActivity.class));
        }
        if (v == buttonWI) {
            //starting game activity
            startActivity(new Intent(this, WhoAmIMenuActivity.class));
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

                                if (ContextCompat.checkSelfPermission(GameMenuActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(GameMenuActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                                } else {
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
    }