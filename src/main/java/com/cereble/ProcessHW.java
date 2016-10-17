package com.cereble;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.view.View.OnClickListener;

public class ProcessHW {

	static int file_no = 0;

	static Context context;

	public ProcessHW(Context c) {
		context = c;
	}

	public static void ProcessHandwriting(Bitmap hwImage) {
		Mat m = new Mat();
		Utils.bitmapToMat(hwImage, m);
		// Convert rgb image to gray
		Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2GRAY);

		// Mask size for Gaussian Blur
		Size s = new Size(11, 11);

		// Blur the image to remove noise
		Imgproc.GaussianBlur(m, m, s, 2);
		Imgproc.medianBlur(m, m, 5);

		// Convert Image to binary
		Imgproc.adaptiveThreshold(m, m, 255, 1, 1, 11, 2);

		// define structuring element and apply close operation to fill gaps
		Size st_size = new Size(3, 3);
		Mat st = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, st_size);
		Imgproc.morphologyEx(m, m, Imgproc.MORPH_CLOSE, st);

		// Convert Mat m to integer matrix iMat the apply thinning
		//Thinning th = new Thinning(context);
		byte buff1[] = new byte[(int) (m.total() * m.channels())];
		m.get(0, 0, buff1);
		int ibuff[] = MyConversion.ByteToIntArray(buff1);
		int iMat[][] = MyConversion.ArraytoMat(ibuff, m.rows(), m.cols());
		int[][] iMatThinned = Thinning.Thinning(iMat, m.rows(), m.cols());
		ibuff = MyConversion.MattoArray(iMatThinned, m.rows(), m.cols());
		buff1 = MyConversion.IntToByteArray(ibuff);

		// put the result back in Mat m
		m.put(0, 0, buff1);

		// we will use this later to extract alphabets
		Mat thinnedMat = new Mat();
		m.copyTo(thinnedMat);

		// Find contours in image
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(m.clone(), contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		List<Rect> initial_contours = new ArrayList<Rect>();

		// filter only big enough contours
		for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
			Rect r = Imgproc.boundingRect(contours.get(contourIdx));
			if (r.height > 20 || r.width > 20) {

				// Core.rectangle(color_image,new Point(r.x, r.y),new
				// Point(r.x+r.width,r.y+r.height), new Scalar(0, 255, 0));
				initial_contours.add(r);
			}

		}

		// arrange contours in alphabetical way
		// Note: There may be more than 26 contours because of
		// disconnected alphabets we find final contours later
		Collections.reverse(initial_contours);
		List<Rect> single_row = new ArrayList<Rect>();
		List<Rect> sorted_contours = new ArrayList<Rect>();
		int sort_counter = 0;
		int start_of_row = 0;
		while (sort_counter < initial_contours.size()) {
			single_row.clear();
			while (sort_counter < initial_contours.size()
					&& (initial_contours.get(sort_counter).y < (initial_contours.get(start_of_row).y
							+ initial_contours.get(start_of_row).height))) {
				single_row.add(initial_contours.get(sort_counter));
				sort_counter++;
			}

			// code to sort List according to x parameter
			Collections.sort(single_row, new Comparator<Rect>() {

				@Override
				public int compare(Rect arg0, Rect arg1) {

					return arg0.x - arg1.x;
				}
			});
			sorted_contours.addAll(single_row);
			start_of_row = sort_counter;
		}

		// find final contours which include disconnected part of alphabets
		List<Rect> final_contours = new ArrayList<Rect>();
		int proximity_check = 10;
		for (int i = 0; i < sorted_contours.size(); i++) {
			int x = sorted_contours.get(i).x;
			int y = sorted_contours.get(i).y;
			int h = sorted_contours.get(i).height;
			int w = sorted_contours.get(i).width;
			if ((i < sorted_contours.size() - 1) && (sorted_contours.get(i + 1).x) < (x + w + proximity_check)
					&& sorted_contours.get(i + 1).x > x) {
				w += (sorted_contours.get(i + 1).x + sorted_contours.get(i + 1).width) - (x + w);
				i = i + 1;
			}
			final_contours.add(new Rect(x, y, w, h));
		}

		// make folder to store letter images
		final File folder = new File(Environment.getExternalStorageDirectory() + "/CAW");
		if (folder.exists())
			DeleteRecursive(folder);

		boolean success = true;
		if (!folder.exists()) {
			success = folder.mkdir();
		}
		if (success) {
			// Do something on success
		} else {
			// Do something else on failure
			Toast.makeText(context, "Can't create folder", Toast.LENGTH_SHORT).show();

		}

		String filename = new String();
		for (int i = 0; i < final_contours.size(); i++) {
			Rect r = final_contours.get(i);
			// Core.rectangle(color_image,new Point(r.x, r.y),new
			// Point(r.x+r.width,r.y+r.height), new Scalar(0, 255, 0));
			Mat cropped = new Mat(thinnedMat, r);
			if (i < 9)
				filename = "0" + (i + 1) + ".jpg";
			else
				filename = (i + 1) + ".jpg";
			File image_to_write = new File(folder, filename);
			filename = image_to_write.toString();
			Highgui.imwrite(filename, cropped);
		}

		final ArrayList<File> mFiles = new ArrayList<File>();
		File[] files = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith(".jpg") || name.endsWith(".jpeg"));
			}
		});

		if (files != null && files.length > 0) {
			for (File f : files) {

				mFiles.add(f);
			}

			Collections.sort(mFiles, new FileComparator());
		}

		/*
		 * ////////////For testing of individual letter using button "
		 * ////////////////////////// ScanHW.bTest.setOnClickListener(new
		 * OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { // TODO Auto-generated method
		 * stub String filename=mFiles.get(file_no).getName(); file_no++;
		 * Toast.makeText(context, "Processing: "+filename,
		 * Toast.LENGTH_SHORT).show(); File image_to_read = new
		 * File(folder,filename);
		 * 
		 * filename = image_to_read.toString(); Mat imTracing =
		 * Highgui.imread(filename); Bitmap bmp =
		 * Bitmap.createBitmap(imTracing.width(), imTracing.height(),
		 * Bitmap.Config.ARGB_8888); Utils.matToBitmap(imTracing, bmp);
		 * ScanHW.imagePreview.setImageBitmap(bmp);
		 * 
		 * Imgproc.cvtColor(imTracing, imTracing, Imgproc.COLOR_RGB2GRAY);
		 * Imgproc.copyMakeBorder(imTracing, imTracing, 1, 1, 1, 1,
		 * Imgproc.BORDER_CONSTANT, new Scalar(0, 0, 0));
		 * Imgproc.threshold(imTracing, imTracing, 200, 255,
		 * Imgproc.THRESH_BINARY); byte[] buff1 = new
		 * byte[(int)(imTracing.total() * imTracing.channels())];
		 * imTracing.get(0, 0, buff1); int[] ibuff =
		 * MyConversion.ByteToIntArray(buff1); int[][] iMat = new
		 * int[imTracing.rows()][imTracing.cols()];
		 * iMat=MyConversion.ArraytoMat(ibuff, imTracing.rows(),
		 * imTracing.cols()); List<Point> points_of_interest = new
		 * ArrayList<Point>(); points_of_interest=Tracing.traceAlphabet(iMat,
		 * imTracing.rows(), imTracing.cols()); ScanHW.etShowData.setText("");
		 * for(int i=0;i<points_of_interest.size();i++) {
		 * ScanHW.etShowData.append("x: "+points_of_interest.get(i).x+"y: "
		 * +points_of_interest.get(i).y); }
		 * 
		 * 
		 * 
		 * //Algorithm to scale image data to send and draw image from points
		 * int new_rows = (int) (imTracing.size().height*10); int new_cols =
		 * (int) (imTracing.size().width*10); Mat construct_image =
		 * Mat.zeros(new_rows,new_cols, CvType.CV_8UC1);
		 * 
		 * final Bitmap bmp_animate =
		 * Bitmap.createBitmap(construct_image.width(),construct_image.height(),
		 * Bitmap.Config.ARGB_8888); for(int
		 * i=0;i<points_of_interest.size()-1;i++) {
		 * if(points_of_interest.get(i+1).equals(new Point(1000, 1000))) {
		 * if(i<points_of_interest.size()-2) { i++; i++; } else break;
		 * 
		 * } Point p1 = new Point(points_of_interest.get(i).x*10,
		 * points_of_interest.get(i).y*10); Point p2 = new
		 * Point(points_of_interest.get(i+1).x*10,
		 * points_of_interest.get(i+1).y*10); Core.line(construct_image,
		 * p1,p2,new Scalar(255)); Utils.matToBitmap(construct_image,
		 * bmp_animate); ScanHW.imagePreview.setImageBitmap(bmp_animate);
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * }
		 * 
		 * } });
		 * 
		 * /////////////////////////////////////////////////////////////////////
		 * /////////
		 */

		Log.v("ProcessHW.java", "Starting to save data");
		SharedPreferences spDataFile = context.getSharedPreferences("TracedDataFile",0);
		for (int i = 0; i < mFiles.size(); i++) {

			filename = mFiles.get(i).getName();
			Log.v("ProcessHW.java", "File:"+filename);
			Toast.makeText(context, "Processing: " + filename, Toast.LENGTH_SHORT).show();
			File image_to_read = new File(folder, filename);
			// filename = "padded_A.jpg";
			// File image_to_read = new
			// File(Environment.getExternalStorageDirectory(),filename);

			filename = image_to_read.toString();
			Mat imTracing = Highgui.imread(filename);
			Imgproc.cvtColor(imTracing, imTracing, Imgproc.COLOR_RGB2GRAY);
			Imgproc.copyMakeBorder(imTracing, imTracing, 1, 1, 1, 1, Imgproc.BORDER_CONSTANT, new Scalar(0, 0, 0));
			Imgproc.threshold(imTracing, imTracing, 200, 255, Imgproc.THRESH_BINARY);
			buff1 = new byte[(int) (imTracing.total() * imTracing.channels())];
			imTracing.get(0, 0, buff1);
			ibuff = MyConversion.ByteToIntArray(buff1);
			iMat = new int[imTracing.rows()][imTracing.cols()];
			iMat = MyConversion.ArraytoMat(ibuff, imTracing.rows(), imTracing.cols());
			List<Point> points_of_interest = new ArrayList<Point>();
			points_of_interest = Tracing.traceAlphabet(iMat, imTracing.rows(), imTracing.cols());

			String dataFileName = mFiles.get(i).getName();
			dataFileName = dataFileName.substring(0, dataFileName.lastIndexOf('.'));
			dataFileName = dataFileName + ".caw";
			
			////////////////////////////	
			SharedPreferences.Editor spEditor = spDataFile.edit();
			String newDataToStore = "";
			StringBuilder sbData = new StringBuilder();
			sbData.append(new String("x_total=" + imTracing.cols() + "y_total=" + imTracing.rows() + "\n"));
//			newDataToStore+="x_total=" + imTracing.cols() + "y_total=" + imTracing.rows() + "\n";
			
			for (int point_pos = 0; point_pos < points_of_interest.size(); point_pos++) {
				String dataToStore = "x=" + points_of_interest.get(point_pos).x + "y="+ points_of_interest.get(point_pos).y + "\n";
//				newDataToStore+="x=" + points_of_interest.get(point_pos).x + "y="+ points_of_interest.get(point_pos).y + "\n";
				sbData.append(dataToStore);
			}
//			ScanHW.etShowData.setText(newDataToStore);
			spEditor.putString(dataFileName,sbData.toString());
			spEditor.commit();
			
			////////////////////////////	
			
/*
//					FileOutputStream outputStream = null;
			
//			File dataFile = new File(context.getFilesDir(),dataFileName);
			OutputStreamWriter outputStream = null;
			try {
//				outputStream = context.openFileOutput(dataFileName, Context.MODE_PRIVATE);
				 outputStream  = new OutputStreamWriter(context.openFileOutput(dataFileName, Context.MODE_PRIVATE));
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// store total x and total y
			try {
				outputStream.write(
						new String("x_total=" + imTracing.cols() + "y_total=" + imTracing.rows() + "\n"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int point_pos = 0; point_pos < points_of_interest.size(); i++) {
				String dataToStore = "x=" + points_of_interest.get(point_pos).x + "y="
						+ points_of_interest.get(point_pos).y + "\n";
				try {
					outputStream.write(dataToStore);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				//outputStream.flush();
				outputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
*/
			
			
			// ScanHW.etShowData.setText("");
			// for(int i=0;i<points_of_interest.size();i++)
			// {
			// ScanHW.etShowData.append("x: "+points_of_interest.get(i).x+"y:
			// "+points_of_interest.get(i).y);
			// }

			// filename=mFiles.get(i).getName();
			// TODO: remove extension store data save file
			// use android dev site for file storing

		}
////////////////////////////////		
		spDataFile = context.getSharedPreferences("TracedDataFile", 0);
		String dataReturned = spDataFile.getString("01.caw", "Error 404:"+"01.caw" +"File not found");
		ScanHW.etShowData.setText(dataReturned);
///////////////////////////////
		// for(int i=0;i<mFiles.size();i++)
		// {
		// String str = mFiles.get(i).getName();
		// str = str.substring(0, str.lastIndexOf('.'));
		// ScanHW.etShowData.append(str+" ");
		// }

		// TODO: extract print data from individual alphabets

		////////////////////// End of function///////////////////////
	}

	public static class FileComparator implements Comparator<File> {

		@Override
		public int compare(File arg0, File arg1) {
			// TODO Auto-generated method stub
			return arg0.getName().compareTo(arg1.getName());
		}

	}

	static void DeleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				DeleteRecursive(child);

		fileOrDirectory.delete();
	}

}
