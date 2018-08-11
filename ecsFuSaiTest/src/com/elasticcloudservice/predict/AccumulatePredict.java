package com.elasticcloudservice.predict;

import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName ecsTest
 * @author Yezhibo
 * @CreatTime 2018年4月8日下午10:46:05
 */
public class AccumulatePredict {
	
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
	 * 得到参数数组 a[n-1] b[n-1] c[n-1]，输入三次平滑序列
	 * @param s1
	 * @param s2
	 * @param s3
	 * @param a
	 * @return
	 */
	public static List<double[]> GetPara(double[] s1, double[] s2, double a){
		
		List<double[]> para = new ArrayList<double[]>();
		
		int n = s1.length;
		
		double[] at = new double[n];    //一个st 对应一个at  最后的一个at用来预测下原始序列后边的值
		double[] bt = new double[n];
		
		for(int k=1; k<n; k++){
			
			at[k] = 2*s1[k] - s2[k];
			
			bt[k] = (a/(1-a))*(s1[k]-s2[k]);

		}
		
		para.add(at);
		para.add(bt);
		
		return para;
		
	}
	
	/**
	 * 获取两个序列之间的残差和，用于二次平滑选取最优系数a
	 * @param ypreArray 表示原始序列预测值，从第二个开始，最后一个表示预测的下一个值
	 * @param yArray  原始序列  只需要计算两个序列后n-1个数值之间的残差即可
	 * @return
	 */
	public static double GetMSE(double[] ypreArray, double[] yArray){
		
		double mse = 0;
		
		int n = ypreArray.length;
		
		double sum = 0;
		
		for(int i=1; i<n; i++){
			
			sum += (ypreArray[i-1]-yArray[i])*(ypreArray[i-1]-yArray[i]);
			
		}
		
		mse = Math.sqrt(sum)/n;
		
		return mse;
	} 
	
	/**
	 * 利用3次指数平滑预测原理得到预测结果,默认m=1,即只预测一步
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public static double[] GetPreY(double[] a, double[] b){
		
		int n = a.length;
		
		double[] y = new double[n];
		
		for(int i=0; i<n; i++){        //计算的第一个值表示原始序列第二个值的预测值，最后一个值表示真实预测的第一个值
			
			y[i] = a[i] + b[i]; 
			
		}
		
		return y;
	}
	
	/**
	 * 通过遍历来求出最优系数，选取二次指数平滑最优系数
	 * @param y
	 * @return
	 */
	public static double GetOpta(double[] yArray){
		
		double opta = 0;
		
		double minMSE = 9999;
		
		for(double a=0.001; a<1; a+=0.001){
			
			double[] smooth1y = GetOnceSmoothArray(yArray, a, yArray[0]);          //对原始序列y进行一次平滑，得到一次平滑序列
			double[] smooth2y = GetOnceSmoothArray(smooth1y, a, yArray[0]);   //对一次平滑后序列再进行平滑，得到二次平滑序列
			
			List<double[]> Para = GetPara(smooth1y, smooth2y, a); //得到用于预测的 a b c 时间序列
			
			double[] at = Para.get(0);      //获取参数序列a
			double[] bt = Para.get(1);      //获取参数序列b
			
			double[] ypreArray = GetPreY(at, bt);  //根据参数序列 a b c 进行一步预测，得到原序列的预测结果（包含真实的预测值,最后一个，计算残差不参与）
			
			double mse = GetMSE(ypreArray, yArray);  //计算原序列与原序列预测值之间的残差和（从第三个值开始）
			
			if(mse<minMSE){
				minMSE = mse;
				opta = a;
			}else{
				break;
			}
			
		}
		
		return opta;
		
	}
	
	/**
	 * 输入原始数据，获取合并后的数据，用于画图调用
	 * @param detectArray
	 * @param pDays
	 * @return
	 */
	public static double[] GetYArray(double[] detectArray){
		
		int n = detectArray.length;  //原始数据的长度
		
		double[] yArray = new double[n];
		
		for(int i=0; i<n; i++){     //获取原始序列的累加和
			
			for(int j=0; j<=i; j++){				
				yArray[i] += detectArray[j];
			}
			
		}
		
		return yArray;
		
	}
	
		
	public static double[] GetPredictArray(double[] detectArray, int pDays){
		
		double[] yArray = GetYArray(detectArray);  //对原始序列进行合并
		
		double opta = GetOpta(yArray);  //获得最优参数a
		
		double[] smooth1y = GetOnceSmoothArray(yArray, opta, yArray[0]);          //对原始序列y进行一次平滑，得到一次平滑序列
		double[] smooth2y = GetOnceSmoothArray(smooth1y, opta, yArray[0]);   //对一次平滑后序列再进行平滑，得到二次平滑序列
		
		List<double[]> Para = GetPara(smooth1y, smooth2y, opta); //得到用于预测的 a b 时间序列
		
		double[] at = Para.get(0);      //获取参数序列a
		double[] bt = Para.get(1);      //获取参数序列b
		
		double[] ypreArray = GetPreY(at, bt);  //根据参数序列 a b 进行一步预测，得到原序列的预测结果（包含真实的预测值）
		
		int n = detectArray.length;
		
		double[] preArray = new double[n+pDays-1];
		
		System.arraycopy(ypreArray, 0, preArray, 0, n);
		
		double a = at[at.length-1];  //得到  at

		double b = bt[bt.length-1];  //得到 bt
				
		int m = 2;
		
		for(int i=n; i<n+pDays-1; i++){  //开始从第二天开始预测
			
			preArray[i] = a + b*m;
			
			m++;
			
		}	
										
		return preArray;
		
	}

}
