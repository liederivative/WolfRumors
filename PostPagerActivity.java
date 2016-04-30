package uk.ac.wlv.wolfrumors;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.List;
import java.util.UUID;

/**
 * Created by user on 4/27/2016.
 */
//AppCompatActivity is a subclass of FragmentActivity
public class PostPagerActivity extends AppCompatActivity {
    private static final String EXTRA_POST_ID = "uk.ac.wlv.wolfrumors.post_id";
    private ViewPager mViewPager;
    private List<Post> mPosts;

    public static Intent newIntent(Context packageContext, UUID crimeId) {
        Intent intent = new Intent(packageContext, PostPagerActivity.class);
        intent.putExtra(EXTRA_POST_ID, crimeId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_pager);
        UUID postId = (UUID) getIntent().getSerializableExtra(EXTRA_POST_ID);
        mViewPager = (ViewPager) findViewById(R.id.activity_post_pager_view_pager);
        mPosts = PostLab.get(this).getPosts();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Post post = mPosts.get(position);
                return FragmentPost.newInstance(post.getId());
            }

            @Override
            public int getCount() {
                return mPosts.size();
            }
        });
        for (int i=0;i<mPosts.size();i++ ){
            if (mPosts.get(i).getId().equals(postId)){
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }


}
