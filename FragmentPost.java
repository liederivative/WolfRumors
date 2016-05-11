package uk.ac.wlv.wolfrumors;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by user on 4/26/2016.
 */
public class FragmentPost extends Fragment {

    private Post mPost;
    private EditText mTitleField;
    private EditText mContentField;
    private TextView mDateText;
    private boolean modified = false;
    private ImageView mPhotoView;
    private File mPhotoFile;
    private static final String ARG_POST_ID = "post_id";
    private static final int REQUEST_PHOTO_FROM_CAMERA = 1024;
    private static final int REQUEST_PHOTO_FROM_GALLERY = 2048;
    private ArrayList<Object> params = new ArrayList<>();
    public static FragmentPost newInstance(UUID postId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_POST_ID, postId);
        FragmentPost fragment = new FragmentPost();
        fragment.setArguments(args);
        return fragment;
    }


    private void updatePhotoView(){
        // refresh ImageView
            if (mPhotoFile == null|| !mPhotoFile.exists()){
                mPhotoView.setImageDrawable(null);
            }else {
                mPhotoView.setImageDrawable(null);
                Bitmap bitmap = BitmapHelper.getScaleBitmap(mPhotoFile.getAbsolutePath(),getActivity());
                mPhotoView.setImageBitmap(bitmap);
            }
    }

    @Override
    public void onResume() {
        super.onResume();
        //When goes back from Intent
        updatePhotoView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
       // thread.quit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //mPost = new Post();

        params.add("refreshPhoto");
        UUID postId = (UUID) getArguments().getSerializable(ARG_POST_ID);
        mPost = PostLab.get(getActivity()).getPost(postId);
        mPhotoFile = PostLab.get(getActivity()).getPhotoFile(mPost);

    }

    // write to db when FragmentPost is done
    @Override
    public void onPause(){
        super.onPause();
        if(mPost != null){
            if(mPost.getTitle() != null){
                if(!mPost.getTitle().isEmpty()){
                    //Just saved when changed
                    if(modified){
                        PostLab.get(getActivity()).updatePost(mPost);
                    }
                }else {
                    PostLab.get(getContext()).deletePost(mPost);
                }
            }else{
                PostLab.get(getContext()).deletePost(mPost);
            }
        }

    }
    public void setValuePost(String text, int value){
        switch (value){
            case(1):
                mPost.setTitle(text);
            break;
            case(2):
                mPost.setContent(text);
        }
        mPost.updateLastMod();
        modified = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_post, container, false);
        mTitleField = (EditText) v.findViewById(R.id.post_title);
        mContentField = (EditText) v.findViewById(R.id.post_content);

        mPhotoView = (ImageView) v.findViewById(R.id.post_photo);

        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                DialogFragment intentChooser = new NoticeDialog(new NoticeDialog.NoticeDialogListener() {
                    @Override
                    public void onResults(ArrayList<Object> params) {
                        Intent chooser;
                        switch ((Integer) params.get(0)) {
                            case 0:
                                chooser = createCameraIntent();
                                if (chooser.resolveActivity(getActivity().getPackageManager()) != null) {

                                    startActivityForResult(chooser, REQUEST_PHOTO_FROM_CAMERA);
                                }
                                break;
                            case 1:
                                chooser = createGalleryIntent();
                                if (chooser.resolveActivity(getActivity().getPackageManager()) != null) {
                                    startActivityForResult(chooser, REQUEST_PHOTO_FROM_GALLERY);
                                }
                                break;
                        }
                    }
                });
                Bundle args = new Bundle();
                args.putSerializable("params",params);
                intentChooser.setArguments(args);
                intentChooser.show(getFragmentManager(), "IntentSource");

            }
        });
        // fix error when swiping deleted blank post
        if(mPost != null) {
            mTitleField.setText(mPost.getTitle());
            if(mPost.getContent() != null){
                mContentField.setText(Html.fromHtml(mPost.getContent()));
            }
            mTitleField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(
                        CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(
                        CharSequence s, int start, int before, int count) {
                        setValuePost(s.toString(),1);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            mContentField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(
                        CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(
                        CharSequence s, int start, int before, int count) {
                            setValuePost(s.toString(),2);

                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });


            String dateString = DateFormat.getDateTimeInstance().format(mPost.getLastMod());
            mDateText = (TextView) v.findViewById(R.id.post_date);
            mDateText.setText(dateString);
            mDateText.setEnabled(false);
            updatePhotoView();


        }
        return v;
    }
    public Intent createCameraIntent(){
        //CAMERA
        mPost.setPhotoPath("",true);
        PackageManager packageManager = getActivity().getPackageManager();
        Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mPhotoFile = PostLab.get(getActivity()).getPhotoFile(mPost);

        boolean canTakePhoto = mPhotoFile !=null
                && captureImage.resolveActivity(packageManager) != null;
        modified = true;
        if (canTakePhoto){
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        }
        return captureImage;
    }
    public Intent createGalleryIntent(){
        //Gallery
        Intent getImage = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        return getImage;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;

        }else if (requestCode == REQUEST_PHOTO_FROM_CAMERA){
            updatePhotoView();

        }else if (requestCode == REQUEST_PHOTO_FROM_GALLERY && data !=null){
            Uri photoUri = data.getData();

            String[] filePathColumn = new String[] { MediaStore.Images.Media.DATA };
            try{
                Cursor cursor = getActivity().getContentResolver().query(photoUri,
                        filePathColumn, null, null, null);
            try {
                if (cursor == null){
                    return;
                }
                if (cursor.getCount() == 0){
                    return;
                }

                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                mPost.setPhotoPath(picturePath,false);
                mPhotoFile = new File(mPost.getPhotoPath());
                modified = true;
            }finally {
                cursor.close();
            }
            }catch (Exception e){
                Log.d("EXCEP",  e.toString() + " Error");
            }
            updatePhotoView();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(modified){
            Toast.makeText(getContext(), R.string.toast_saved_post, Toast.LENGTH_SHORT).show();
        }

    }

}
