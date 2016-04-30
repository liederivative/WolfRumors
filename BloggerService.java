package uk.ac.wlv.wolfrumors;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.blogger.Blogger;
import com.google.api.services.blogger.BloggerScopes;

import java.util.Arrays;

/**
 * Created by user on 4/29/2016.
 */
public class BloggerService extends IntentService{
    private Context mContext;
    private static final String TAG = "PollService";
    public static Intent newIntent(Context context){
        return new Intent(context, BloggerService.class);
    }
    public BloggerService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG,"Received an intent: "+ intent);
    }


}
