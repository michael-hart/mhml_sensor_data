package net.mandown.journal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
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
import net.mandown.sensors.SensorService;


public class JournalActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton btnOptions;
    private ImageButton btnEmergency;
    private Button btnConfirm;
    EditText enterUnit;
    TextView displayUnit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        //setting the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        btnOptions  = (ImageButton) findViewById(R.id.OptionLevers);
        btnEmergency= (ImageButton) findViewById(R.id.DrunkMan);
        btnConfirm= (Button) findViewById(R.id.Confirm);
        btnOptions.setOnClickListener(this);
        btnEmergency.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

        displayUnit=(TextView) findViewById(R.id.display);
        displayUnit.setMovementMethod(new ScrollingMovementMethod());

    }


    @Override
    public void onClick(View v) {

        if (v == btnEmergency) {
            new AlertDialog.Builder(this)
                    .setTitle("Contact Emergency Help")
                    .setMessage("Are you sure you want to send a distress call")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with process
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

        if (v == btnConfirm){
            enterUnit=(EditText) findViewById(R.id.Unit_input);
            displayUnit.setText("Your input: \n"+enterUnit.getText().toString());
        }

    }
}

