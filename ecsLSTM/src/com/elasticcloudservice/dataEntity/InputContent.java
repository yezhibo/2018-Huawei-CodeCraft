package com.elasticcloudservice.dataEntity;

import java.util.ArrayList;
import java.util.List;

import com.elasticcloudservice.dataTool.DataPreprocess;

/**
 * @ProjectName ecsFuSaiTest
 * @author Yezhibo
 * @CreatTime 2018年4月17日下午4:22:38
 */
public class InputContent {

	public int serverCont;    //输入物理机个数
	public int flavorCont;    //输入虚拟机个数
	public int preDays;       //预测天数
	public int delayDays;     //预测的时间与训练集最后一天的间隔
	
	public String[] fNameArray;  //虚拟机名称数组
	public String[] sNameArray;  //物理机名称数组
	public int[] sCpuArray;      //物理机CPU数组
	public int[] sMemArray;      //物理机MEM数组
	public int[] fCpuArray;      //虚拟机CPU数组
	public int[] fMemArray;      //虚拟机MEM数组
	
	public List<Server> inputSList; //输入服务器集合
	public List<Flavor> inputFList; //输入虚拟机集合
	
	public InputContent(String[] inputContent){
		
		this.serverCont = Integer.parseInt(inputContent[0]);    //得到物理机的个数		
		this.sNameArray = new String[serverCont];
		this.sCpuArray = new int[serverCont];
		this.sMemArray = new int[serverCont];		
		for(int i=0; i<serverCont; i++){                    //将物理机的名称、CPU、MEM对应放入数组
			String[] str = inputContent[i+1].split(" ");
			this.sNameArray[i] = str[0];
			this.sCpuArray[i] = Integer.parseInt(str[1]);
			this.sMemArray[i] = Integer.parseInt(str[2]);
		}
		
		this.flavorCont = Integer.parseInt(inputContent[serverCont+2]); //得到预测虚拟机个数
		this.fNameArray = new String[flavorCont];
		this.fCpuArray = new int[flavorCont];
		this.fMemArray = new int[flavorCont];
		for(int i=0; i<flavorCont; i++){                    //将虚拟机的名称、CPU、MEM对应放入数组
			String[] str = inputContent[serverCont+3+i].split(" ");
			this.fNameArray[i] = str[0];
			this.fCpuArray[i] = Integer.parseInt(str[1]);
			this.fMemArray[i] = Integer.parseInt(str[2])/1024;
		}
		
		String preStartDate = inputContent[serverCont+flavorCont+4];
		String preEndDate = inputContent[serverCont+flavorCont+5];
		
		this.preDays = DataPreprocess.getDaysBetween(preStartDate, preEndDate,"yyyy-MM-dd HH:mm:ss"); //获取预测天数
		
		this.inputSList = new ArrayList<Server>();
		for(int i=0; i<serverCont; i++){
			Server server = new Server(this.sNameArray[i], this.sCpuArray[i], this.sMemArray[i]);
			this.inputSList.add(server);
		}
		
		this.inputFList = new ArrayList<Flavor>();
		for(int i=0; i<flavorCont; i++){
			Flavor flavor = new Flavor(this.fNameArray[i], this.fCpuArray[i], this.fMemArray[i]);
			this.inputFList.add(flavor);
		}
	}
}
