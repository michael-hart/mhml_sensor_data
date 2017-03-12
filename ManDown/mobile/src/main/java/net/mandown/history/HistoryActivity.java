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
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import net.mandown.R;
import net.mandown.db.DBService;
import net.mandown.games.GameMenuActivity;
import android.view.SurfaceView;
import android.widget.TextView;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import static java.security.AccessController.getContext;


public class HistoryActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton btnEmergency;
    private ImageButton btnBeerGlass;

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

        btnEmergency = (ImageButton) findViewById(R.id.DrunkMan);
        btnEmergency.setOnClickListener(this);


        btnBeerGlass = (ImageButton) findViewById(R.id.BeerGlass);

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
