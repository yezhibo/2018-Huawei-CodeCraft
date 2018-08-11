package com.elasticcloudservice.predict;

import java.util.ArrayList;
import java.util.List;

import com.elasticcloudservice.dataTool.DataPreprocess;

//import com.elasticcloudservice.dataTool.DataMath;

/**
 * 58.806分
 * @ProjectName ecsTest
 * @author Yezhibo
 * @CreatTime 2018年4月8日上午12:21:37
 */
public class CombinedPredict {

	/**
	 * 通过梯度下降法拟合出曲线方程
	 * @param x
	 * @param y
	 * @return
	 */
	public static double[] GetPara(double[] x, double[] y){
		
		double[] w = new double[3];  //拟合方程为  y = w1 * x2 + w2 *x + w3;
		
		for(int i=0; i<3; i++){  //给权重赋初始值，-1~1之间的随机数
			
			w[i] = Math.random()*2-1;
			
		}		
		
		return w;
		
	}
		
	
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
	public static List<double[]> GetPara(double[] s1, double[] s2, double[] s3, double a){
		
		List<double[]> para = new ArrayList<double[]>();
		
		int n = s1.length;
		
		double[] at = new double[n];    //一个st 对应一个at  最后的一个at用来预测下原始序列后边的值
		double[] bt = new double[n];
		double[] ct = new double[n];
		
		for(int k=1; k<n; k++){
			
			at[k] = 3*s1[k] - 3*s2[k]+ s3[k];
			
			bt[k] = ( a / (2*(1-a)*(1-a)) ) * ( (6-5*a)*s1[k] - 2*(5-4*a)*s2[k] + (4-3*a)*s3[k] );
			
			ct[k] = ( (a*a) / (2*(1-a)*(1-a)) ) * ( s1[k] - 2*s2[k] + s3[k] );
		}
		
		para.add(at);
		para.add(bt);
		para.add(ct);
		
		return para;
		
	}
	
	/**
	 * 获取两个序列之间的残差和，用于三次平滑选取最优系数a
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
	public static double[] GetPreY(double[] a, double[] b, double[] c){
		
		int n = a.length;
		
		double[] y = new double[n];
		
		for(int i=0; i<n; i++){        //计算的第一个值表示原始序列第二个值的预测值，最后一个值表示真实预测的第一个值
			
			y[i] = a[i] + b[i] + 0.5*c[i]; 
			
		}
		
		return y;
	}
		
	
	/**
	 * 通过遍历来求出最优系数，选取三次指数平滑最优系数
	 * @param y
	 * @return
	 */
	public static double GetOpta(double[] yArray){
		
		double opta = 0;
		
		double minMSE = 9999;
		
		for(double a=0.001; a<1; a+=0.001){
			
			double[] smooth1y = GetOnceSmoothArray(yArray, a, yArray[0]);          //对原始序列y进行一次平滑，得到一次平滑序列
			double[] smooth2y = GetOnceSmoothArray(smooth1y, a, yArray[0]);   //对一次平滑后序列再进行平滑，得到二次平滑序列
			double[] smooth3y = GetOnceSmoothArray(smooth2y, a, yArray[0]);   //对二次平滑后序列再进行平滑，得到三次平滑序列
			
			List<double[]> Para = GetPara(smooth1y, smooth2y, smooth3y, a); //得到用于预测的 a b c 时间序列
			
			double[] at = Para.get(0);      //获取参数序列a
			double[] bt = Para.get(1);      //获取参数序列b
			double[] ct = Para.get(2);      //获取参数序列c
			
			double[] ypreArray = GetPreY(at, bt, ct);  //根据参数序列 a b c 进行一步预测，得到原序列的预测结果（包含真实的预测值,最后一个，计算残差不参与）
			
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
	
	
	
	
	
	public static int GetPredictArray(double[] detectArray,int IntervalDays, int preDays){
		
		/*1.先对原始数据进行累加，预测出间隔数据*/
		double[] yArray1 = DataPreprocess.GetYArray(detectArray);  		
		double a1 = 0.9;   //第一遍平滑的系数
		double a2 = 0.9;   //第二遍平滑的系数
		double[] smooth1y = GetOnceSmoothArray(yArray1, a1, yArray1[0]);          //对原始序列y进行一次平滑，得到一次平滑序列
		double[] smooth2y = GetOnceSmoothArray(smooth1y, a2, yArray1[0]);             //对一次平滑后序列再进行平滑，得到二次平滑序列
		
		double at = 2*smooth1y[smooth1y.length-1] - smooth2y[smooth2y.length-1];
		double bt = a2/(1-a2) * (smooth1y[smooth1y.length-1] - smooth2y[smooth2y.length-1]);
		
		//用累加后的数据来预测间隔数据
		int n = detectArray.length;
		double[] intvArray = new double[IntervalDays];  //累加数据的间隔数据预测结果
		for(int i=0; i<IntervalDays; i++){                   
			intvArray[i] = at + bt*(i+1);
		}
		double[] pintvArray = new double[IntervalDays]; //将累加数据进行还原
		for(int i=0; i<IntervalDays; i++){
			if(i==0)
				pintvArray[i] = intvArray[i]-yArray1[n-1];
			else
				pintvArray[i] = intvArray[i]-intvArray[i-1];
		}
		
		//将间隔数据与训练数据合并作为新的训练数据
		int orgl = n+IntervalDays;
		double[] orgArray = new double[orgl];
		for(int i=0; i<orgl; i++){
			if(i<n)
				orgArray[i] = detectArray[i];
			else
				orgArray[i] = pintvArray[i-n];
		}
		
		/*2.利用间隔数据和原始数据来平滑预测数据*/
		double[] yArray2 = DataPreprocess.GetYArray(orgArray, 7);
		double a3 = 0.9;   //第一遍平滑的系数
		double a4 = 0.9;   //第二遍平滑的系数
		double[] smooth3y = GetOnceSmoothArray(yArray2, a3, yArray2[0]);          //对原始序列y进行一次平滑，得到一次平滑序列
		double[] smooth4y = GetOnceSmoothArray(smooth3y, a4, yArray2[0]);             //对一次平滑后序列再进行平滑，得到二次平滑序列
		
		double at1 = 2*smooth3y[smooth3y.length-1] - smooth4y[smooth4y.length-1];
		double bt1 = a4/(1-a4) * (smooth3y[smooth3y.length-1] - smooth4y[smooth4y.length-1]);
		
		//用累加后的数据来预测间隔数据
		int pweek = (int)Math.ceil((double)preDays/7);
		double[] pArray = new double[pweek];  //累加数据的间隔数据预测结果
		for(int i=0; i<pweek; i++){                   
			pArray[i] = at1 + bt1*(i+1);
		}
		
		double lp = (double)preDays/7 - pweek +1;
		
		double sum = 0;
		for(int i=0; i<pweek; i++){
			if(i==pweek-1)
				sum += pArray[i]*lp;
			else
				sum += pArray[i];
		}
				
		int psum = (int)(sum);		
				
		return psum;
		
	}
	
}
