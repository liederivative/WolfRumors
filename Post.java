package uk.ac.wlv.wolfrumors;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * Created by user on 4/26/2016.
 */
public class Post {
    private UUID mId;
    private String mTitle;
    private String mContent;
    private Date mDate;
    private Date mLastMod;
    private boolean mIsCamera;
    private String mPhoto;
    private String mPostID;

    public Post() {
        // Generate unique identifier
        this(UUID.randomUUID());
        //mId = UUID.randomUUID();
        //mDate = new Date();
    }
    public Post(UUID id){
        mId = id;
        mDate = new Date();
        mLastMod = new Date();
        mIsCamera = true;
    }
    public Date getDate(){

        return mDate;
    }
    public boolean getStatusPhoto(){ return mIsCamera; };
    public Date getLastMod(){
        return mLastMod;
    }
    public void setIsCamera(String i) {
        Log.d("TAG",i);
        if (i.equals("0")){
        mIsCamera = false;
        } else {
            mIsCamera = true;
        }
    };
    public void setDate(Date date){
        mDate = date;
    }
    public void setLastMod(Date date){
        mLastMod = date;
    }
    public void updateLastMod(){
        mLastMod = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }
    public String getContent() {return mContent;}
    public void setContent(String content){ mContent = content;}
    public String getPhotoPath() {
        return mPhoto;
    }
    public void setPhotoPath(String path, boolean opt){
        if (!opt){
            mIsCamera = false;
            mPhoto = path;
        }else {

            File photo = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "IMG_"+getId().toString()+".jpg");
            mIsCamera = true;
            mPhoto = photo.getPath();
        }

    }
    public void setPostId(String post_id){
        mPostID = post_id;
    }
    public String getPostId(){
        return mPostID;
    }
    //public void setPhotoPath(Integer i){
    //    if (i!=0){
    //        get
    //    }else {

    //    }

    //}

}
