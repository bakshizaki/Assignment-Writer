package com.cereble;

public class MyConversion {
	
	public static int[] ByteToIntArray(byte[] buff)
	{
		int[] a = new int[buff.length];
		int counter=0;
		for(byte b:buff){
				a[counter]=b & 0xff;
				counter++;
		}
		return a;
	}

	public static byte[] IntToByteArray(int[] a)
	{
		 
		byte[] buff = new byte[a.length];
		int counter=0;
		for(int i:a){
			buff[counter]=(byte)i;
			counter++;
		}
		return buff;
	}
	
	public static int[][] ArraytoMat(int[] a,int rows,int cols)
	{
		int[][] iMat = new int[rows][cols];
		int counter=0;
		for(int i=0;i<rows;i++)
		{
			for(int j=0;j<cols;j++)
			{
				iMat[i][j]=a[counter];
				counter++;
			}
		}
		return iMat;
	}


	public static int[] MattoArray(int[][] iMat,int rows,int cols)
	{
		int[] a = new int[rows*cols];
		int counter=0;
		for(int i=0;i<rows;i++)
		{
			for(int j=0;j<cols;j++)
			{
			a[counter]=iMat[i][j];
			counter++;
			}
		}
		return a;
	}
}