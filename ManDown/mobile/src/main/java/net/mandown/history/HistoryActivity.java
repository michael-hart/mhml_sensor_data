package net.mandown.history;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import net.mandown.R;
import net.mandown.games.GameMenuActivity;
import android.view.SurfaceView;


import static java.security.AccessController.getContext;


public class HistoryActivity extends AppCompatActivity implements View.OnClickListener, Runnable {

    private ImageButton btnEmergency;

//    private Thread beerThread = null;
    private Canvas canvas;
    private Paint paint;

    //  private DisplayMetrics dm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        //setting the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        paint = new Paint();
        paint.setColor(Color.parseColor("#da4747"));

//        beerThread = new Thread(this);
//        beerThread.start();


        //display metrics
//        dm = new DisplayMetrics();
//        ((Activity) getContext()).getWindowManager()
//                .getDefaultDisplay()
//                .getMetrics(dm);





        //Canvas canvas = new Canvas(bg);


      //  LinearLayout ll = (LinearLayout) findViewById(R.id.rect);
     //   ll.setBackground(new BitmapDrawable(bg));

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

    public void draw() {
        Bitmap empty_glass = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.empty_beer_glass),400,400,false);
        Bitmap bg = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);

        canvas = new Canvas(bg);

        canvas.drawBitmap(empty_glass,0,0,paint);//.drawColor(Color.BLACK);

        Log.d("image drawn:","yes");

    }

    @Override
    public void run() {
       //     update();
            draw();
         //   control();

    }
//    private void control() {
//        try {
//            beerThread.sleep(17);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

}
