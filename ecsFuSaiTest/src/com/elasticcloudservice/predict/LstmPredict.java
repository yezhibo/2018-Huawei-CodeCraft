package com.elasticcloudservice.predict;

import com.elasticcloudservice.dataTool.*;


/**
 * @ProjectName ecsTest
 * @author Yezhibo
 * @CreatTime 2018年4月11日上午10:49:17
 */
public class LstmPredict {
	
	/**
	 * LSTM训练函数
	 * @param sample   输入层训练样本数据
	 * @param label    输出层标签数据
	 * @param epoch    最大迭代次数
	 * @param hDim     输出数据维数
	 * @param xDim     输入数据维数
	 * @param lr       步长
	 * @return LSTM    训练好的神经网络
	 */
	public static LSTM TrainLstm(double[][] sample, double[] label, int epoch, int hDim, int xDim, double baselr, double baseMinLoss){
		
		LSTM lstm = new LSTM(hDim, xDim/*, lr*/);
		
		double loss = 10;
		
		double lr = baselr;
		
		double minLoss = baseMinLoss;
		
		int iter = 1;
		
		while(loss>minLoss && iter<epoch ){			
			
			for (int j = 0; j < sample.length; ++j) {        //迭代一次输入一次，输入数据是固定的，输入过程伴随前向传播，经过这一步后神经网络的各个神经元状态值已经确定
				
				lstm.x_list_add(sample[j]);  
				
			}
			
			loss = lstm.y_list_is(label, new ToyLossLayer());        //loss值计算函数，同时计算梯度值
			
			//if(iter%10==0){
				lr = baselr * Math.pow(0.9929, iter); 
				minLoss = baseMinLoss * Math.pow(1.001, iter);
			//}
				
			/*if(iter%500==0){
				lr *= 0.1;
			}*/
				
			
			lstm.param.apply_diff(lr,iter);                        //反向传播过程，更新每个神经元的参数
			
			lstm.XListClear();			
			
			iter++;
			
		}
		
		System.out.println("iter: " + iter + ",loss: " + loss+",lr:"+lr+",minLoss"+minLoss);
		
		return lstm;
		
	}
	
	/**
	 * lstm前向传播预测函数
	 * @param lstm
	 * @param normalArray
	 * @param pDays
	 * @return
	 */
	public static double[] GetLstmPreArray(LSTM lstm, double[] normalArray, int pDays){
		
		/*1. 得到神经网络最后一个神经元的输出*/
		int nodeCont = lstm.lstm_node_list.size();
		
		double[] s_prev = lstm.lstm_node_list.get(nodeCont - 1).state.s;     //得到前一个神经元的s输出
		
		double[] h_prev = lstm.lstm_node_list.get(nodeCont - 1).state.h;     //得到前一个神经元的h输出
		
		/*2. 清空神经网络的隐藏层*/
		lstm.NodeListClear();
		
		double[] lstmPreArray = new double[pDays];
		
		int n = normalArray.length;
		
		int xDim = lstm.x_dim;
		
		for (int i = 0 ; i<pDays ; i++){     //开始预测接下来pDays天的数量并保存在数组中
			
			/*得到LSTM神经网络输入值*/
			double[] x = new double[xDim];      //存放预测值前n天的数据,神经网络的输入层	
			
			if(i<xDim){
							
				System.arraycopy(normalArray, n-xDim+i, x, 0, xDim-i);  //将原数组dataArray 从下标n-p+i 个元素开始 的 p-i个值复制到 数组x中，从下标0开始存放
				
				int m=0;
				
				for(int j=xDim-i;j<xDim;j++){
					x[j]=lstmPreArray[m];
					m++;
				}
				
			}else{
				
				for (int k=0 ; k<xDim ; k++){
					x[k]=lstmPreArray[i-xDim+k];
				}
			}
			
			/*将输入值输入神经网络开始前向传播预测*/
			
			LSTMState state = new LSTMState(lstm.mem_cell_cnt, lstm.x_dim);  //初始化隐藏层神经元的状态信息
			
			lstm.lstm_node_list.add(new LSTMNode(state, lstm.param));        //添加一个新的神经元
			
			lstm.lstm_node_list.get(0).bottom_data_is(x, s_prev, h_prev);    //根据输入层和前一个神经元的输出开始在本神经元中前向传播 
			
			s_prev = lstm.lstm_node_list.get(0).state.s;                     //获取当前神经元的s输出，作为下个神经元的s输入
			
			h_prev = lstm.lstm_node_list.get(0).state.h;                     //获取当前神经元的h输出，作为下个神经元的h输入
			
			lstmPreArray[i] = h_prev[0];         //得到预测值        
			
			lstm.NodeListClear();
		}
		
		return lstmPreArray;
		
	}
	
	/**
	 * 预测函数
	 * @param detectArray  异常处理后数组
	 * @param pDays        预测天数
	 * @param hDim         神经网络输出维数
	 * @param xDim         神经网络输入维数
	 * @param maxEpoch     最大迭代次数
	 * @param lr           初始迭代步长
	 * @param minLoss      最小损失值
	 * @return
	 */
	public static double[] GetPredictArray(double[] detectArray, int pDays, int hDim, int xDim, int maxEpoch, double lr, double minLoss,int week){
		
        double[] WeekSum = new double[(int)(detectArray.length/week)];
		
		WeekSum = DataPreprocess.GetYArray(detectArray,week);         //按week将原始数据合并
		
		double[] normalArray = DataPreprocess.Normal(WeekSum);
		
		double[] preArray = new double[pDays];
		
		double[][] X = DataMath.GetSample(normalArray, xDim);
		
		double[] label = DataMath.GetLabel(normalArray, xDim);
		
		LSTM lstm = TrainLstm(X, label, maxEpoch, hDim, xDim, lr, minLoss);
		
		preArray = GetLstmPreArray(lstm, normalArray,pDays);
				
		double[] rPreArray = DataPreprocess.RNormal(WeekSum, preArray);
				
		return rPreArray;
		
	}
	
	/**
	 * 测试函数
	 * @param args
	 */
	/*public static void main(String[] args) {
		
		double[] flavor1 = {3,	6,	2,	2,	0,	3,	4,	7,	2.78181818181818,	3,	
	            4,	0,	0,	4,	1,	5,	3,	2.78181818181818,	3,	0,	
	            6,	2,	4,	7,	7,	2,	1,	2.78181818181818,	2,	3,	
	            1,	0,	2,	1,	1,	0,	6,	2,	1,	0,	
	            2,	1,	4,	3,	2.78181818181818,	2,	3,	1,	3,	3,	
	            2,	5,	2.78181818181818,	1,	2, 8, 1};    //训练集  55天数据
		
		double[] normalArray = DataPreprocess.Normal(flavor1);
		
		double[] preArray = GetPredictArray(normalArray, 7);
		
		double[] rnormal = DataPreprocess.RNormal(flavor1, preArray);
		
		System.out.println(Arrays.toString(rnormal));
		
		
	}*/

}
