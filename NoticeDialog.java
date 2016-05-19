package uk.ac.wlv.wolfrumors;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for dialog of the system .
 *
 * @author Albert Jimenez
 *  Created:
 *  30 April 2016
 *  Reference:
 *  Phillips, B., Hardy, B. and Big Nerd Ranch (2015) Android Programming: The Big Nerd Ranch Guide. Big Nerd Ranch.
 *
 */
public class NoticeDialog extends DialogFragment {
    public static final String ARRAY_EXTRA = "array.params";
    private ArrayList<Object> resultParams = new ArrayList<>();
    private NoticeDialogListener mNoticeDialogListener;

    public interface NoticeDialogListener{
        void onResults (ArrayList<Object> params);
    }
    public void setNoticeDialogListener ( NoticeDialogListener listener){
        mNoticeDialogListener = listener;
    }
    public NoticeDialog(NoticeDialogListener listener){
        super();
        this.mNoticeDialogListener = listener;
    }
    public NoticeDialog(){
        super();
    }
    public static NoticeDialog newInstance(ArrayList<Object> params){
        Bundle args = new Bundle();
        args.putSerializable("params",params);

        NoticeDialog fragment = new NoticeDialog();

        fragment.setArguments(args);
        return fragment;
    }
    private void sendResults(int resultCode, ArrayList params){
        if(getTargetFragment() == null){
            return;
        }
        Intent intent = new Intent();
        intent.putStringArrayListExtra(ARRAY_EXTRA,params);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
    private AlertDialog.Builder inflateYesNoQuestion (AlertDialog.Builder builder,int msgId){

        builder.setMessage(msgId)
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        sendResults(Activity.RESULT_OK,resultParams);
                    }
                })
                .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        NoticeDialog.this.getDialog().cancel();

                    }
                });
        return builder;
    }
    private AlertDialog.Builder inflateIntentSelection (AlertDialog.Builder builder, int layout, String title){
        builder.setTitle("Select source")
                .setItems(layout, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                resultParams.add(0);
                                mNoticeDialogListener.onResults(resultParams);
                                //sendResults(Activity.RESULT_OK,resultParams);
                                return;
                            case 1:
                                resultParams.add(1);
                                mNoticeDialogListener.onResults(resultParams);
                                //sendResults(Activity.RESULT_OK,resultParams);
                                return;
                        }
                    }
                });
        return builder;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        ArrayList<Object> params = (ArrayList<Object>) getArguments().getSerializable("params");


        if(params.get(0).equals("delete")){
            //LayoutInflater inflater = getActivity().getLayoutInflater();
            resultParams.add("delete");
            resultParams.add("YES");

            if ((String)params.get(1) != null){
                builder = inflateYesNoQuestion(builder,R.string.dialog_delete);
            }else {
                builder = inflateYesNoQuestion(builder,R.string.dialog_delete_nosync);
            }
        }else if (params.get(0).equals("upload")){
            //LayoutInflater inflater = getActivity().getLayoutInflater();
            resultParams.add("upload");
            resultParams.add("YES");
            builder = inflateYesNoQuestion(builder,R.string.dialog_upload);
        }else if (params.get(0).equals("refreshPhoto")){
            builder = inflateIntentSelection(builder,R.array.intent_menu, "Select Source");
        }

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
