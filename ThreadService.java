package uk.ac.wlv.wolfrumors;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
/**
 * Thread class to handle AsyncTask in a separate Thread.
 *
 * @author Albert Jimenez
 *  Created:
 *  29 April 2016
 *  Reference:
 *  Phillips, B., Hardy, B. and Big Nerd Ranch (2015) Android Programming: The Big Nerd Ranch Guide. Big Nerd Ranch.
 *
 */

public class ThreadService<T> extends HandlerThread{
    private Context mContext;
    private static final int MSG_POST = 0;
    private Handler mRequestHandler;
    private ConcurrentMap<T,ArrayList> mRequestMap = new ConcurrentHashMap<>();
    private static final String TAG = "ThreadService";
    private Handler mResponseHandler;
    private BloggerListener<T> mBloggerServiceListener;

    private String accessToken;

    public interface BloggerListener<T>{
        void onPostExecutionBlogger (T target, ArrayList url);
    }
    public void setBloggerServiceListener (BloggerListener<T> listener){
        mBloggerServiceListener = listener;
    }
    public ThreadService(Handler responseHandler, ArrayList init) {
        super(TAG);
        String token = (String) init.get(0);
        Context context = (Context) init.get(1);
        mRequestHandler = responseHandler;
        accessToken = token;
        mContext = context;
    }

    @Override
    protected void onLooperPrepared() {
        //super.onLooperPrepared();
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MSG_POST){
                    T target = (T) msg.obj;
                    Log.d(TAG,"Got a request for:"+mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    public void queueBlogger(T target, ArrayList params){
        Log.d(TAG,"Got an URL");
        if (params == null){
            mRequestMap.remove(target);
        }else {
            mRequestMap.put(target,params);
            mRequestHandler.obtainMessage(MSG_POST,target).sendToTarget();
        }
    }
    private void handleRequest(final T target){

            final ArrayList params = mRequestMap.get(target);
            if (params == null) {
                return;
            }

            try {
                if(!params.get(0).equals("refreshPhoto")){
                    final ArrayList<Post> posts = (ArrayList<Post>) params.get(1);
                    Object[] u = {(params.get(0)), posts};

                    if(accessToken == null){
                        return;
                    }

                    new BloggerHandler(accessToken, mContext).execute(u).get();
                }

            mRequestHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target) != params) {
                        return;
                    }
                    mRequestMap.remove(target);
                    mBloggerServiceListener.onPostExecutionBlogger(target, params);
                }
            });
//            }catch (IOException e){
//              Log.d(TAG,"ERROR DOWNLOADING");
            }catch (InterruptedException e){
                e.printStackTrace();
            }catch(ExecutionException e){
                e.printStackTrace();
            }

    }
    public void clearQueue(){
        mRequestHandler.removeMessages(MSG_POST);
    }
    private class Test extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void... params) {
            Log.d(TAG,"Doing some background stuff");
            return "Some data Mo..";
        }

    }


}
