package com.cereble;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Point;



public class EndPoints {

	public static List<Point> findEndPoint(int[][] im,int rows,int cols)
	{
		int[][] mask = {{1,1,1},{1,15,1},{1,1,1}};
		List<Point> end_points = new ArrayList<Point>();
		boolean first_flag = true;
		Point first_point = new Point();
		for(int y=1;y<rows-1;y++)
		{
			for(int x=1;x<cols-1;x++)
			{
				if(im[y][x]==255 && first_flag)
				{
					first_flag=false;
					first_point.x = x;
					first_point.y = y;
				}
				int[][] roi = new int[3][3];
				for(int i=0, y_dash = y-1;y_dash<y+2;y_dash++,i++)
					for(int j=0, x_dash=x-1;x_dash<x+2;x_dash++,j++)
						roi[i][j] = im[y_dash][x_dash];
				
				int[][] convoluted = Thinning.multiply2matrix(roi, 3, 3,mask);
				int summation = Thinning.matrix_sum(convoluted, 3, 3);
				if(summation == 4080)
				{
					end_points.add(new Point(x, y));
				}
			}
		}
		
		if(end_points.isEmpty())
			end_points.add(first_point);
		
		return end_points;
	}
	
	
	public static int[][] removeDots(int[][] im, int rows, int cols)
	{
		int[][] mask = {{1,1,1},{1,15,1},{1,1,1}};
		for(int y=1;y<rows-1;y++)
		{
			for(int x=1;x<cols-1;x++)
			{
				int[][] roi = new int[3][3];
				for(int i=0, y_dash = y-1;y_dash<y+2;y_dash++,i++)
					for(int j=0, x_dash=x-1;x_dash<x+2;x_dash++,j++)
						roi[i][j] = im[y_dash][x_dash];
				
				int[][] convoluted = Thinning.multiply2matrix(roi, 3, 3,mask);
				int summation = Thinning.matrix_sum(convoluted, 3, 3);
				if(summation == 3825)
				{
					im[y][x] = 0;
				}
			}
		}
		
		return im;
	}
}
