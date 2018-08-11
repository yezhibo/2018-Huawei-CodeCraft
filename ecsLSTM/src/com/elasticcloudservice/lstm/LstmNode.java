package com.elasticcloudservice.lstm;

import com.elasticcloudservice.dataTool.DataMath;

/**
 * LSTM神经元类
 * @ProjectName ecsLSTM
 * @author Yezhibo
 * @CreatTime 2018年4月26日下午6:26:09
 */
public class LstmNode {

	public LstmState state;   //表示当前神经元节点的状态值也就是各个门的输出信息：g, i, f, o, s, h, bottom_diff_h, bottom_diff_s
	public LstmParam param;   //表是当前神经元的参数信息 wg, wi, wf, wo; wg_diff, wi_diff, wf_diff, wo_diff;
	                                             //bg, bi, bf, bo;bg_diff, bi_diff, bf_diff, bo_diff;	
	double[] s_prev;          //表示前一个神经元的输入 c(t-1)
	double[] h_prev;          //表示前一个神经元的输入h(t-1)	
	double[] xc;              //表示输入层的输入
	
	/**
	 * 节点类构造函数
	 * @param state
	 * @param param
	 */
	public LstmNode(LstmState state, LstmParam param) {
		this.state = state;
		this.param = param;
	}
	
	
	/**
	 * LSTM核心运算函数
	 * @param x
	 * @param s_prev
	 * @param h_prev
	 */
	public void bottom_data_is(double[] x, double[] s_prev, double[] h_prev) {
		if (s_prev == null) s_prev = DataMath.zero_like(this.state.s);
		if (h_prev == null) h_prev = DataMath.zero_like(this.state.h);
		
		this.s_prev = s_prev;  //前一个神经元的输出c(t-1)
		this.h_prev = h_prev;  //前一个神经元的输出h(t-1)
		
		// concatenate x(t) and h(t - 1)
		this.xc = DataMath.hstack(x, h_prev); //表示上一个神经元底部输出h(t-1)和当前神经元的输入 x(t)向量的叠加
		
		/*1. 决定丢弃的信息*/
		this.state.f = DataMath.sigmoid(DataMath.WtxPlusBias(this.param.wf, xc, this.param.bf));  //遗忘门输出
		
		/*2. 确定更新的信息*/
		this.state.i = DataMath.sigmoid(DataMath.WtxPlusBias(this.param.wi, xc, this.param.bi));//得到i(t)			
		this.state.g = DataMath.tanh(DataMath.WtxPlusBias(this.param.wg, xc, this.param.bg));  //C_(t-1)更新为C_(t)
		
		/*3. 更新细胞状态*/
		this.state.s = DataMath.add(DataMath.mat(this.state.g, this.state.i), DataMath.mat(s_prev, this.state.f));//神经元的输出c(t)
		
		/*4. 确定输出信息*/
		this.state.o = DataMath.sigmoid(DataMath.WtxPlusBias(this.param.wo, xc, this.param.bo));						
		this.state.h = DataMath.mat(DataMath.tanh(this.state.s), this.state.o);//神经元的输出h(t)
		
	}
	
	  
	/**
	 * 计算网络参数梯度
	 * @param top_diff_h
	 * @param top_diff_s
	 */
	public void top_diff_is(double[] top_diff_h, double[] top_diff_s) {
		
		double[] ds  = DataMath.add(top_diff_s, DataMath.mat(DataMath.tanh_derivative(this.state.s), DataMath.mat(this.state.o, top_diff_h)));  
		
		//四个门的梯度
		double[] dot = DataMath.mat(this.state.s, top_diff_h);    //ot门梯度
		double[] di  = DataMath.mat(this.state.g, ds);            //i门梯度
		double[] dg  = DataMath.mat(this.state.i, ds);
		double[] df  = DataMath.mat(this.s_prev, ds);
		
		//四个权重矩阵的梯度
		double[] di_input = DataMath.mat(DataMath.sigmoid_derivative(this.state.i), di);
		double[] df_input = DataMath.mat(DataMath.sigmoid_derivative(this.state.f), df);
		double[] do_input = DataMath.mat(DataMath.sigmoid_derivative(this.state.o), dot);
		double[] dg_input = DataMath.mat(DataMath.tanh_derivative(this.state.g), dg);
		
		//更新权重矩阵梯度
		this.param.wi_diff = DataMath.add(this.param.wi_diff, DataMath.outer(di_input, this.xc));
		this.param.wf_diff = DataMath.add(this.param.wf_diff, DataMath.outer(df_input, this.xc));
		this.param.wo_diff = DataMath.add(this.param.wo_diff, DataMath.outer(do_input, this.xc));
		this.param.wg_diff = DataMath.add(this.param.wg_diff, DataMath.outer(dg_input, this.xc));
		
		//更新偏置矩阵梯度
		this.param.bi_diff = DataMath.add(this.param.bi_diff, di_input);
		this.param.bf_diff = DataMath.add(this.param.bf_diff, df_input);
		this.param.bo_diff = DataMath.add(this.param.bo_diff, do_input);
		this.param.bg_diff = DataMath.add(this.param.bg_diff, dg_input);
		
		double[] dxc = DataMath.zero_like(this.xc);
		dxc = DataMath.add(dxc, DataMath.dot(DataMath.transpose(this.param.wi), di_input));
		dxc = DataMath.add(dxc, DataMath.dot(DataMath.transpose(this.param.wf), df_input));
		dxc = DataMath.add(dxc, DataMath.dot(DataMath.transpose(this.param.wo), do_input));
		dxc = DataMath.add(dxc, DataMath.dot(DataMath.transpose(this.param.wg), dg_input));
		
		this.state.bottom_diff_s = DataMath.mat(ds, this.state.f);
		this.state.bottom_diff_h = DataMath.dim(dxc, this.param.x_dim, dxc.length);
	}
	
}
