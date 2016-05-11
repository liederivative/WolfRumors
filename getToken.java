package uk.ac.wlv.wolfrumors;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class getToken extends AsyncTask<String,String,String> {
    static final String TOKEN_URL ="https://accounts.google.com/o/oauth2/token";
    static final String REFRESH_TOKEN = "https://www.googleapis.com/oauth2/v4/token";
    static final String CLIENT_ID = "605511989748-5h38harhnq0ta6jllir0fg6a7t9vb5o6.apps.googleusercontent.com";
    static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob:auto";




    @Override
    protected String doInBackground(String... authToken) {
        if (authToken ==  null){
            return null;
        }
        try {
            if(authToken[1].equals("refresh_token")){

                String tmp = this.getGoogleRefreshToken(authToken[0]);
                Log.d("REFRESH_TOKEN", tmp);
                String token = new JSONObject(tmp).get("access_token").toString();
                String refresh_token = new JSONObject(tmp).get("refresh_token").toString();
                return refresh_token;

            }else if (authToken[1].equals("access_token")) {
                String tmp = this.getGoogleToken(authToken[0]);
                Log.d("TOKEN", tmp);
                String token = new JSONObject(tmp).get("access_token").toString();
                return token;

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e){
            Log.d("JSON","Failed to parse Json");
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

    }

    public String getUrlByte(String token,String condition) throws IOException{
        String StringURL;
        String urlParameters = "client_id="+ CLIENT_ID;
        if(condition.equals("auth")){
            StringURL = TOKEN_URL;
              urlParameters += "&code="+ Uri.encode(token)
                     +"&redirect_uri="+ REDIRECT_URI +"&grant_type=authorization_code"+"&" ;
        }else if(condition.equals("refresh")){
            StringURL = REFRESH_TOKEN;
            urlParameters += "&grant_type=refresh_token"+"&refresh_token=" + token;
        } else {
            return "false";
        }
        URL url = new URL(StringURL);

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestMethod("POST");

        try{
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            //OutputStream in = connection.getOutputStream(); // GET request, for POST  getOutputStream()
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage()+":"+StringURL);
            }
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        }finally {
            connection.disconnect();
        }
    }
    public String getGoogleRefreshToken(String token) throws IOException {
        return new String(getUrlByte(token,"auth"));
    }
    public String getGoogleToken(String refreshToken) throws IOException {
        return new String(getUrlByte(refreshToken,"refresh"));
    }

}
