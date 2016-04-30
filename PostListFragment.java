package uk.ac.wlv.wolfrumors;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.app.ActionBarActivity;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by user on 4/26/2016.
 */
public class PostListFragment extends Fragment {
    private RecyclerView mPostRecyclerView;
    private PostAdapter mAdapter;
    private boolean mdeleteVisible = false;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //////////////////
        //Intent i = BloggerService.newIntent(getActivity());
        //getActivity().startActivity(i);

        //////////////////
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
        updateUI();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_post_list, menu);
        MenuItem deleteItem = menu.findItem(R.id.menu_item_delete_post);
        // hide option until an element is created
        if (mdeleteVisible){
            deleteItem.setVisible(false);
            getActivity().invalidateOptionsMenu();
        }
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void updateUI() {
        PostLab postlab = PostLab.get(getActivity());
        List<Post> posts = postlab.getPosts();
        //Log.d("TAG",posts.toString());
        if (mAdapter ==null){
            mAdapter = new PostAdapter(posts);

            mPostRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setPosts(posts);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void sharePost(){

    }
    private void syncPosts(){

    }
    private MultiSelector mMultiSelector = new MultiSelector();
    /////////////////////////////////////////////////
    //keep track of the visual element of each Post
    //private class PostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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


            PostLab postlab = PostLab.get(getActivity());
            List<Post> posts = postlab.getPosts();

            switch(menuItem.getItemId()){
                case R.id.menu_item_delete_post:
                    actionMode.finish();
                    for (int i = posts.size(); i >= 0; i--) {
                        if (mMultiSelector.isSelected(i, 0)) { // (1)
                            // remove item from list
                            PostLab.get(getActivity()).deletePost(posts.get(i));
                            posts.remove(i);
                            mAdapter.setPosts(posts);
                            mAdapter.notifyItemRemoved(i);
                        }
                    }
                    mMultiSelector.clearSelections(); // (2)

                    return true;
                case R.id.menu_item_share_post:
                    Post post = posts.get(0);

                    Intent i = new Intent();
                    ArrayList<String> contents = new ArrayList<>();
                    for (int a = posts.size(); a >= 0; a--) {
                        if (mMultiSelector.isSelected(a, 0)) { // (1)
                            //
                            contents.add(posts.get(a).getContent());
                        }
                    }
                    if (contents.size() > 1){
                         i.setAction(Intent.ACTION_SEND_MULTIPLE);
                        i.putExtra(Intent.EXTRA_TEXT,contents);

                    }else {
                        i.setAction(Intent.ACTION_SEND);
                        i.putExtra(Intent.EXTRA_TEXT,contents.get(0));
                        i.putExtra(Intent.EXTRA_SUBJECT,"Post from Wolfrumors");
                    }
                    i.setType("text/plain");
                    Log.d("TAG",contents.toString());

                    startActivity(
                            Intent.createChooser(i,
                                    getString(
                                            R.string.share_post_string_button)));


                    //Intent i = new Intent(Intent.ACTION_SEND);
                    //i.setType("text/plain");

                    mMultiSelector.clearSelections();
                    return true;
                case R.id.menu_item_uplodad_post:
                    return true;
            }
            return false;
        }
    };
    private class PostHolder extends SwappingHolder {

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private CheckBox mSolvedCheckBox;
        private Post mPost;

        public PostHolder(View itemView) {
            super(itemView, mMultiSelector); // (2)
            itemView.setLongClickable(true);
            mTitleTextView = (TextView)itemView.findViewById(R.id.list_item_post_title_text_view);
            mDateTextView = (TextView)itemView.findViewById(R.id.list_item_post_date_text_view);

            itemView.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View view) { // (6)
                    if (!mMultiSelector.isSelectable()) { // (3)
                        ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                        mMultiSelector.setSelectable(true); // (4)
                        mMultiSelector.setSelected(PostHolder.this, true); // (5)
                        int r = mMultiSelector.getSelectedPositions().size();

                        Toast.makeText(getActivity(),Integer.toString(r) + " OnLongClick Clicked!",Toast.LENGTH_SHORT).show();

                        return true;
                    }
                    return false;
                }
            });
            itemView.setOnClickListener( new View.OnClickListener(){
                @Override
                public void onClick(View view) {

                    if (!mMultiSelector.tapSelection(PostHolder.this)){
                        // do whatever we want to do when not in selection mode
                        // perhaps navigate to a detail screen
                        Toast.makeText(getActivity(),"tapSelection Clicked!",Toast.LENGTH_SHORT).show();

                        Intent intent = PostPagerActivity.newIntent(getActivity(), mPost.getId());
                        //Intent intent = WolfActivity.newIntent(getActivity(),mPost.getId());
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
            Log.d("TAG",Integer.toString(holder.getAdapterPosition()));

            //holder.mTitleTextView.setText(post.getTitle());
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