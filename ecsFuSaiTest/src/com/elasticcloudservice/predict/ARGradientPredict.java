package com.elasticcloudservice.predict;

import com.elasticcloudservice.dataTool.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 77.514分
 * @ProjectName ecsTest
 * @author Yezhibo
 * @CreatTime 2018年4月8日下午11:02:16
 */
public class ARGradientPredict {
	
	/**
	 * 获取AR模型参数
	 * @param normalArray
	 * @return
	 */
	public static List<double[]> GetARpara(double[] normalArray,int pDays){
		
		List<double[]> ARpara = new ArrayList<double[]>();
		
		/**对标准化处理后的数据进行回归分析，线性预测**/		
		double minAIC = 9999;
		//double minSigma = 9999;
		int n = normalArray.length;
		
		/**
		 * p最大值需要满足的条件：
		 * 1. 使得系数矩阵X至少为p行，线性方程组有解。即p=n/2;
		 * 2. 最后至少要留出1个数据来评价模型参数。
		 */	
		/**
		 * 思路：残差最小的即为最优的阶数 / AIC最小
		 */
		for(int p=n/3 ; p>n/7 ; p--){        //通过遍历求出最优的AR阶数,针对于差分数据
			//System.out.println("========================"+p+"阶=========================");
			//用梯度下降算法估算p阶AR系数
			double[] a = gradientDescent( normalArray, p);
			
			//用遗传算法估算p阶AR系数
			//double[] a = GAPredict.GenAlg(normalArray, 10, p, 2500, 1.5);
			
			//存放残差
			//double[] residualErr = new double[n-p];
			
			double sigma = 0;
			double AIC = 0;
			//double sum = 0;
			
			//计算残差和sigma
			double sse = GetSSE(normalArray, a);
			
			sigma = sse/(n-p-1);
			
			AIC = Math.log(sigma)+2*p/n;															
					    
		    //System.out.println("flavor"+fi+"的"+p+"阶AIC："+sigma);
		    
		    //同过遍历p选出最小的残差和，同时确定AR模型阶数
			if(AIC<minAIC){
				
				ARpara.clear();	
				
				ARpara.add(a); //添加AR模型系数
				double[] sArray = {sigma};
				ARpara.add(sArray);  //添加残差和
				//ARpara.add(residualErr);  //添加残差序列
				
				minAIC = AIC;
				
			}
			//System.out.println("=================================================");
			
		}
		//System.out.println("flavor"+fi+"的AR阶数："+ARpara.get(0).length+",产生的残差："+Arrays.toString(ARpara.get(2)));
		return ARpara;
	}
	
	/**
	 * 获取梯度下降算法中目标函数值
	 * @param normalArray
	 * @param a
	 * @return
	 */
	public static double GetSSE(double[] normalArray, double[] a){
		
		int p = a.length;
		
		int n = normalArray.length;
		
		double sse = 0;
		
		for(int t=p ; t<n ; t++){
			
			double xt = normalArray[t];
			double xt1 = 0;
			for(int i=0 ; i<p ; i++){
				xt1 += a[i]*normalArray[t-1-i];
			}
			
			sse += Math.pow(xt-xt1, 2);
		}
		
		return sse;
		
	}
	
	/**
	 * 梯度下降算法,求解线性方程组  X*A=Y
	 * @param dataArray  平稳时间序列
	 * @param p   AR阶数
	 * @param gama  下降速度
	 * @param epsilon 终止阈值
	 * @return a 最优参数
	 */
	public static double[] gradientDescent(double[] dataArray, int p){
		
		double[][] X = DataMath.xMatrix(dataArray, p);  //得到AR模型系数矩阵X
		
		double[][] Y = DataMath.yMatrix(dataArray, p);  //得到系数矩阵Y
		
		double[][] Xtran = Matrix.trans(X);   //得到矩阵X的转置
		
		double[][] M1=Matrix.multiplyBetweenAB(Xtran, X);  //计算XT*X
						
		double[][] M2=Matrix.multiplyBetweenAB(Xtran, Y); //计算XT*Y
		
		double[] opta = new double[p];  
			
		double[][] newA = new double[p][1];
		double[][] oldA = new double[p][1];
		double[][] newV = new double[p][1];
		double[][] oldV = new double[p][1];
		
		for(int i=0 ; i<p ; i++){       //对AR模型参数A进行初始化，初始化为-0.05~0.05
			oldA[i][0] = 0/*(Math.random()-0.5)*0.1*/;
			newA[i][0] = 0;
			newV[i][0] = 0;
			oldV[i][0] = 0;
		}
		
		int iter = 1; //迭代的次数	
		int maxIter = 2000;
		double baselr = 0.1;  //初始步长设置一个较大的值
		double lr = baselr;    
		double baseSse = 0.0001;
		double minSSE = baseSse;
		
		while(iter<maxIter){      //开始利用梯度下降算法估计残差最小的AR模型参数
			
			double[][] M3 = Matrix.multiplyBetweenAB(M1, oldA);
			
			for(int i=0; i<p; i++)				
				newV[i][0] = 0.9*oldV[i][0] + lr*(M3[i][0] - M2[i][0]);
										
			for(int i=0 ; i<p ; i++)				
				newA[i][0] = oldA[i][0] - newV[i][0];    //梯度下降公式									
			
			double distance = 0;
			for(int i=0 ; i<p ; i++)      //计算新的模型参数与旧的模型参数之间的距离				
				distance += Math.pow(newA[i][0]-oldA[i][0], 2);				
			
			System.out.println("第"+iter+"次迭代误差值："+Math.sqrt(distance));
			
			/**如果距离小于给定值, 结束梯度下降算法**/
			if(Math.sqrt(distance)<minSSE)			
				break;
			
			
			for(int i=0 ; i<p ; i++){				
				oldA[i][0] = newA[i][0];
				oldV[i][0] = newV[i][0];				
			}
			
			lr = baselr * Math.pow(0.996, iter); 
			//minSSE = baseSse * Math.pow(1.001, iter);
			iter++;
		}
		
		for(int i=0 ; i<p ; i++){
			opta[i] = newA[i][0];
		}
		
		return opta;
		
	}
	
	/**
	 * 梯度下降算法,求解线性方程组  X*A=Y
	 * @param dataArray  平稳时间序列
	 * @param p   AR阶数
	 * @param gama  下降速度
	 * @param epsilon 终止阈值
	 * @return a 最优参数
	 */
	/*public static double[] gradientDescent(double[] normalArray, int p){
		
		double[][] X = DataMath.xMatrix(normalArray, p);  //获取方程系数x [n-p][p]
		
		double[][] Y = DataMath.yMatrix(normalArray, p);  //获取方程系数Y [n-p][1]
		
		double[][] Xtran = Matrix.trans(X);   //对矩阵X进行转置
		
		double[][] M1=Matrix.multiplyBetweenAB(Xtran, X);  //得到XTX
						
		double[][] M2=Matrix.multiplyBetweenAB(Xtran, Y);  //得到XTY
				
		double[] opta = new double[p];   //存放最优系数		
		int n = normalArray.length;
		double[][] mt = new double[p][1];
		double[][] emt = new double[p][1];
		double[][] vt = new double[p][1];
		double[][] evt = new double[p][1];
		double[][] Ldiff = new double[p][1];
		double[][] optA = new double[p][1];
		
		for(int i=0 ; i<p ; i++){
			optA[i][0] = 0Math.random();  //参数初始值产生 -0.1 到0.1之间的随机数
		}
				
		int iter = 1; //迭代的次数	
		int maxIter = 3000;
		double baselr = 0.1;  //初始步长设置一个较大的值
		double lr = baselr;    
		double baseSse = 0.05;
		double minSSE = baseSse;
		
		while(iter<maxIter){  //开始梯度下降迭代
			
			double[][] M3 = Matrix.multiplyBetweenAB(M1, optA);
			
			-------------------------------------Adam-------------------------------
			double B1 = 0.9, B2 = 0.999, c = Math.pow(10,-8);
			
            for(int i=0; i<p; i++){     //防止梯度下降进入局部最优解				
				Ldiff[i][0] = M3[i][0] - M2[i][0];				
			}
			
			for (int i = 0; i < p; i++) {								
				mt[i][0] = B1 * mt[i][0] + (1 - B1) * Ldiff[i][0];
				emt[i][0] = mt[i][0]/(1 - Math.pow(B1, iter));
			}
			
			for (int i = 0; i < p; i++) {								
				vt[i][0] = B2 * vt[i][0] + (1 - B2) * Math.pow(Ldiff[i][0],2);
				evt[i][0] = vt[i][0]/(1 - Math.pow(B2, iter));				
			}
			
			for (int i = 0; i < p; i++) {								
				optA[i][0] -= lr * emt[i][0] / (Math.sqrt(evt[i][0]) + c);				
			}			
			
			*//**计算目标函数值**//*
			double[] a = new double[p];
			for(int i=0; i<p; i++){
				a[i] = optA[i][0];
			}
			
			double sse = GetSSE(normalArray, a)/(n-p);
			*//**判断是否到梯度下降算法结束条件**//*
			if( sse < minSSE ){									
				break;
			}			
			lr = baselr * Math.pow(0.996, iter); 
			minSSE = baseSse * Math.pow(1.001, iter);
			
			System.out.println("第"+iter+"次迭代SSE为："+sse+"期望误差为："+ minSSE);
			
			iter++;																	
		}	
		for(int i=0 ; i<p ; i++)
			opta[i] = optA[i][0];
		return opta;
	}*/
					
	/**
	 * 时间序列预测函数
	 * @param dataArray 原始数据时间序列
	 * @param pDays     预测的天数
	 * @param a         AR模型系数	
	 * @param sigma     随机干扰项方差
	 * @return preDataArray  预测时间段内的数据
	 */
	public static double[] fdayPredit(double[] dataArray, int pDays, double[] a,  double sigma){
		
		double[] preDataArray = new double[pDays];
		
		int n = dataArray.length;  //标准化数据总个数
		
		int p = a.length;      //AR模型阶数
		
		for (int i = 0 ; i<pDays ; i++){                           //预测接下来pDays天的数量并保存在数组中
			
			double[] x = new double[p];   //存放预测值前n天的数据			
			if(i<p){
							
				System.arraycopy(dataArray, n-p+i, x, 0, p-i);  //将原数组dataArray 从下标n-p+i 个元素开始 的 p-i个值复制到 数组x中，从下标0开始存放
				
				int m=0;
				
				for(int j=p-i;j<p;j++){
					x[j]=preDataArray[m];
					m++;
				}
				
			}else{
				
				for (int k=0 ; k<p ; k++){
					x[k]=preDataArray[i-p+k];
				}
			}
			
			double y = 0;                      //开始计算预测值
			
			for ( int t=0 ; t<p ; t++ ){
				y += a[t]*x[t];
			}
			
			//double e = DataMath.gaussData(0, sigma);
															
			preDataArray[i] =  y/* + e*/ ;
									
		}
	
		return preDataArray;
	}
	
	/**
	 * 预测入口函数
	 * @param dayCount 原始数据频数
	 * @param pDays    要预测的天数
	 * @return optN    相关性最大的N天
	 */
	public static double[] GetPredictFArray(double[] detectArray, int pDays, int diff){
		
		/**数据预处理**/						
		double[] diffarray = DataPreprocess.AnyDiff(detectArray,diff); //7阶差分						
		double[] normalArray = DataPreprocess.Normal(diffarray);  //对数据进行标准化
				
		/**获取AR系数**/
		List<double[]> ARpara = GetARpara(normalArray, pDays);
				
		/**开始预测**/		
		double[] a = ARpara.get(0);    //AR(optp)模型系数
		double sigma = ARpara.get(1)[0];
		double[] fpreArray = fdayPredit(normalArray, pDays, a, sigma); //获取标准化处理后的数据的预测数据		
		
		
		/**反预处理操作**/
		double[] rNormalArray = DataPreprocess.RNormal(diffarray, fpreArray);		
		double[] rDiffArray = DataPreprocess.RAnyDiff(detectArray, rNormalArray, diff);
		
		System.out.println("最优AR模型阶数:"+a.length+",参数："+Arrays.toString(a)+",扰乱项方差："+sigma);
				
		return rDiffArray;
		
	}
	
	/*
	public static void main(String[] args) {
		double[] flavor2 = {0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	0,	0,	0,	0,	0,	0,	1,	8,	8,	0,	0,	0,	0,	2,	0,	0,	4,	0,	0,	0,	4,	0,	8,	4,	4,	0,	0,	1,	0,	7,	1,	0,	0,	0};
	
		int sum = Getfsum(flavor2, 14, 1.75, 2);
		
		System.out.println("预测结果："+sum);
	}*/
	

}
