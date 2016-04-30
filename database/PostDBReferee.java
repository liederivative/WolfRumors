package uk.ac.wlv.wolfrumors.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import uk.ac.wlv.wolfrumors.PostLab;
import uk.ac.wlv.wolfrumors.database.PostsDBSchema.PostTable;

/**
 * Created by user on 4/28/2016.
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
                PostTable.Cols.IS_CAMERA + ")"

        );

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,int newVersion) {

    }

}
