package com.cereble;

import java.io.File;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.android.camera.CropImageIntentBuilder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;



public class ScanHW extends Activity implements OnClickListener {

	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/SimpleAndroidOCR/";
	private static final String TAG = "ScanHW.java";
	protected Button takepic,selectpic;
	protected String _path;
	protected boolean _taken;
	protected Bitmap pic;
	protected static final String PHOTO_TAKEN = "photo_taken";
	public static EditText etShowData;
	public static Button bTest; 
	int PICK_IMAGE_REQUEST = 1,TAKE_IMAGE_REQUEST=2,REQUEST_CROP_PICTURE=3;
	public static ImageView imagePreview;
	Uri outputFileUri;
	Bitmap bmp;

	static {
	    if (!OpenCVLoader.initDebug()) {
	        // Handle initialization error
	    	Log.i(TAG, "Zaki! It failed");
	    }
	}

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan_hw);
		takepic = (Button) findViewById(R.id.bscanTakePic);
		selectpic = (Button) findViewById(R.id.bscanSelectPic);
		takepic.setOnClickListener(this);
		selectpic.setOnClickListener(this);
		_path = DATA_PATH + "/handwriting.jpg";
		etShowData = (EditText) findViewById(R.id.etscanShowData);
		bTest = (Button) findViewById(R.id.bscanTest);
		bTest.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				File root = Environment.getExternalStorageDirectory();
				File file = new File(root,"alphabets2.png");
				Mat m = Highgui.imread(file.getAbsolutePath());
				if(file.exists())
					Toast.makeText(getApplicationContext(), "File Exist", Toast.LENGTH_SHORT).show();
				if(!m.empty())
				{
					Toast.makeText(getApplicationContext(), "Height:"+m.height()+" Width:"+m.width() ,	 Toast.LENGTH_LONG).show();
				}
				bmp = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);
//				Imgproc.cvtColor(m, m, Imgproc.COLOR_RGB2BGR);
				Utils.matToBitmap(m, bmp);
				ProcessHW phw = new ProcessHW(getApplicationContext());
				phw.ProcessHandwriting(bmp);
				imagePreview = (ImageView) findViewById(R.id.ivscanPic);
				if(bmp!=null)
				imagePreview.setImageBitmap(bmp);
				
			}
		});
		
//		File root = Environment.getExternalStorageDirectory();
//		File file = new File(root,"alphabets2.png");
//		Mat m = Highgui.imread(file.getAbsolutePath());
//		if(file.exists())
//			Toast.makeText(getApplicationContext(), "File Exist", Toast.LENGTH_SHORT).show();
//		if(!m.empty())
//		{
//			Toast.makeText(getApplicationContext(), "Height:"+m.height()+" Width:"+m.width() ,	 Toast.LENGTH_LONG).show();
//		}
//		bmp = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);
//////		Imgproc.cvtColor(m, m, Imgproc.COLOR_RGB2BGR);
//		Utils.matToBitmap(m, bmp);
//		ProcessHW phw = new ProcessHW(getApplicationContext());
//		phw.ProcessHandwriting(bmp);
//		imagePreview = (ImageView) findViewById(R.id.ivscanPic);
//		if(bmp!=null)
//		imagePreview.setImageBitmap(bmp);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.bscanTakePic:
			startCameraActivity();
			break;
		case R.id.bscanSelectPic:
			Intent intent = new Intent();
			// Show only images, no videos or anything else
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			// Always show the chooser (if there are multiple options available)
			startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
			break;
		}
	}
	
	protected void startCameraActivity() {
		File file = new File(_path);
		outputFileUri = Uri.fromFile(file);

		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

		startActivityForResult(intent, TAKE_IMAGE_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		File croppedImageFile = new File(getFilesDir(), "scantest.jpg");
		
		if (resultCode == -1 && requestCode== TAKE_IMAGE_REQUEST) {
			Uri croppedImage = Uri.fromFile(croppedImageFile);

            CropImageIntentBuilder cropImage = new CropImageIntentBuilder(0,0,0,0, croppedImage);
            cropImage.setOutlineColor(0xFF03A9F4);
            cropImage.setSourceImage(outputFileUri);
            startActivityForResult(cropImage.getIntent(this), REQUEST_CROP_PICTURE);

		}
		if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
			Uri croppedImage = Uri.fromFile(croppedImageFile);
			CropImageIntentBuilder cropImage = new CropImageIntentBuilder(0,0,0,0, croppedImage);
            cropImage.setOutlineColor(0xFF03A9F4);
            cropImage.setSourceImage(data.getData());
/*            cropImage.getIntent(this).putExtra("aspectX", 0);
            cropImage.getIntent(this).putExtra("aspectY", 0);*/
            startActivityForResult(cropImage.getIntent(this), REQUEST_CROP_PICTURE);
		
		
		}
		
		 if ((requestCode == REQUEST_CROP_PICTURE) && (resultCode == RESULT_OK)) {
	       	  pic=BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath());
	       	  int nh = (int) ( pic.getHeight() * (900.0 / pic.getWidth()) );
	   	  	  Bitmap scaled = Bitmap.createScaledBitmap(pic, 900, nh, true);
	   	  	  
	   	  	  
	   	  	  	imagePreview = (ImageView) findViewById(R.id.ivscanPic);
	   	        imagePreview.setImageBitmap(scaled);
	   	     ProcessHW phw = new ProcessHW(getApplicationContext());
	 		phw.ProcessHandwriting(scaled);
	       }
		
	}
	
	
	

}
