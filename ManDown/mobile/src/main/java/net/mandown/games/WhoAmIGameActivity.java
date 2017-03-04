package net.mandown.games;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WhoAmIGameActivity extends AppCompatActivity implements WhoAmIView.Callback, View.OnClickListener{

    //declaring gameview
    private WhoAmIView view;

    //declaring layout
    private FrameLayout game;
    private LinearLayout gameWidgets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate","Created my game activity");
        //setting the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        game = new FrameLayout(this);
        view = new WhoAmIView(this,this);
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

    public void gameOver(){
        finish();
    }

}
