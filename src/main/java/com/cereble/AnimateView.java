package com.cereble;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class AnimateView extends SurfaceView implements Runnable {

	Bitmap bmp_animate;
	Thread ourThread;
	Context c;
	SurfaceHolder ourHolder;
	boolean isRunning = false;
	public String consFileName = null;
	int consFileNumber = 1;
	
	
	public AnimateView(Context context) {
		super(context);
		c = context;
		ourHolder = getHolder();
		// String dataReturned = ProcessHWDebug.getDefaults("01.caw");
		// if(dataReturned == null)
		// Toast.makeText(context.getApplicationContext(), "Cant read
		// SharedPrefs", Toast.LENGTH_SHORT).show();
		// else{
		// String[] lines =
		// dataReturned.split(System.getProperty("line.separator"));
		// String[] coordinates = lines[0].split(" ");
		// String xPrefix = "x_total=";
		// String yPrefix = "y_total=";
		// String xTotal =
		// coordinates[0].substring(coordinates[0].indexOf(xPrefix)+xPrefix.length());
		// String yTotal =
		// coordinates[1].substring(coordinates[1].indexOf(yPrefix)+yPrefix.length());
		// int new_rows = Integer.parseInt(yTotal);
		// int new_cols = Integer.parseInt(xTotal);
		// Mat construct_image = Mat.zeros(new_rows, new_cols, CvType.CV_8UC1);
		//
		// bmp_animate = Bitmap.createBitmap(construct_image.width(),
		// construct_image.height(),
		// Bitmap.Config.ARGB_8888);
		// for (int j = 1; j < lines.length - 2; j++) {
		// if (lines[j+1].equals("x=1000.0 y=1000.0")) {
		// if (j < lines.length - 3) {
		// j++;
		// j++;
		// } else
		// break;
		//
		// }
		// if(j<lines.length - 2)
		// {
		// coordinates = lines[j].split(" ");
		// String xValue =
		// coordinates[0].substring(coordinates[0].indexOf("x=")+"x=".length());
		// String yValue =
		// coordinates[1].substring(coordinates[1].indexOf("y=")+"y=".length());
		// coordinates = lines[j+1].split(" ");
		// String xNextValue =
		// coordinates[0].substring(coordinates[0].indexOf("x=")+"x=".length());
		// String yNextValue =
		// coordinates[1].substring(coordinates[1].indexOf("y=")+"y=".length());
		// Point p1 = new
		// Point(Integer.parseInt(xValue),Integer.parseInt(yValue));
		// Point p2 = new
		// Point(Integer.parseInt(xNextValue),Integer.parseInt(yNextValue));
		// Core.line(construct_image, p1, p2, new Scalar(255));
		// Utils.matToBitmap(construct_image, bmp_animate);
		// invalidate();
		//
		// }
		// }
		//
		//// textBox.setText(xValue+yValue);
		// }
	}

	// @Override
	// protected void onDraw(Canvas canvas) {
	// // TODO Auto-generated method stub
	// super.onDraw(canvas);
	// canvas.drawColor(Color.WHITE);
	// canvas.drawBitmap(bmp_animate, 50,50, null);
	//// try {
	//// Thread.sleep(500);
	//// } catch (InterruptedException e) {
	//// // TODO Auto-generated catch block
	//// e.printStackTrace();
	//// }
	// }

	public void pause() {
		isRunning = false;
		while (true) {
			try {
				ourThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
		ourThread = null;
	}

	public void resume() {
		isRunning = true;
		ourThread = new Thread(this);
		ourThread.start();
	}

	@Override
	public void run() {

		while (isRunning) {
			if (!ourHolder.getSurface().isValid())
				continue;

			Canvas canvas = ourHolder.lockCanvas();
			canvas.drawColor(Color.WHITE);
			ourHolder.unlockCanvasAndPost(canvas);

//			String dataReturned = ProcessHWDebug.getDefaults(consFileName);
			String dataReturned = ProcessHWDebug.getDefaults("18.caw");
			if (dataReturned == null)
				consFileNumber++;
			else {
				String[] lines = dataReturned.split(System.getProperty("line.separator"));
				String[] coordinates = lines[0].split(" ");
				String xPrefix = "x_total=";
				String yPrefix = "y_total=";
				String xTotal = coordinates[0].substring(coordinates[0].indexOf(xPrefix) + xPrefix.length());
				String yTotal = coordinates[1].substring(coordinates[1].indexOf(yPrefix) + yPrefix.length());
				int new_rows = Integer.parseInt(yTotal) * 10;
				int new_cols = Integer.parseInt(xTotal) * 10;
				Mat construct_image = Mat.zeros(new_rows, new_cols, CvType.CV_8UC1);

				bmp_animate = Bitmap.createBitmap(construct_image.width(), construct_image.height(),
						Bitmap.Config.ARGB_8888);
				for (int j = 1; j < lines.length - 2; j++) {
					canvas = ourHolder.lockCanvas();

					if (lines[j + 1].equals("x=1000.0 y=1000.0")) {
						if (j < lines.length - 3) {
							j++;
							j++;
						} else
							break;

					}
					if (j < lines.length - 2) {
						coordinates = lines[j].split(" ");
						String xValue = coordinates[0].substring(coordinates[0].indexOf("x=") + "x=".length());
						String yValue = coordinates[1].substring(coordinates[1].indexOf("y=") + "y=".length());
						Double temp = Double.parseDouble(xValue);
						int x = temp.intValue();
						temp = Double.parseDouble(yValue);
						int y = temp.intValue();
						coordinates = lines[j + 1].split(" ");
						String xNextValue = coordinates[0].substring(coordinates[0].indexOf("x=") + "x=".length());
						String yNextValue = coordinates[1].substring(coordinates[1].indexOf("y=") + "y=".length());
						temp = Double.parseDouble(xNextValue);
						int x_next = temp.intValue();
						temp = Double.parseDouble(yNextValue);
						int y_next = temp.intValue();

						Point p1 = new Point(x * 10, y * 10);
						Point p2 = new Point(x_next * 10, y_next * 10);
						Core.line(construct_image, p1, p2, new Scalar(255));
						Utils.matToBitmap(construct_image, bmp_animate);
						canvas.drawBitmap(bmp_animate, 100, 100, null);
						ourHolder.unlockCanvasAndPost(canvas);
						// invalidate();
						// try {
						// Thread.sleep(500);
						// } catch (InterruptedException e) {
						// // TODO Auto-generated catch block
						// e.printStackTrace();
						// }
						//
					}
				}
			}

			// textBox.setText(xValue+yValue);
		}

	}
	
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		pause();
//		consFileNumber++;
//		if (consFileNumber < 9)
//			consFileName = "0" + (consFileNumber) + ".caw";
//		else
//			consFileName = (consFileNumber) + ".caw";
//		
//		resume();
//		return true;
//	}
}
