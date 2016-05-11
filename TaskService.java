package uk.ac.wlv.wolfrumors;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by user on 5/8/2016.
 */
public class TaskService extends IntentService{
    private static final String TAG = "TaskService";

    public static Intent newIntent(Context context){
        return new Intent(context, TaskService.class);
    }
    public TaskService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        new Test().execute();
        Log.d(TAG,"Received an intent:");
    }
    private class Test extends AsyncTask<Void,Void,Void>{
        public Test(){

        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG,"Doing in background");
            return null;
        }
    }
}
