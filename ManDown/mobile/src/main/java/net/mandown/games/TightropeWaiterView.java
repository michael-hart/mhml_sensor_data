package net.mandown.games;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import net.mandown.R;
import net.mandown.db.DBService;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by Santiago on 2/26/2017.
 */
class TRWDrink {
    private Bitmap bm;
    private int x;
    private  int y;

    public TRWDrink(){
        x=0;
        y=0;
        bm=Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888);
    }

    public TRWDrink(int _x, int _y, Bitmap _bm){
        x=_x;
        y=_y;
        bm=_bm;
    }

    public void Update(int _x, int _y){
        x+=_x;
        y+=_y;
    }

    public Bitmap getBm() {
        return bm;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


}

public class TightropeWaiterView extends SurfaceView implements Runnable  {

    volatile boolean playing;
    private Thread gameThread = null;

    //screen size (display metrics)
    DisplayMetrics dm;


    //These objects will be used for drawing
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    private Resources res;
    private Bitmap background_bm;
    private Bitmap drink_bm;

    //private sensor data
    private TRWDrink drink;
    private int zero_x;
    private int zero_y;
    private ArrayList<Float[]> sensordata;

    //gameplay variables
    private int steps;
    private OutputStreamWriter file_out;
    private Callback observer;

    public TightropeWaiterView(Callback _observer, Context context) {
        super(context);
        observer=_observer;

        //display metrics
        dm = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(dm);

        //initializing drawing objects
        surfaceHolder = getHolder();
        paint = new Paint();
        res = getResources();

        int drink_width = 250*dm.widthPixels/1280;
        int drink_height = 150*dm.heightPixels/720;

        background_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.background),dm.widthPixels,dm.heightPixels,false);
        drink_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.glass_beer),drink_width,drink_height,false);


        ///NEED SENSOR DATA HERE!!!   AND IN UPDATE!!!!!
        zero_x = dm.widthPixels/2 - drink_width/2;
        zero_y = dm.heightPixels/2 - drink_height/2;

        drink= new TRWDrink(zero_x,zero_y,drink_bm);

        sensordata = new ArrayList<>();


        try {
            file_out = new OutputStreamWriter(context.openFileOutput("reaction.txt", Context.MODE_PRIVATE));
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }


    }

    @Override
    public void run() {
        while (playing) {
            update();
            draw();
            control();
        }
    }


    private void update() {
        //updating player position
        //player.update();
        drink.Update(5,5);

        sensordata.add(new Float[]{0.0f,0.0f});

        int dist = distance(drink.getX(),drink.getY());

        if(dist>dm.widthPixels/2){
            gameOver();
        }
    }

    private int distance (int x, int y){
        return (int)Math.sqrt(Math.pow(x-zero_x,2)+Math.pow(y-zero_y,2));
    }

    private void draw() {
        //checking if surface is valid
        if (surfaceHolder.getSurface().isValid()) {
            //locking the canvas
            canvas = surfaceHolder.lockCanvas();
            //drawing a background
            canvas.drawBitmap(background_bm,0,0,paint);//.drawColor(Color.BLACK);
            //Drawing the player
            canvas.drawBitmap(
                        drink.getBm(),
                        drink.getX(),
                        drink.getY(),
                        paint);

            //Unlocking the canvas
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            gameThread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void gameOver(){
        DBService.startActionPutSensorGamedata(getContext(),sensordata);
        //Log.d("hi", String.valueOf(sensordata));
        observer.gameOver();

    }

    interface Callback {
        public void gameOver();
    }

}
