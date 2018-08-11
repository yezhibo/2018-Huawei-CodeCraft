package com.elasticcloudservice.predict;

import java.util.List;

import com.filetool.util.FileUtil;

public class GetTest {

	public static double[] GetTestArray(String[] fNameArray){
		double[] ftestTotalArray = new double[fNameArray.length];
		String[] testContent = FileUtil.read("C:\\Users\\Yezhibo\\Desktop\\测试用例\\测试用例9\\test.txt", null);
		List<String> fhistory = InputPreprocess.getHistoryNameAndTime(testContent);
		for(int i=0 ; i<fNameArray.length ; i++){	
			//System.out.println(fNameArray[i]+"真实数据");
			double[] dataArray = InputPreprocess.getFlavorArray(fNameArray[i], fhistory); //获取原始数据时间序列
			//dataArray = DataPreprocess.OutlierDetect(dataArray);
			for(int j=0; j<dataArray.length; j++){
				ftestTotalArray[i] += dataArray[j];
				//System.out.println(dataArray[j]);
			}
		}
		return ftestTotalArray;
	}
	
	public static double GetPredictAccuracy(int[] fArray1, double[] fArray){
		
		double accuracy = 0;
		int n = fArray1.length;
		double sumf1fE = 0;
		double sumf = 0;
		double sumf1 = 0;
		for(int i=0; i<n; i++){
			
			sumf1fE += (fArray[i]-fArray1[i])*(fArray[i]-fArray1[i]);
			sumf += fArray[i]*fArray[i];
			sumf1 += fArray1[i]*fArray1[i];
			
		} 
		
		sumf1fE = Math.sqrt(sumf1fE/n);
		sumf = Math.sqrt(sumf/n);
		sumf1 = Math.sqrt(sumf1/n);
		
		accuracy = 1 - sumf1fE/(sumf+sumf1);
		
		return accuracy;
		
	}
	
}
