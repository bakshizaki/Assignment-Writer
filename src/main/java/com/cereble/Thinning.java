package com.cereble;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.widget.Toast;

public class Thinning {

	private static Context context;
	public Thinning(Context c) {
		context=c;
	}
	static int boolToInt(boolean b) {
		return b ? 1 : 0;
	}

	public static int[][] matrix_not(int[][] iMat, int rows, int cols) {
		int[][] result= new int[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (iMat[i][j] == 0)
					result[i][j] = 1;
				else if (iMat[i][j] == 1)
					result[i][j] = 0;
//				else
//					Toast.makeText(context, "ERROR ERROR ERROR", Toast.LENGTH_LONG).show();
			}
		}
		return result;
	}

	public static int[][] matrix_and(int[][] mat1, int rows, int cols, int[][] mat2) {
		int[][] result= new int[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				result[i][j] = mat1[i][j] & mat2[i][j];
			}

		}
		return result;
	}

	public static int[][] matrix_divide(int[][] iMat, int rows, int cols, int number) {
		int[][] result= new int[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				result[i][j] = iMat[i][j] / number;
			}

		}
		return result;
	}

	public static int[][] matrix_multiply(int[][] iMat, int rows, int cols, int number) {
		int[][] result= new int[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				result[i][j] = iMat[i][j] * number;
			}

		}
		return result;
	}

	
	public static int[][] abs_diff(int[][] mat1, int rows, int cols, int[][] mat2) {
		int[][] result= new int[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				result[i][j] = Math.abs(mat1[i][j] - mat2[i][j]);
			}

		}
		return result;
	}

	public static int matrix_sum(int[][] iMat, int rows, int cols) {
		int sum = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				sum += iMat[i][j];
			}
		}

		return sum;
	}

	public static int[][] multiply2matrix(int[][] mat1, int rows, int cols, int[][] mat2) {
		int[][] result = new int[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				result[i][j] = mat1[i][j] * mat2[i][j];
			}
		}
		return result;
	}
	
	public static int[][] ThinningIteration(int[][] im, int rows, int cols, int iter) {
		int[][] marker = new int[rows][cols];

		// Mat marker = Mat.zeros(im.size(), CvType.CV_8UC1);
		for (int i = 1; i < rows-1; i++) {
			for (int j = 1; j < cols-1; j++) {
				int p2 = im[i - 1][j];
				int p3 = im[i - 1][j + 1];
				int p4 = im[i][j + 1];
				int p5 = im[i + 1][j + 1];
				int p6 = im[i + 1][j];
				int p7 = im[i + 1][j - 1];
				int p8 = im[i][j - 1];
				int p9 = im[i - 1][j - 1];

				int A = boolToInt(p2 == 0 && p3 == 1) + boolToInt(p3 == 0 && p4 == 1) + boolToInt(p4 == 0 && p5 == 1)
						+ boolToInt(p5 == 0 && p6 == 1) + boolToInt(p6 == 0 && p7 == 1) + boolToInt(p7 == 0 && p8 == 1)
						+ boolToInt(p8 == 0 && p9 == 1) + boolToInt(p9 == 0 && p2 == 1);
				int B = p2 + p3 + p4 + p5 + p6 + p7 + p8 + p9;
				int m1 = iter == 0 ? (p2 * p4 * p6) : (p2 * p4 * p8);
				int m2 = iter == 0 ? (p4 * p6 * p8) : (p2 * p6 * p8);
				// if (A == 1 && (B >= 2 && B <= 6) && m1 == 0 && m2 == 0)
				// marker.put(i,j) = 1;
				if (A == 1 && (B >= 2 && B <= 6) && m1 == 0 && m2 == 0)
					marker[i][j] = 1;
			}
		}
		//MainActivity.etImageData.append("Marker"+matrix_sum(marker, rows, cols)+"\n");
		return matrix_and(im, rows, cols, matrix_not(marker, rows, cols));
	}

	public static int[][] Thinning(int[][] im,int rows,int cols)
	{
		int num_iteration=0;
//		Toast.makeText(context, "original:"+matrix_sum(im, rows, cols), Toast.LENGTH_LONG).show();
		im=matrix_divide(im, rows, cols, 255);
//		Toast.makeText(context, "divided:"+matrix_sum(im, rows, cols), Toast.LENGTH_LONG).show();
		int[][] prev = new int[rows][cols];
		int[][] diff = new int[rows][cols];
		do{
			im=ThinningIteration(im, rows, cols, 0);
			im=ThinningIteration(im, rows, cols, 1);
			diff=abs_diff(im, rows, cols, prev);
			prev=im.clone();
			num_iteration++;
			//MainActivity.etImageData.append(""+matrix_sum(im, rows, cols)+"\n");
		}while(matrix_sum(diff, rows, cols)>0);
		
//		Toast.makeText(context, "Iteration:"+num_iteration, Toast.LENGTH_LONG).show();
		return matrix_multiply(im, rows, cols, 255);
	}

}
