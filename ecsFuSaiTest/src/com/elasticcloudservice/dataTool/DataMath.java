package com.elasticcloudservice.dataTool;

import java.util.List;
import java.util.Random;

public class DataMath {
	
	/**
	 * 将一个集合中的两个元素交换位置
	 * @param list
	 * @param oldIndex
	 * @param newIndex
	 */
	public static <T> void swap(List<T> list, int oldIndex, int newIndex){
		
		T tempElement = list.get(oldIndex);
		
		if(oldIndex < newIndex){
			for(int i=oldIndex; i<newIndex; i++){
				list.set(i, list.get(i+1));
			}
			list.set(newIndex, tempElement);
		}
		
		if(oldIndex > newIndex){
			for(int i=oldIndex; i>newIndex; i--){
				list.set(i, list.get(i-1));
			}
			list.set(newIndex, tempElement);
		}
	}
	
	/*================================LSTM======================================*/
	
	/**
	 * 根据输入序列得到标签数据
	 * @param array
	 * @param step
	 * @return
	 */
 	public static double[] GetLabel(double[] array, int step){
		int n = array.length;
		double[] label = new double[n-step];
		for(int i=0; i<n-step; i++){				
			label[i] = array[i+step];			
		}
		return label;
	}
	
	/**
	 * 根据输入序列得到神经网络输入样本数据
	 * @param array
	 * @param step
	 * @return
	 */
	public static double[][] GetSample(double[] array, int step){
		int n = array.length;
		double[][] sample = new double[n-step][step];
		for(int i=0; i<n-step; i++){			
			for(int j=i; j<i+step; j++){
				sample[i][j-i] = array[j];
			}							
		}
		return sample;
	}
	
	/**
	 * 将两个向量合并为一个向量
	 * @param a
	 * @param b
	 * @return
	 */
	public static double[] hstack(double[] a, double[] b) {
		
		double[] ret = new double[a.length + b.length];
		int k = 0;
		for (int i = 0; i < a.length; ++i) ret[k++] = a[i];
		for (int i = 0; i < b.length; ++i) ret[k++] = b[i];
		return ret;
		
	}
	
	/**
	 * 将两个矩阵合并为一个矩阵
	 * @param a
	 * @param b
	 * @return
	 */
	public static double[][] hstack(double[][] a, double[][] b) {
		
		double[][] ret = new double[a.length][];
		for (int i = 0; i < a.length; ++i) ret[i] = hstack(a[i], b[i]);
		return ret;
		
	}
	
	/**
	 * sigmoid 激活函数，返回一个数的激活值
	 * @param x
	 * @return
	 */
	public static double sigmoid(double x) {
		
		return 1 / (1 + Math.exp(-x));
		
	}
	
	/**
	 * 返回一个向量的激活函数值
	 * @param a
	 * @return
	 */
	public static double[] sigmoid(double[] a) {
		
		double[] b = new double[a.length];
		for (int i = 0; i < a.length; ++i) b[i] = sigmoid(a[i]);
		return b;
		
	}
	
	/**
	 * sigmod函数的求导
	 * @param v
	 * @return
	 */
	public static double sigmoid_derivative(double v) {
		
		return v * (1 - v);
		
	}
	
	/**
	 * 对一个向量进行求导
	 * @param v
	 * @return
	 */
	public static double[] sigmoid_derivative(double[] v) {
		
		double[] ret = new double[v.length];
		for (int i = 0; i < v.length; ++i) ret[i] = sigmoid_derivative(v[i]);
		return ret;
		
	}
	
	/**
	 * 一个数的双曲正切导数值
	 * @param v
	 * @return
	 */
	public static double tanh_derivative(double v) {
		
		return 1 - v * v;
		
	}
	
	/**
	 * 一个向量的双曲正切导数值
	 * @param v
	 * @return
	 */
	public static double[] tanh_derivative(double[] v) {
		
		double[] ret = new double[v.length];
		for (int i = 0; i < v.length; ++i) ret[i] = tanh_derivative(v[i]);
		return ret;
		
	}
	
	/**
	 * 产生一个 x*y阶的 元素在a~b间随机数的 随机矩阵
	 * @param a
	 * @param b
	 * @param x
	 * @param y
	 * @return
	 */
	public static double[][] rand_arr(double a, double b, int x, int y){
		
		double[][] ret = new double[x][y];
		Random random = new Random(2016666);
		for (int i = 0; i < x; ++i) {
			for (int j = 0; j < y; ++j) {
				ret[i][j] = random.nextDouble() * (b - a) + a;
			}
		}
		
		return ret;
		
	}
	
	/**
	 * 产生一个x维的 元素维a~b之间随机数的随机向量
	 * @param a
	 * @param b
	 * @param x
	 * @return
	 */
	public static double[] rand_vec(double a, double b, int x){
		
		double[] ret = new double[x];
		Random random = new Random(2016666);
		for (int i = 0; i < x; ++i) {
			ret[i] = random.nextDouble() * (b - a) + a;
		}
		return ret;
		
	}
	
	/**
	 * 返回一个跟向量a同维数的0向量
	 * @param a
	 * @return
	 */
	public static double[] zero_like(double[] a) {
		
		double[] b = new double[a.length];
		return b;
		
	}
	
	/**
	 * 返回一个跟矩阵a同维数的0矩阵
	 * @param a
	 * @return
	 */
	public static double[][] zero_like(double[][] a) {
		
		double[][] b = new double[a.length][a[0].length];
		return b;
		
	}
	
	/**
	 * 向量点乘求和
	 * @param a
	 * @param b
	 * @return
	 */
	public static double dot(double[] a, double[] b) {
		double sum = 0.0;
		for (int i = 0; i < a.length; ++i) {
			sum += a[i] * b[i];
		}
		return sum;
	}
	
	/**
	 * 矩阵乘向量
	 * @param a
	 * @param b
	 * @return
	 */
	public static double[] dot(double[][] a, double[] b) {
		double[] ret = new double[a.length];
		for (int i = 0; i < a.length; ++i) {
			ret[i] = dot(a[i], b);
		}
		return ret;
	}
	
	/**
	 * 向量点乘得到新向量
	 * @param a
	 * @param b
	 * @return
	 */
	public static double[] mat(double[] a, double[] b) {
		double[] ret = new double[a.length];
		for (int i = 0; i < a.length; ++i) ret[i] = a[i] * b[i];
		return ret;
	}
	
	/**
	 * 矩阵转置
	 * @param a
	 * @return
	 */
	public static double[][] transpose(double[][] a){
		int n = a.length;
		int m = a[0].length;
		double[][] ret = new double[m][n];
		for (int i = 0; i < m; ++i) {
			for (int j = 0; j < n; ++j) {
				ret[i][j] = a[j][i];
			}
		}
		return ret;
	}
	
	/**
	 * 向量相加得到新向量
	 * @param a
	 * @param b
	 * @return
	 */
	public static double[] add(double[] a, double[] b) {
		double[] ret = new double[a.length];
		for (int i = 0; i < a.length; ++i) ret[i] = a[i] + b[i];
		return ret;
	}
	
	/**
	 * 两个矩阵相加
	 * @param a
	 * @param b
	 * @return
	 */	
	public static double[][] add(double[][] a, double[][] b){
		double[][] ret = new double[a.length][a[0].length];
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < a[0].length; ++j) {
				ret[i][j] = a[i][j] + b[i][j];
			}
		}
		return ret;
	}
	
	
	/**
	 * 两个向量相乘 得到一个新的矩阵
	 * @param a [1, 2, 3]
	 * @param b [1, 1, 1, 1]
	 * @return
	 * [[1, 1, 1, 1]
	 * ,[2, 2, 2, 2]
	 * ,[3, 3, 3, 3]]
	 * 
	 */
	public static double[][] outer(double[] a, double[] b){
		int n = a.length;
		int m = b.length;
		double[][] ret = new double[n][m];
		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < m; ++j) {
				ret[i][j] = a[i] * b[j];
			}
		}
		return ret;
	}
	
	
	/**
	 * 从一个向量中截取一部分 a[l, r)
	 * @param a
	 * @param l
	 * @param r
	 * @return
	 */
	public static double[] dim(double[] a, int l, int r) {
		int len = r - l;
		double[] ret = new double[len];
		for (int i = l; i < r; ++i) {
			ret[i - l] = a[i];
		}
		return ret;
	}
	
	/**
	 * 矩阵w 乘上 向量x 在加上向量 b
	 * @param w
	 * @param x
	 * @param b
	 * @return
	 */			
	public static double[] WtxPlusBias(double[][] w, double[] x, double[] b) {
		int n = w.length;
		double[] ans = new double[n];
		for (int i = 0; i < n; ++i) {
			double wtx = dot(w[i], x);
			ans[i] = wtx + b[i];
		}
		return ans;
	}
	
	/**
	 * 返回一个向量的双曲正切值
	 * @param a
	 * @return
	 */	
	public static double[] tanh(double[] a) {
		double[] b = new double[a.length];
		for (int i = 0; i < a.length; ++i) b[i] = Math.tanh(a[i]);
		return b;
	}
	
	/*======================================================================*/
	
	/**
	 * 滤除小于零的元素
	 * @param dataArray
	 * @return
	 */
	public static double[] filterNegative(double[] dataArray){
		double[] newArray = new double[dataArray.length];
		
		for(int i=0; i<dataArray.length; i++){
			if(dataArray[i]<0){
				newArray[i] = -0.2*dataArray[i];
			}else{
				newArray[i] = dataArray[i];
			}
		}
		
		return newArray;
	}
	
	/**
	 * 标准平方差
	 * @param dataArray
	 * @return
	 */
	public static double stderrData(double[] dataArray)
	{
		return Math.sqrt(varerrData(dataArray));
	}
	
	/**
	 * 平方差函数
	 * @param dataArray
	 * @return
	 */
	public static double varerrData(double[] dataArray)
	{
		double variance=0;
		double avgsumData=avgData(dataArray);
		double[] tempArray = new double[dataArray.length];
		
		for(int i=0 ; i<dataArray.length ; i++){
			
			tempArray[i] = dataArray[i];
			
		}
		
		for(int i=0;i<dataArray.length;i++)
		{
			tempArray[i]-=avgsumData;
			variance+=tempArray[i]*tempArray[i];
		}
		return variance/dataArray.length;//variance error;
	}
	
	/**
	 * 对数据从大到小进行排序
	 * @param dataArray
	 * @return
	 */
	public static double[] sorts(double[] dataArray){
		
		double[] sortsArray = new double[dataArray.length];
		
		for(int i=0 ; i<dataArray.length ; i++){
			
			sortsArray[i] = dataArray[i];
			
		}		
		
		double temp = 0;
		
		for(int i=0 ; i<dataArray.length-1 ; i++){
			
			for(int j=i+1 ; j<dataArray.length ; j++){
				
				if(sortsArray[i]<sortsArray[j]){
					
					temp = sortsArray[i];
					sortsArray[i] = sortsArray[j];
					sortsArray[j] = temp;
					
				}
			}
			
		}
		
		return sortsArray;
	}
	
	public static int[] sortInt(int[] dataArray){
		
		int[] sortsArray = new int[dataArray.length];
		
		for(int i=0 ; i<dataArray.length ; i++){
			
			sortsArray[i] = dataArray[i];
			
		}		
		
		int temp = 0;
		
		for(int i=0 ; i<dataArray.length-1 ; i++){
			
			for(int j=i+1 ; j<dataArray.length ; j++){
				
				if(sortsArray[i]<sortsArray[j]){
					
					temp = sortsArray[i];
					sortsArray[i] = sortsArray[j];
					sortsArray[j] = temp;
					
				}
			}
			
		}
		
		return sortsArray;
	}
	
	/**
	 * 产生一个均值为a方差为b的高斯随机数
	 * @param a
	 * @param b
	 * @return
	 */
	public static double gaussData(double a, double b){
		Random random = new Random();
		return Math.sqrt(b)*random.nextGaussian()+a;
	}
	
	/**
	 * 最大值函数
	 * @param dataArray
	 * @return
	 */
	public static double maxData(double[] dataArray){
		
		double max = dataArray[0];
		for(int i=1 ; i<dataArray.length ; i++){
			if(max<dataArray[i])
				max = dataArray[i];
		}
		return max;
		
	}
	
	/**
	 * 最小值函数
	 * @param dataArray
	 * @return
	 */
	public static double minData(double[] dataArray){
		
		double min = dataArray[0];
		for(int i=1 ; i<dataArray.length ; i++){
			if(min>dataArray[i])
				min = dataArray[i];
		}
		return min;
		
	}
	
	public static int minIntData(int[] dataArray){
		
		int min = dataArray[0];
		for(int i=1 ; i<dataArray.length ; i++){
			if(min>dataArray[i])
				min = dataArray[i];
		}
		return min;
		
	}
	
	/**
	 * 平均值
	 * @param dataArray
	 * @return
	 */
	public static double avgData(double[] dataArray)
	{
		return sumData(dataArray)/dataArray.length;
	}
	
	/**
	 * 和函数
	 * @param dataArray
	 * @return
	 */
	public static double sumData(double[] dataArray)
	{
		double sumData=0;
		for(int i=0;i<dataArray.length;i++)
		{
			sumData+=dataArray[i];
		}
		return sumData;
	}
	
	public static int sumIntData(int[] dataArray)
	{
		int sumData=0;
		for(int i=0;i<dataArray.length;i++)
		{
			sumData+=dataArray[i];
		}
		return sumData;
	}
		
	/**
	 * 最小二乘法系数矩阵x
	 * @param dataArray
	 * @param p
	 * @return x[n-p][p] 系数矩阵，从第p+1个元素开始，依次往后顺延
	 */
	public static double[][] xMatrix(double[] dataArray, int p){
		
		int n = dataArray.length;
		double[][] X = new double[n-p][p];
		for(int i=0;i<n-p;i++){
			for(int j=0;j<p;j++){
				X[i][j]=dataArray[i+j];
			}
		}
		return X;
		
	}
	
 	/**
 	 * 最小二乘法系数矩阵y
 	 * @param dataArray
 	 * @param p
 	 * @return y[n-p][1], 返回矩阵形式系数，方便在递归运算时候求矩阵的转置
 	 */
 	public static double[][] yMatrix(double[] dataArray, int p){
 		
 		int n = dataArray.length;
 		double[][] y = new double[n-p][1];
 		for(int i=0;i<n-p;i++){
 			y[i][0]=dataArray[i+p];
 		}
 		return y;
 		
 	}
			 			
}
