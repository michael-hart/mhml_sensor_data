package net.mandown.history;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import net.mandown.R;


public class HistoryActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        //setting the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#da4747"));

            Bitmap bg = Bitmap.createBitmap(480,800, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bg);
            canvas.drawRect(100,50,200,200,paint);

        LinearLayout ll= (LinearLayout) findViewById(R.id.rect);
        ll.setBackground(new BitmapDrawable(bg));

    }

}

