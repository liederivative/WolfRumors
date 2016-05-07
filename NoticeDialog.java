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

public class NoticeDialog extends DialogFragment {
    public static final String ARRAY_EXTRA = "array.params";
    private ArrayList<String> resultParams = new ArrayList<>();
    public static NoticeDialog newInstance(ArrayList<String> params){
        Bundle args = new Bundle();
        args.putStringArrayList("params",params);
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
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        ArrayList<String> params = (ArrayList) getArguments().getStringArrayList("params");


        if(params.get(0).equals("delete")){
            LayoutInflater inflater = getActivity().getLayoutInflater();
            builder.setMessage("Are you sure?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            resultParams.add("delete");
                            resultParams.add("YES");
                            sendResults(Activity.RESULT_OK,resultParams);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            NoticeDialog.this.getDialog().cancel();

                        }
                    });
        }

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
