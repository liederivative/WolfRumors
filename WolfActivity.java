package uk.ac.wlv.wolfrumors;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;

import java.util.UUID;

public class WolfActivity extends SingleFragmentActivity {
    private static final String EXTRA_POST_ID ="uk.ac.wlv.wolfrumors.post_id";

    public static Intent newIntent(Context packageContext, UUID postId) {
        Intent intent = new Intent(packageContext, WolfActivity.class);
        intent.putExtra(EXTRA_POST_ID, postId);
        return intent;
    }

    @Override
    protected Fragment createFragment(){
        UUID postId = (UUID) getIntent().getSerializableExtra(EXTRA_POST_ID);
        return FragmentPost.newInstance(postId);
    }

}
