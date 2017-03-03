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
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import net.mandown.R;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Santiago on 3/3/2017.
 */

class Card {
    private Bitmap bm;
    private String text;

    public Card(){
        text = "Tap to draw card";
        bm=Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888);
    }

    public Card(Bitmap _bm, String _text){
        text = _text;
        bm=_bm;
    }

    public Bitmap getBm() {
        return bm;
    }

    public String getText() {
        return text;
    }



}

public class RingOfFireView extends SurfaceView implements Runnable  {

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
    private int[] prob_arr;
    private Random rand;


    //gameplay variables
    private RingOfFireView.Callback observer;

    public RingOfFireView(RingOfFireView.Callback _observer, Context context) {
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

        int card_width = 250*dm.widthPixels/1280;
        int card_height = 150*dm.heightPixels/720;

        background_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.background),dm.widthPixels,dm.heightPixels,false);
        Bitmap card_bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.heart),card_width,card_height,false);

        card = new Card[6];

        card[0] = new Card(card_bm,"Distribute 4 Sip");
        card[1] = new Card(card_bm,"You drink 1 Sip");
        card[2] = new Card(card_bm,"You drink 2 Sip");
        card[3] = new Card(card_bm,"Make someone drink 1 Sip");
        card[4] = new Card(card_bm,"Make someone drink 2 Sip");
        card[5] = new Card(card_bm,"Everyone drinks 1 Sip");

        rand = new Random();
        prob_arr = new int[]{0,1,1,2,2,3,3,4,4,5,5};

        cur_card = new Card(card_bm,"Tap to draw card");
    }

    private Card rand_card(){
        return card[prob_arr[rand.nextInt(prob_arr.length)]];
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

            canvas.drawBitmap(cur_card.getBm(),canvas.getWidth()/3,canvas.getHeight()/2,paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(canvas.getHeight()/7);
            canvas.drawText(cur_card.getText(),canvas.getWidth()/3,canvas.getHeight()*2/3,paint);

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

