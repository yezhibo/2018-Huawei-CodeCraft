package com.elasticcloudservice.lstm;

import java.util.ArrayList;
import java.util.List;

import com.elasticcloudservice.dataTool.*;

/**
 * LSTM类
 * @ProjectName ecsTest
 * @author Yezhibo
 * @CreatTime 2018年4月10日上午10:10:59
 */
public class Lstm {
	
	public int mem_cell_cnt;  //输出维数	
	public int x_dim;         //输入维数	
	
	public LstmParam param;                //单个神经元的参数，对于一个神经网络，所有隐藏层的神经元参数模型都是一样的	
	public List<LstmNode> lstm_node_list;  //神经网络隐藏层神经元集合，可以看作神经网络的隐藏层，但每个神经元中存放的也有输出信息，所以也包含输出层	
	public List<double[]> x_list;          //输入数据集合，存放历次输入数据， 可以看作神经网络的输入层
		
	/**
	 * LSTM构造函数
	 * @param mem_cell_cnt
	 * @param x_dim
	 * @param lr
	 */
	public Lstm(int mem_cell_cnt, int x_dim/*, double lr*/) {
		
		this.mem_cell_cnt = mem_cell_cnt;
		this.x_dim = x_dim;
		
		param = new LstmParam(mem_cell_cnt, x_dim);    //初始化神经元的参数信息
		
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
	public double y_list_is(double[] y, LossLayer lossLayer) {
		
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
			
			LstmState state = new LstmState(this.mem_cell_cnt, this.x_dim);  //初始化隐藏层神经元的状态信息
			
			lstm_node_list.add(new LstmNode(state, this.param));             //添加一个神经元到神经网络的隐藏层集合
			
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





