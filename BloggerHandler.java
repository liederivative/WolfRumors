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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Class responsible for upload, download and sync from Blogger.com .
 *
 * @author Albert Jimenez
 *  Created:
 *  5 May 2016
 *  Modified:
 *  16 May 2016
 *  ==========
 *  Change design for download posts from Blogger on sync.
 *  ==========
 *  Reference:
 *  Phillips, B., Hardy, B. and Big Nerd Ranch (2015) Android Programming: The Big Nerd Ranch Guide. Big Nerd Ranch.
 *  Google (2016) Blogger API Documentation [online]. [Accessed 12 April 2016]. Available at: <https://developers.google.com/blogger/docs/3.0/reference/>
 *
 */

public class BloggerHandler extends AsyncTask<Object,Void,Object>{

    private GoogleCredential credential;
    private Blogger blog;
    private Context mContext;
    private ProgressDialog loadDialog;
    private List<Object> result;


    public BloggerHandler(String accessToken,Context context){
        credential = new GoogleCredential().setAccessToken(accessToken);
        blog = new Blogger.Builder(new NetHttpTransport(), new JacksonFactory(),credential).setApplicationName("wolfrumors").build();

        mContext = context;


    }

    public static BloggerHandler newInstance(String accessToken, Context context){
        BloggerHandler handler = new BloggerHandler(accessToken,context);
        return handler;
    }

//    public String getUserId()throws IOException{
//
//        Blogger.Users.Get usersGetAction = null;
//        usersGetAction = blog.users().get("self");
//        //usersGetAction.setFields("displayName,id");
//        usersGetAction.setFields("id");
//        User user = usersGetAction.execute();
//        return user.getId();
//    }
    public String getBlogId() throws IOException{

        Blogger.Blogs.ListByUser blogListByUserAction = blog.blogs().listByUser("self");
        // Restrict the result content to just the data we need.
        blogListByUserAction.setFields("items(id)");
        // This step sends the request to the server.
        BlogList blogList = blogListByUserAction.execute();
        Blog tmp = (Blog)blogList.getItems().get(0);
        return tmp.getId();
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public Object postIdExists(String BLOG_ID, String POST_ID){
        // The request action.
        //Blogger.Posts.List postsListAction = null;
        List<Object> returnResults= new ArrayList<>();
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
        } catch (NullPointerException e){
            e.printStackTrace();
            return returnResults;
        }

    }

    public boolean uploadPost(String BLOG_ID,com.google.api.services.blogger.model.Post content,Post post){

        content.setTitle(post.getTitle());
        content.setContent(post.getContent());
        //com.google.api.services.blogger.model.Post.Images test = new com.google.api.services.blogger.model.Post.Images();
        //test.set("test.jpg",new File(post.getPhotoPath()).createNewFile());

        //com.google.api.services.blogger.model.Post.Images[] = {test};
        //List<com.google.api.services.blogger.model.Post.Images> list = new ArrayList<com.google.api.services.blogger.model.Post.Images>();
        //content.setImages( list.add(test));
        //List<com.google.api.services.blogger.model.Post.Images> images = new ArrayList<>();
        //com.google.api.services.blogger.model.Post.Images img = new com.google.api.services.blogger.model.Post.Images();
        //img.set(getFileName(post.getPhotoPath()),new File(post.getPhotoPath()));
        //images.add(img);
        //content.setImages(images);
        //content.set("fetchImages",true);
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

    // for future implementation
    private String getFileName(String b){
        char[] c = new char[b.length()];
        int i;
        char[] d = b.toCharArray();
        for(i = b.length()-1;i>0;i--){
            if(String.valueOf(b.charAt(i)).equals("/")) break;
            c[i]+=d[i];
        }
        return String.copyValueOf(c);
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
            post.setLastMod(new Date(result.getUpdated().getValue())); //result.getUpdated().getValue()
            PostLab.get(mContext).updatePost(post);
            Log.d("UPDATE",post.getTitle());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean downloadPost( Post post, Object params){
        com.google.api.services.blogger.model.Post result = (com.google.api.services.blogger.model.Post) params;
        post.setContent(result.getContent());
        post.setTitle(result.getTitle());
        post.setLastMod(new Date(result.getUpdated().getValue()) );
        PostLab.get(mContext).updatePost(post);
        return true;
    }
    public List downloadList(String BLOG_ID){
        com.google.api.services.blogger.Blogger.Posts.List getListPost = null;
        ArrayList results = new ArrayList(); // container
        try {
            getListPost = blog.posts().list(BLOG_ID);
            getListPost.setFields("items(id,content,updated,title),nextPageToken");

            PostList ListOfPost = getListPost.execute();
            //List ListOfPost = getListPostResults.getItems();
            int postCount = 0;
            int pageCount = 0;


            while (ListOfPost.getItems() != null && !ListOfPost.getItems().isEmpty()) {
                for ( int i = ListOfPost.getItems().size() -1 ; i >= 0;i-- ) {
                    //Post p = new Post();
                    results.add(ListOfPost.getItems().get(i));
                    //com.google.api.services.blogger.model.Post post = (com.google.api.services.blogger.model.Post)List_post.get(i);
//                    p.setContent(post.getContent());
//                    p.setTitle(post.getTitle());
//                    p.setLastMod(new Date(post.getUpdated().getValue()));
//                    p.setPostId(post.getId());
//                    e_p.add(p);
//                    Log.d("POST____",post.getTitle());
                }

                // Pagination logic
                String pageToken = ListOfPost.getNextPageToken();
                if (pageToken == null || ++pageCount >= 5) {
                    break;
                }
                //go next page
                getListPost.setPageToken(pageToken);
                ListOfPost = getListPost.execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return results;
    }

    public ArrayList returnNewPosts(List BloggerList, List LocalPost ){
        ArrayList results = new ArrayList();
        int count = 0;
        if( LocalPost.size() == 0){
            //all new post
            for(Object bp: BloggerList){
                com.google.api.services.blogger.model.Post bloggerPost = (com.google.api.services.blogger.model.Post)bp;
                results.add(bp);

            }
            return results;
        }
        for(Object bp: BloggerList){
            com.google.api.services.blogger.model.Post bloggerPost = (com.google.api.services.blogger.model.Post)bp;

            int j = 0;
            for(Object p: LocalPost){
                Post post = (Post)p;
                if(bloggerPost.getId().equals(post.getPostId())){
                    j++;
                }
            }
            if(j==0){
                results.add(bp);
            }

        }
        return results;
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
            List newPostsFromBlogger = new ArrayList();
            ArrayList<Post> paramsArray = new ArrayList<>();
            List localPosts = PostLab.get(mContext).getPosts();
           // List t = new ArrayList();
            if (params[0].equals("sync")){
                List BloggerPosts = downloadList(BLOG_ID);
                newPostsFromBlogger = returnNewPosts(BloggerPosts,localPosts);
                paramsArray = (ArrayList)params[1];
            }else {
                paramsArray = (ArrayList)params[1];
            }

            // Construct a post to insert
            for(Object p : paramsArray){
                // Post to work during sync or upload
                Post post = (Post) p;
                String postID = post.getPostId(); //Google's Blogger ID
                //content from model to fill
                com.google.api.services.blogger.model.Post content = new com.google.api.services.blogger.model.Post();
                //check if Google's Blogger ID exists
                List<Object> resultsFromPostId = (List)postIdExists(BLOG_ID,postID);

                if ( postID == null ){
//                    if(params[0].equals("sync") ){
//
//                            for (Object contentWithPostID :yu){
//                                Post yuo = (Post)contentWithPostID;
//                                if (post.getTitle().equals(yuo.getTitle()) && post.getContent().equals(yuo.getContent()) ){
//                                    post.setPostId(yuo.getPostId());
//                                    //add new post from blogger
//                                    PostLab.get(mContext).addPost(post);
//                                }
//                            }
//
//                    }else{
//                        //create new post on Google's Blogger
//                        uploadPost(BLOG_ID,content,post); //create post
//
//
//                    }
//                    if(params[0].equals("sync")){
//                        downloadPost(post,resultsFromPostId.get(1));
//                    }

                }else if(!postID.isEmpty() && (boolean)resultsFromPostId.get(0) ) {


                    if(params[0].equals("delete") ){
                        //delete from Google's Blogger
                        Blogger.Posts.Delete postsDeleteAction = blog.posts().delete(BLOG_ID, post.getPostId());
                        postsDeleteAction.execute();
                        continue;
                    }
                    //get Post from Blogger
                    com.google.api.services.blogger.model.Post run = (com.google.api.services.blogger.model.Post) resultsFromPostId.get(1);

                    //Compare dates
                    List comparisonDates = getDiff( new Date(run.getUpdated().getValue()) , post.getLastMod());
                    // Check if there's any different on dates
                    boolean dateEquals = (boolean)comparisonDates.get(0);

                    if (!dateEquals){
                        Log.d("BOOL",(dateEquals)?"true":"false");
                        Date bigger = (Date)comparisonDates.get(1);
                        Date smaller = (Date)comparisonDates.get(2);
                        Log.d("DATE BIG",  bigger.toString());
                        Log.d("DATE SMALL",smaller.toString());
                        // check the latest date
                        String whosLatest = (String)comparisonDates.get(3);

                        if (params[0].equals("upload")){
                            //Upload Post
                            updatePost(BLOG_ID,post);

                        }else if (params[0].equals("sync")){

                            if( whosLatest.equals("POST") ){
                                //Upload Post
                                updatePost(BLOG_ID,post);

                            }else if(whosLatest.equals("GOOGLE") ){
                                //Download Post
                                downloadPost(post,resultsFromPostId.get(1));
                            }
                        }
                    }

                }else if (!(boolean)resultsFromPostId.get(0)){
                    uploadPost(BLOG_ID,content,post); //upload post
                }

            }
            if(params[0].equals("sync")) {
                //add new post
                if (newPostsFromBlogger != null){
                    for(Object p: newPostsFromBlogger){
                        com.google.api.services.blogger.model.Post bloggerPost = (com.google.api.services.blogger.model.Post)p;
                        Post localPost = new Post();
                        localPost.setTitle(bloggerPost.getTitle());
                        localPost.setContent(bloggerPost.getContent());
                        localPost.setLastMod(new Date(bloggerPost.getUpdated().getValue()));
                        localPost.setPostId(bloggerPost.getId());
                        PostLab.get(mContext).addPost(localPost);
                    }
                }

            }

            return result;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object aVoid) {
        super.onPostExecute(aVoid);
        Log.d("FINISH_TASK","IM FINESHED");
    }

    public List getDiff(Date dateBlog, Date datePost){
        Calendar calendarBlog = Calendar.getInstance();
        Calendar calendarPost = Calendar.getInstance();
        calendarBlog.setTime(dateBlog);
        calendarPost.setTime(datePost);
        TimeZone.getDefault();
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        //format.setTimeZone(TimeZone.getTimeZone("UTC"));
        format.setTimeZone(TimeZone.getDefault());
        String postTime = format.format(calendarPost.getTime());
        String blogTime = format.format(calendarBlog.getTime());
        Date b = calendarBlog.getTime();
        Date p = calendarPost.getTime();

        try {
            b = format.parse(dateBlog.toString());
            p = format.parse(datePost.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(b!=null)Log.d("B_TIME",b.toString());
        //Date p = calendarPost.getTime();
        Log.d("POST TIME",postTime);
        Log.d("BLOG TIME",blogTime);
        calendarBlog.setTimeZone(TimeZone.getDefault());
        calendarPost.setTimeZone(TimeZone.getDefault());
        Log.d("Blog_CL_TIME",calendarBlog.getTime().toString());
        Log.d("Post_CL_TIME",calendarPost.getTime().toString());

        List arr = new ArrayList();


        if(b.after(p)) {
            arr.add(false);
            arr.add(dateBlog);
            arr.add(datePost);
            arr.add("GOOGLE");
            Log.d("ARR","GOOGLE");
        }else if(b.before(p)) {
            arr.add(false);
            arr.add(datePost);
            arr.add(dateBlog);
            arr.add("POST");
            Log.d("ARR","POST");

        }else {
            arr.add(true);
            arr.add(datePost);
            arr.add(dateBlog);
            arr.add("NONE");
        }
        return arr;
    }
}
