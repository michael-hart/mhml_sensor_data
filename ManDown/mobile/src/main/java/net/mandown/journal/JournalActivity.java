package net.mandown.journal;

import android.app.AlertDialog;
import android.content.DialogInterface;
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


public class JournalActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton btnOptions;
    private ImageButton btnEmergency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        //setting the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        btnOptions  = (ImageButton) findViewById(R.id.OptionLevers);
        btnEmergency= (ImageButton) findViewById(R.id.DrunkMan);
        btnOptions.setOnClickListener(this);
        btnEmergency.setOnClickListener(this);

    }
    @Override
    public void onClick(View v) {

        if (v == btnEmergency) {
            new AlertDialog.Builder(this)
                    .setTitle("Contact Emergency Help")
                    .setMessage("Are you sure you want to send a distress call")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
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

    }
}

