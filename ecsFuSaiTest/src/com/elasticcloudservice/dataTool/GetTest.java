package com.elasticcloudservice.dataTool;

import java.util.List;

import com.elasticcloudservice.dataEntity.EcsContent;
import com.filetool.util.FileUtil;

public class GetTest {

	/**
	 * 获取所有虚拟机的测试数据内的总量
	 * @param fNameArray
	 * @return
	 */
	public static List<double[]> GetTestSeriesList(String[] fNameArray){
		
		String[] testContent = FileUtil.read("C:\\Users\\Yezhibo\\Desktop\\测试用例\\示例1\\test.txt", null);
		
		EcsContent test = new EcsContent(testContent,fNameArray);
		
		return test.flavorSeriesList;
	}	
	
	/**
	 * 精度计算函数
	 * @param fArray1
	 * @param fArray
	 * @return
	 */
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
