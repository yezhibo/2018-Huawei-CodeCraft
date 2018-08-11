package com.elasticcloudservice.predict;


/**
 * 指数平滑类
 * @ProjectName ecsExpSmooth
 * @author Yezhibo
 * @CreatTime 2018年4月5日下午8:47:12
 */
public class ExpSmooth {
	
	/**
	 * 平滑计算
	 * @param dataArray
	 * @param a
	 * @return
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

	public static double[] ExpPredict(double[] dataArray, int pDays, double a){
		
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
	
	public static int Getfsum(double[] dataArray, int pDays, double ftNum, int fi, double a){
		
		/**数据预处理**/
		double[] detectArray = DataPreprocess.OutlierDetect(dataArray);  //异常检测
		
		//double[] diff7Array = DataPreprocess.SevenDiff(detectArray);  //7阶差分
		
		//double[] diff1Array = DataPreprocess.OneDiff(diff7Array);  //1阶差分
		
		/**开始预测**/
		double[] preArray = ExpPredict(detectArray, pDays, a);
		
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
			
		return fsum;
		
	}
}
