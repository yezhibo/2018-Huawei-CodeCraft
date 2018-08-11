package com.elasticcloudservice.lstm;

/**
 * lstm状态类
 * @ProjectName ecsLSTM
 * @author Yezhibo
 * @CreatTime 2018年4月26日下午6:20:59
 */
public class LstmState {

	public double[] g, i, f, o, s, h, bottom_diff_h, bottom_diff_s;
	
	/**
	 * 状态类构造函数
	 * @param mem_cell_cnt
	 * @param x_dim
	 */
	public LstmState(int mem_cell_cnt, int x_dim) {
		
		this.g = new double[mem_cell_cnt]; 
		
		this.i = new double[mem_cell_cnt]; //输入门输出
		
		this.f = new double[mem_cell_cnt]; //遗忘门输出
		
		this.o = new double[mem_cell_cnt]; //ot门输出
		
		this.s = new double[mem_cell_cnt]; //细胞状态
		
		this.h = new double[mem_cell_cnt]; //底层输出
		
		this.bottom_diff_h = new double[mem_cell_cnt];   
		
		this.bottom_diff_s = new double[mem_cell_cnt];
		
	}
	
}
