package com.elasticcloudservice.predict;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ARGradient {
		
	/**
	 * 获取AR模型参数
	 * @param normalArray
	 * @return
	 */
	public static List<double[]> GetARpara(double[] detectArray, double[] diffarray1, double[] diffarray7, double[] normalArray, double ftNum, int pDays, int fi){
		
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
		for(int p=n/2 ; p>2 ; p--){        //通过遍历求出最优的AR阶数,针对于差分数据
			
			//用梯度下降算法估算p阶AR系数
			double[] a = gradientDescent( normalArray, p);
			
			//存放残差
			double[] residualErr = new double[n-p];
			
			double sigma = 0;
			double AIC = 0;
			double sum = 0;
			
			//计算残差和sigma
			for(int t=p ; t<n ; t++){     
				double xt = normalArray[t];
				double xt1 = 0;
				for(int i=0 ; i<p ; i++){
					xt1 += a[i]*normalArray[t-1-i];
				}
				residualErr[t-p] = Math.pow(xt-xt1, 2);
				sum += residualErr[t-p];
			}			
			sigma = sum/(n-p-1);
			
			AIC = Math.log(sigma)+2*p/n;
			
			
			double[] fpreArray = fdayPredit(normalArray, pDays, a, sigma);															
			double accuracy = 0;
			double[] rNormalArray = DataPreprocess.RNormal(diffarray1, fpreArray);									
			double[] r1DiffArray = DataPreprocess.ROneDiff(diffarray7, rNormalArray);			
			double[] r7DiffArray = DataPreprocess.RSevenDiff(detectArray, r1DiffArray);			
			double fsum = DataMath.sumData(DataMath.filterNegative(r7DiffArray));			
			int fpNum = (int)fsum;			
			if(ftNum==0){
				accuracy = fpNum*10;
			}else{
				accuracy = (double)fpNum/ftNum;
			}
		    System.out.println("flavor"+fi+"的"+p+"阶预测精度："+accuracy);
		    
		    //System.out.println("flavor"+fi+"的"+p+"阶AIC："+sigma);
		    
		    //同过遍历p选出最小的残差和，同时确定AR模型阶数
			if(AIC<minAIC){
				
				ARpara.clear();	
				
				ARpara.add(a); //添加AR模型系数
				double[] sArray = {sigma};
				ARpara.add(sArray);  //添加残差和
				ARpara.add(residualErr);  //添加残差序列
				
				minAIC = AIC;
				
			}
			
		}
		//System.out.println("flavor"+fi+"的AR阶数："+ARpara.get(0).length+",产生的残差："+Arrays.toString(ARpara.get(2)));
		return ARpara;
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
		
		double[][] X = DataMath.xMatrix(dataArray, p);
		
		double[][] Y = DataMath.yMatrix(dataArray, p);
		
		double[][] Xtran = Matrix.trans(X);
		
		double[][] M1=Matrix.multiplyBetweenAB(Xtran, X);
						
		double[][] M2=Matrix.multiplyBetweenAB(Xtran, Y);
				
		double[] opta = new double[p];   
			
		double[][] newA = new double[p][1];
		double[][] oldA = new double[p][1];
		double[][] newV = new double[p][1];
		double[][] oldV = new double[p][1];
		
		for(int i=0 ; i<p ; i++){
			oldA[i][0] = Math.random();  //参数初始值产生 -0.1 到0.1之间的随机数
			newA[i][0] = 0;
			newV[i][0] = 0;
			oldV[i][0] = 0;
		}
		int k = 0;
		//double tempLoss = 0;
		double gama = 0.2;    //初始步长设置一个较大的值
		double epsilon = 0.0001;  //初始精度设置一个较小的值
		while(true){
			
			double[][] M3 = Matrix.multiplyBetweenAB(M1, oldA);
			
			for(int i=0; i<p; i++){
				
				newV[i][0] = 0.9*oldV[i][0] + gama*(M3[i][0] - M2[i][0]);
				
			}
			
			for(int i=0 ; i<p ; i++){
				
				newA[i][0] = oldA[i][0] - newV[i][0];  //梯度下降公式
						
			}
			
			double err=0;
			for(int i=0 ; i<p ; i++){
				
				err += Math.pow(newA[i][0]-oldA[i][0], 2);
				
			}
			
			
			/**判断是否到梯度下降算法结束条件**/
			if(Math.sqrt(err)<epsilon){														
				for(int i=0 ; i<p ; i++){
					opta[i] = newA[i][0];
				}
				return opta;				
			}
			
			if((Math.sqrt(err))<epsilon*10){
				gama *= 0.02;
			}
			
			//tempLoss = Math.sqrt(err);
			
			for(int i=0 ; i<p ; i++){
				
				oldA[i][0] = newA[i][0];
				oldV[i][0] = newV[i][0];
				
			}
			k++;
			//System.out.println("第"+k+"次迭代误差为："+Math.sqrt(err)+"期望误差为："+epsilon);
			if(k>100 && k%20==0){//每300步检查一次
				epsilon *= 3;				
			}				
		}			
	}
					
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
							
				System.arraycopy(dataArray, n-p+i, x, 0, p-i);
				
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
															
			preDataArray[i] =  y /*+ e*/ ;
									
		}
	
		return preDataArray;
	}
	
	/**
	 * 最终使得随机干扰项的方差最小的N
	 * @param dayCount 原始数据频数
	 * @param pDays    要预测的天数
	 * @return optN    相关性最大的N天
	 */
	public static int Getfsum(double[] dataArray, int pDays, double ftestNum, int i){
		
		/**数据预处理**/				
		double[] detectArray = DataPreprocess.OutlierDetect(dataArray);
		
		double[] diffarray7 = DataPreprocess.SevenDiff(detectArray); //7阶差分
								
		double[] diffarray1 = DataPreprocess.OneDiff(diffarray7);  //一次差分
				
		double[] normalArray = DataPreprocess.Normal(diffarray1);  //对数据进行标准化
		
		
		/**获取AR系数**/
		List<double[]> ARpara = GetARpara(detectArray, diffarray1, diffarray7, normalArray, ftestNum, pDays, i);
		
		
		/**开始预测**/		
		double[] a = ARpara.get(0);    //AR(optp)模型系数
		double sigma = ARpara.get(1)[0];
		double[] fpreArray = fdayPredit(normalArray, pDays, a, sigma); //获取标准化处理后的数据的预测数据		
		
		
		/**反预处理操作**/
		double[] rNormalArray = DataPreprocess.RNormal(diffarray1, fpreArray);
				
		double[] r1DiffArray = DataPreprocess.ROneDiff(diffarray7, rNormalArray);
		
		double[] r7DiffArray = DataPreprocess.RSevenDiff(detectArray, r1DiffArray);
		
		double sum = DataMath.sumData(DataMath.filterNegative(r7DiffArray));
		
		int fpNum = (int)sum;
		
		System.out.println("flavor"+i+"的最优AR模型阶数:"+a.length+",参数："+Arrays.toString(a)+",扰乱项方差："+sigma);
		
		double accuracy = 0;
		if(ftestNum==0){
			accuracy = fpNum*100;
		}else{
			accuracy = (double)fpNum/ftestNum;
		}
		System.out.println("flavor"+i+"的最终预测精度为："+accuracy);
		System.out.println("==================================================================");
		return fpNum;
		
	}
	
	/*
	public static void main(String[] args) {
		double[] flavor2 = {0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	0,	0,	0,	0,	0,	0,	1,	8,	8,	0,	0,	0,	0,	2,	0,	0,	4,	0,	0,	0,	4,	0,	8,	4,	4,	0,	0,	1,	0,	7,	1,	0,	0,	0};
	
		int sum = Getfsum(flavor2, 14, 1.75, 2);
		
		System.out.println("预测结果："+sum);
	}*/
	

}
