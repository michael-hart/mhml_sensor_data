package net.mandown.history;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import net.mandown.R;
import net.mandown.games.GameMenuActivity;


public class HistoryActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton btnEmergency;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        //setting the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#da4747"));

        Bitmap bg = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bg);
        canvas.drawRect(100, 50, 200, 200, paint);

        LinearLayout ll = (LinearLayout) findViewById(R.id.rect);
        ll.setBackground(new BitmapDrawable(bg));

        btnEmergency = (ImageButton) findViewById(R.id.DrunkMan);
        btnEmergency.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {

        if (v == btnEmergency) {
            new AlertDialog.Builder(this)
                    .setTitle("Contact Emergency Help")
                    .setMessage("Are you sure you want to send a distress call?")
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
