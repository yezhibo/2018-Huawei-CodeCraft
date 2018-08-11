package com.elasticcloudservice.predict;
import com.elasticcloudservice.dataTool.*;

/**
 * 
 * @ProjectName ecsTest
 * @author Yezhibo
 * @CreatTime 2018年4月9日下午9:03:22
 */
public class GAPredict {

	/**
	 * 初始化种群
	 * @param dataArray
	 * @param p
	 * @param n
	 * @return
	 */
	public static double[][] InitPopulation(double[] dataArray, int p, int n){
		double[][] wPopulation = new double[n][p];
		//double e = 1;
		/*double[] array = GradientDescent(dataArray, p);
		for(int i=0; i<p; i++){
			wPopulation[0][i] = array[i];
		}*/
		for(int i=1; i<n; i++){
			for(int j=0; j<p; j++){   //随机产生初始种群
				wPopulation[i][j] = (Math.random()*0.02-0.01);
			}
			//e *= 0.5;
			
			//double[] varray = NonuniformVariation(array, 10, 10);
			/*for(int j=0; j<p; j++){
				double r = Math.random()*2-1;
				wPopulation[i][j] = array[j]*r;
			}*/
			 
		}
		
		return wPopulation;
	}
	
	
	/**
	 * 适应度计算函数
	 * @param population
	 * @param dataArray
	 * @return
	 */
	public static double[] Getfitness(double[][] population, double[] dataArray){
		int n = population.length;
		int p = population[0].length;
		double[] fitArray = new double[n];
		double[][] X = DataMath.xMatrix(dataArray, p);		
		double[][] Y = DataMath.yMatrix(dataArray, p);
		
		for(int i=0; i<n; i++){  //计算种群内每个个体的适应度值
			
			double sum =0;
			for(int j=0; j<X.length; j++){ //对于每个个体计算n-p个预测值与实际值之间的误差
				
				double y1 = 0;
				for(int k=0; k<p; k++){   //计算预测值
					y1 += population[i][k]*X[j][k];
				}
				sum += (Y[j][0]-y1)*(Y[j][0]-y1);
				
			}
			fitArray[i] = 1/(sum+0.0001);  //误差值越小代表适应度越高
			
		}	
		
		return fitArray;
	}

	/**
	 * 轮盘赌选择算子
	 * @param population
	 * @param fitArray
	 * @return
	 */
	public static double[] ChoosenParent(double[][] population, double[] fitArray){
		
		int p = population[0].length;
		double[] parent = new double[p];
		
		double[] refitArray = new double[fitArray.length];
		double sum = DataMath.sumData(fitArray);
		
		for(int i=0; i<fitArray.length; i++){  //计算个体的相对适应度值
			refitArray[i] = (double)fitArray[i]/sum;
		}
		
		double r = Math.random();
		double sumFit = 0;
		for(int i=0; i<refitArray.length; i++){
			
			sumFit += refitArray[i];
			if(sumFit > r){      //选择第i个个体作为下一代的父代
				for(int j=0; j<p; j++){
					parent[j] = population[i][j];
				}
				break;
			}
		}
		
		return parent;
		
	}
	
	/**
	 * 中间交叉算子1
	 * @param parent1
	 * @param parent2
	 * @return
	 */
	public static double[] CrossVariation1(double[] parent1, double[] parent2){
		double[] child1 = new double[parent1.length];
		
		for(int i=0; i<parent1.length; i++){
			
			double r = Math.random();
			child1[i] = parent2[i] + r*(parent1[i]-parent2[i]);
			
		}
				
		return child1;
	}
	
	/**
	 * 中间交叉算子2
	 * @param parent1
	 * @param parent2
	 * @return
	 */
	public static double[] CrossVariation2(double[] parent1, double[] parent2){
		double[] child2 = new double[parent1.length];
		
		for(int i=0; i<parent1.length; i++){
			
			double r = Math.random();
			child2[i] = parent1[i] + r*(parent2[i]-parent1[i]);
			
		}
				
		return child2;
	}
	
	/**
	 * 非均匀变异
	 * @param parent
	 * @param cg
	 * @param gmax
	 * @return
	 */
	public static double[] NonuniformVariation(double[] parent, int cg, int gmax){
		
		double[] child = new double[parent.length];
	    double r = Math.random();
	    double s = Math.random()*2;
	    int sign = (r>=0.5) ? 0 : 1;
	    
	    for(int i=0; i<parent.length; i++){
	    	double dis = 0;
	    	double gd = 0;
	    	if(sign==0){
	    		
	    		dis = 1-parent[i];
	    		gd = dis*Math.pow(r*(1-(double)cg/gmax), s);
	    		child[i] = parent[i] + gd;
	    		
	    	}else{
	    		
	    		dis = parent[i]+1;
	    		gd = dis*Math.pow(r*(1-(double)cg/gmax), s);
	    		child[i] = parent[i] - gd;
	    		
	    	}	    		    	
	    }
		
		return child;
		
	}
	
	
	/**
	 * 遗传算法引擎
	 * @param dataArray
	 * @param n       //初始种群的规模大小
	 * @param p       //给定阶数p
	 * @param gmax      //最大的遗传代数
	 * @param maxFitness //最大的适应度值
	 * @return
	 */
	public static double[] GenAlg(double[] dataArray, int n, int p, int gmax, double maxFitness){
		
		double[] optW = new double[p];  
		
		double[][] parentPopulation = InitPopulation(dataArray, p, n);     //产生初始化种群
				
		double[] parentFitArray = Getfitness(parentPopulation, dataArray);  //父代的适应度值
		
		boolean end = false;
		
		int cg = 1;
		
		while(!end){  //开始遗传迭代
			
			/**首先对种群进行交叉操作产生下一代种群**/
			double[][] childPopulation = new double[n][p];
			int l = 0;
			int o = 0;
			if(n%2==0){  //选择父代最优的个体直接复制到子代
				o = 2;
				double[][] optPopulation = GetOptPopulation(parentPopulation, parentFitArray, 2);
				for(int i=0; i<2; i++){
					for(int j=0; j<p; j++){
						childPopulation[l][j] = optPopulation[i][j];
					}
					l++;
				}
			}else{
				o = 3;
				double[][] optPopulation = GetOptPopulation(parentPopulation, parentFitArray, 3);
				for(int i=0; i<3; i++){
					for(int j=0; j<p; j++){
						childPopulation[l][j] = optPopulation[i][j];
					}
					l++;
				}
			}
			int t = (n-o)/2;
			for(int i=1; i<t; i++){  //从父代中选出t对个体交叉产生子代
				double[] parent1 = ChoosenParent(parentPopulation, parentFitArray);
				double[] parent2 = ChoosenParent(parentPopulation, parentFitArray);
				double[] child1 = CrossVariation1(parent1, parent2);
				double[] child2 = CrossVariation2(parent1, parent2);
				for(int j=0; j<p; j++){
					childPopulation[l][j] = child1[j];
					childPopulation[l+1][j] = child2[j];
				}
				l = l + 2;
			}
			
			for(int i=o; i<n; i++){  //对种群内交叉产生的子代进行变异操作				
				double[] child = NonuniformVariation(childPopulation[i], cg, gmax);
				for(int j=0; j<p; j++){
					childPopulation[i][j] = child[j];
				}				
			}
			
			double[] childFitArray = Getfitness(childPopulation, dataArray);
			
			parentPopulation = GetOptPopulation(childPopulation, childFitArray, n);
			
			//System.out.println("第"+cg+"代的最大适应度为："+childFitArray[0]);
			
			if(cg>=gmax || childFitArray[0]>=maxFitness){
				
				for(int i=0; i<p; i++){
					optW[i] = parentPopulation[0][i];
				}
				//System.out.println("最优适应度值："+childFitArray[0]);
				end = true;
			}
			
			/*if(cg%50==0){
				maxFitness *= 0.7;
			}*/
			
			cg++;
			for(int i=0; i<n; i++){
				parentFitArray[i] = childFitArray[i];
			}
		}
		
		return optW;
		
	}
	
	/**
	 * 根据种群适应度值选取最优的n个个体
	 * @param population
	 * @param fitArray
	 * @param n
	 * @return
	 */
	public static double[][] GetOptPopulation(double[][] population, double[] fitArray, int n){
		
		int k = population[0].length;
		double[][] optPopulation = new double[n][k];
		
		/**根据fitArray从大到小排序**/
		double tempf = 0;
		double tempP = 0;
		for(int i=0; i<fitArray.length-1; i++){
			for(int j=i+1; j<fitArray.length; j++){
				if(fitArray[i]<fitArray[j]){
					
					tempf = fitArray[i];
					fitArray[i] = fitArray[j];
					fitArray[j] = tempf;
					
					for(int t=0; t<k; t++){
						tempP = population[i][t];
						population[i][t] = population[j][t];
						population[j][t] = tempP;
					}
					
				}
			}
		}
		
		for(int i=0; i<n; i++){
			for(int j=0; j<k; j++){
				optPopulation[i][j] = population[i][j];
			}
		}
		
		return optPopulation;
	}
	
	
	/*public static void main(String[] args) {
		double[] dataArray = {3,	7,	1,	2,	2,	0,	4};
	
		//double[][] p = InitPopulation(dataArray, 3, 10);
		
		//double[] fit = Getfitness(p, dataArray);
		
		//double[] parent = ChoosenParent(p, fit);
		
		double[] w = GenAlg(dataArray, 10, 3, 500, 0.12);
		
		System.out.println(Arrays.toString(w));
	}*/
}
