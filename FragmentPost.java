package uk.ac.wlv.wolfrumors;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by user on 4/26/2016.
 */
public class FragmentPost extends Fragment {

    private Post mPost;
    private EditText mTitleField;
    private EditText mContentField;
    private TextView mDateText;
    private ImageButton mCameraButton;
    private ImageButton mPhotoButton;

    private ImageView mPhotoView;
    private File mPhotoFile;
    private static final String ARG_POST_ID = "post_id";
    private static final int REQUEST_PHOTO = 1024;
    private static final int REQUEST_PHOTO_GALLERY = 2048;

    public static FragmentPost newInstance(UUID postId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_POST_ID, postId);
        FragmentPost fragment = new FragmentPost();
        fragment.setArguments(args);
        return fragment;
    }


    private void updatePhotoView(){
        // refresh ImageView

        if (mPost.getStatusPhoto()){
            mPhotoFile = PostLab.get(getActivity()).getPhotoFile(mPost);
            if (mPhotoFile == null|| !mPhotoFile.exists()){
                mPhotoView.setImageDrawable(null);
            }else {
                Bitmap bitmap = BitmapHelper.getScaleBitmap(mPhotoFile.getPath(),getActivity());
                mPhotoView.setImageDrawable(null);
                mPhotoView.setImageBitmap(bitmap);


            }
        }else {
            Uri uri = Uri.parse(mPost.getPhotoFilename());
            mPhotoView.setImageDrawable(null);
            mPhotoView.setImageURI(uri);

        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //mPost = new Post();
        UUID postId = (UUID) getArguments().getSerializable(ARG_POST_ID);
        mPost = PostLab.get(getActivity()).getPost(postId);
       // mPhotoFile = PostLab.get(getActivity()).getPhotoFile(mPost);


    }

    // write to db when FragmentPost is done
    @Override
    public void onPause(){
        super.onPause();
        PostLab.get(getActivity()).updatePost(mPost);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {



        View v = inflater.inflate(R.layout.fragment_post, container, false);
        mTitleField = (EditText) v.findViewById(R.id.post_title);
        mContentField = (EditText) v.findViewById(R.id.post_content);
        mCameraButton = (ImageButton) v.findViewById(R.id.post_camera_button);
        mPhotoButton = (ImageButton) v.findViewById(R.id.post_photo_button);

        mPhotoView = (ImageView) v.findViewById(R.id.post_photo);

        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentSelector u = new intentSelector();
                u.show(getFragmentManager(),"SELECT");
            }
        });
        mTitleField.setText(mPost.getTitle());
        mContentField.setText(mPost.getContent());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                mPost.setTitle(s.toString());
                mPost.updateLastMod();
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
                mPost.setContent(s.toString());
                mPost.updateLastMod();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        ///// Photo Button
        final PackageManager packageManager = getActivity().getPackageManager();
        final Intent captureImage = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
        //final Intent getImage = new Intent();
        //getImage.setType("image/*");
        //getImage.setAction(Intent.ACTION_GET_CONTENT);
        final Intent getImage = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        getImage.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);




        mCameraButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                mPhotoFile = PostLab.get(getActivity()).getPhotoFile(mPost);
                // mPhotoView.setImageDrawable(null);
                boolean canTakePhoto = mPhotoFile !=null
                        && captureImage.resolveActivity(packageManager) != null;
                mPost.setPhotoPath("",true);
                mCameraButton.setEnabled(canTakePhoto);
                if (canTakePhoto){
                    Uri uri = Uri.fromFile(mPhotoFile);
                    captureImage.putExtra(MediaStore.EXTRA_OUTPUT,uri);

                }
                // Create intent for picking a photo from the gallery

                startActivityForResult(captureImage,REQUEST_PHOTO);
            }
        });
        mPhotoButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                if (getImage.resolveActivity(packageManager) != null) {
                    // Bring up gallery to select a photo
                    //startActivityForResult(getImage, REQUEST_PHOTO_GALLERY);
                    startActivityForResult(getImage, REQUEST_PHOTO_GALLERY);

                }

                //startActivityForResult(captureImage,REQUEST_PHOTO);
            }
        });

        String dateString = DateFormat.getDateTimeInstance().format(mPost.getLastMod());
        mDateText = (TextView) v.findViewById(R.id.post_date);
        mDateText.setText(dateString);
        mDateText.setEnabled(false);
        updatePhotoView();

        return v;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK){
            return;
        }else if (requestCode == REQUEST_PHOTO ){
            updatePhotoView();
        }else if (requestCode == REQUEST_PHOTO_GALLERY && data !=null){
            Uri photoUri = data.getData();

            String[] filePathColumn = new String[] { MediaStore.Images.Media.DATA };
            try{

                Cursor cursor = getActivity().getContentResolver().query(photoUri,
                        filePathColumn, null, null, null);

            try {
                if (cursor.getCount() == 0){
                    return;
                }

                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                mPost.setPhotoPath(picturePath,false);
            }finally {
                cursor.close();
            }
            }catch (Exception e){

                Log.d("EXCEP",  e.toString() + " Error");
            }
            //mPhotoFile = PostLab.get(getActivity()).getPhotoFile(mPost);
            //mPhotoView.setImageURI(photoUri);
            updatePhotoView();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getActivity(), "Post changes have been saved.", Toast.LENGTH_SHORT).show();
    }
    public class intentSelector extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Select source")
                    .setItems(R.array.intent_menu , new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                            switch (which){
                                case 0:
                                    PackageManager packageManager = getActivity().getPackageManager();
                                    Intent captureImage = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);

                                    mPhotoFile = PostLab.get(getActivity()).getPhotoFile(mPost);
                                    // mPhotoView.setImageDrawable(null);
                                    boolean canTakePhoto = mPhotoFile !=null
                                            && captureImage.resolveActivity(packageManager) != null;
                                    mPost.setPhotoPath("",true);
                                    mCameraButton.setEnabled(canTakePhoto);
                                    if (canTakePhoto){
                                        Uri uri = Uri.fromFile(mPhotoFile);
                                        captureImage.putExtra(MediaStore.EXTRA_OUTPUT,uri);

                                    }
                                    // Create intent for picking a photo from the gallery

                                    startActivityForResult(captureImage,REQUEST_PHOTO);

                                case 1:
                                    return;

                            }
                        }
                    });
            return builder.create();

        }


    }
}
