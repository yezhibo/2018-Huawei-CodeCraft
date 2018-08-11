package com.elasticcloudservice.predict;

/**
 * 指数平滑类
 * @ProjectName ecsExpSmooth
 * @author Yezhibo
 * @CreatTime 2018年4月5日下午8:47:12
 */
public class ExpSmoothPredict {
	
	/**
	 * 获取一次指数平滑序列
	 * @param dataArray
	 * @param a
	 * @param s0
	 * @return
	 */
	public static double[] GetOnceSmoothArray(double[] dataArray, double a, double s0){
		
		double[] smoothArray = new double[dataArray.length];
		
		smoothArray[0] = s0;
		
		for(int i=1; i<dataArray.length; i++){
			
			smoothArray[i] = a*dataArray[i] + (1-a)*smoothArray[i-1];
														
		}
		
		return smoothArray;
		
	}
		
	/**
	 * 预测类
	 * @param dataArray
	 * @param pDays
	 * @param a
	 * @return
	 */
	public static double[] GetPredictArray(double[] dataArray, double a1,int pw ){
		int n = dataArray.length;
		double mean = 0;
		for(int i=0; i<n/3; i++){
			mean += dataArray[i];
		}
		mean /= (n/3); 
		double[] pwArray = new double[pw];
		for(int i=0; i<pw; i++){     
			dataArray = GetOnceSmoothArray(dataArray, a1, mean);          //对原始序列y进行一次平滑，得到一次平滑序列
			pwArray[i] = dataArray[n-1];
		}
		
		return pwArray;
		
	}
	
}
