package com.cereble;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opencv.core.Point;

import android.util.Log;

public class Tracing {

	public static List<Point> traceAlphabet(int[][] im,int rows,int cols)
	{
		String TAG="Tracing.java";
		List<Point> end_points = new ArrayList<Point>();
		int[][] mask0 = {{1,1,1},{1,0,1},{1,1,1}};
		end_points = EndPoints.findEndPoint(im, rows, cols);
		List<Point> points_of_interest = new ArrayList<Point>();
		int pixel_count=0;
		int least_count_pixel = 5;
		int up_down = 0;
		int left_right = 0;
		List<Point> direction_points_queue = new ArrayList<Point>();
		int direction_pixel_count=0;
		
		while(!end_points.isEmpty())
		{
			Point curr_ep = end_points.get(0);
			int x = (int) curr_ep.x;
			int y = (int) curr_ep.y;
			if(!points_of_interest.contains(curr_ep))
			{
				points_of_interest.add(curr_ep);
				pixel_count=0;
			}
			
			Point remove_from_endpoints = end_points.get(0);
			while(end_points.contains(remove_from_endpoints))
			end_points.remove(remove_from_endpoints);
			boolean first_time=true;
			int temp=1;
			List<Point> eight_connect_option_points = new ArrayList<Point>();
			int eight_connect_option_counter = 0;
			List<Point> hide_end_point = new ArrayList<Point>();
			int hide_end_point_counter = 0;
			while(!(end_points.contains(new Point(x,y))) || first_time)
			{
				Log.i(ProcessHWDebug.fileproc+temp, "x: "+x+" y: "+y);
				
				if(x== 37 && y==5)
					 temp = 0;
				first_time=false;
				if(pixel_count == least_count_pixel)
				{
					points_of_interest.add(new Point(x,y));
					pixel_count = 0;
				}
				int[][] roi = new int[3][3];
				for(int i=0, y_dash = y-1;y_dash<y+2;y_dash++,i++)
					for(int j=0, x_dash=x-1;x_dash<x+2;x_dash++,j++)
						roi[i][j] = im[y_dash][x_dash];

				int[][] convoluted0 = Thinning.multiply2matrix(roi, 3, 3,mask0);
				int summation0 = Thinning.matrix_sum(convoluted0, 3, 3);
				
				if(summation0>0)
				{
					int ii=0,jj=0;
					List<Point> four_connectivity_points = new ArrayList<Point>();
					List<Point> eight_connectivity_points = new ArrayList<Point>();
					for(int i=0;i<3;i++)
						for(int j=0;j<3;j++)
							if (convoluted0[i][j]==255)
								{
									if(Math.abs(i-j)==1)
										four_connectivity_points.add(new Point(i,j));
									else
										eight_connectivity_points.add(new Point(i,j));
								}
					
					im[y][x]=0;
					if(direction_pixel_count<5)
					{
						
						if(!four_connectivity_points.isEmpty())
						{
							ii=(int) four_connectivity_points.get(0).x;
							jj=(int) four_connectivity_points.get(0).y;
						}
						else
						{
							ii=(int) eight_connectivity_points.get(0).x;
							jj=(int) eight_connectivity_points.get(0).y;
						}
						direction_pixel_count++;
						up_down+=(1-ii);
						left_right+=(1-jj);
						direction_points_queue.add(0, new Point(ii, jj));
					}
					else
					{
						if(summation0 == 255)
						{
							
							if(!four_connectivity_points.isEmpty())
							{
								ii=(int) four_connectivity_points.get(0).x;
								jj=(int) four_connectivity_points.get(0).y;
							}
							else
							{
								ii=(int) eight_connectivity_points.get(0).x;
								jj=(int) eight_connectivity_points.get(0).y;
							}
							Point point_to_remove = direction_points_queue.remove(direction_points_queue.size()-1);
							int remove_i=(int) point_to_remove.x;
							int remove_j=(int) point_to_remove.y;
							up_down-=(1-remove_i);
							left_right-=(1-remove_j);
							direction_points_queue.add(0, new Point(ii, jj));
							up_down+=(1-ii);
							left_right+=(1-jj);
							
						}
						else if(summation0>255)
						{
							List<Point> option_points = new ArrayList<Point>();
							List<Integer> correlation_with_direction = new ArrayList<Integer>();
							
							for(int i=0;i<3;i++)
								for(int j=0;j<3;j++)
									if(convoluted0[i][j]==255)
									{
										if(eight_connectivity_points.contains(new Point(i, j)))
										{
											if(i==0 && j==0 && (convoluted0[i+1][j]==255 || convoluted0[i][j+1]==255))
												continue;
											if(i==0 && j==2 && (convoluted0[i+1][j]==255 || convoluted0[i][j-1]==255))
												continue;
											if(i==2 && j==0 && (convoluted0[i-1][j]==255 || convoluted0[i][j+1]==255))
												continue;
											if(i==2 && j==2 && (convoluted0[i-1][j]==255 || convoluted0[i][j-1]==255))
												continue;
										}
										option_points.add(new Point(i, j));
										correlation_with_direction.add(Math.max(Math.abs((up_down)+(1-i)),Math.abs((left_right)+(1-j))));
									}
							int selec_point_index = correlation_with_direction.indexOf(Collections.max(correlation_with_direction));
							ii = (int) option_points.get(selec_point_index).x;
							jj = (int) option_points.get(selec_point_index).y;
							Point point_to_remove = direction_points_queue.remove(direction_points_queue.size()-1);
							int remove_i = (int) point_to_remove.x;
							int remove_j = (int) point_to_remove.y;
							up_down-=(1-remove_i);
							left_right-=(1-remove_j);
							direction_points_queue.add(0, new Point(ii, jj));
							up_down+=(1-ii);
							left_right+=(1-jj);
						}
						
					}
					if(summation0>255)
					{
						for(int i=0;i<3;i++)
						{
							for(int j=0;j<3;j++)
							{
								if(convoluted0[i][j]==255 && !(i==ii && j==jj))
								{
									if(eight_connectivity_points.contains(new Point(i, j)))
									{
										if(i==0 && j==0 && (convoluted0[i+1][j]==255 || convoluted0[i][j+1]==255))
										{
											eight_connect_option_points.add(new Point(x+(j-1),y+(i-1)));
											eight_connect_option_counter=0;
											continue;
										}
										if(i==0 && j==2 && (convoluted0[i+1][j]==255 || convoluted0[i][j-1]==255))
										{
											eight_connect_option_points.add(new Point(x+(j-1),y+(i-1)));
											eight_connect_option_counter=0;
											continue;
										}
											
										if(i==2 && j==0 && (convoluted0[i-1][j]==255 || convoluted0[i][j+1]==255))
										{
											eight_connect_option_points.add(new Point(x+(j-1),y+(i-1)));
											eight_connect_option_counter=0;
											continue;
										}
										if(i==2 && j==2 && (convoluted0[i-1][j]==255 || convoluted0[i][j-1]==255))
										{
											eight_connect_option_points.add(new Point(x+(j-1),y+(i-1)));
											eight_connect_option_counter=0;
											continue;
										}
									}
									end_points.add(0, new Point(x+(j-1),y+(i-1)));
									
									hide_end_point.add(0, new Point(x+(j-1),y+(i-1)));
									hide_end_point_counter=0;
									im[y+(i-1)][x+(j-1)] = 0;
									
									if(!points_of_interest.contains(new Point(x,y)))
									{
										points_of_interest.add(new Point(x, y));
										pixel_count=0;
									}
								}
							}
						}
					}
					
					
					
					y=y+(ii-1);
					x=x+(jj-1);
					if(hide_end_point_counter == 1 && !(hide_end_point.isEmpty()))
						{
							for(int iHide = 0; iHide < hide_end_point.size() ; iHide++)
							{
								Point pHide = hide_end_point.get(iHide);
								im[(int) pHide.y][(int) pHide.x] = 255;
							}
							hide_end_point.clear();
							hide_end_point_counter=0;
						}
					
					hide_end_point_counter++;
					
					
					if(eight_connect_option_points.contains(new Point(x,y)))
					{
						eight_connect_option_points.remove(new Point(x,y));
					}
					if(!eight_connect_option_points.isEmpty())
					{
						eight_connect_option_counter++;
						if(eight_connect_option_counter==2)
						{
							Point p = eight_connect_option_points.get(0);
							end_points.add(0, p);
							eight_connect_option_points.clear();
							eight_connect_option_counter=0;
						}
					}
					
					pixel_count+=1;
					
					if(end_points.contains(new Point(x, y)) && (!points_of_interest.contains(new Point(x, y))))
					{
						points_of_interest.add(new Point(x, y));
						pixel_count=0;
						Point remove_from_ep = new Point(x, y); 
						while(end_points.contains(remove_from_ep))
						end_points.remove(remove_from_ep);

						//end_points.remove();
						points_of_interest.add(new Point(1000, 1000));
						
						up_down=0;
						left_right=0;
						direction_points_queue.clear();
						direction_pixel_count=0;
						break;
					}
					
					

					
				}
				else
				{
					if(summation0==0)
					{
					if(!points_of_interest.contains(new Point(x, y)))
					{
						points_of_interest.add(new Point(x, y));
						points_of_interest.add(new Point(1000, 1000));
						pixel_count=0;
					
						
					}
					break;
					}
//					end_points.remove(new Point(x, y));
//					points_of_interest.add(new Point(1000, 1000));
//					
//					up_down=0;
//					left_right=0;
//					direction_points_queue.clear();
//					direction_pixel_count=0;

				}
			}
			
			
		}
		
		return points_of_interest;
		
	}
	
}
