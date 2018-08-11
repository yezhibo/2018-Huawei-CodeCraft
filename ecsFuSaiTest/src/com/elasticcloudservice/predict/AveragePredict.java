package com.elasticcloudservice.predict;

import com.elasticcloudservice.dataTool.DataPreprocess;

/**
 * 69.525分
 * @ProjectName ecsTest
 * @author Yezhibo
 * @CreatTime 2018年4月8日下午5:12:58
 */
public class AveragePredict {

		
	/**
	 * 预测
	 * @param detectArray
	 * @param itvDays
	 * @param preDays
	 * @return
	 */
	public static int GetPredictArray(double[] detectArray, int itvDays, int preDays){
		
		double w1 = 1.1;                //平移间隔数据时的系数
		double w = 1.5;                 //根据间隔数据平移预测数据时的倍数
		
		//计算历史间隔数据的和
		double sum1 = 0;
		int n = detectArray.length;
		for(int i=n-1; i>n-1-itvDays; i--){
			sum1 += detectArray[i];
		}
		
		//平移间隔数据并合并为训练数据
		int orgl = n+itvDays;
		double[] orgArray = new double[orgl];
		for(int i=0; i<orgl; i++){
			if(i<n)
				orgArray[i] = detectArray[i];
			else
				orgArray[i] = sum1*w1/itvDays;
		}
		
		//对训练数据进行按周合并 然后按周平移
		double[] yArray = DataPreprocess.GetYArray(orgArray, 7);
		int pweek = preDays/7;
		double sum2 = 0;		
		int tw = yArray.length;		
		for(int i=tw-1; i>tw-1-pweek; i--){
			sum2 += yArray[i]*w+2;
		}
		
		int psum = (int)(sum2+sum1);
		
		return psum;
		
	}
	
}
