package com.elasticcloudservice.predict;

/**
 * 指数平滑类，71分
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
			
			//double fait = a / (1 - Math.pow(1-a, i+1));

			smoothArray[i] = a*dataArray[i] + (1-a)*smoothArray[i-1];
														
		}
		
		return smoothArray;
		
	}
	
	
	/**
	 * 三次指数平滑 得到预测p天数据
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
	
	
	/*
	public static void main(String[] args) {
		//double[] dataArray = {253993,275396.2,315229.5,356949.6,400158.2,442431.7,495102.9,570164.8,640993.1,704250.4,767455.4,781807.8,776332.3,794161.7,834177.7,931651.5,1028390,1114914};
		
		double[] flavor1 = {3,	6,	2,	2,	0,	3,	4,	7,	2.78181818181818,	3,	4,	0,	0,	4,	1,	5,	3,	2.78181818181818,	3,	0,	6,	2,	4,	7,	7,	2,	1,	2.78181818181818,	2,	3,	1,	0,	2,	1,	1,	0,	6,	2,	1,	0,	2,	1,	4,	3,	2.78181818181818,	2,	3,	1,	3,	3,	2,	5,	2.78181818181818,	1,	2};
		
		double[] preArray = ExpThrPredict(flavor1, 7);
		
		System.out.println("最后预测值："+Arrays.toString(preArray));
				
	}*/
}
