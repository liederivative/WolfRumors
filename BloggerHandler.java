package uk.ac.wlv.wolfrumors;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.blogger.Blogger;
import com.google.api.services.blogger.model.Blog;
import com.google.api.services.blogger.model.BlogList;
import com.google.api.services.blogger.model.Comment;
import com.google.api.services.blogger.model.PostList;
import com.google.api.services.blogger.model.User;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by user on 5/5/2016.
 */

public class BloggerHandler extends AsyncTask<Object,Void,Object>{

    private GoogleCredential credential;
    private Blogger blog;
    private Context mContext;
    private ProgressDialog loadDialog;
    private List<Object> result;

    public BloggerHandler(String accessToken, Context context){
        credential = new GoogleCredential().setAccessToken(accessToken);
        blog = new Blogger.Builder(new NetHttpTransport(), new JacksonFactory(),credential).build();
        mContext = context;
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
    protected void onPreExecute() {
        super.onPreExecute();
        loadDialog = new ProgressDialog(mContext);
        loadDialog.setMessage("Working on your selection...");
        loadDialog.setIndeterminate(false);
        loadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loadDialog.setCancelable(true);
        loadDialog.show();
    }
    public Object postIdExists(String BLOG_ID, String POST_ID){
        // The request action.
        //Blogger.Posts.List postsListAction = null;
        List<Object> returnResults= new ArrayList<Object>();
        try {
            // The request action.
            Blogger.Posts.Get postsGetAction = blog.posts().get(BLOG_ID, POST_ID);
            // Restrict the result content to just the data we need.
            postsGetAction.setFields("id,content,updated,title");
            // This step sends the request to the server.
            com.google.api.services.blogger.model.Post result = postsGetAction.execute();
            returnResults.add(true);
            returnResults.add(result);
            return returnResults;
        } catch (IOException e) {
            e.printStackTrace();
            returnResults.add(false);
            return returnResults;
        }

    }

    public boolean uploadPost(String BLOG_ID,com.google.api.services.blogger.model.Post content,Post post){

        content.setTitle(post.getTitle());
        content.setContent(post.getContent());
        //com.google.api.services.blogger.model.Post.Images test = new com.google.api.services.blogger.model.Post.Images();
        //test.set("test.jpg",new File(post.getPhotoFilename()).createNewFile());

        //com.google.api.services.blogger.model.Post.Images[] = {test};
        //List<com.google.api.services.blogger.model.Post.Images> list = new ArrayList<com.google.api.services.blogger.model.Post.Images>();
        //content.setImages( list.add(test));

        // The request action.
        Blogger.Posts.Insert postsInsertAction = null;
        try {
            postsInsertAction = blog.posts().insert(BLOG_ID, content);

            postsInsertAction.setFields("updated,id");

            // This step sends the request to the server.
            com.google.api.services.blogger.model.Post result = postsInsertAction.execute();

            Log.d("UPLOAD",result.getId());
            // Update Post
            post.setPostId(result.getId());
            post.setLastMod( new Date(result.getUpdated().getValue()) );
            PostLab.get(mContext).updatePost(post);
            Log.d("UPLOAD",result.getUpdated().toString());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }
    public boolean updatePost(String BLOG_ID, Post post){
        // Construct the post update body
        com.google.api.services.blogger.model.Post content = new com.google.api.services.blogger.model.Post();
        content.setId(post.getPostId());
        content.setTitle(post.getTitle());
        content.setContent(post.getContent());
        // This step sends the request to the server.
        try {
            // The request action.
            Blogger.Posts.Update postsUpdateAction = blog.posts().update(BLOG_ID, post.getPostId(), content);

            // Restrict the result content to just the data we need.
            postsUpdateAction.setFields("published,updated");
            com.google.api.services.blogger.model.Post result = postsUpdateAction.execute();
            result.getUpdated();
            post.setLastMod(new Date(result.getUpdated().getValue()));
            PostLab.get(mContext).updatePost(post);
            Log.d("UPDATE",post.getTitle());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean downloadPost(String BLOG_ID, Post post, Object params){
        com.google.api.services.blogger.model.Post result = (com.google.api.services.blogger.model.Post) params;
        post.setContent(result.getContent());
        post.setTitle(result.getTitle());
        post.setLastMod(new Date(result.getUpdated().getValue()) );
        PostLab.get(mContext).updatePost(post);
        return true;
    }
    @Override
    protected Object doInBackground(Object... params) {
        Log.d("TY",params[0].toString());

        String BLOG_ID = null;
        try {
            BLOG_ID = getBlogId();
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
                    String eval = post.getPostId();
                    com.google.api.services.blogger.model.Post content = new com.google.api.services.blogger.model.Post();
                    List<Object> resultsFromPostId = (List)postIdExists(BLOG_ID,eval);
                    if ( eval == null ){
                        uploadPost(BLOG_ID,content,post); //create post

                    }else if(!eval.isEmpty() && (boolean)resultsFromPostId.get(0) ) {

                        Blogger.Posts.Get postsGetAction = blog.posts().get(BLOG_ID, post.getPostId());

                        // Restrict the result content to just the data we need.
                        postsGetAction.setFields("content,published,title,updated");

                        // This step sends the request to the server.
                        com.google.api.services.blogger.model.Post run = postsGetAction.execute();
                        if (run.getUpdated() == null){

                            if(new Date(run.getPublished().getValue()) != post.getLastMod()){
                                Log.d("DATE-Nl-POST",post.getLastMod().toString());
                                Log.d("DATE-Nl-GOOGLE",run.getPublished().toString());
                            }
                        }else if (new Date(run.getUpdated().getValue()) != post.getLastMod()){
                            List comparisonDates = getDiff( new Date(run.getUpdated().getValue()) , post.getLastMod());
                            boolean dateEquals = (boolean)comparisonDates.get(0);
                            Log.d("BOOL",(dateEquals)?"true":"false");
                            Date bigger = (Date)comparisonDates.get(1);
                            Date smaller = (Date)comparisonDates.get(2);
                            Log.d("DATE BIG",  bigger.toString());
                            Log.d("DATE SMALL",smaller.toString());
                            String whosLastest = (String)comparisonDates.get(3);
                            if( whosLastest.equals("POST") ){
                                //UpdatePost
                                updatePost(BLOG_ID,post);

                            }else if(whosLastest.equals("GOOGLE") ){
                                //Download Post
                                downloadPost(BLOG_ID,post,resultsFromPostId.get(1));

                            }
                        }

                    }else if (!(boolean)resultsFromPostId.get(0)){
                        uploadPost(BLOG_ID,content,post); //update post
                    }

                    //listNewIds.add(result.getId());
                    //listNewUpdated.add(result.getUpdated());


                }
                //result.add(listNewIds);
                //result.add(listNewUpdated);

                return result;




            }else if (params[0].equals("sync")){

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object aVoid) {
        super.onPostExecute(aVoid);
        loadDialog.dismiss();


    }
    public List getDiff(Date dateBlog, Date datePost){
//        Calendar calendarBlog = Calendar.getInstance();
//        Calendar calendarPost = Calendar.getInstance();
//        calendarBlog.setTime(dateBlog);
//        calendarPost.setTime(datePost);
//        SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
//        format.setTimeZone(TimeZone.getTimeZone("UTC"));
//        String postTime = format.format(calendarPost.getTime());
//        String blogTime = format.format(calendarBlog.getTime());
//        Date b = calendarBlog.getTime();
//        Date p = calendarPost.getTime();
//        Log.d("POST TIME",postTime);
//        Log.d("BLOG TIME",blogTime);

        List arr = new ArrayList();


        if(dateBlog.after(datePost)) {
            arr.add(true);
            arr.add(dateBlog);
            arr.add(datePost);
            arr.add("GOOGLE");
            Log.d("ARR","GOOGLE");
        }else if(dateBlog.before(datePost)) {
            arr.add(true);
            arr.add(datePost);
            arr.add(dateBlog);
            arr.add("POST");
            Log.d("ARR","POST");

        }else {
            arr.add(false);
            arr.add(datePost);
            arr.add(dateBlog);
            arr.add("NONE");
        }
        return arr;
    }
}
