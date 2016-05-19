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
 * Extends from SingleFragmentActivity.
 *
 * @author Albert Jimenez
 *  Created:
 *  26 April 2016
 *  Reference:
 *  Phillips, B., Hardy, B. and Big Nerd Ranch (2015) Android Programming: The Big Nerd Ranch Guide. Big Nerd Ranch.
 *
 */

public class PostListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment(){
        return new PostListFragment();
    }


}
