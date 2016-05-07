package uk.ac.wlv.wolfrumors;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

/**
 * Created by user on 4/26/2016.
 */
public class PostListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment(){
        return new PostListFragment();
    }


}
