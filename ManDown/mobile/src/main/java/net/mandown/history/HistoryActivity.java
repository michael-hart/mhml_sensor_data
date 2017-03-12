package net.mandown.history;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import net.mandown.MainActivity;
import net.mandown.R;
import net.mandown.games.GameMenuActivity;
import android.view.SurfaceView;


import static java.security.AccessController.getContext;


public class HistoryActivity extends AppCompatActivity implements View.OnClickListener, Runnable {

    private ImageButton btnEmergency;

//    private Thread beerThread = null;
    private Canvas canvas;
    private Paint paint;

    private Toolbar toolbar;
    private static final int REQUEST_PHONE_CALL = 1;



    private final String mDisclaimerText =
            "This app is distributed for the collection of accelerometer, gyroscope, and " +
                    "magnetometer data over time. The use of the app's games will collect information " +
                    "and store it online. The app does not accept responsibility for inaccurate readings " +
                    "or results for intoxication levels.\n\nIf you wish to opt out, please uninstall " +
                    "the application.";

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


        toolbar = (Toolbar) findViewById(R.id.tool_bar_history);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("History");


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

                                if (ContextCompat.checkSelfPermission(HistoryActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(HistoryActivity.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
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
