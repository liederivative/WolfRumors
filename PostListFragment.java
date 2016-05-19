package uk.ac.wlv.wolfrumors;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Fragment List of Post.
 *
 * @author Albert Jimenez
 *  Created:
 *  12 May 2016
 *  Reference:
 *  Phillips, B., Hardy, B. and Big Nerd Ranch (2015) Android Programming: The Big Nerd Ranch Guide. Big Nerd Ranch.
 *
 */
public class PostListFragment extends Fragment{
    private RecyclerView mPostRecyclerView;
    private PostAdapter mAdapter;
    static final int SYNC_OAUTH_REQUEST = 2096;
    static final int DELETE_DIALOG = 2097;
    static final int UPLOAD_OAUTH_REQUEST = 2098;
    static final int UPLOAD_DIALOG = 2099;
    boolean mState = false; // setting state
    private MultiSelector mMultiSelector = new MultiSelector();
    private String refreshToken;
    OAuthHelper oauthHandler = new OAuthHelper();
    private ProgressDialog loadDialog;
    private ThreadService<String> thread;
    private List<Post> posts;
    private ArrayList<Post> checked;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        oauthHandler.init(getContext());
        refreshToken = oauthHandler.getRefreshToken();

        final Handler responseHandler = new Handler(Looper.getMainLooper());
        ArrayList<Object> init = new ArrayList<>();
        init.add(oauthHandler.getAccessToken(refreshToken));
        init.add(getContext());

        thread = new ThreadService<>(responseHandler, init);
        thread.setBloggerServiceListener( new ThreadService.BloggerListener<String>(){
            @Override
            public void onPostExecutionBlogger(String target, ArrayList results) {
                //loadDialog.dismiss();
                //manageDialog("null","dismiss");
                responseHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateUI();
                        loadDialog.dismiss();
                    }
                });


                Log.d("ThreadService","PostListFragment "+ results.toString());

            }
        });
        thread.start();
        thread.getLooper();
        Log.i("THREAD","Started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        thread.quit();
        //PostLab.get(getContext()).close();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        thread.clearQueue();
    }


    public void manageDialog(String msg,String action){
        if(!action.equals("dismiss")){
            loadDialog = new ProgressDialog(getContext());
            loadDialog.setMessage(msg);
            loadDialog.setIndeterminate(false);
            loadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loadDialog.setCancelable(true);
            loadDialog.show();
        }else {
            loadDialog.dismiss();
        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);
        mPostRecyclerView = (RecyclerView) view.findViewById(R.id.post_recycler_view);
        mPostRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        posts = getPosts();
        refreshToken = oauthHandler.getRefreshToken();
        updateUI();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_post_list_menu, menu);
        MenuItem deleteItem = menu.findItem(R.id.menu_item_delete_post);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

//        Log.d("TAG",Integer.toString(info.position));
        switch(item.getItemId()){
            case R.id.menu_item_new_post:
                Post post = new Post();
                PostLab.get(getActivity()).addPost(post);
                Intent intent = PostPagerActivity.newIntent(getActivity(),post.getId());
                startActivity(intent);
                return true;
            case R.id.menu_item_sync_post:
                //syncPosts();
                if((refreshToken == null)?true:refreshToken.isEmpty()){
                Intent share = new Intent();
                share.setClass(getContext(), OAuthHelper.class);
                startActivityForResult(share, SYNC_OAUTH_REQUEST);
                }else{
                    syncPosts("sync",getString(R.string.progress_dialog_sync),(ArrayList<Post>) posts);

                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        if (requestCode == SYNC_OAUTH_REQUEST){

            if (data != null) {
                String token = data.getStringExtra("access_token");
                Log.d("OAUTH", token);
                syncPosts("sync",getString(R.string.progress_dialog_sync),(ArrayList<Post>) posts);
            }

        }else if (requestCode == DELETE_DIALOG){
            ArrayList<String> params = data.getStringArrayListExtra("array.params");
            if(params.get(0).equals("delete")){
                if(params.get(1).equals("YES")){
                    //Delete posts
                    for (int i = posts.size(); i >= 0; i--) {
                        if (mMultiSelector.isSelected(i, 0)) { // (1)
                            // remove item from list
                            PostLab.get(getActivity()).deletePost(posts.get(i));
                            posts.remove(i);
                            mAdapter.setPosts(posts);
                            mAdapter.notifyItemRemoved(i);
                        }
                    }
                    //syncPosts("delete",getString(R.string.progress_dialog_delete),checked);
                    mMultiSelector.clearSelections();
                }
            }
        }else if (requestCode == UPLOAD_OAUTH_REQUEST){

        }else if (requestCode == UPLOAD_DIALOG){

            syncPosts("upload",getString(R.string.progress_dialog_upload),checked);

        }
    }

    public void updateUI() {
        List<Post> posts = getPosts();
        if (mAdapter ==null){
            if(isAdded()){
                mAdapter = new PostAdapter(posts);
                mPostRecyclerView.setAdapter(mAdapter);
            }
        } else {
            mAdapter.setPosts(posts);
            mAdapter.notifyDataSetChanged();
        }
    }
    public List<Post> getPosts(){
        PostLab postlab = PostLab.get(getActivity());
        List<Post> posts = postlab.getPosts();
        return posts;
    }
    private void sharePost(){

    }
    private void syncPosts(String msg, String progressMsg, ArrayList<Post> posts){
        if(refreshToken != null){

            loadDialog = new ProgressDialog(getContext());
            loadDialog.setMessage(progressMsg);
            loadDialog.setIndeterminate(false);
            loadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loadDialog.setCancelable(false);
            loadDialog.show();

            ArrayList<Object> params = new ArrayList<>();
            params.add(msg);
            params.add(posts);
            thread.queueBlogger("PO",params);
        }
    }


    /////////////////////////////////////////////////
    //keep track of the visual element of each Post
    //private class PostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public ArrayList<Post> getSelectedPost(List<Post> posts){
        ArrayList<Post> contents = new ArrayList<>();
        for (int a = (posts.size()-1); a >= 0; a--) {
            if (mMultiSelector.isSelected(a, 0)) { // (1)
                //
                contents.add(posts.get(a));
            }
        }
        return contents;
    }
    //fixes no multiple selection of items on LongClick event
    private ModalMultiSelectorCallback mActionModeCallback
            = new ModalMultiSelectorCallback(mMultiSelector) {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            super.onCreateActionMode(actionMode, menu);
            getActivity().getMenuInflater().inflate(R.menu.post_list_delete, menu);

            return true;
        }
        @Override

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu){


            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {


            checked = getSelectedPost(posts);

            if(checked.size() !=0){
                switch(menuItem.getItemId()){
                    case R.id.menu_item_delete_post:
                        actionMode.finish();
                        ArrayList<Object> params = new ArrayList<>();
                        params.add("delete");
                        params.add(refreshToken);
                        DialogFragment m = NoticeDialog.newInstance(params);
                        m.setTargetFragment(PostListFragment.this, DELETE_DIALOG);
                        m.show(getFragmentManager(),"NoticeDialog");

                        return true;
                    case R.id.menu_item_share_post:
                        Intent i = new Intent();
                        if (checked.size() > 1){
                            Toast.makeText(getActivity(),R.string.share_multiples, Toast.LENGTH_SHORT).show();
                            mMultiSelector.clearSelections();
                        }else {
                            actionMode.finish();
                            i.setAction(android.content.Intent.ACTION_SEND);
                            i.putExtra(android.content.Intent.EXTRA_SUBJECT,checked.get(0).getTitle());
                            i.putExtra(android.content.Intent.EXTRA_TEXT, checked.get(0).getContent());
                            Post post = checked.get(0);
                            if(post.getPhotoPath()!=null){
                                File photo = new File(post.getPhotoPath());
                                if(photo.exists()){
                                    i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(photo));
                                }
                            }
                            i.setType("text/plain");
                            Log.d("TAG",checked.toString());
                            startActivity(Intent.createChooser(i,getString(
                                    R.string.share_post_string_button)));
                        }

                        return true;
                    case R.id.menu_item_uplodad_post:

                        if ( (refreshToken == null)?true:refreshToken.isEmpty()) {

                            Toast.makeText(getActivity(),R.string.login_auth_error, Toast.LENGTH_SHORT).show();
                            Intent share = new Intent();
                            share.setClass(getContext(), OAuthHelper.class);
                            startActivityForResult(share, UPLOAD_OAUTH_REQUEST);


                        }else if (!refreshToken.isEmpty()) {

                            Log.d("CLASS", refreshToken);
                            ArrayList<Object> paramsUpload = new ArrayList<>();
                            paramsUpload.add("upload");
                            DialogFragment uploadDialog = NoticeDialog.newInstance(paramsUpload);

                            uploadDialog.setTargetFragment(PostListFragment.this, UPLOAD_DIALOG);
                            uploadDialog.show(getFragmentManager(),"UploadDialog");

                        }

                        actionMode.finish();
                        return true;
                }
            }else {
                Toast.makeText(getActivity(), R.string.post_selection_error, Toast.LENGTH_SHORT).show();
                actionMode.finish();
            }

            return false;
        }
    };
    private class PostHolder extends SwappingHolder {

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private Post mPost;

        public PostHolder(View itemView) {
            super(itemView, mMultiSelector);
            itemView.setLongClickable(true);
            mTitleTextView = (TextView)itemView.findViewById(R.id.list_item_post_title_text_view);
            mDateTextView = (TextView)itemView.findViewById(R.id.list_item_post_date_text_view);

            itemView.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View view) {
                    if (!mMultiSelector.isSelectable()) {
                        ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                        mMultiSelector.setSelectable(true);
                        mMultiSelector.setSelected(PostHolder.this, true);
                        int r = mMultiSelector.getSelectedPositions().size();

                        //Toast.makeText(getActivity(),Integer.toString(r) + " OnLongClick Clicked!",Toast.LENGTH_SHORT).show();

                        return true;
                    }
                    return false;
                }
            });
            itemView.setOnClickListener( new View.OnClickListener(){
                @Override
                public void onClick(View view) {

                    if (!mMultiSelector.tapSelection(PostHolder.this)){

                        //Toast.makeText(getActivity(),"tapSelection Clicked!",Toast.LENGTH_SHORT).show();
                        //Start Activity for Post's edition
                        Intent intent = PostPagerActivity.newIntent(getActivity(), mPost.getId());
                        startActivity(intent);

                    }
                    //Toast.makeText(getActivity(),mPost.getTitle() + "Clicked!",Toast.LENGTH_SHORT).show();
                }
            });
        }

        ///////////////////////////////////
        public void bindPost(Post post) {
            mPost = post;
            mTitleTextView.setText(mPost.getTitle());
            String dateString = DateFormat.getDateTimeInstance().format(mPost.getLastMod());
            mDateTextView.setText(dateString);

        }

    }
    //"interpreter" to talk the data objects
    //also know about how to put the date into the visual elements
    private class PostAdapter extends RecyclerView.Adapter<PostHolder> {
        private List<Post> mPosts;

        public PostAdapter(List<Post> posts) {
            mPosts = posts;
        }
        //create the View and wrap it in a ViewHolder
        @Override
        public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_post , parent, false);
            return new PostHolder(view);
        }
        //bind a ViewHolder’s View to model data object
        @Override
        public void onBindViewHolder(PostHolder holder, int position) {
            Post post = mPosts.get(position);
            holder.bindPost(post);

        }
        @Override
        public int getItemCount() {
            return mPosts.size();
        }

        public void setPosts(List<Post> posts){
            mPosts = posts;
        }


    }


}
