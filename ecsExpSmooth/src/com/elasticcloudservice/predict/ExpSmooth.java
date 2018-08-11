package com.elasticcloudservice.predict;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 指数平滑类，71分
 * @ProjectName ecsExpSmooth
 * @author Yezhibo
 * @CreatTime 2018年4月5日下午8:47:12
 */
public class ExpSmooth {
	
	/**
	 * 平滑计算  
	 * @param dataArray 原始数据数组
	 * @param a   系数
	 * @return  返回一步预测结果
	 */
	public static double OncePredict(double[] dataArray, double a){
		
		double st = 0;
		
		double s0 = dataArray[0];   //初始平滑值	
		
		int n = dataArray.length;
		
		double sum = 0;
		
		for(int i=0; i<n; i++){    
			
			sum += dataArray[n-i-1] * Math.pow(1-a, i);
			
		}
		
		st = a * sum + Math.pow(1-a, n)*s0;
		
		return st;
		
	}

	/**
	 * 一步预测算法，返回pDays预测结果
	 * @param dataArray
	 * @param pDays
	 * @param a
	 * @return
	 */
	public static double[] ExpOncePredict(double[] dataArray, int pDays, double a){
		
		double[] preArray = new double[pDays];
		
		int orgN = dataArray.length;
		
		for(int i=0; i<pDays; i++){
			
			double[] orgArray = new double[orgN+i];
			
			System.arraycopy(dataArray, 0, orgArray, 0, orgN);
			
			for(int j=0; j<i; j++){
				orgArray[orgN+j] = preArray[j];
			}
			
			preArray[i] = OncePredict(orgArray, a);
			
		}
		
		return preArray;
		
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
	 * 获取两个序列之间的残差和
	 * @param sArray
	 * @param xArray
	 * @return
	 */
	public static double GetMSE(double[] sArray, double[] xArray){
		
		double mse = 0;
		
		int n = xArray.length;
		
		double sum = 0;
		
		for(int i=2; i<n; i++){
			
			sum += (sArray[i-2]-xArray[i])*(sArray[i-2]-xArray[i]);
			
		}
		
		mse = Math.sqrt(sum)/n;
		
		return mse;
	} 
	
	/**
	 * 利用3次指数平滑预测原理得到预测结果
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public static double[] GetY(double[] a, double[] b, double[] c){
		
		int n = a.length;
		
		double[] y = new double[n-1];
		
		for(int i=0; i<n-1; i++){
			y[i] = a[i] + b[i] + 0.5*c[i]; 
		}
		
		return y;
	}
	
	/**
	 * 得到参数数组
	 * @param s1
	 * @param s2
	 * @param s3
	 * @param a
	 * @return
	 */
	public static List<double[]> GetPara(double[] s1, double[] s2, double[] s3, double a){
		
		List<double[]> para = new ArrayList<double[]>();
		
		int n = s1.length;
		
		double[] at = new double[n-1];
		double[] bt = new double[n-1];
		double[] ct = new double[n-1];
		
		for(int k=1; k<n; k++){
			at[k-1] = 3*s1[k] - 3*s2[k]+ s3[k];
			
			bt[k-1] = ( a / (2*(1-a)*(1-a)) ) * ( (6-5*a)*s1[k] - 2*(5-4*a)*s2[k] + (4-3*a)*s3[k] );
			
			ct[k-1] = ( (a*a) / (2*(1-a)*(1-a)) ) * ( s1[k] - 2*s2[k] + s3[k] );
		}
		
		para.add(at);
		para.add(bt);
		para.add(ct);
		
		return para;
		
	}
	
	public static double GetSSE(double[] yArray, double s0, double a){
		
		int n = yArray.length;
		
		double sse = 0;
		
		for(int t=1; t<n; t++){
			
			double sum = 0;
			for(int i=0; i<=t-1; i++){
				sum += a*Math.pow(1-a, t-i)*yArray[i];
			}
			
			sse += (yArray[t] - sum - Math.pow(1-a, t+1)*s0) * (yArray[t] - sum - Math.pow(1-a, t+1)*s0);
			
		}
			
		return sse;
		
	}
	
	public static double GetoptArfa(double[] yArray, double s0){
		
		double optArfa = 0;
		
		double minSSE = 9999;
		
		for(double a=0.001; a<1; a+=0.001){
			
			double sse = GetSSE(yArray, s0, a);
			System.out.println("系数"+a+"对应的SSE:"+sse);
			if(sse<minSSE){
				minSSE = sse;
				optArfa = a;
			}
		}
		
		return optArfa;
		
	}
	
	/**
	 * 三次指数平滑 得到预测p天数据
	 * @param dataArray
	 * @param pDays
	 * @param a
	 * @return
	 */
	public static double[] ExpThrPredict(double[] dataArray, int pDays){
		
		double[] preArray = new double[pDays];
		double st1 = 0;
		double st2 = 0;
		double st3 = 0;
		//double a = 0;
		//double minMSE = 99999;
		
		/**通过遍历选取最优参数a**/
		//for(double at=0.001; at<1; at+=0.001){
		
		double a = GetoptArfa(dataArray, dataArray[0]);
			
			/**获取三次指数平滑预测值**/
			double[] onceSmoothArray = GetOnceSmoothArray(dataArray, a, dataArray[0]);
			
			st1 = onceSmoothArray[onceSmoothArray.length-1];
			
			double[] twiceSmoothArray = GetOnceSmoothArray(onceSmoothArray, a, dataArray[0]);
			
			st2 = twiceSmoothArray[twiceSmoothArray.length-1];
			
			double[] thirdSmoothArray = GetOnceSmoothArray(twiceSmoothArray, a, dataArray[0]);
			
			st3 = thirdSmoothArray[thirdSmoothArray.length-1];
			
			//List<double[]> para = GetPara(onceSmoothArray, twiceSmoothArray, thirdSmoothArray, a);
			
			//double[] A = para.get(0);
			
			//double[] B = para.get(1);
			
			//double[] C = para.get(2);
			
			//double[] yArray = GetY(A, B, C);
			
			//double mse = GetMSE(yArray, dataArray);
			
			/*System.out.println("系数"+at+"对应的MSE值为:"+mse);
			if(mse<minMSE){
				a = at;
				st1 = st1temp;
				st2 = st2temp;
				st3 = st3temp;
				minMSE = mse;
			}else
				break;
			
		}*/
		System.out.println("================================");
		System.out.println("最优的系数为："+a);
		System.out.println("================================");
		/**计算系数A B C**/
		double At = 3*st1 - 3*st2 + st3;
		
		double Bt = ( a / (2*(1-a)*(1-a)) ) * ( (6-5*a)*st1 - 2*(5-4*a)*st2 + (4-3*a)*st3 );
		
		double Ct = ( (a*a) / (2*(1-a)*(1-a)) ) * ( st1 - 2*st2 + st3 );
		
		/**开始计算pDays预测值**/
		for(int i=0; i<pDays; i++){
			
			preArray[i] = At + Bt*(i+1) + 0.5*Ct*(i+1)*(i+1);
			
		}
		
		return preArray;
		
	}
	
	/**
	 * 
	 * @param dataArray
	 * @param pDays
	 * @param ftNum
	 * @param fi
	 * @param a
	 * @return
	 */
	public static int Getfsum(double[] dataArray, int pDays, double ftNum, int fi){
		
		/**数据预处理**/
		double[] detectArray = DataPreprocess.OutlierDetect(dataArray);  //异常检测
		
		//double[] diff7Array = DataPreprocess.SevenDiff(detectArray);  //7阶差分
		
		//double[] diff1Array = DataPreprocess.OneDiff(diff7Array);  //1阶差分
		
		/**开始预测**/
		double[] preArray = ExpThrPredict(detectArray, pDays);
		
		/**反预处理**/
		//double[] rdiff1Array = DataPreprocess.ROneDiff(diff7Array, preArray);
		
		//double[] rdiff7Array = DataPreprocess.RSevenDiff(detectArray, preArray);
						
		int fsum = (int)DataMath.sumData(preArray); 
			
		if(fsum<0)
			fsum=0;
		
		double acc = 0;
		if(ftNum == 0)
			acc = fsum * 100;
		else
			acc = (double)fsum/ftNum;
		System.out.println("flavor"+fi+"的预测精度为："+acc);
		System.out.println("=======================================================");	
		return fsum;
		
	}
	
	public static void main(String[] args) {
		//double[] dataArray = {253993,275396.2,315229.5,356949.6,400158.2,442431.7,495102.9,570164.8,640993.1,704250.4,767455.4,781807.8,776332.3,794161.7,834177.7,931651.5,1028390,1114914};
		
		double[] flavor1 = {3,	6,	2,	2,	0,	3,	4,	7,	2.78181818181818,	3,	4,	0,	0,	4,	1,	5,	3,	2.78181818181818,	3,	0,	6,	2,	4,	7,	7,	2,	1,	2.78181818181818,	2,	3,	1,	0,	2,	1,	1,	0,	6,	2,	1,	0,	2,	1,	4,	3,	2.78181818181818,	2,	3,	1,	3,	3,	2,	5,	2.78181818181818,	1,	2};
		
		double[] preArray = ExpThrPredict(flavor1, 7);
		
		System.out.println("最后预测值："+Arrays.toString(preArray));
		
		
		
		
		
		
		
	}
}
