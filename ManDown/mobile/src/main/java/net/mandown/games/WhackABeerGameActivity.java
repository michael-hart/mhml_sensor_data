package net.mandown.games;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import net.mandown.R;

public class WhackABeerGameActivity extends AppCompatActivity implements WhackABeerView.Callback, View.OnClickListener{

    private static final String LOG_TAG = "Whack-A-Beer";

    //declaring gameview
    private WhackABeerView view;

    //declaring layout
    private FrameLayout game;
    private LinearLayout gameWidgets;

    //image button
    private ImageButton bucket1;
    private ImageButton bucket2;
    private ImageButton bucket3;
    private ImageButton bucket4;
    private ImageButton bucket5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate","Created my game activity");
        //setting the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Get display metrics to get a density pixel value, hence working across all screen sizes
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float dp = 20f;
        float fpixels = dm.density * dp;
        int pixels = (int) (fpixels + 0.5f);

        game = new FrameLayout(this);

        gameWidgets = new LinearLayout(this);
        gameWidgets.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        gameWidgets.setOrientation(LinearLayout.HORIZONTAL);
        gameWidgets.setPadding(0, 3*pixels, 0, 2*pixels);

        bucket1 = new ImageButton(this);
        bucket2 = new ImageButton(this);
        bucket3 = new ImageButton(this);
        bucket4 = new ImageButton(this);
        bucket5 = new ImageButton(this);
        init_bucket(this, this, bucket1, Gravity.CENTER);
        init_bucket(this, this, bucket2, Gravity.CENTER);
        init_bucket(this, this, bucket3, Gravity.CENTER);
        init_bucket(this, this, bucket4, Gravity.CENTER);
        init_bucket(this, this, bucket5, Gravity.CENTER);

        view = new WhackABeerView(this, this);

        game.addView(gameWidgets);
        game.addView(view);

        setContentView(game);
    }

    //pausing the game when activity is paused
    @Override
    protected void onPause() {
        super.onPause();
        view.pause();
    }

    //running the game when activity is resumed
    @Override
    protected void onResume() {
        super.onResume();
        view.resume();
    }

    //buttons from xml
    @Override
    public void onClick(View v) {

        if (v == bucket1) {
            Log.d(LOG_TAG,"bucket1 tapped");
            view.tapped(1);
        }
        if (v == bucket2) {
            Log.d(LOG_TAG,"bucket2 tapped");
            view.tapped(2);
        }
        if (v == bucket3) {
            Log.d(LOG_TAG,"bucket3 tapped");
            view.tapped(3);
        }
        if (v == bucket4) {
            Log.d(LOG_TAG,"bucket4 tapped");
            view.tapped(4);
        }
        if (v == bucket5) {
            Log.d(LOG_TAG,"bucket5 tapped");
            view.tapped(5);
        }
    }

    private void init_bucket(Activity act, View.OnClickListener v, ImageButton b, int g){

        b.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        b.setScaleType(ImageButton.ScaleType.FIT_XY);
        b.setBackground(null);
        b.setImageResource(R.drawable.icebucket_back);
        b.setPadding(0, 0, 0, 0);
        b.setOnClickListener(v);

        gameWidgets.addView(b);
    }

    public void gameOver(){
        finish();
    }

}
