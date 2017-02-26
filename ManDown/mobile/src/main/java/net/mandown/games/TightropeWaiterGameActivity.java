package net.mandown.games;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.diyar.myapplication.R;

public class TightropeWaiterGameActivity extends AppCompatActivity implements TightropeWaiterView.Callback, View.OnClickListener{

    //declaring gameview
    private TightropeWaiterView view;

    //declaring layout
    private FrameLayout game;
    private LinearLayout gameWidgets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate","Created my game activity");
        //setting the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        game = new FrameLayout(this);
        view = new TightropeWaiterView(this,this);
        gameWidgets = new LinearLayout (this);
        gameWidgets.setGravity(Gravity.CENTER);
        gameWidgets.setOrientation(LinearLayout.HORIZONTAL);

        TextView myText = new TextView(this);


        //myText.setText("rIZ..i");
        gameWidgets.addView(myText);

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

    }

    private void init_bucket(Activity act, View.OnClickListener v, ImageButton b, int g){

        //b = new ImageButton(act);
        b.setMaxHeight(600);
        b.setAdjustViewBounds(true);
        b.setScaleType(ImageView.ScaleType.FIT_CENTER);
        b.setImageResource(R.drawable.icebucket_front);
        b.setBackgroundResource(0);//background null
        b.setOnClickListener(v);

        LinearLayout l = new LinearLayout (act);
        l.setMinimumWidth(0);
        l.setWeightSum(1);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER);
        l.addView(b);
        gameWidgets.addView(l);
    }

    public void gameOver(){
        finish();
    }

}
