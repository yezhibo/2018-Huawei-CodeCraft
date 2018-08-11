package com.elasticcloudservice.dataTool;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DataPreprocess {
	
	/**
	 * 获取序列种非零元素个数
	 * @param dataArray
	 * @return
	 */
	public static int GetNotZeroNum(double[] dataArray){
		int notZeroNum = 0;
		int n = dataArray.length;
		for(int i=0; i<n; i++){
			if(dataArray[i]>0)
				notZeroNum++;
		}
		return notZeroNum;
	}
		
	/**
	 * 输入原始数据，获取合并后的数据，用于画图调用
	 * @param detectArray
	 * @param subt
	 * @return
	 */
 	public static double[] GetYArray(double[] detectArray, int subt){
		
		int n = detectArray.length;  //原始数据的长度
		
		int yCount = n/subt;   //原始数据能分成totalPDays个分数组
		
		double[] yArray = new double[yCount];
		
		double[] x = new double[yCount];
		
		int t = n - yCount*subt; //表示能合并的原始数组的最小下标，例如原数组有55个，预测天数为7， 则前6个元素舍掉，从第7个元素开始合并，t=6;
		
		for(int i=0; i<yCount; i++){  //开始合并,得出自变量和因变量数组
			
			for(int j=0; j<subt; j++){				
				yArray[i] += detectArray[t++];				
			}
			
			x[i] = i+1;
			
		}
		
		return yArray;
		
	}
	
	/**
	 * 输入原始数据，获取按天累加数据
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
	
	/**
	 * 日期间隔计算函数
	 * @param start 开始时间
	 * @param end  结束时间
	 * @return days  间隔天数
	 * @throws ParseException
	 */
    public static int getDaysBetween(String start,String end, String DateFormat) {  
    	
        int daysBetween = 0;
        
		try {
			
			SimpleDateFormat sdf=new SimpleDateFormat(DateFormat);  			
			Calendar cal = Calendar.getInstance();   
			
			cal.setTime(sdf.parse(start));         
			long time1 = cal.getTimeInMillis();  //将第一个时间转化为毫秒形式
			
			cal.setTime(sdf.parse(end));    
			long time2 = cal.getTimeInMillis();  //将第二个时间转化为毫秒形式
			
			daysBetween = (int)Math.ceil((double)(time2-time1)/(1000*3600*24)); //日期取上整
			
		} catch (Exception e) {
			e.printStackTrace();
		}  
            
       return daysBetween;     
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
	 * 箱型图异常检测
	 * @param dataArray
	 * @return
	 */
	public static double[] BoxDetect(double[] dataArray){
		int n = dataArray.length;
		double[] sortArray = new double[n];
		double[] detectArray = new double[n];
		System.arraycopy(dataArray, 0, sortArray, 0, n);
		//先对数据进行从大到小排序
		for(int i=0; i<n-1; i++){
			for(int j=i+1; j<n; j++){
				if(sortArray[i]<sortArray[j]){
					double temp = sortArray[i];
					sortArray[i] = sortArray[j];
					sortArray[j] = temp;
				}
			}
		}
		//开始计算上下4分位值
		double U = 0;
		double L = 0;		
		double q = (double)n/4;		
		int l = (int)q;		
		int u = (int)Math.ceil(q);		
		if(l==u){
			U = sortArray[l-1];			
			L = sortArray[n-l];
		}else{
			U = sortArray[l-1]*(q-l)+sortArray[u-1]*(u-q);
			L = sortArray[n-l]*(q-l)+sortArray[n-u]*(u-q);
		}
		double IQR = 0;		      //上下界之间的差值
		double upper = 0;         //上界		
		double lower = 0;         //下界		
		double bigReplace = 0;    //超过上界的替代值
		double smallReplace = 0;  //低于下界的替代值
		
		//如果上四分位数据大于0，则拿全部数据用来坐箱型图异常检测，计算上下界
		if(U>0){
			IQR = U - L;		
			upper = U + 3.5*IQR;    //上界		
			lower = L - 1.5*IQR;    //下界		
			lower = (lower>=0) ? lower : 0;	
			for(int i=0; i<n; i++){
				if(sortArray[i]<=upper){
					bigReplace = sortArray[i];
					break;
				}
			}
			for(int i=n-1; i>=0; i--){
				if(sortArray[i]>=lower){
					smallReplace = sortArray[i];
					break;
				}
			}			
			
		}
		
		//如果上四分位数据为0，则截取四分位以上的数据，重新计算上下界
		else{
			
			//求出大于0的元素个数
			int uplength = 0;
			for(int i=0; i<n; i++){
				if(sortArray[i]==0){
					uplength = i;
					break;
				}
			}
			double[] upArray = new double[uplength];
			for(int i=0; i<uplength; i++){
				upArray[i] = sortArray[i];
			}      
						
			int up4 = (int)Math.ceil((double)uplength/3);
			
			if(uplength==0){
				upper=0;
			}else{
				upper = upArray[up4-1];
			}
			
			bigReplace = upper;
			
		}
		
		//将超过上下界的数据作为异常数据，并用相应正常值替代
		for(int i=0; i<n; i++){
			if(dataArray[i]>upper)
				detectArray[i] = bigReplace;
			else if(dataArray[i]<lower)
				detectArray[i] = smallReplace;
			else
				detectArray[i] = dataArray[i];
		}
		
		return detectArray;
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
			
			double upper = U + 3*IQR;    //上界
			
			double lower = L - 3*IQR;    //下界
			
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
			double upper = IQ3 + 5*IQR;
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
