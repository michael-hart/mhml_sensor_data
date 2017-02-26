package net.mandown;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.diyar.myapplication.R;

import net.mandown.games.GameMenuActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageButton GamePlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setting the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //getting the button
        GamePlay = (ImageButton) findViewById(R.id.JoyStick);

        //adding a click listener
        GamePlay.setOnClickListener(this);
    }
    public void onClick(View v) {

        //starting game activity
        startActivity(new Intent(this, GameMenuActivity.class));
    }


}

