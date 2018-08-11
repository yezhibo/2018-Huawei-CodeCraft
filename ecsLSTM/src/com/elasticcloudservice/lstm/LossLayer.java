package com.elasticcloudservice.lstm;

/**
 * LSTM损失计算类
 * @ProjectName ecsLSTM
 * @author Yezhibo
 * @CreatTime 2018年4月26日下午6:14:18
 */
public class LossLayer {
	/**
	 * 计算loss值
	 * @param pred
	 * @param label
	 * @return
	 */
	public double loss(double[] pred, double label) { 
		
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
