package uk.ac.wlv.wolfrumors;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.AccountChangeEvent;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by user on 4/30/2016.
 */
public class OAuthHelper extends AppCompatActivity{
    HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    //JsonFactory JSON_FACTORY = new JsonFactory();
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final String AUTH_BLOGGER = "oauth2:https://www.googleapis.com/auth/blogger";
    static OAuthHelper mContext;

    static OAuthHelper getContext(){
        return mContext;
    }

    public void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }
    private String getToken(Context context, Account account, String scope){
        try {
            return  GoogleAuthUtil.getToken( context, account, scope  );

        } catch (IOException e) {
            Log.d("TAG",e.toString());
        } catch (GoogleAuthException e) {
            Log.d("TAG",e.toString());
        }
        return null;
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OAuthHelper.mContext = this;
        pickUserAccount();

    }

    String mEmail; // Received from newChooseAccountIntent(); passed to getToken()

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                // With the account name acquired, go get the auth token
                String username = getUsername();
                final Account acc = new Account( username, AccountManager.KEY_ACCOUNT_NAME);
                AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {

                    @Override
                    protected String doInBackground(Void... params) {
                        String token = "";
                        token = getToken(OAuthHelper.getContext(),acc,AUTH_BLOGGER);
                        return token;
                    }
                    @Override
                    protected void onPostExecute(String token) {
                        Log.d("TAG", "Access token retrieved:" + token);
                    }

                };
                task.execute();


            } else if (resultCode == RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
                Toast.makeText(this, "Pick an Account Motherf** ", Toast.LENGTH_SHORT).show();
            }
        }
        // Handle the result from exceptions

    }
    public String getUsername() {
        AccountManager manager = AccountManager.get(this);
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<String>();

        for (Account account : accounts) {
            // TODO: Check possibleEmail against an email regex or treat
            // account.name as an email address only for certain account.type values.
            possibleEmails.add(account.name);
        }

        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            String email = possibleEmails.get(0);
            String[] parts = email.split("@");

            if (parts.length > 1)
                return parts[0];
        }
        return null;
    }
}
