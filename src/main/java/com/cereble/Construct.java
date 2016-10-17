package com.cereble;

import java.io.BufferedReader;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager.WakeLock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.Toast;

public class Construct extends Activity implements OnTouchListener {
	WakeLock wL;
	EditText textBox;
	AnimateView aw;
	int consFileNumber = 1;
	String fileName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.text_box_layout);
		textBox = (EditText) findViewById(R.id.etTextBox);
//		SharedPreferences spDataFile = ProcessHWDebug.getDefaults("01.caw", ScanHWDebug.this);
		aw = new AnimateView(this);
		fileName = "01.caw";
		aw.consFileName = fileName;
		setContentView(aw);
//		if (i < 9)
//			filename = "0" + (i + 1) + ".jpg";
//		else
//			filename = (i + 1) + ".jpg";
			
		
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		aw.pause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		aw.resume();
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		aw.pause();
		consFileNumber++;
		if (consFileNumber < 9)
			aw.consFileName = "0" + (consFileNumber) + ".caw";
		else
			aw.consFileName = (consFileNumber) + ".caw";
		
		aw.resume();
		return false;
	}

	
}
