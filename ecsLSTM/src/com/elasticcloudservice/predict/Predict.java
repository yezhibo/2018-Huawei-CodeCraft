package com.elasticcloudservice.predict;

import com.elasticcloudservice.dataEntity.EcsContent;
import com.elasticcloudservice.dataEntity.Flavor;
import com.elasticcloudservice.dataEntity.InputContent;
import com.elasticcloudservice.dataEntity.Server;
import com.elasticcloudservice.dataTool.*;
import com.elasticcloudservice.distribute.*;

//import java.util.Arrays;
import java.util.List;


public class Predict {
	
	/**
	 * 预测给定日期的虚拟机
	 * @param ecsContent 历史云服务申请记录 训练数据
	 * @param inputContent 服务器参数、要预测的虚拟机规格、资源利用率指标、预测日期
	 * @return results 虚拟机规格数量以及每个种类具体数量
	 * @throws ParseException 
	 */
	public static String[] predictVm(String[] ecsContent, String[] inputContent){
                                                                    
		/**=====处理input数据=====**/		
		InputContent input = new InputContent(inputContent);
		int fNum = input.flavorCont;
		int preDays = input.preDays;
		String[] fNameArray = input.fNameArray;
		
		/**=====处理ecs数据===**/		
		EcsContent ecs = new EcsContent(ecsContent, fNameArray);		
		List<double[]> flavorSeriesList = ecs.flavorSeriesList;
		
		/*计算预测开始时间与训练结束时间的时间间隔*/
		String trainEndDate = ecs.content[ecs.content.length-1].split(" ")[1];
		String preStartDate = inputContent[inputContent.length-2].split(" ")[0];
		int IntervalDays = DataPreprocess.getDaysBetween(trainEndDate, preStartDate, "yyyy-MM-dd")-1;
		 
		/**=====预测阶段===**/
		/**************************提交注释******************************/
		/*List<double[]> testflavorList = GetTest.GetTestSeriesList(fNameArray);//获取测试数据每个规格虚拟机的总量 用于画图
		double[] tfArray = new double[fNum];
		for(int i=0; i<fNum; i++){
			tfArray[i] = DataMath.sumData(DataPreprocess.OutlierDetect(testflavorList.get(i)));
		}*/
		/***************************提交注释*****************************/
		int[] fdataArray = new int[fNum]; //表示每种虚拟机最后预测的总数
		
		for(int i=0 ; i<fNum ; i++){      //预测是按照fNameArray的顺序来预测每个虚拟机的预测结果
			
			double[] orginalArray = flavorSeriesList.get(i);
			double[] detectArray = DataPreprocess.outlierDetect(orginalArray);  //首先对原始数据进行降噪
			int fpNum = 0;                                                  //最终预测结果
			
			int week = 3;
			int hDim = 1;                 //神经网络输出维数
			int xDim = 2;                 //神经网络输入维数
			int maxEpoch = 1000;           //最大迭代次数
			double lr = 0.01;             //初始步长
			double minLoss = 0.5;          //最小损失值
			
			int pWeek = (int)Math.ceil((preDays+IntervalDays)/week);
			
			double[] predictArray = LstmPredict.GetPredictArray(detectArray,
										pWeek, hDim, xDim, maxEpoch, lr, minLoss,week);	//AR模型获取预测数据		
							
			double sum = DataMath.sumData(DataMath.filterNegative(predictArray));  //将预测数据经过去负值后计算总和
			
			fpNum = (int)(sum);
			
			
			if(fpNum<0)
				fpNum = 0;
			
			fdataArray[i] = fpNum;   // 将计算的结果加入最后预测结果中
			/**************************提交注释******************************/			
			/*double accuracy = 0;     //计算精度			
			if(tfArray[i]==0){
				accuracy = fpNum*100;
			}else{
				accuracy = (double)fpNum/tfArray[i];
			}
			System.out.println(fNameArray[i]+"的实际值："+tfArray[i]+",预测值："+fpNum+",精度："+accuracy);*/ //输出flavori最终预测精度			
			/********************************************************/
					
		}
		
		/****************************提交注释****************************/		
		/*double acc = GetTest.GetPredictAccuracy(fdataArray, tfArray);
		System.out.println("预测总量"+DataMath.sumIntData(fdataArray)+"实际总量"+DataMath.sumData(tfArray));
		System.out.println("预测的虚拟机总精度："+acc);*/ //输出打分公式计算的总预测精度		
		/********************************************************/
		
		/**=====放置阶段=====**/
		List<Flavor> inputFList = input.inputFList;          //拿到输入的虚拟机集合
		List<Server> inputSList = input.inputSList;          //拿到输入的服务器集合
		Distribute distribute = new Distribute(fdataArray, inputSList, inputFList);    //开始分配
		
		String[] resultS = distribute.resultD;             //拿到分配结果
		int[] modifyFDataArray = distribute.fDataArray;    //拿到修正的预测结果
		String[] resultF = GetPredictResult(fNameArray, modifyFDataArray);     //将预测结果转换为字符串

		/**========返回结果=======**/
		return GetCombineStr(resultF, resultS);    //返回输出结果
		
	}
	
	/**
	 * 将预测结果转换为字符串
	 * @param fNameArray
	 * @param fDataArray
	 * @return
	 */
	public static String[] GetPredictResult(String[] fNameArray, int[] fDataArray){
		int fNum = fNameArray.length;
		int sumFlavor=0;               //将处理后的数据按题目要求封装到字符串数组中
		for(int i=0 ; i<fNum ; i++){   //计算总的虚拟机个数
			sumFlavor += fDataArray[i];
		}
		String[] resultF = new String[fNum+2];  //存放虚拟机最终预测结果字符串
		resultF[0]=Integer.toString(sumFlavor);
		for(int i=0 ; i<fNum ; i++){
			
			resultF[i+1] = fNameArray[i]+" "+Integer.toString(fDataArray[i]);
			
		}
		resultF[fNum+1]="";
		
		return resultF;
	}
	
	/**
	 * 合并两个字符串
	 * @param resultF
	 * @param resultS
	 * @return
	 */
	public static String[] GetCombineStr(String[] resultF, String[] resultS){
		
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
