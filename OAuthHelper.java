package uk.ac.wlv.wolfrumors;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import uk.ac.wlv.wolfrumors.database.PostCursorWrapper;
import uk.ac.wlv.wolfrumors.database.PostDBReferee;
import uk.ac.wlv.wolfrumors.database.PostsDBSchema.oauthTokenFactory;

/**
 * Created by user on 4/30/2016.
 */
public class OAuthHelper extends AppCompatActivity {
    static final int REQUEST_AUTHORIZATION = 1003;
    static final String AUTH_BLOGGER = "oauth2:https://www.googleapis.com/auth/blogger";
    private WebView mWebView;
    Uri.Builder uriWebView = new Uri.Builder();
    private String token;
    private getToken task = new getToken();
    //private BloggerHandler mBlogger = new BloggerHandler();
    private SQLiteDatabase mDatabase;
    private boolean canIProceed;


    public void init(Context mContext){

        mDatabase = new PostDBReferee(mContext).getWritableDatabase();
        token = this.getRefreshToken();
        canIProceed = (token == null)?true:token.isEmpty();
    }

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.init(getApplicationContext());

        if(canIProceed) {
            setContentView(R.layout.logging_page);
            mWebView = (WebView) findViewById(R.id.web_login);
            uriWebView.scheme("https")
                    .authority("accounts.google.com")
                    .appendPath("o")
                    .appendPath("oauth2")
                    .appendPath("v2")
                    .appendPath("auth")
                    .appendQueryParameter("response_type", "code")
                    //.appendQueryParameter("approval_prompt", "force")
                    //.appendQueryParameter("prompt", "consent")
                    //.appendQueryParameter("access_type", "offline")
                    .appendQueryParameter("redirect_uri", "urn:ietf:wg:oauth:2.0:oob:auto")
                    .appendQueryParameter("client_id", "605511989748-5h38harhnq0ta6jllir0fg6a7t9vb5o6.apps.googleusercontent.com")
                    .appendQueryParameter("scope", "https://www.googleapis.com/auth/blogger");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(canIProceed) {
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.loadUrl(uriWebView.build().toString());
            mWebView.setWebViewClient(new WebViewClient() {
                boolean proceed = false;

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    Log.d("URL", url);
                    String titleWebView = mWebView.getTitle();
                    Log.d("TITLE", mWebView.getTitle());
                    if (titleWebView.contains("code=")) {

                        proceed = true;
                        token = titleWebView.split("=")[1];

                        try {
                            String refreshToken = task.execute(new String[] {token,"refresh_token"}).get();
                            //Save on DB
                            token= refreshToken;
                            sendTokenBack();
                            ContentValues values = getContentValues(refreshToken);
                            mDatabase.insert(oauthTokenFactory.NAME, null, values);
                            finish();
                           // mBlogger.execute(accessToken);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });

        }else {
            //mBlogger.execute(token);
            sendTokenBack();
            finish();
        }

    }
    private void sendTokenBack(){
        setResult(RESULT_OK, new Intent().putExtra("access_token",token));
    }
    private static ContentValues getContentValues (String token){
        ContentValues values = new ContentValues();
        values.put(oauthTokenFactory.Cols.TOKEN, token);
        values.put(oauthTokenFactory.Cols.DATE, new Date().toString());

        return values;
    }
    public String getRefreshToken() {
        Cursor cursor_ = mDatabase.query(
                oauthTokenFactory.NAME, new String[] {oauthTokenFactory.Cols.TOKEN}, // null selects all columns
                null,
                null,
                null, //groupby
                null, //having
                null // orderBy
        );
        PostCursorWrapper cursor = new PostCursorWrapper(cursor_);
        try{
            if (cursor.getCount() == 0){
                return null;
            }
            cursor.moveToFirst();
            return cursor.getAccessToken();
        }finally {
            cursor.close();
        }

    }
    public String getAccessToken(String refreshToken) {
        String accessToken = null;
        try {
            accessToken = task.execute(new String[] {refreshToken,"access_token"}).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return accessToken;
    }
}

