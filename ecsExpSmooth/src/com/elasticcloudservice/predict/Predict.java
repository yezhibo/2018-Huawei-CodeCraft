package com.elasticcloudservice.predict;

import java.util.List;

public class Predict {
	
	/**
	 * 预测给定日期的虚拟机
	 * @param ecsContent 历史云服务申请记录 训练数据
	 * @param inputContent 服务器参数、要预测的虚拟机规格、资源利用率指标、预测日期
	 * @return results 虚拟机规格数量以及每个种类具体数量
	 * @throws ParseException 
	 */
	public static String[] predictVm(String[] ecsContent, String[] inputContent/*, String[] testContent*/){
                                                                    /////////////上传时需要删除最后一个参数
		/**=====处理input数据=====**/
		
		String strService = inputContent[0];                    //物理服务器配置	
		int nCPU = Integer.parseInt(strService.split(" ")[0]);  //服务器CPU个数
		int nMEM = Integer.parseInt(strService.split(" ")[1]);  //服务器内存大小G
		int  fNum = Integer.parseInt(inputContent[2]);          //需要预测虚拟机的种类个数		
		//String strDim = inputContent[4 + fNum];                 //资源评价指标		
		String start = inputContent[6 + fNum].split(" ")[0];    //预测天数
		String end = inputContent[7 + fNum].split(" ")[0];
		int pDays = InputPreprocess.getIntervalDay(start,end);
		String[] fNameArray = new String[fNum];                   //预测虚拟机的名称
		for (int i = 0; i < fNum;i++){
			fNameArray[i] = inputContent[i + 3].split(" ")[0];
		}		
		
		
		/**=====处理ecs数据===**/
		
		List<String> fhistory = InputPreprocess.getHistoryNameAndTime(ecsContent);  //将输入训练集中的flavor和具体到天的数据分离出来，并存在history栈中		
				

		/**=====预测阶段===**/
		
		int[] fdataArray = new int[fNum]; //表示每种虚拟机最后预测的总数
		
		double[] fTestTotalArray = GetTest.GetTestArray(fNameArray);////////////////////////////////////////////////////////
		
		for(int i=0 ; i<fNum ; i++){
			
			double[] dataArray = InputPreprocess.getFlavorArray(fNameArray[i], fhistory); //获取原始数据时间序列
			
			fdataArray[i] = ExpSmooth.Getfsum(dataArray, pDays, fTestTotalArray[i], i);	
			
		}
	
		System.out.println("预测的虚拟机总精度："+GetTest.GetPredictAccuracy(fdataArray, fTestTotalArray));
		int sumFlavor=0;               //将处理后的数据按题目要求封装到字符串数组中
		for(int i=0 ; i<fNum ; i++){
			sumFlavor += fdataArray[i];
		}
		String[] resultF = new String[fNum+2];
		resultF[0]=Integer.toString(sumFlavor);
		for(int i=0 ; i<fNum ; i++){
			
			resultF[i+1] = fNameArray[i]+" "+Integer.toString(fdataArray[i]);
			
		}
		resultF[fNum+1]="";
		
		
		/**=====放置阶段=====**/
		
		String[] resultS = GADistribute.distribute(fNameArray, fdataArray, nCPU, nMEM);
		
		
		/**=====合并数据=====**/
		
		String[] result = new String[resultF.length + resultS.length];
		
		for(int i=0 ; i<resultF.length ; i++){
			
			result[i] = resultF[i];
			
		}
		
		for(int i=0 ; i<resultS.length ; i++){
			
			result[resultF.length+i] = resultS[i];
			
		}
		

		return result;
		
	}
	
}
