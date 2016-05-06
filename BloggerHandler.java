package uk.ac.wlv.wolfrumors;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.blogger.Blogger;
import com.google.api.services.blogger.model.Blog;
import com.google.api.services.blogger.model.BlogList;
import com.google.api.services.blogger.model.User;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 5/5/2016.
 */
public class BloggerHandler extends AsyncTask<Object,Void,Void>{

    private GoogleCredential credential;
    private Blogger blog;
    public BloggerHandler(String accessToken){
        //SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        credential = new GoogleCredential().setAccessToken(accessToken);
        //credential.setSelectedAccountName("jimenez.serrata@gmail.com");
        blog = new Blogger.Builder(new NetHttpTransport(), new JacksonFactory(),credential).build();
    }

    public String getUserId()throws IOException{
        Blogger.Users.Get usersGetAction = null;
        usersGetAction = blog.users().get("self");
        //usersGetAction.setFields("displayName,id");
        usersGetAction.setFields("id");
        User user = usersGetAction.execute();
        return user.getId();
    }
    public String getBlogId() throws IOException{

        Blogger.Blogs.ListByUser blogListByUserAction = blog.blogs().listByUser("self");
        // Restrict the result content to just the data we need.
        //blogListByUserAction.setFields("items(id,name,posts/totalItems,updated)");
        blogListByUserAction.setFields("items(id)");
        // This step sends the request to the server.
        BlogList blogList = blogListByUserAction.execute();
        Blog tmp = (Blog)blogList.getItems().get(0);
        return tmp.getId();
    }

    @Override
    protected Void doInBackground(Object... params) {
        Log.d("TY",params.toString());

        String BLOG_ID = null;
        try {
            BLOG_ID = this.getBlogId();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        try {
            if (params[0].equals("upload")){
                // Construct a post to insert
//                for(Blog t : blogList.getItems()){
//                    String h = t.getId();
//                }
                for(Object p : (ArrayList)params[1]){
                    Post post = (Post) p;
                    com.google.api.services.blogger.model.Post content = new com.google.api.services.blogger.model.Post();
                    content.setTitle(post.getTitle());
                    content.setContent(post.getContent());
                    // The request action.
                    Blogger.Posts.Insert postsInsertAction = blog.posts()
                            .insert(BLOG_ID, content);

                    postsInsertAction.setFields("updated,id");

                    // This step sends the request to the server.
                    com.google.api.services.blogger.model.Post result = postsInsertAction.execute();

                    Log.d("UPLOAD",result.getId());
                    Log.d("UPLOAD",result.getUpdated().toString());

                }





            }else if (params[0].equals("upload")){

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
