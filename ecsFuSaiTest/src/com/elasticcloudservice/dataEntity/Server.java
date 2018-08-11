
package com.elasticcloudservice.dataEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName ecsTest
 * @author Yezhibo
 * @CreatTime 2018年4月12日下午9:09:45
 */
public class Server {
	
	public String name;
	public int totalCpu;       //服务器总的cpu
	public int totalMem;       //服务器总的mem
	public int reCpu;          //服务器剩余cpu
	public int reMem;          //服务器剩余mem
	public List<Flavor> flist; //服务器已存放的flavor
	
	/**
	 * 构造函数
	 * @param cpu
	 * @param mem
	 */
	public Server(String name,int cpu, int mem){
		this.name = name;
		this.totalCpu = cpu;
		this.totalMem = mem;
		this.reCpu = cpu;
		this.reMem = mem;
		this.flist = new ArrayList<Flavor>();
	}
	
	/**
	 * 虚拟机放置函数
	 * @param flavor
	 * @return
	 */
	public boolean PutFlavor(Flavor flavor){
		
		if(reCpu>=flavor.cpu && reMem>=flavor.mem){
			reCpu -= flavor.cpu;
			reMem -= flavor.mem;
			flist.add(flavor);
			return true;
		}
		
		return false;
		
	}
	
	/**
	 * 获取当前服务器cpu的利用率
	 * @return
	 */
	public double GetCpuUseRate(){
		
		return 1 - (double)reCpu/totalCpu;
				
	}
	
	/**
	 * 获取当前服务器mem的利用率
	 * @return
	 */
	public double GetMemUseRate(){
		
		return 1 - (double)reMem/totalMem;
		
	}

	public void Clear(){
		this.flist.clear();
		this.reCpu = this.totalCpu;
		this.reMem = this.totalMem;
	}
	
	
}