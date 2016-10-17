package com.cereble;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.zip.GZIPInputStream;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.view.textservice.SpellCheckerSession.SpellCheckerSessionListener;
import android.view.textservice.SuggestionsInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;
import com.googlecode.tesseract.android.TessBaseAPI;

public class TextImage extends Activity implements OnClickListener,SpellCheckerSessionListener {
	public static final String PACKAGE_NAME = "com.cereble";
	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/SimpleAndroidOCR/";
	
	// You should have the trained data file in assets folder
	// You can get them at:
	// http://code.google.com/p/tesseract-ocr/downloads/list
	public static final String lang = "eng";

	private static final String TAG = "SimpleAndroidOCR.java";

	protected Button takepic,selectpic;
	// protected ImageView _image;
	protected EditText _field,suggested;
	protected String _path;
	protected boolean _taken;
	protected Bitmap pic;
	protected static final String PHOTO_TAKEN = "photo_taken";
	int PICK_IMAGE_REQUEST = 1,TAKE_IMAGE_REQUEST=2,REQUEST_CROP_PICTURE=3;
	
	Uri outputFileUri;
	String new_word,old_word;
	String new_sentence;
	String[] words;
	private SpellCheckerSession mScs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

		for (String path : paths) {
			File dir = new File(path);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
					return;
				} else {
					Log.v(TAG, "Created directory " + path + " on sdcard");
				}
			}

		}
		
		// lang.traineddata file with the app (in assets folder)
		// You can get them at:
		// http://code.google.com/p/tesseract-ocr/downloads/list
		// This area needs work and optimization
		if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
				//GZIPInputStream gin = new GZIPInputStream(in);
				OutputStream out = new FileOutputStream(DATA_PATH
						+ "tessdata/" + lang + ".traineddata");

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				//while ((lenf = gin.read(buff)) > 0) {
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				//gin.close();
				out.close();
				
				Log.v(TAG, "Copied " + lang + " traineddata");
			} catch (IOException e) {
				Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
			}
		}

		super.onCreate(savedInstanceState);

		setContentView(R.layout.textimage);

		// _image = (ImageView) findViewById(R.id.image);
		_field = (EditText) findViewById(R.id.field);
		takepic = (Button) findViewById(R.id.bTakePic);
		selectpic = (Button) findViewById(R.id.bSelectPic);
		takepic.setOnClickListener(this);
		selectpic.setOnClickListener(this);
		suggested = (EditText) findViewById(R.id.suggested);
		_path = DATA_PATH + "/ocr.jpg";
	}

	@Override
	   public void onResume() {
	      super.onResume();
	      final TextServicesManager tsm = (TextServicesManager) getSystemService(
	      Context.TEXT_SERVICES_MANAGER_SERVICE);
	      mScs = tsm.newSpellCheckerSession(null, null, this, true);         
	      Toast.makeText(getApplicationContext(), "on resume", Toast.LENGTH_SHORT).show();
	   }

	   @Override
	   public void onPause() {
	      super.onPause();
	      if (mScs != null) {
	         mScs.close();
	      }
	      Toast.makeText(getApplicationContext(), "on pause", Toast.LENGTH_SHORT).show();
	   }
	
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.bTakePic:
			Log.v(TAG, "Starting Camera app");
			startCameraActivity();
	
			break;
		case R.id.bSelectPic:
			Intent intent = new Intent();
			// Show only images, no videos or anything else
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			// Always show the chooser (if there are multiple options available)
			startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
			break;
		}
	}

	// Simple android photo capture:
	// http://labs.makemachine.net/2010/03/simple-android-photo-capture/

	protected void startCameraActivity() {
		File file = new File(_path);
		outputFileUri = Uri.fromFile(file);

		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

		startActivityForResult(intent, TAKE_IMAGE_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, "resultCode: " + resultCode);

		File croppedImageFile = new File(getFilesDir(), "test.jpg");
		//File croppedImageFile = new File(_path);
		//Toast.makeText(getApplicationContext(), croppedImageFile.getPath(), Toast.LENGTH_LONG).show();
		
		if (resultCode == -1 && requestCode== TAKE_IMAGE_REQUEST) {
			/*Bundle extras = data.getExtras();
			pic = (Bitmap) extras.get("data");
		    ImageView imageView = (ImageView) findViewById(R.id.ivPic);
            imageView.setImageBitmap(pic);
			onPhotoTaken();*/

			//File zImageFile = new File(_path);
			
			
//			ExifInterface exif = null;
//			try {
//				exif = new ExifInterface(outputFileUri.getPath());
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			int exifOrientation = exif.getAttributeInt(
//					ExifInterface.TAG_ORIENTATION,
//					ExifInterface.ORIENTATION_NORMAL);
			
//	        int rotation =-1;
//	        File zfile = new File(outputFileUri.getPath());
//	        
//	        if(!zfile.exists())
//	        	Toast.makeText(getApplicationContext(), "File doesnt exist", Toast.LENGTH_LONG).show();
//	        
//	        long fileSize = zfile.length();
//	        
//	        long captureTime = System.currentTimeMillis();
//	        
//	        Cursor mediaCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] {MediaStore.Images.ImageColumns.ORIENTATION, MediaStore.MediaColumns.SIZE }, MediaStore.MediaColumns.DATE_ADDED + ">=?", new String[]{String.valueOf(captureTime/1000 - 1)}, MediaStore.MediaColumns.DATE_ADDED + " desc");
//
//	        if (mediaCursor != null && captureTime != 0 && mediaCursor.getCount() !=0 ) {
//	            while(mediaCursor.moveToNext()){
//	                long size = mediaCursor.getLong(1);
//	                //Extra check to make sure that we are getting the orientation from the proper file
//	                if(size == fileSize){
//	                	Toast.makeText(getApplicationContext(), "Inside if", Toast.LENGTH_LONG).show();
//	                    rotation = mediaCursor.getInt(0);
//	                    break;
//	                }
//	            }
//	        }	        
	        
			String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
            Cursor cur = managedQuery(outputFileUri, orientationColumn, null, null, null);
            int orientation = -1;
            if (cur != null && cur.moveToFirst()) {
                orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
            }
			
	        Toast.makeText(getApplicationContext(), "Orei: "+orientation, Toast.LENGTH_LONG).show();


			//Toast.makeText(getApplicationContext(), "Orei: "+orientation, Toast.LENGTH_LONG).show();
	        
	        Uri croppedImage = Uri.fromFile(croppedImageFile);

            CropImageIntentBuilder cropImage = new CropImageIntentBuilder(0,0,0,0, croppedImage);
            cropImage.setOutlineColor(0xFF03A9F4);
            cropImage.setSourceImage(outputFileUri);
            startActivityForResult(cropImage.getIntent(this), REQUEST_CROP_PICTURE);
			
		}
		
		if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
			 
//	        Uri uri = data.getData();
	        
	        Uri croppedImage = Uri.fromFile(croppedImageFile);
	        
	        ///////////////remove after use/////////////////////
//	        String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
//	        Cursor cur = getContentResolver().query(data.getData(), orientationColumn, null, null, null);
//	        int orientation = -1;
//	        if (cur != null && cur.moveToFirst()) {
//	            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
//	        }
	        
	        Toast.makeText(getApplicationContext(), ">>"+data.getData().getPath(), Toast.LENGTH_LONG).show();
	        Toast.makeText(getApplicationContext(), ">>"+data.getData().toString(), Toast.LENGTH_LONG).show();
//			ExifInterface exif = null;
//			try {
//				exif = new ExifInterface(data.getData().toString());
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			int exifOrientation = exif.getAttributeInt(
//					ExifInterface.TAG_ORIENTATION,
//					ExifInterface.ORIENTATION_NORMAL);
//
//
//	        
//	        Toast.makeText(getApplicationContext(), "Orei: "+exifOrientation, Toast.LENGTH_LONG).show();
	        
//	        InputStream is = getApplicationContext().getContentResolver().openInputStream(data.getData());
//	        BitmapFactory.Options dbo = new BitmapFactory.Options();
//	        dbo.inJustDecodeBounds = true;
//	        BitmapFactory.decodeStream(is, null, dbo);
//	        is.close();

	        int rotatedWidth, rotatedHeight;
	        int orientation = getOrientation(getApplicationContext(), data.getData());
	        Toast.makeText(getApplicationContext(), "Orein: "+orientation, Toast.LENGTH_LONG).show();
	        ///////////////remove after use/////////////////////

            CropImageIntentBuilder cropImage = new CropImageIntentBuilder(0,0,0,0, croppedImage);
            cropImage.setOutlineColor(0xFF03A9F4);
            cropImage.setSourceImage(data.getData());
/*            cropImage.getIntent(this).putExtra("aspectX", 0);
            cropImage.getIntent(this).putExtra("aspectY", 0);*/
            startActivityForResult(cropImage.getIntent(this), REQUEST_CROP_PICTURE);
	      /*  String image_uri= uri.toString();
	        Bundle b = new Bundle();
	        b.putString("image_uri", image_uri);
	        Intent a = new Intent("android.intent.action.CROPPICTURE");
	        a.putExtras(b);
	        startActivity(a);*/
	        
	      
	        
	      /*  try {
	            pic = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
	            // Log.d(TAG, String.valueOf(bitmap));
	  onPhotoSelected();
	  
	  int nh = (int) ( pic.getHeight() * (512.0 / pic.getWidth()) );
	  Bitmap scaled = Bitmap.createScaledBitmap(pic, 512, nh, true);
	  
	  
	  ImageView imageView = (ImageView) findViewById(R.id.ivPic);
      imageView.setImageBitmap(scaled);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }*/
	    }
		 if ((requestCode == REQUEST_CROP_PICTURE) && (resultCode == RESULT_OK)) {
       	  pic=BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath());
       	  int nh = (int) ( pic.getHeight() * (512.0 / pic.getWidth()) );
   	  	  Bitmap scaled = Bitmap.createScaledBitmap(pic, 512, nh, true);
   	  	  
   	  	  
   	  	  ImageView imageView = (ImageView) findViewById(R.id.ivPic);
   	        imageView.setImageBitmap(scaled);
   	     onPhotoSelected();
       }
	}

	 public static int getOrientation(Context context, Uri photoUri) {
	        /* it's on the external media. */
	        Cursor cursor = context.getContentResolver().query(photoUri,
	                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

	        if (cursor.getCount() != 1) {
	            return -1;
	        }
	       // Toast.makeText(context, cursor.getString(0), Toast.LENGTH_LONG).show();
	        Toast.makeText(context, cursor.toString(), Toast.LENGTH_LONG).show();
	        
	        cursor.moveToFirst();
	        return cursor.getInt(0);
	    }
	
	private Bitmap decodeFile(String path) {
		// TODO Auto-generated method stub
		int orientation;

        try {

            if(path==null){

                return null;
            }
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 70;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 4;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale++;
            }
            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            Bitmap bm = BitmapFactory.decodeFile(path,o2);


            Bitmap bitmap = bm;

            ExifInterface exif = new ExifInterface(path);
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.e("orientation",""+orientation);
            Toast.makeText(getApplicationContext(), "Oreintation: "+orientation, Toast.LENGTH_LONG).show();
            Matrix m=new Matrix();

            if((orientation==3)){

            m.postRotate(180);
            m.postScale((float)bm.getWidth(), (float)bm.getHeight());

//          if(m.preRotate(90)){
            Log.e("in orientation",""+orientation);

            bitmap = Bitmap.createBitmap(bm, 0, 0,bm.getWidth(),bm.getHeight(), m, true);
            return  bitmap;
            }
            else if(orientation==6){

             m.postRotate(90);

             Log.e("in orientation",""+orientation);

             bitmap = Bitmap.createBitmap(bm, 0, 0,bm.getWidth(),bm.getHeight(), m, true);
                return  bitmap;
            }

            else if(orientation==8){

             m.postRotate(270);

             Log.e("in orientation",""+orientation);

             bitmap = Bitmap.createBitmap(bm, 0, 0,bm.getWidth(),bm.getHeight(), m, true);
                return  bitmap;
            }
            return bitmap;
        }
        catch (Exception e) {
        }
        return null;
    }
		

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(TextImage.PHOTO_TAKEN, _taken);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.i(TAG, "onRestoreInstanceState()");
		if (savedInstanceState.getBoolean(TextImage.PHOTO_TAKEN)) {
			onPhotoTaken();
		}
	}

	protected void onPhotoTaken() {
		_taken = true;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;

		Bitmap bitmap = BitmapFactory.decodeFile(_path, options);

		try {
			ExifInterface exif = new ExifInterface(_path);
			int exifOrientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			Log.v(TAG, "Orient: " + exifOrientation);

			int rotate = 0;

			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			}

			Log.v(TAG, "Rotation: " + rotate);
			
			if (rotate != 0) {

				// Getting width & height of the given image.
				int w = bitmap.getWidth();
				int h = bitmap.getHeight();

				// Setting pre rotate
				Matrix mtx = new Matrix();
				mtx.preRotate(rotate);

				// Rotating Bitmap
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
			}

			// Convert to ARGB_8888, required by tess
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

		} catch (IOException e) {
			Log.e(TAG, "Couldn't correct orientation: " + e.toString());
		}

		// _image.setImageBitmap( bitmap );
		
		Log.v(TAG, "Before baseApi");

		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(DATA_PATH, lang);
		baseApi.setImage(bitmap);
		
		String recognizedText = baseApi.getUTF8Text();
		
		baseApi.end();

		// You now have the text in recognizedText var, you can do anything with it.
		// We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
		// so that garbage doesn't make it to the display.

		Log.v(TAG, "OCRED TEXT: " + recognizedText);

		if ( lang.equalsIgnoreCase("eng") ) {
			recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
		}
		
		recognizedText = recognizedText.trim();

		if ( recognizedText.length() != 0 ) {
			_field.setText(_field.getText().toString().length() == 0 ? recognizedText : _field.getText() + " " + recognizedText);
			_field.setSelection(_field.getText().toString().length());
		}
		
		// Cycle done.
	}

	protected void onPhotoSelected()
	{
		//required by tess
		pic = pic.copy(Bitmap.Config.ARGB_8888, true);
		
		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(DATA_PATH, lang);
		baseApi.setImage(pic);
		
		String recognizedText = baseApi.getUTF8Text();
		
		baseApi.end();

		// You now have the text in recognizedText var, you can do anything with it.
		// We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
		// so that garbage doesn't make it to the display.

		Log.v(TAG, "OCRED TEXT: " + recognizedText);

		if ( lang.equalsIgnoreCase("eng") ) {
			//recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9\\.-]+", " ");
			recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
		}
		
		recognizedText = recognizedText.trim();

		if ( recognizedText.length() != 0 ) {
			_field.setText(_field.getText().toString().length() == 0 ? recognizedText : _field.getText() + " " + recognizedText);
			_field.setSelection(_field.getText().toString().length());
			
			
			final TextServicesManager tsm = (TextServicesManager) getSystemService(
				      Context.TEXT_SERVICES_MANAGER_SERVICE);
				      mScs = tsm.newSpellCheckerSession(null, null, this, true);
			new_sentence = _field.getText().toString();
			words= new_sentence.split("\\s+");
			mScs.getSentenceSuggestions(new TextInfo[] {new TextInfo(_field.getText().toString())}, 1);
			Toast.makeText(getApplicationContext(), "msg msg", Toast.LENGTH_SHORT).show();
		}
		
	}


	@Override
	public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {
		// TODO Auto-generated method stub
		for(SentenceSuggestionsInfo result:results){
		    int n = result.getSuggestionsCount();
		    for(int i=0; i < n; i++){
		    	
		        int m = result.getSuggestionsInfoAt(i).getSuggestionsCount();
		        if((result.getSuggestionsInfoAt(i).getSuggestionsAttributes() &
		        	    SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO) != SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO )
		        {	if(i==(n-1)) suggested.setText(new_sentence);
		        	    continue;
		        }
		       
		        old_word=words[i];
		        for(int k=0; k < m; k++) {
		            //sb.append(result.getSuggestionsInfoAt(i).getSuggestionAt(k)).append("\n");
		        	//arrayAdapter.add(result.getSuggestionsInfoAt(i).getSuggestionAt(k));
		        	new_word=result.getSuggestionsInfoAt(i).getSuggestionAt(k);
		        }
		        Log.v("SpellChecker", "this word is "+old_word);
		        //new MyAsyncTask().execute();
		     /*   MyAsyncTask a = new MyAsyncTask();
		        a.execute();
		        
		        try {
					a.get();
//		        	a.get(1000, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
		        
		     /*   builderSingle.setAdapter(arrayAdapter,
		                new DialogInterface.OnClickListener() {

		                    @Override
		                    public void onClick(DialogInterface dialog, int which) {
		                        
		                      new_word =   arrayAdapter.getItem(which);
		                      new_sentence = new_sentence.replaceAll("\\b"+old_word+"\\b",new_word);
		                      Log.v("SpellChecker", "replacing "+old_word+" with "+new_word);
		                       if(old_word.equals(words[(words.length-1)]))
		                    	   sugg.setText(new_sentence);
		                       
		                    }
		                });
		        builderSingle.show();*/
		        new_sentence = new_sentence.replaceAll("\\b"+old_word+"\\b",new_word);
		        Toast.makeText(getApplicationContext(), "Replacing", Toast.LENGTH_SHORT).show();
		    }
		}
		 
		runOnUiThread(new Runnable() {

		      public void run() {
		    	  //builderSingle.show();
		    	  
		         //sugg.append(sb.toString());
		    	  Toast.makeText(getApplicationContext(), "showing", Toast.LENGTH_SHORT).show();
		        suggested.setText(new_sentence);
		      }
		   });
	}


	@Override
	public void onGetSuggestions(SuggestionsInfo[] results) {
		// TODO Auto-generated method stub
		
	}
	
	// www.Gaut.am was here
	// Thanks for reading!
}
