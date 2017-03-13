package net.mandown;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by weicongt on 13/3/2017.
 */

public class InputDrinks extends Activity {

    private GoogleApiClient mGoogleApiClient;

    private static final String BEER_KEY = "net.mandown.key.beer";
    private static final String WINE_KEY = "net.mandown.key.wine";
    private static final String COCKTAIL_KEY = "net.mandown.key.cocktail";
    private static final String SHOT_KEY = "net.mandown.key.shot";

    private String beertext = "beer";
    private String winetext = "wine";
    private String cocktailtext = "cocktail";
    private String shottext = "shot";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_drinks);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    ///////////////////////////////////////////////////////////////////

    //Send message
    public void drankbeer(View v) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/beer");
        putDataMapReq.getDataMap().putString(BEER_KEY, beertext);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        putDataReq.setUrgent();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        Log.d("beer","SENT BEER!");
        if (beertext == "beer")
        {
            beertext = "notbeer";
        }
        else
        {
            beertext = "beer";
        }
    }

    public void drankwine(View v) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/wine");
        putDataMapReq.getDataMap().putString(WINE_KEY, winetext);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        putDataReq.setUrgent();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        Log.d("wine","SENT WINE!");
        if (winetext == "wine")
        {
            winetext = "notwine";
        }
        else
        {
            winetext = "wine";
        }
    }

    public void drankcocktail(View v) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/cocktail");
        putDataMapReq.getDataMap().putString(COCKTAIL_KEY, cocktailtext);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        putDataReq.setUrgent();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        Log.d("cocktail","SENT COCKTAIL!");
        if (cocktailtext == "cocktail")
        {
            cocktailtext = "notcocktail";
        }
        else
        {
            cocktailtext = "cocktail";
        }
    }

    public void drankshot(View v) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/shot");
        putDataMapReq.getDataMap().putString(SHOT_KEY, shottext);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        putDataReq.setUrgent();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        Log.d("shot","SENT SHOT!");
        if (shottext == "shot")
        {
            shottext = "notshot";
        }
        else
        {
            shottext = "shot";
        }
    }
}
