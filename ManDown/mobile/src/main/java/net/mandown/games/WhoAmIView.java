package net.mandown.games;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import net.mandown.R;

import java.util.Random;

/**
 * Created by Santiago on 3/3/2017.
 */

public class WhoAmIView extends SurfaceView implements Runnable  {

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
    private Card[] card;
    private Card cur_card;

    //random
    private Random rand;


    //gameplay variables
    private WhoAmIView.Callback observer;

    public WhoAmIView(WhoAmIView.Callback _observer, Context context) {
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

        int card_width = 240*dm.widthPixels/1280;
        int card_height = 240*dm.heightPixels/720;

        background_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.background),dm.widthPixels,dm.heightPixels,false);
        Bitmap U_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.unknown),card_width,card_height,false);
        Bitmap AL_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.abraham_lincoln),card_width,card_height,false);
        Bitmap AJ_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.angelina_jolie),card_width,card_height,false);
        Bitmap BO_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.barack_obama),card_width,card_height,false);
        Bitmap CR_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.cristiano_ronaldo),card_width,card_height,false);
        Bitmap DT_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.donald_trump),card_width,card_height,false);
        Bitmap EP_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.elvis_presley),card_width,card_height,false);
        Bitmap GC_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.george_clooney),card_width,card_height,false);
        Bitmap MM_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.marilyn_monroe),card_width,card_height,false);
        Bitmap MJ_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.michael_jordan),card_width,card_height,false);
        Bitmap OW_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.oprah_winfrey),card_width,card_height,false);
        Bitmap TC_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.tom_cruise),card_width,card_height,false);

        card = new Card[11];

        card[0] = new Card(AL_bm,"Abraham Lincoln");
        card[1] = new Card(AJ_bm,"Angelina Jolie");
        card[2] = new Card(BO_bm,"Barrack Obama");
        card[3] = new Card(CR_bm,"Cristiano Ronaldo");
        card[4] = new Card(DT_bm,"Donald Trump");
        card[5] = new Card(EP_bm,"Elvis Presley");
        card[6] = new Card(GC_bm,"George Clooney");
        card[7] = new Card(MM_bm,"Marilyn Monroe");
        card[8] = new Card(MJ_bm,"Michael Jordan");
        card[9] = new Card(OW_bm,"Oprah Winfrey");
        card[10] = new Card(TC_bm,"Tom Cruise");

        rand = new Random();

        cur_card = new Card(U_bm,"Tap to Start");

    }

    private Card rand_card(){
        return card[rand.nextInt(card.length)];
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
    }


    private void draw() {
        //checking if surface is valid
        if (surfaceHolder.getSurface().isValid()) {
            //locking the canvas
            canvas = surfaceHolder.lockCanvas();
            //drawing a background
            canvas.drawBitmap(background_bm,0,0,paint);//.drawColor(Color.BLACK);
            //Drawing the player

            canvas.drawBitmap(cur_card.getBm(),(canvas.getWidth()-cur_card.getBm().getWidth())/2,(canvas.getHeight()-cur_card.getBm().getHeight())/2,paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(canvas.getWidth()/10);
            canvas.drawText(cur_card.getText(),canvas.getWidth()/ 7,canvas.getHeight()*9/10,paint);

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

        observer.gameOver();

    }

    interface Callback {
        public void gameOver();
    }

    public void tapped(){
            cur_card = rand_card();
    }

}


