package com.elasticcloudservice.datapreprocess;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据预处理类
 * @ProjectName AR80
 * @author Yezhibo
 * @CreatTime 2018年4月15日上午10:05:38
 */
public class DataPreprocess {
	
	/**
	 * 反7阶差分操作
	 * @param detectArray
	 * @param diffarray
	 * @param fpreArray
	 * @return
	 */
	 public static double[] RSevenDiff(double[] detectArray, double[] fpreArray){
	
		int pDays = fpreArray.length;
		
		double[] rdiffpreArray = new double[pDays];
		
		for(int j=0 ; j<pDays ; j++){    //对去中心化后的数据进行反差分
			
			if(j<7){
				rdiffpreArray[j] = fpreArray[j] + detectArray[detectArray.length-7+j];
			}else{
				rdiffpreArray[j] = fpreArray[j] + rdiffpreArray[j-7];
			}			
		}
		
		return rdiffpreArray;
	} 
	
	/**
	 * 反标准化操作
	 * @param diffarray
	 * @param fpreArray
	 * @return
	 */
	public static double[] RNormal(double[] diffarray,double[] fpreArray){
		
		/**对预测的数据进行反标准化操作**/		
		int pDays = fpreArray.length;
		
		double[] newArray = new double[pDays];
						
		double avg = DataMath.avgData(diffarray);  //获取原始数据均值
		
		double max = DataMath.maxData(diffarray);  //原始数据最大值
		
		double min = DataMath.minData(diffarray);  //原始数据最小值
		
		if(max==min){    //判断最大值是否等于最小值
			for(int i=0 ; i<pDays ; i++){
				newArray[i] = max;								
			}			
		}else{
			for(int i=0 ; i<pDays ; i++){
				newArray[i] = fpreArray[i]*(max-min) +avg;								
			}
		}
		
		
		return newArray;
		
	}
	
	/**
	 * z-score反标准化函数
	 * @param orgArray
	 * @param preArray
	 * @return
	 */
	public static double[] RZScoreNormal(double[] orgArray, double[] preArray){
		double avg = DataMath.avgData(orgArray);
		double std = DataMath.stderrData(orgArray);
		int n = preArray.length;
		double[] rPreArray = new double[n];
		for(int i=0; i<n; i++){
			rPreArray[i] = preArray[i]*std + avg;
		}
		return rPreArray;
	}
	
	/**
	 * 反一阶差分操作
	 * @param detectArray
	 * @param diffarray
	 * @param fpreArray
	 * @return
	 */
	public static double[] ROneDiff(double[] detectArray, double[] fpreArray){
		
		/**对预测的数据进行反差分操作**/
		int pDays = fpreArray.length;
		
		double[] rdiffpreArray = new double[pDays];
		
		rdiffpreArray[0] = fpreArray[0] + detectArray[detectArray.length-1];
		
		for(int j=1 ; j<pDays ; j++){    //对去中心化后的数据进行反差分
			
			rdiffpreArray[j] = fpreArray[j] + rdiffpreArray[j-1];	
			
		}
								
		return rdiffpreArray;
	} 
	
	/**
	 * 异常处理算法，异常值用均值替代
	 * @param dataArray
	 * @return
	 */
	public static double[] OutlierDetect(double[] dataArray){
		
		double[] normalArray = new double[dataArray.length];
		
		for(int i=0; i<dataArray.length; i++){
			normalArray[i] = dataArray[i];			
		}
		
		//定义一个数据集合，存放非零元素和非零元素的下标
		List<Double> nonzeroArray = new ArrayList<Double>();
		List<Integer> nonzeroIndex = new ArrayList<Integer>();
		
		for(int i=0; i<dataArray.length; i++){
			if(normalArray[i]>0){
				nonzeroArray.add(normalArray[i]);
				nonzeroIndex.add(i);
			}
		}
		
		//对非零元素从大到小进行排序，同时下标也随之排序
		double datatemp = 0;
		int indextemp = 0;
		
		for(int i=0; i<nonzeroArray.size()-1; i++){      //选择排序
			for(int j=i+1; j<nonzeroArray.size(); j++){
				if(nonzeroArray.get(i)<nonzeroArray.get(j)){
					
					datatemp = nonzeroArray.get(i);
					nonzeroArray.set(i, nonzeroArray.get(j));
					nonzeroArray.set(j, datatemp);
					
					indextemp = nonzeroIndex.get(i);
					nonzeroIndex.set(i, nonzeroIndex.get(j));
					nonzeroIndex.set(j, indextemp);
				}
			}
		}
		
		
		//对排序后的数据进行箱型异常检测，最后得到异常值的下标
		List<Integer> outlierIndex = new ArrayList<Integer>();
		double nonzeroAvg = 0;  //非零集合的均值
		
		if(nonzeroArray.size()<=4){                        //如果非零元素的个数小于4，无法适用箱型图，大于2的值即判为异常值
			for(int i=0; i<nonzeroArray.size(); i++){
				if(nonzeroArray.get(i)>3){
					outlierIndex.add(nonzeroIndex.get(i));
					nonzeroArray.set(i, 0.0);
				}
				nonzeroAvg += nonzeroArray.get(i);
			}
			nonzeroAvg = nonzeroAvg/nonzeroArray.size();
			 
		}else{                                            //非零元素个数大于4，采用箱型图判断异常值
			int n = nonzeroArray.size();
			
			double U = 0;
			double L = 0;
			
			double q = (double)n/4;
			
			int l = (int)q;
			
			int u = (int)Math.ceil(q);
			
			if(l==u){
				U = nonzeroArray.get(l-1);			
				L = nonzeroArray.get(n-l);
			}else{
				U = nonzeroArray.get(l-1)*(q-l)+nonzeroArray.get(u-1)*(u-q);
				L = nonzeroArray.get(n-l)*(q-l)+nonzeroArray.get(n-u)*(u-q);
			}
			
			double IQR = U - L;
			
			double upper = U + 1.5*IQR;    //上界
			
			double lower = L - 1.5*IQR;    //下界
			
			lower = (lower>=0) ? lower : 0;
			
			
			for(int i=0; i<nonzeroArray.size(); i++){
				if(nonzeroArray.get(i)>upper){
					outlierIndex.add(nonzeroIndex.get(i));
					nonzeroArray.set(i, 0.0);
				}
				nonzeroAvg += nonzeroArray.get(i);
			}
			nonzeroAvg = nonzeroAvg/nonzeroArray.size();
		}
		
		//对异常值进行处理
		for(int i=0; i<outlierIndex.size(); i++){
			
			normalArray[outlierIndex.get(i)] = nonzeroAvg; //用平均值代替异常值
			
		}
						
		return normalArray;
	}
	
	/**
	 * 异常处理算法，异常值用最大值替代
	 * @param dataArray
	 * @return
	 */
	public static double[] outlierDetect(double[] dataArray){
		
		double[] normalArray = new double[dataArray.length];
		
		for(int i=0; i<dataArray.length; i++){
			normalArray[i] = dataArray[i];			
		}
		
		//定义一个数据集合，存放非零元素和非零元素的下标
		List<Double> nonzeroArray = new ArrayList<Double>();
		List<Integer> nonzeroIndex = new ArrayList<Integer>();
		
		for(int i=0; i<dataArray.length; i++){
			if(normalArray[i]>0){
				nonzeroArray.add(normalArray[i]);
				nonzeroIndex.add(i);
			}
		}
		
		//对非零元素从大到小进行排序，同时下标也随之排序
		double datatemp = 0;
		int indextemp = 0;
		
		for(int i=0; i<nonzeroArray.size()-1; i++){      //选择排序
			for(int j=i+1; j<nonzeroArray.size(); j++){
				if(nonzeroArray.get(i)<nonzeroArray.get(j)){
					
					datatemp = nonzeroArray.get(i);
					nonzeroArray.set(i, nonzeroArray.get(j));
					nonzeroArray.set(j, datatemp);
					
					indextemp = nonzeroIndex.get(i);
					nonzeroIndex.set(i, nonzeroIndex.get(j));
					nonzeroIndex.set(j, indextemp);
				}
			}
		}
		
		
		//对排序后的数据进行箱型异常检测，最后得到异常值的下标
		List<Integer> outlierIndex = new ArrayList<Integer>();
		double normalMax = 0;
		
		if(nonzeroArray.size()<=4){                        //如果非零元素的个数小于4，无法适用箱型图，大于2的值即判为异常值
			for(int i=0; i<nonzeroArray.size(); i++){
				if(nonzeroArray.get(i)>2){
					outlierIndex.add(nonzeroIndex.get(i));
				}else{
					normalMax = nonzeroArray.get(i);
					break;					
				}
			}
		}else{                                            //非零元素个数大于4，采用箱型图判断异常值
			int n = nonzeroArray.size();
			int IQ3 = (int)((n+1)/4);//上四分位坐标      
			int IQ1 = n-IQ3-1;//下四分位坐标
			double IQR = nonzeroArray.get(IQ3)-nonzeroArray.get(IQ1);
			double upper = IQ3 + 1.5*IQR;
			for(int i=0; i<nonzeroArray.size(); i++){
				if(nonzeroArray.get(i)>upper){
					outlierIndex.add(nonzeroIndex.get(i));
				}else{
					normalMax = nonzeroArray.get(i);
					break;   //非零元素是从大到小排好序的，如果当前元素非异常，则后边元素一定也是非异常            
				}
			}
		}
		
		//对异常值进行处理
		for(int i=0; i<outlierIndex.size(); i++){
			
			normalArray[outlierIndex.get(i)] = normalMax;
			
		}
						
		return normalArray;
	}
	
	/**
	 * 对x进行中心化
	 * @param x
	 * @return
	 */
	public static double[] Normal(double[] dataArray){
		
		double[] normalData = new double[dataArray.length];
		
		double avg = DataMath.avgData(dataArray);
		double max = DataMath.maxData(dataArray);
		double min = DataMath.minData(dataArray);
		
		if(max == min){
			for(int i=0 ; i<dataArray.length ; i++){
				normalData[i] = dataArray[i];
			}
		}else{
			for(int i=0 ; i<dataArray.length ; i++){
				normalData[i] = (dataArray[i]-avg)/(max-min);
			}
		}
				
		return normalData;
	}
	
	/**
	 * z-score 标准化
	 * @param dataArray
	 * @return
	 */
	public static double[] ZScoreNormal(double[] dataArray){
		int n = dataArray.length;
		double[] normalArray = new double[n];
		double avg = DataMath.avgData(dataArray);
		double std = DataMath.stderrData(dataArray);
		for(int i=0; i<n; i++){
			normalArray[i] = (dataArray[i]-avg)/std;
		}
		return normalArray;
	}
	
	/**
	 * 对数据作一阶差分
	 * @param dataArray
	 * @return
	 */
 	public static double[] OneDiff(double[] dataArray){
		double[] diffArray = new double[dataArray.length-1];
		for(int i=0 ; i<dataArray.length-1 ; i++){
			diffArray[i] = dataArray[i+1] - dataArray[i];
		}
		return diffArray;
	}
	
	/**
	 * 原始数据标准化处理：一阶季节性差分
	 * @return 差分过后的数据
	 */ 
	public static double[] SevenDiff(double[] originalData)
	{
		
		double[] tempData=new double[originalData.length-7];
		for(int i=0;i<originalData.length-7;i++)
		{
			tempData[i]=originalData[i+7]-originalData[i];
		}

		return tempData;
	}
	
	/**
	 * 原始数据标准化处理：一阶季节性差分
	 * @return 差分过后的数据
	 */ 
	public static double[] AnyDiff(double[] originalData, int d)
	{
		
		double[] tempData=new double[originalData.length-d];
		for(int i=0;i<originalData.length-d;i++)
		{
			tempData[i]=originalData[i+d]-originalData[i];
		}

		return tempData;
	}
	
	/**
	 * 反任意阶阶差分操作
	 * @param detectArray
	 * @param diffarray
	 * @param fpreArray
	 * @return
	 */
	 public static double[] RAnyDiff(double[] detectArray, double[] fpreArray, int d){
	
		int pDays = fpreArray.length;
		
		double[] rdiffpreArray = new double[pDays];
		
		for(int j=0 ; j<pDays ; j++){    //对去中心化后的数据进行反差分
			
			if(j<d){
				rdiffpreArray[j] = fpreArray[j] + detectArray[detectArray.length-d+j];
			}else{
				rdiffpreArray[j] = fpreArray[j] + rdiffpreArray[j-d];
			}			
		}
		
		return rdiffpreArray;
	}
	
}
