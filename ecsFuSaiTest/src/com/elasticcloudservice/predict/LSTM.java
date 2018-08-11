package com.elasticcloudservice.predict;

import java.util.ArrayList;
import java.util.List;

import com.elasticcloudservice.dataTool.*;

/**
 * LSTM类
 * @ProjectName ecsTest
 * @author Yezhibo
 * @CreatTime 2018年4月10日上午10:10:59
 */
public class LSTM {
	
	int mem_cell_cnt;  //输出维数	
	int x_dim;         //输入维数	
	
	LSTMParam param;                //单个神经元的参数，对于一个神经网络，所有隐藏层的神经元参数模型都是一样的
	
	List<LSTMNode> lstm_node_list;  //神经网络隐藏层神经元集合，可以看作神经网络的隐藏层，但每个神经元中存放的也有输出信息，所以也包含输出层
	
	List<double[]> x_list;          //输入数据集合，存放历次输入数据， 可以看作神经网络的输入层
	
	
	/**
	 * LSTM构造函数
	 * @param mem_cell_cnt
	 * @param x_dim
	 * @param lr
	 */
	public LSTM(int mem_cell_cnt, int x_dim/*, double lr*/) {
		
		this.mem_cell_cnt = mem_cell_cnt;
		this.x_dim = x_dim;
		//this.lr = lr;
		
		param = new LSTMParam(mem_cell_cnt, x_dim);    //初始化神经元的参数信息
		
		this.lstm_node_list = new ArrayList<>();       //初始化的时候并没有设置具体长度
		
		this.x_list = new ArrayList<double[]>();               //初始化输入数据集合也没有设置具体长度
	}
	
	public void XListClear() {    //将神经网络输入层数据集合清空
		
		x_list.clear();
		
	}
	
	public void NodeListClear() {    //将神经网络隐藏层神经元集合清空
		
		lstm_node_list.clear();
		
	}
	
	/**
	 * 反向传播函数
	 * @param y              //标签向量
	 * @param lossLayer
	 * @return
	 */
	public double y_list_is(double[] y, ToyLossLayer lossLayer) {
		
		//判断输入标签的数量和样本个数是否相等
		assert y.length == x_list.size();  //断言，如果表达式为真，则程序继续执行，否则抛出AssertionError 并终止执行
		
		int idx = this.x_list.size() - 1;
		
		//计算最后一个样本输出的loss值
		double loss = lossLayer.loss(this.lstm_node_list.get(idx).state.h, y[idx]); //得到此时隐藏层最后一个神经元输出和标签的最后一个值
		
		double[] diff_h = lossLayer.bottom_diff(this.lstm_node_list.get(idx).state.h, y[idx]);
		
		double[] diff_s = new double[this.mem_cell_cnt];
		
		this.lstm_node_list.get(idx).top_diff_is(diff_h, diff_s);
		
		idx -= 1;
		
		while (idx >= 0) {
			
			loss += lossLayer.loss(this.lstm_node_list.get(idx).state.h, y[idx]);
			
			diff_h = lossLayer.bottom_diff(this.lstm_node_list.get(idx).state.h, y[idx]);  //表示本时刻loss的梯度
			
			diff_h = DataMath.add(diff_h, this.lstm_node_list.get(idx + 1).state.bottom_diff_h); //表示本时刻h梯度和“前边”的h梯度和
			
			diff_s = this.lstm_node_list.get(idx + 1).state.bottom_diff_s;   //表示“前一个神经元”的s梯度
			
			this.lstm_node_list.get(idx).top_diff_is(diff_h, diff_s);
			
			idx -= 1;
		}
		
		return loss;
		
	}
	
	/**
	 * 将数据x输入神经网络，经过前向传播得到输出
	 * @param x
	 */
	public void x_list_add(double[] x) { 
		
		x_list.add(x);                               //将输入数据放入成员函数中
		
		if (x_list.size() > lstm_node_list.size()) {         //判断输入数据的集合是否大于隐藏层神经元的个数
			
			LSTMState state = new LSTMState(this.mem_cell_cnt, this.x_dim);  //初始化隐藏层神经元的状态信息
			
			lstm_node_list.add(new LSTMNode(state, this.param));             //添加一个神经元到神经网络的隐藏层集合
			
		}
		
		int idx = x_list.size() - 1;   //表示隐藏层前一个神经元的在集合中的位置，本测试中一共有4个输入数据，对应4个神经元
		
		if(idx == 0){                  //如果是第一个神经元，则ct-1 和 ht-1 为0
			
			this.lstm_node_list.get(idx).bottom_data_is(x, null, null); //备注：如果获取的“index”值不存在，就会报错数组越界，所以开发过程中一定多加注意。
			
		}else{  //从第二个神经元开始，前向传播
			
			double[] s_prev = this.lstm_node_list.get(idx - 1).state.s;     //得到前一个神经元的s输出
			
			double[] h_prev = this.lstm_node_list.get(idx - 1).state.h;     //得到前一个神经元的h输出
			
			this.lstm_node_list.get(idx).bottom_data_is(x, s_prev, h_prev); //根据输入层和前一个神经元的输出开始在本神经元中前向传播 
			
		}
	}
}

/**
 * 损失函数计算类
 * @ProjectName ecsTest
 * @author Yezhibo
 * @CreatTime 2018年4月10日上午11:44:19
 */
class ToyLossLayer{
	
	/**
	 * 计算loss值
	 * @param pred
	 * @param label
	 * @return
	 */
	public double loss(double[] pred, double label) { // 第一个记忆单元的输出？
		
		return (pred[0] - label) * (pred[0] - label);
		
	}
	
	/**
	 * 计算loss函数的导数，不同神经元取值不同
	 * @param pred
	 * @param label
	 * @return
	 */
	public double[] bottom_diff(double[] pred, double label) {
		
		double[] diff = new double[pred.length];
		diff[0] = 2 * (pred[0] - label);
		return diff;
		
	}
	
}

class LSTMParam{
	
	int mem_cell_cnt;  //神经元的个数
	int x_dim;         //神经元输入数据的维数
	int concat_len;   
	
	double[][] wg, wi, wf, wo, mt, vt, emt, evt;
	double[][] wg_diff, wi_diff, wf_diff, wo_diff,Lwg_diff,Lwi_diff, Lwf_diff, Lwo_diff;
	double[] bg, bi, bf, bo,mbt, vbt, embt, evbt;
	double[] bg_diff, bi_diff, bf_diff, bo_diff,Lbg_diff, Lbi_diff, Lbf_diff, Lbo_diff;

	/**
	 * 参数构造函数
	 * @param mem_cell_cnt
	 * @param x_dim
	 */
	public LSTMParam(int mem_cell_cnt, int x_dim) {
		
		this.mem_cell_cnt = mem_cell_cnt;
		this.x_dim = x_dim;
		this.concat_len = mem_cell_cnt + x_dim;
		
		this.wg = DataMath.rand_arr(-0.1, 0.1, mem_cell_cnt, concat_len);
		this.wf = DataMath.rand_arr(-0.1, 0.1, mem_cell_cnt, concat_len);
		this.wi = DataMath.rand_arr(-0.1, 0.1, mem_cell_cnt, concat_len);
		this.wo = DataMath.rand_arr(-0.1, 0.1, mem_cell_cnt, concat_len);
		
		this.bg = DataMath.rand_vec(-0.1, 0.1, mem_cell_cnt);
		this.bf = DataMath.rand_vec(-0.1, 0.1, mem_cell_cnt);
		this.bo = DataMath.rand_vec(-0.1, 0.1, mem_cell_cnt);
		this.bi = DataMath.rand_vec(-0.1, 0.1, mem_cell_cnt);
		
		this.wg_diff = new double[mem_cell_cnt][concat_len];
		this.wo_diff = new double[mem_cell_cnt][concat_len];
		this.wi_diff = new double[mem_cell_cnt][concat_len];
		this.wf_diff = new double[mem_cell_cnt][concat_len];
		
		this.bg_diff = new double[mem_cell_cnt];
		this.bo_diff = new double[mem_cell_cnt];
		this.bi_diff = new double[mem_cell_cnt];
		this.bf_diff = new double[mem_cell_cnt];
		
		//上次迭代更新的梯度
		this.Lwg_diff = new double[mem_cell_cnt][concat_len];
		this.Lwo_diff = new double[mem_cell_cnt][concat_len];
		this.Lwi_diff = new double[mem_cell_cnt][concat_len];
		this.Lwf_diff = new double[mem_cell_cnt][concat_len];
		
		this.Lbg_diff = new double[mem_cell_cnt];
		this.Lbo_diff = new double[mem_cell_cnt];
		this.Lbi_diff = new double[mem_cell_cnt];
		this.Lbf_diff = new double[mem_cell_cnt];
		
		//梯度的矩估计
		this.mt = new double[mem_cell_cnt][concat_len];
		this.vt = new double[mem_cell_cnt][concat_len];
		this.emt = new double[mem_cell_cnt][concat_len];
		this.evt = new double[mem_cell_cnt][concat_len];
		
		this.mbt = new double[mem_cell_cnt];
		this.vbt = new double[mem_cell_cnt];
		this.embt = new double[mem_cell_cnt];
		this.evbt = new double[mem_cell_cnt];
	}
	
	/**
	 * 反向传播函数，更新神经元的参数
	 * @param lr
	 */
	public void apply_diff(double lr, int iter) {
		
		this.Lwg_diff = this.wg_diff;
		this.Lwo_diff = this.wo_diff;
		this.Lwi_diff = this.wi_diff;
		this.Lwf_diff = this.wf_diff;
		
		this.Lbg_diff = this.bg_diff;
		this.Lbo_diff = this.bo_diff;
		this.Lbi_diff = this.bi_diff;
		this.Lbf_diff = this.bf_diff;
		
		reducem(wg, wg_diff, lr, this.Lwg_diff, iter);
		reducem(wf, wf_diff, lr, this.Lwo_diff, iter);
		reducem(wo, wo_diff, lr, this.Lwo_diff, iter);
		reducem(wi, wi_diff, lr, this.Lwo_diff, iter);
		
		reduceb(bf, bf_diff, lr, this.Lbg_diff, iter);
		reduceb(bg, bg_diff, lr, this.Lbg_diff, iter);
		reduceb(bo, bo_diff, lr, this.Lbg_diff, iter);
		reduceb(bi, bi_diff, lr, this.Lbg_diff, iter);
		
		
		this.wg_diff = new double[mem_cell_cnt][concat_len];
		this.wo_diff = new double[mem_cell_cnt][concat_len];
		this.wi_diff = new double[mem_cell_cnt][concat_len];
		this.wf_diff = new double[mem_cell_cnt][concat_len];
		
		this.bg_diff = new double[mem_cell_cnt];
		this.bo_diff = new double[mem_cell_cnt];
		this.bi_diff = new double[mem_cell_cnt];
		this.bf_diff = new double[mem_cell_cnt];
	}
	
	/**
	 * 更新权重
	 * @param w
	 * @param w_diff
	 * @param lr
	 */
	private void reducem(double[][] w, double[][] w_diff, double lr, double [][] Ldiff, int iter) {
		
        int n = w.length;
		int m = w[0].length;
		//Adam
		double B1 = 0.9, B2 = 0.999, c = Math.pow(10,-8);
		
		for (int i = 0; i < n; ++i) {			
			for (int j = 0; j < m; ++j) {	
				
				mt[i][j] = B1 * mt[i][j] + (1 - B1) * Ldiff[i][j];
				emt[i][j] = mt[i][j]/(1 - Math.pow(B1, iter));
			}
		}
		for (int i = 0; i < n; ++i) {			
			for (int j = 0; j < m; ++j) {	
				
				vt[i][j] = B2 * vt[i][j] + (1 - B2) * Math.pow(Ldiff[i][j],2);
				evt[i][j] = vt[i][j]/(1 - Math.pow(B2, iter));
			}
		}
		
		for (int i = 0; i < n; ++i) {			
			for (int j = 0; j < m; ++j) {	
				
				w[i][j] -= lr * emt[i][j] / (Math.sqrt(evt[i][j]) + c);
			}
		}
		
	}
	
	/**
	 * 更新偏置
	 * @param b
	 * @param b_diff
	 * @param lr
	 */
	private void reduceb(double[] b, double[] b_diff, double lr,double [] Ldiff, int iter) {
		
		int n = b.length;
		
//		for (int i = 0; i < n; ++i) {
//			b[i] -= lr * b_diff[i];
//		}
		
		//Adam
		double B1 = 0.9, B2 = 0.999, c = Math.pow(10,-8);
		
		for (int i = 0; i < n; ++i) {			
				
				mbt[i] = B1 * mbt[i] + (1 - B1) * Ldiff[i];
				embt[i] = mbt[i]/(1 - Math.pow(B1, iter));
			}
		
		for (int i = 0; i < n; ++i) {			
				
				vbt[i] = B2 * vbt[i] + (1 - B2) * Math.pow(Ldiff[i],2);
				evbt[i] = vbt[i]/(1 - Math.pow(B2, iter));
			}		
		
		for (int i = 0; i < n; ++i) {			
				
				b[i] -= lr * embt[i] / (Math.sqrt(evbt[i]) + c);
			}				
		
		
	}
	
}

/**
 * 神经元内部状态类
 * @ProjectName ecsTest
 * @author Yezhibo
 * @CreatTime 2018年4月10日上午11:28:27
 */
class LSTMState{
	
	double[] g, i, f, o, s, h, bottom_diff_h, bottom_diff_s;
	
	/**
	 * 状态类构造函数
	 * @param mem_cell_cnt
	 * @param x_dim
	 */
	public LSTMState(int mem_cell_cnt, int x_dim) {
		
		this.g = new double[mem_cell_cnt];
		
		this.i = new double[mem_cell_cnt];
		
		this.f = new double[mem_cell_cnt];  //遗忘门输出？
		
		this.o = new double[mem_cell_cnt];
		
		this.s = new double[mem_cell_cnt];
		
		this.h = new double[mem_cell_cnt];
		
		this.bottom_diff_h = new double[mem_cell_cnt];
		
		this.bottom_diff_s = new double[mem_cell_cnt];
		
	}

}



/**
 * 神经元节点类
 * @ProjectName ecsTest
 * @author Yezhibo
 * @CreatTime 2018年4月10日上午11:33:30
 */
class LSTMNode{
	
	LSTMState state;   //表示当前神经元节点的状态值也就是各个门的输出信息：g, i, f, o, s, h, bottom_diff_h, bottom_diff_s
	LSTMParam param;   //表是当前神经元的参数信息 wg, wi, wf, wo; wg_diff, wi_diff, wf_diff, wo_diff;
	                                      //bg, bi, bf, bo;bg_diff, bi_diff, bf_diff, bo_diff;
	
	double[] s_prev;  //表示前一个神经元的输入 c(t-1)
	double[] h_prev;  //表示前一个神经元的输入h(t-1)
	
	double[] xc;      //表示输入层的输入
	
	/**
	 * 节点类构造函数
	 * @param state
	 * @param param
	 */
	public LSTMNode(LSTMState state, LSTMParam param) {
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

