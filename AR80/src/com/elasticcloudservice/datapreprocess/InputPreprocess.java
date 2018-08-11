package com.elasticcloudservice.datapreprocess;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 输入处理类
 * @ProjectName AR80
 * @author Yezhibo
 * @CreatTime 2018年4月15日上午10:05:51
 */
public class InputPreprocess {
		
	/**
	 * flavorName和day分离函数
	 * @param ecsContent 程序输入原始字符串数组
	 * @return history 处理后的字符串数组
	 */
 	public static List<String> getHistoryNameAndTime(String[] ecsContent){
		
		List<String> history = new ArrayList<String>();
		for (int i = 0; i < ecsContent.length; i++) {

			if (ecsContent[i].contains("	")
					&& ecsContent[i].split("	").length == 3) {

				String[] array = ecsContent[i].split("	");
				//String uuid = array[0];
				String flavorName = array[1];
				String[] time=array[2].split(" ");
				String createTime = time[0];

				history.add(flavorName + " " + createTime);
			}
		}
		return history;
		
	}
	
	/**
	 * 历史记录虚拟机频数统计函数
	 * @param flavorName 要预测的虚拟机名称
	 * @param history 训练数据集（name+" "+time）
	 * @param days 训练集的天数
	 * @return array 表示flavorName在训练数据集中每天预定的个数
	 */
 	public static double[] getFlavorArray(String flavorName,List<String> history){
 		
 		String start = history.get(0).split(" ")[1];                     //求出训练数据集中间隔天数，最终需要多加1
		String end = history.get(history.size() - 1).split(" ")[1];
		int days = getIntervalDay(start,end) + 1;	
 		
		//定义一个整型数组，存放符合要求的虚拟机个数分布
		double[] array=new double[days];
		int d=0,count=0;
		
		//初始化对比时间
		String tempTime=history.get(0).split(" ")[1];
		
		//开始遍历历史记录数据并统计出符合要求的虚拟机个数
		for (int i = 0; i<history.size(); i ++){
			
			String time=history.get(i).split(" ")[1];
			
			String name=history.get(i).split(" ")[0];
			
			int space=getIntervalDay(tempTime,time);
			
			if(time.equals(tempTime)){
				
				if(name.equals(flavorName)){
					
					count++;
					
				}
				
				if(d==(days-1))
					
					array[d]=count;	
				
			}else if(space!=1){
				
				array[d]=count;
				
				//对跳过的日期自动补零
				for(int k=1;k<space;k++){
					
					if((d+k)>=days)
						break;
					array[d+k]=0;
				}
				
				if(name.equals(flavorName))
					count=1;
				
				else
				count=0;
				
				d=d+space;
				tempTime=time;
				
			}else{		
				
				array[d]=count;
				
				if(name.equals(flavorName))
					count=1;
				
				else					
				count=0;
				
				d++;
				tempTime=time;
			}
		}	
		
		return array;
		
	}
		
 	/**
	 * 日期间隔计算函数
	 * @param smdate 开始时间
	 * @param bdate  结束时间
	 * @return days  间隔天数
	 * @throws ParseException
	 */
    public static int getIntervalDay(String smdate,String bdate) {  
        long between_days = 0;
		try {
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");  
			Calendar cal = Calendar.getInstance();    
			cal.setTime(sdf.parse(smdate));    
			long time1 = cal.getTimeInMillis();                 
			cal.setTime(sdf.parse(bdate));    
			long time2 = cal.getTimeInMillis();         
			between_days = (time2-time1)/(1000*3600*24);
		} catch (Exception e) {
			e.printStackTrace();
		}  
            
       return Integer.parseInt(String.valueOf(between_days));     
    }  

}
