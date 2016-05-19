package uk.ac.wlv.wolfrumors.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import uk.ac.wlv.wolfrumors.PostLab;
import uk.ac.wlv.wolfrumors.database.PostsDBSchema.PostTable;
/**
 * creation of SQLite DB.
 *
 * @author Albert Jimenez
 *  Created:
 *  28 April 2016
 *  Reference:
 *  Phillips, B., Hardy, B. and Big Nerd Ranch (2015) Android Programming: The Big Nerd Ranch Guide. Big Nerd Ranch.
 *
 */

public class PostDBReferee extends SQLiteOpenHelper {
    public static final int VERSION = 1;
    private static final String DATABASE_NAME = "Posts.db";
    public PostDBReferee(Context context){
        super(context, DATABASE_NAME,null,VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + PostTable.NAME +
        "(" + "_id integer primary key autoincrement," +
                PostTable.Cols.UUID + "," +
                PostTable.Cols.TITLE + "," +
                PostTable.Cols.CONTENT + "," +
                PostTable.Cols.DATE + ","+
                PostTable.Cols.LAST_MOD + ","+
                PostTable.Cols.PHOTO_URL + "," +
                PostTable.Cols.IS_CAMERA + "," +
                PostTable.Cols.POST_ID + ")"

        );
        db.execSQL("CREATE TABLE " + PostsDBSchema.oauthTokenFactory.NAME +
                "(" + "_id integer primary key autoincrement," +
                PostsDBSchema.oauthTokenFactory.Cols.TOKEN + "," +
                PostsDBSchema.oauthTokenFactory.Cols.DATE + ")"

        );

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,int newVersion) {

    }

}
