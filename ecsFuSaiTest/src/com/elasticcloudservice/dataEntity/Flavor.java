package com.elasticcloudservice.dataEntity;

/**
 * @ProjectName ecsTest
 * @author Yezhibo
 * @CreatTime 2018年4月12日下午9:13:16
 */
public class Flavor {

	public String name;
	public int mem;
	public int cpu;
	//int serverId;
	
	/**
	 * 构造函数
	 * @param name
	 * @param cpu
	 * @param mem
	 */
	public Flavor(String name, int cpu, int mem/*, int serverId*/){
		this.cpu = cpu;
		this.mem = mem;
		this.name = name;
		//this.serverId = serverId;
	}
	
}
