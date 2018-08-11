package com.elasticcloudservice.datapreprocess;

import java.util.Random;

/**
 * 数学运算类
 * @ProjectName AR80
 * @author Yezhibo
 * @CreatTime 2018年4月15日上午10:03:21
 */
public class DataMath {
	
	/**
	 * 滤除小于零的元素
	 * @param dataArray
	 * @return
	 */
	public static double[] filterNegative(double[] dataArray){
		double[] newArray = new double[dataArray.length];
		
		for(int i=0; i<dataArray.length; i++){
			if(dataArray[i]<0){
				newArray[i] = -0.2*dataArray[i];
			}else{
				newArray[i] = dataArray[i];
			}
		}
		
		return newArray;
	}
	
	/**
	 * 标准平方差
	 * @param dataArray
	 * @return
	 */
	public static double stderrData(double[] dataArray)
	{
		return Math.sqrt(varerrData(dataArray));
	}
	
	/**
	 * 平方差函数
	 * @param dataArray
	 * @return
	 */
	public static double varerrData(double[] dataArray)
	{
		double variance=0;
		double avgsumData=avgData(dataArray);
		double[] tempArray = new double[dataArray.length];
		
		for(int i=0 ; i<dataArray.length ; i++){
			
			tempArray[i] = dataArray[i];
			
		}
		
		for(int i=0;i<dataArray.length;i++)
		{
			tempArray[i]-=avgsumData;
			variance+=tempArray[i]*tempArray[i];
		}
		return variance/dataArray.length;//variance error;
	}
	
	/**
	 * 对数据从大到小进行排序
	 * @param dataArray
	 * @return
	 */
	public static double[] sorts(double[] dataArray){
		
		double[] sortsArray = new double[dataArray.length];
		
		for(int i=0 ; i<dataArray.length ; i++){
			
			sortsArray[i] = dataArray[i];
			
		}		
		
		double temp = 0;
		
		for(int i=0 ; i<dataArray.length-1 ; i++){
			
			for(int j=i+1 ; j<dataArray.length ; j++){
				
				if(sortsArray[i]<sortsArray[j]){
					
					temp = sortsArray[i];
					sortsArray[i] = sortsArray[j];
					sortsArray[j] = temp;
					
				}
			}
			
		}
		
		return sortsArray;
	}
	
	public static int[] sortInt(int[] dataArray){
		
		int[] sortsArray = new int[dataArray.length];
		
		for(int i=0 ; i<dataArray.length ; i++){
			
			sortsArray[i] = dataArray[i];
			
		}		
		
		int temp = 0;
		
		for(int i=0 ; i<dataArray.length-1 ; i++){
			
			for(int j=i+1 ; j<dataArray.length ; j++){
				
				if(sortsArray[i]<sortsArray[j]){
					
					temp = sortsArray[i];
					sortsArray[i] = sortsArray[j];
					sortsArray[j] = temp;
					
				}
			}
			
		}
		
		return sortsArray;
	}
	
	/**
	 * 产生一个均值为a方差为b的高斯随机数
	 * @param a
	 * @param b
	 * @return
	 */
	public static double gaussData(double a, double b){
		Random random = new Random();
		return Math.sqrt(b)*random.nextGaussian()+a;
	}
	
	/**
	 * 最大值函数
	 * @param dataArray
	 * @return
	 */
	public static double maxData(double[] dataArray){
		
		double max = dataArray[0];
		for(int i=1 ; i<dataArray.length ; i++){
			if(max<dataArray[i])
				max = dataArray[i];
		}
		return max;
		
	}
	
	/**
	 * 最小值函数
	 * @param dataArray
	 * @return
	 */
	public static double minData(double[] dataArray){
		
		double min = dataArray[0];
		for(int i=1 ; i<dataArray.length ; i++){
			if(min>dataArray[i])
				min = dataArray[i];
		}
		return min;
		
	}
	
	public static int minIntData(int[] dataArray){
		
		int min = dataArray[0];
		for(int i=1 ; i<dataArray.length ; i++){
			if(min>dataArray[i])
				min = dataArray[i];
		}
		return min;
		
	}
	
	/**
	 * 平均值
	 * @param dataArray
	 * @return
	 */
	public static double avgData(double[] dataArray)
	{
		return sumData(dataArray)/dataArray.length;
	}
	
	/**
	 * 和函数
	 * @param dataArray
	 * @return
	 */
	public static double sumData(double[] dataArray)
	{
		double sumData=0;
		for(int i=0;i<dataArray.length;i++)
		{
			sumData+=dataArray[i];
		}
		return sumData;
	}
	
	public static int sumIntData(int[] dataArray)
	{
		int sumData=0;
		for(int i=0;i<dataArray.length;i++)
		{
			sumData+=dataArray[i];
		}
		return sumData;
	}
		
	/**
	 * 最小二乘法系数矩阵x
	 * @param dataArray
	 * @param p
	 * @return x[n-p][p] 系数矩阵，从第p+1个元素开始，依次往后顺延
	 */
	public static double[][] xMatrix(double[] dataArray, int p){
		
		int n = dataArray.length;
		double[][] X = new double[n-p][p];
		for(int i=0;i<n-p;i++){
			for(int j=0;j<p;j++){
				X[i][j]=dataArray[i+j];
			}
		}
		return X;
		
	}
	
 	/**
 	 * 最小二乘法系数矩阵y
 	 * @param dataArray
 	 * @param p
 	 * @return y[n-p][1], 返回矩阵形式系数，方便在递归运算时候求矩阵的转置
 	 */
 	public static double[][] yMatrix(double[] dataArray, int p){
 		
 		int n = dataArray.length;
 		double[][] y = new double[n-p][1];
 		for(int i=0;i<n-p;i++){
 			y[i][0]=dataArray[i+p];
 		}
 		return y;
 		
 	}
			 			
}
