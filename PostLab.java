package uk.ac.wlv.wolfrumors;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import uk.ac.wlv.wolfrumors.database.PostCursorWrapper;
import uk.ac.wlv.wolfrumors.database.PostDBReferee;
import uk.ac.wlv.wolfrumors.database.PostsDBSchema.PostTable;

/**
 * Created by user on 4/26/2016.
 */
public class PostLab {
    private static PostLab sPostLab;
    //private List<Post> mPosts;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static PostLab get(Context context) {

        if (sPostLab == null ) {
            sPostLab = new PostLab(context);
        }
        return sPostLab;
    }

    private PostLab(Context context) {

        mContext = context.getApplicationContext();
        mDatabase = new PostDBReferee(mContext).getWritableDatabase();
        //mPosts = new ArrayList<>();

    }

    public List<Post> getPosts() {
        //return mPosts;
        List<Post> posts = new ArrayList<>();
        PostCursorWrapper cursor = queryPosts(null,null);
        try {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                posts.add(cursor.getPost());
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
        return posts;
    }
    public Post getPost(UUID id) {
        PostCursorWrapper cursor = queryPosts(PostTable.Cols.UUID+" = ?",
                new String[]{id.toString()}); //String[] to avoid SQL injection
        try{
            if (cursor.getCount() == 0){
                return null;
            }
            cursor.moveToFirst();
            return cursor.getPost();
        }finally {
            cursor.close();
        }

    }
    public void addPost(Post p){
        //mPosts.add(p);
        ContentValues values = getContentValues(p);
        mDatabase.insert(PostTable.NAME, null, values);
    }
    public void updatePost (Post post){
        String uuid_ = post.getId().toString();
        ContentValues values = getContentValues(post);
        mDatabase.update(PostTable.NAME, values,
                PostTable.Cols.UUID + " = ?",
                new String[] {uuid_});
    }

    public void deletePost(Post post) {
        //mPosts.remove(post);
        String uuid_ = post.getId().toString();
        mDatabase.delete(PostTable.NAME,
                PostTable.Cols.UUID + " = ?",
                new String[] {uuid_});
    }
    private static ContentValues getContentValues (Post post){
        ContentValues values = new ContentValues();
        values.put(PostTable.Cols.UUID, post.getId().toString());
        values.put(PostTable.Cols.TITLE, post.getTitle());
        values.put(PostTable.Cols.CONTENT, post.getContent());
        values.put(PostTable.Cols.DATE, post.getDate().getTime());
        values.put(PostTable.Cols.LAST_MOD, post.getLastMod().getTime() );//.getTime());
        values.put(PostTable.Cols.PHOTO_URL,post.getPhotoPath());
        values.put(PostTable.Cols.IS_CAMERA,post.getStatusPhoto()?"1":"0");
        values.put(PostTable.Cols.POST_ID,post.getPostId());

        return values;
    }
    private PostCursorWrapper queryPosts (String where, String[] Args ){
        Cursor cursor = mDatabase.query(
                PostTable.NAME, null, // null selects all columns
                where,
                Args,
                null, //groupby
                null, //having
                PostTable.Cols.LAST_MOD+ " DESC" // orderBy
        );
        return new PostCursorWrapper(cursor);
    }
    /// Manage Photos
    public File getPhotoFile(Post post){
        if(post.getPhotoPath() == null){
            return null;
        }
        return new File(post.getPhotoPath());

    }
    public void close(){
        if (mDatabase != null && mDatabase.isOpen()) {
            mDatabase.close();
        }

    }

}
