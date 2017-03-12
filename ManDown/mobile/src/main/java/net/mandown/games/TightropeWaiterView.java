package net.mandown.games;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import net.mandown.sensors.SensorBroadcastService;
import net.mandown.sensors.SensorSample;
import net.mandown.sensors.SensorType;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by Santiago on 2/26/2017.
 */
class TRWPlate {
    private Resources res;
    private Bitmap bm;
    private float radius;

    public TRWPlate(){
        radius=100;
        bm=Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888);
    }

    public TRWPlate(int r, Resources _res){
        radius=r;
        res=_res;
        bm=Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.plate),r,r,false);
    }

    public void Update(float r){
        radius-=r;
        bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.plate),(int)radius,(int)radius,false);

    }

    public Bitmap getBm() {
        return bm;
    }

    public int getR() {
        return (int)radius;
    }


}


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
        x-=_x;
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


public class TightropeWaiterView extends SurfaceView implements Runnable {

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
    private TRWPlate plate;
    private int zero_x;
    private int zero_y;
    private ArrayList<SensorSample> mSensorData;

    //gameplay variables
    private OutputStreamWriter file_out;
    private Callback observer;

    //accel values
    private long mAccTS = 0;
    private float mAccX = 0.0f;
    private float mAccY = 0.0f;
    private float mAccZ = 0.0f;

    private BroadcastReceiver br = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            long ts;
            float x, y, z;
            ts = intent.getLongExtra("ts", -1);
            x = intent.getFloatExtra("x", -1);
            y = intent.getFloatExtra("y", -1);
            z = intent.getFloatExtra("z", -1);

            if (ts > -1) {
                updateAccValues(ts, x, y, z);
            }
        }
    };


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

        int drink_width = 200*dm.widthPixels/1280;
        int drink_height = 150*dm.heightPixels/720;

        background_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.background),dm.widthPixels,dm.heightPixels,false);
        drink_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.tw_drink),drink_width,drink_height,false);
        ///NEED SENSOR DATA HERE!!!   AND IN UPDATE!!!!!
        zero_x = dm.widthPixels/2 - drink_width/2;
        zero_y = dm.heightPixels/2 - drink_height/2;

        drink= new TRWDrink(zero_x,zero_y,drink_bm);
        plate= new TRWPlate(500,res);

        try {
            file_out = new OutputStreamWriter(
                    context.openFileOutput("reaction.txt", Context.MODE_PRIVATE));
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    //Function to allow accel service to update accelerometer readings
    public void updateAccValues(long ts, float x, float y, float z) {
        mAccTS = ts;
        mAccX = x;
        mAccY = y;
        mAccZ = z;
        mSensorData.add(new SensorSample(ts, x, y, z));
    }

    @Override
    public void run() {
        // Clear sensor data array list
        mSensorData = new ArrayList<>();

        Context context = getContext();

        //Start accelerometer service
        Intent intent = new Intent(getContext(), SensorBroadcastService.class);
        context.startService(intent);
        context.registerReceiver(br, new IntentFilter(context.getString(R.string.accel_broadcast)));

        while (playing) {
            update();
            draw();
            control();
        }

        context.stopService(intent);
        context.unregisterReceiver(br);
        // Insert sensor values into database
        DBService.startActionPutSensorList(context, mSensorData, SensorType.ACCELEROMETER);
    }


    private void update() {
        //updating player position
        //player.update();
        drink.Update(Math.round(mAccX *(-20)),Math.round(mAccY *20));
		plate.Update(0.5f);

        //mSensorData.add(new SensorSample(mAccTS, mAccX, mAccY, mAccZ));

        int dist = distance(drink.getX(),drink.getY());

        if(dist>plate.getR()/2){
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
                    plate.getBm(),
                    dm.widthPixels/2-plate.getR()/2,
                    dm.heightPixels/2-plate.getR()/2,
                    paint);
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
        try {
            OutputStreamWriter outputStreamWriter = file_out;
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        observer.gameOver();
    }

    interface Callback {
        public void gameOver();
    }

}