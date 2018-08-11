package com.elasticcloudservice.dataEntity;

import java.util.ArrayList;
import java.util.List;

import com.elasticcloudservice.dataTool.DataPreprocess;

/**
 * @ProjectName ecsFuSaiTest
 * @author Yezhibo
 * @CreatTime 2018年4月17日下午5:34:19
 */
public class EcsContent {
	
	public int seriesCont;
	public String[] content;
	public List<double[]> flavorSeriesList;
	
	public EcsContent(String[] ecsContent, String[] fNameArray){
		
		this.content = RemoveId(ecsContent);
		this.flavorSeriesList = new ArrayList<double[]>();
		int cCont = content.length;
		int fCont = fNameArray.length;
		
		
		/*获取时间序列的长度*/
		String startDate = content[0].split(" ")[1]+" "+content[0].split(" ")[2];
		String endDate = content[cCont-1].split(" ")[1]+" "+content[cCont-1].split(" ")[2];
		this.seriesCont = DataPreprocess.getDaysBetween(startDate, endDate, "yyyy-MM-dd HH:mm:ss");
		
		/*开始获取每个虚拟机的时间序列*/		
		for(int i=0; i<fCont; i++){      //开始计算每个虚拟机的时间序列  
			
			String flavorName = fNameArray[i]; //要遍历的虚拟机名称
			int d = 0;       //表示时间序列的下标
			int count = 0;   //表示虚拟机每天的销售量
			double[] flavorArray = new double[seriesCont];	//存放时间序列		
			String tempTime = content[0].split(" ")[1];
			
			for(int j=0; j<cCont; j++){                    //开始遍历训练集的每行数据
				
				String name = content[j].split(" ")[0];    //得到第i行数据的虚拟机名称
				String time = content[j].split(" ")[1];	   //得到第i行数据的时间戳									
				int space = DataPreprocess.getDaysBetween(tempTime,time,"yyyy-MM-dd");//判断跟上一行数据的时间间隔
				
				if(space == 0){		                     //如果两行数据的时间相等表示是一天数据
					
					if(name.equals(flavorName)) count++; //判断是否是遍历的虚拟机													
					if(d==(seriesCont-1))	flavorArray[d]=count;
					
				}else if(space == 1){	                 //表示遍历到下一天的数据
					
					flavorArray[d]=count;   //把前一天的数据放入时间序列					
					if(name.equals(flavorName)) count=1; //开始对新的一天数据进行从新计算					
					else count=0;					
					d++;
					tempTime=time;
					
				}else{	                                 //表示两个记录之间有空缺值
					
					flavorArray[d]=count;					
					//对跳过的日期自动补零
					for(int k=1;k<space;k++){
						
						if((d+k)>=seriesCont) break;
						flavorArray[d+k]=0;
						
					}
					
					if(name.equals(flavorName)) count=1;					
					else count=0;
					
					d=d+space;
					tempTime=time;
					
				}
			}	
			this.flavorSeriesList.add(flavorArray);						
		}		
	}
	
	/**
	 * 将原始数据中的ID滤除
	 * @param ecsContent 程序输入原始字符串数组
	 * @return content 处理后的字符串数组
	 */
 	public String[] RemoveId(String[] ecsContent){
 		
 		int n = ecsContent.length;		
		List<String> contentList = new ArrayList<String>();
		
		/**
		 * 经过测试发现TrainData数据集是以制表符分割的
		 */
		for (int i=0; i<n; i++) {
			
			if (ecsContent[i].contains("	")
					&& ecsContent[i].split("	").length == 3) {
				String[] array = ecsContent[i].split("	");
				contentList.add(array[1]+" "+array[2]) ;
			}
		}
		
		int c = contentList.size();          //将集合转换为数组
		String[] content = new String[c];
		for(int i=0; i<c; i++){
			content[i] = contentList.get(i);
		}
		
		return content;
		
	}

}
