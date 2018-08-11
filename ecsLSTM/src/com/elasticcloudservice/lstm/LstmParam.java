package com.elasticcloudservice.lstm;

import com.elasticcloudservice.dataTool.DataMath;

/**
 * LSTM参数类
 * @ProjectName ecsLSTM
 * @author Yezhibo
 * @CreatTime 2018年4月26日下午6:16:04
 */
public class LstmParam {

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
	public LstmParam(int mem_cell_cnt, int x_dim) {
		
		this.mem_cell_cnt = mem_cell_cnt;
		this.x_dim = x_dim;
		this.concat_len = mem_cell_cnt + x_dim;
		
		//每个门的权重
		this.wg = DataMath.rand_arr(-0.1, 0.1, mem_cell_cnt, concat_len);
		this.wf = DataMath.rand_arr(-0.1, 0.1, mem_cell_cnt, concat_len);
		this.wi = DataMath.rand_arr(-0.1, 0.1, mem_cell_cnt, concat_len);
		this.wo = DataMath.rand_arr(-0.1, 0.1, mem_cell_cnt, concat_len);
		
		//每个门的偏置
		this.bg = DataMath.rand_vec(-0.1, 0.1, mem_cell_cnt);
		this.bf = DataMath.rand_vec(-0.1, 0.1, mem_cell_cnt);
		this.bo = DataMath.rand_vec(-0.1, 0.1, mem_cell_cnt);
		this.bi = DataMath.rand_vec(-0.1, 0.1, mem_cell_cnt);
		
		//梯度
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
