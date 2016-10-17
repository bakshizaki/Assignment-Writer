package com.cereble;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;



public class CropPicture extends Activity implements OnClickListener {

private DragRectView view;
private ImageView original;
private ImageView imageViewtest;
Button cropit;
Bitmap bitmap; 
// private ImageView croppedImage;

/**
 * 
 */
@Override
protected void onCreate(Bundle savedInstanceState) {
	 
    super.onCreate(savedInstanceState);
    setContentView(R.layout.crop_pic);
    Bundle gotExtras = getIntent().getExtras();
    final Uri uri = Uri.parse(gotExtras.getString("image_uri"));
    view = (DragRectView) findViewById(R.id.dragRect);
    original = (ImageView) findViewById(R.id.image);
    cropit = (Button) findViewById(R.id.bCrop);
   
    
   
    cropit.setOnClickListener(this);
    Bitmap picked_image=null;
	try {
picked_image = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 int nh = (int) ( picked_image.getHeight() * (512.0 / picked_image.getWidth()) );
	  Bitmap scaled = Bitmap.createScaledBitmap(picked_image, 512, nh, true);
	original.setImageBitmap(scaled);
	final Bitmap bitmap1 = picked_image;
	
	final int scaling =  picked_image.getWidth()/512;
    // croppedImage = (ImageView) findViewById(R.id.crop_image);
    if (null != view) {
        view.setOnUpCallback(new DragRectView.OnUpCallback() {
            @Override
            public void onRectFinished(final Rect rect) {
                Toast.makeText(
                        getApplicationContext(),
                        "Rect is (" + rect.left*scaling + ", " + rect.top*scaling + ", "
                                + rect.right*scaling + ", " + rect.bottom *scaling+ ")",
                        Toast.LENGTH_LONG).show();
               /* Bitmap bitmap1 = Bitmap.createScaledBitmap(
                        ((BitmapDrawable) original.getDrawable())
                                .getBitmap(), original.getWidth(), original
                                .getHeight(), false);*/
               
             
                System.out.println(rect.height() + "    "
                        + bitmap1.getHeight() + "      " + rect.width()
                        + "    " + bitmap1.getWidth());
                if (rect.height() <= bitmap1.getHeight()
                        && rect.width() <= bitmap1.getWidth()) {
                 /*   Bitmap bitmap = Bitmap.createBitmap(bitmap1,
                            view.getLeft(), view.getTop(), view.getWidth(),
                            view.getHeight());*/
                	bitmap = Bitmap.createBitmap(bitmap1,
                            rect.left, rect.top, (rect.right-rect.left),
                            (rect.bottom-rect.top));
                    System.out
                            .println("MainActivity.onCreate(...).new OnUpCallback() {...}.onRectFinished() if true");
                   // startActivity(intent);
                    
                    System.out.println("MainActivity.onCreate() ");
               /*     String uri = "@drawable/batman";

                    int imageResource = getResources().getIdentifier(uri, null, getPackageName());

             
                    Drawable res = getResources().getDrawable(imageResource);
                    original.setImageDrawable(res);*/
                   // original.setImageBitmap(bitmap);
                }
            }
        });
    }
}
@Override
public void onClick(View v) {
	// TODO Auto-generated method stub
	switch(v.getId())
	{
	case R.id.bCrop:
		original.setImageBitmap(bitmap);
		break;
	}
}
}


