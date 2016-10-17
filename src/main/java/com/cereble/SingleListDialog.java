package com.cereble;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.Toast;

public class SingleListDialog extends DialogFragment {
	private int selectedItemIndex=0;
	private int clickedItemIndex = selectedItemIndex;
	String[] options;
	String dialog_title;
	AlertDialog dialog;
	public interface GetResultDialogListner {
		void onDialogFinish(int selected);
	}
	public SingleListDialog(String title,String[] choices) {
		super();
		options=choices;
		dialog_title=title;
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity())
		.setTitle(dialog_title)
		.setSingleChoiceItems(options, selectedItemIndex, new OnClickListener() {
		
			@Override
			public void onClick(DialogInterface arg0, int which) {
				// TODO Auto-generated method stub
				clickedItemIndex=which;
				Toast.makeText(getActivity(), options[which], Toast.LENGTH_LONG).show();
			}
		})
		.setPositiveButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				selectedItemIndex=clickedItemIndex;
				GetResultDialogListner activity = (GetResultDialogListner) getActivity();
				activity.onDialogFinish(selectedItemIndex);
				
			}
		})
		.setNegativeButton("Cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				
			}
		});
		dialog = alertDialogBuilder.create();
		return dialog;
		
	}
	
	

}
