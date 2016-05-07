package uk.ac.wlv.wolfrumors.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;

import java.util.Date;
import java.util.UUID;

import uk.ac.wlv.wolfrumors.Post;
import uk.ac.wlv.wolfrumors.database.PostsDBSchema.PostTable;
import uk.ac.wlv.wolfrumors.database.PostsDBSchema.oauthTokenFactory;

/**
 * Created by user on 4/28/2016.
 */
public class PostCursorWrapper extends CursorWrapper {
    public PostCursorWrapper(Cursor cursor){
        super(cursor);
    }
    public String getAccessToken(){
        String refreshToken = getString(getColumnIndex(oauthTokenFactory.Cols.TOKEN));
        //Long date = getLong(getColumnIndex(oauthTokenFactory.Cols.DATE));
        return refreshToken;
    }
    public Post getPost() {
        String uuid_ = getString(getColumnIndex(
                PostTable.Cols.UUID));
        String title = getString(getColumnIndex(
                PostTable.Cols.TITLE));
        String content = getString(getColumnIndex(
                PostTable.Cols.CONTENT));
        Long date = getLong(getColumnIndex(
                PostTable.Cols.DATE));
        Long last_mod = getLong(getColumnIndex(
                PostTable.Cols.LAST_MOD));
        String photo_url = getString(getColumnIndex(PostTable.Cols.PHOTO_URL));
        String is_camera = getString(getColumnIndex(PostTable.Cols.IS_CAMERA));
        String post_id = getString(getColumnIndex(PostTable.Cols.POST_ID));

        Post post = new Post(UUID.fromString(uuid_));
        post.setTitle(title);
        Log.d("TAG",(new Date()).toString());
        post.setDate(new Date(date) );
        post.setLastMod(new Date(last_mod) );
        post.setContent(content);
        if (is_camera.equals("1")){
            post.setPhotoPath("",true);
        }else if (is_camera.equals("0")){
            post.setPhotoPath(photo_url,false);
        }
        post.setIsCamera(is_camera);
        post.setPostId(post_id);

        return post;
    }
}
