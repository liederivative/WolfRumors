package uk.ac.wlv.wolfrumors;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
/**
 * Change ratio of images and transform to Bitmap .
 *
 * @author Albert Jimenez
 *  Created:
 *  29 April 2016
 *  Reference:
 *  Phillips, B., Hardy, B. and Big Nerd Ranch (2015) Android Programming: The Big Nerd Ranch Guide. Big Nerd Ranch.
 *
 */

public class BitmapHelper {
    public static Bitmap getScaleBitmap(String path,Activity activity){
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return getScaledBitmap(path,size.x,size.y);
    }
    public static Bitmap getScaledBitmap(String path,int destWidth,int destHeight){
        //Read dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
        //Scale down (how much)
        int inSampleSize = 1;
        if (srcHeight>destHeight||srcWidth > destWidth){
            if (srcWidth > srcHeight){
                inSampleSize = Math.round(srcHeight/destHeight);
            }else {
                inSampleSize = Math.round(srcWidth/destWidth);
            }
        }
        options =new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        return BitmapFactory.decodeFile(path,options);
    }
}
