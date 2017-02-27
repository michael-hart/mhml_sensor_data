package net.mandown.games;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import net.mandown.R;

public class GameMenuActivity extends AppCompatActivity implements View.OnClickListener{


    private Button buttonWAB;
    private Button buttonTRW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menu);


        //setting the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //getting the button
        buttonWAB = (Button) findViewById(R.id.Button_Whack_a_beer);
        buttonTRW = (Button) findViewById(R.id.Button_Tightrope_Waiter);

        //adding a click listener
        buttonWAB.setOnClickListener(this);
        buttonTRW.setOnClickListener(this);
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

    }
}
