package com.cereble;

import java.io.File;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import com.android.camera.CropImageIntentBuilder;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ScanHWDebug extends Activity implements OnClickListener,OnTouchListener {
	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/SimpleAndroidOCR/";
	private static final String TAG = "ScanHW.java";
	protected Button takepic,selectpic;
	protected String _path;
	protected boolean _taken;
	protected Bitmap pic;
	protected static final String PHOTO_TAKEN = "photo_taken";
	public static EditText etShowData;
	public static Button bTest,bTest2;
//	public Thread timer2;
	int PICK_IMAGE_REQUEST = 1,TAKE_IMAGE_REQUEST=2,REQUEST_CROP_PICTURE=3;
	public static ImageView imagePreview;
	Uri outputFileUri;
	

	int imageNumber=1;
	public static Bitmap bmp,gray,binary,closed,test,thinned,contours_image;
	Bitmap b;
	TextView tvImageTitle;
	
	
	
	
	
	
	
	
	
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
		tvImageTitle =(TextView) (findViewById(R.id.tvImageTitle));
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
				File file = new File(root,"alphabets2.jpg");
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
				imagePreview = (ImageView) findViewById(R.id.ivscanPic);
				if(bmp!=null)
				imagePreview.setImageBitmap(bmp);
				
				ProcessHWDebug phw = new ProcessHWDebug(getApplicationContext());
				phw.ProcessHandwriting(bmp,1);
				
			}
		});
		imagePreview = (ImageView) findViewById(R.id.ivscanPic);
		imagePreview.setOnTouchListener(this);
		
		bTest2 = (Button) findViewById(R.id.bscanTest2);
		bTest2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ProcessHWDebug phw = new ProcessHWDebug(getApplicationContext());
				phw.ProcessHandwriting(bmp,2);
				
			}
		});
		
		
		Thread timer2 = new Thread() {
			public void run()
			{
				try {
					sleep(5000);
				}
				catch(InterruptedException e) {
					e.printStackTrace();
				}

			}
		};

		
		File root = Environment.getExternalStorageDirectory();
		File file = new File(root,"alphabets2.jpg");
		Mat m = Highgui.imread(file.getAbsolutePath());
		if(file.exists())
			Toast.makeText(getApplicationContext(), "File Exist", Toast.LENGTH_SHORT).show();
		if(!m.empty())
		{
			Toast.makeText(getApplicationContext(), "Height:"+m.height()+" Width:"+m.width() ,	 Toast.LENGTH_LONG).show();
		}
		bmp = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);
////		Imgproc.cvtColor(m, m, Imgproc.COLOR_RGB2BGR);
		Utils.matToBitmap(m, bmp);
		imagePreview.setImageBitmap(bmp);
//		new Handler().postDelayed(new Runnable() {
//		    @Override
//		        public void run() {
//
//		    	imagePreview.setVisibility(View.GONE);
//		        }
//		    }, 3000);
		

		
//		ProcessHWDebug phw = new ProcessHWDebug(getApplicationContext());
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
	       	  int nh = (int) ( pic.getHeight() * (512.0 / pic.getWidth()) );
	   	  	  Bitmap scaled = Bitmap.createScaledBitmap(pic, 512, nh, true);
	   	  	  
	   	  	  
	   	  	  	imagePreview = (ImageView) findViewById(R.id.ivscanPic);
	   	        imagePreview.setImageBitmap(scaled);
	   	     ProcessHWDebug phw = new ProcessHWDebug(getApplicationContext());
	 		phw.ProcessHandwriting(pic,1);
	       }
		
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			imageNumber++;
			switch(imageNumber) {
			case 2:
				imagePreview.setImageBitmap(gray);
				tvImageTitle.setText("gray");
				break;
			case 3:
				imagePreview.setImageBitmap(binary);
				tvImageTitle.setText("binary");
				break;
			case 4:
				imagePreview.setImageBitmap(closed);
				//imagePreview.setImageBitmap(gray);
				tvImageTitle.setText("closed");
				break;
			case 5:
				imagePreview.setImageBitmap(thinned);
				tvImageTitle.setText("thinned");
				
				break;
			case 6:
				imagePreview.setImageBitmap(contours_image);
				tvImageTitle.setText("contours");
				imageNumber=1;
				break;
			case 7:
				imagePreview.setImageBitmap(b);
				tvImageTitle.setText("B");
				
				break;


				
			default: 
				Toast.makeText(getApplicationContext(), "Error in switch case", Toast.LENGTH_SHORT).show();
			}
			
		}
		return super.onTouchEvent(event);
		
	}

		
	}
	
	
