package net.mandown;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.wearable.view.WatchViewStub;
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

public class CallSOS extends Activity {

    private GoogleApiClient mGoogleApiClient;

    private static final String WATCH_SOS_KEY = "net.mandown.key.SOS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_sos);

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

    public void confirmSOS(View v){
        //Send system time to phone for SOS
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/SOS");
        putDataMapRequest.getDataMap().putLong(WATCH_SOS_KEY, System.currentTimeMillis());
        PutDataRequest putDataReq = putDataMapRequest.asPutDataRequest();
        putDataReq.setUrgent();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

        finish();
    }

    public void denySOS(View v){
        finish();
    }
}
