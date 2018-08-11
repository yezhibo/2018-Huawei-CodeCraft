package com.elasticcloudservice.distribute;

import com.elasticcloudservice.datapreprocess.*;
import com.filetool.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 遗传算法装箱类
 * @ProjectName AR80
 * @author Yezhibo
 * @CreatTime 2018年4月15日上午10:02:57
 */
public class GA2Distribute {
		
	/**
	 * 初始化种群
	 * @param person 初始化个体
	 * @param n     种群规模
	 * @return
	 */
 	public static int[][] InitPopulation(int[] person, int n){
		
		int k = person.length;  //虚拟机的总数	
		
		int[] sortPerson = DataMath.sortInt(person);
		
		int g = sortPerson[0]/3;  //将排序后的个体分为三组，方便产生初始种群
		
		List<Integer> group1 =new ArrayList<Integer>();
		List<Integer> group2 =new ArrayList<Integer>();
		List<Integer> group3 =new ArrayList<Integer>();
		
		for(int i=0; i<k; i++){
			
			if(sortPerson[i]>sortPerson[0]-g && sortPerson[i]<=sortPerson[0]){
				
				group1.add(sortPerson[i]);
				
			}else if(sortPerson[i]>sortPerson[0]-2*g && sortPerson[i]<=sortPerson[0]-g){
				
				group2.add(sortPerson[i]);
				
			}else{
				
				group3.add(sortPerson[i]);
				
			}
			
		}
		
		int[][] Population = new int[n][k]; //存放种群个体，每行代表一个个体
		
		/**======================开始产生初始种群=========================**/
		
		for(int i=0; i<k; i++){			   //第一个个体存放原始排序序列
			Population[0][i] = sortPerson[i];			
		}
		
		for(int i=1; i<n; i++){
			
			double[] r1 = new double[group1.size()];
			for(int j=0; j<group1.size(); j++){
				r1[j] = Math.random();  //产生一个0~1的随机数
			}
			int tempp1 = 0;  
			double tempr1 = 0;
			for(int j=0; j<group1.size()-1; j++){  //按照产生的随机数对第一组数进行排序
				for(int l=j+1; l<group1.size(); l++){
					if(r1[j]<r1[l]){
						tempr1 = r1[j];
						r1[j] = r1[l];
						r1[l] = tempr1;
						
						tempp1 = group1.get(j);
						group1.set(j, group1.get(l));
						group1.set(l, tempp1);
					}
				}
			}
			
			double[] r2 = new double[group2.size()];
			for(int j=0; j<group2.size(); j++){
				r2[j] = Math.random();  //产生一个0~1的随机数
			}
			int tempp2 = 0;  
			double tempr2 = 0;
			for(int j=0; j<group2.size()-1; j++){  //按照产生的随机数对第二组数进行排序
				for(int l=j+1; l<group2.size(); l++){
					if(r2[j]<r2[l]){
						tempr2 = r2[j];
						r2[j] = r2[l];
						r2[l] = tempr2;
						
						tempp2 = group2.get(j);
						group2.set(j, group2.get(l));
						group2.set(l, tempp2);
					}
				}
			}
			
			double[] r3 = new double[group3.size()];
			for(int j=0; j<group3.size(); j++){
				r3[j] = Math.random();  //产生一个0~1的随机数
			}
			int tempp3 = 0;  
			double tempr3 = 0;
			for(int j=0; j<group3.size()-1; j++){  //按照产生的随机数对第一组数进行排序
				for(int l=j+1; l<group3.size(); l++){
					if(r3[j]<r3[l]){
						tempr3 = r3[j];
						r3[j] = r3[l];
						r3[l] = tempr3;
						
						tempp3 = group3.get(j);
						group3.set(j, group3.get(l));
						group3.set(l, tempp3);
					}
				}
			}
			
			for(int j=0; j<k; j++){        //将三组数据装入种群中
				
				if(j<group1.size()){//先放第一组数据
					
					Population[i][j] = group1.get(j);
					
				}else if(j-group1.size()<group2.size()){// 再放第二组数据
					
					Population[i][j] = group2.get(j-group1.size());
					
				}else{
					
					Population[i][j] = group3.get(j-group1.size()-group2.size());
					
				}
				
			}
			
		}
						
		return Population;
	}
	
	/**
	 * 适应度计算函数  个体最终所需的服务器个数
	 * @param population
	 * @param nCPU
	 * @param nMEM
	 * @return
	 */
	public static double[] GetFitness(int[][] population, int nCPU, int nMEM, String strDim){
		
		int n = population.length;
		int k = population[0].length;
		int[][] tempP = new int[n][k];
		double[] fitArray = new double[n];
		for(int i=0; i<n; i++){
			for(int j=0; j<k; j++){
				tempP[i][j] = population[i][j];
			}
		}
		
		/**=================开始计算种群内个体的适应值====================**/
		for(int i=0; i<n; i++){
			
			List<Integer> rhCpu = new ArrayList<Integer>();
			List<Integer> rhMem = new ArrayList<Integer>();
			
			rhCpu.add(nCPU);
			rhMem.add(nMEM);
			
			int sum = 0;
			for(int j=0; j<k; j++){
				sum += tempP[i][j];
			}
			int s=0;
			
			/*将虚拟机放入服务器中*/
			while(sum>0){
				sum =0;
				for(int l=0; l<k; l++){					
					int fcpu = Getcpu(tempP[i][l]);
					int fmem = Getmem(tempP[i][l]);					
					if(tempP[i][l]>0 && fcpu<=rhCpu.get(s) && fmem<=rhMem.get(s)){						
						tempP[i][l] = 0;
						rhCpu.set(s, rhCpu.get(s)-fcpu);
						rhMem.set(s, rhMem.get(s)-fmem);						
					}										
				}
				
				for(int l=0; l<k; l++){
					sum += tempP[i][l];
				}
				
				if(sum>0){
					s += 1;
					rhCpu.add(nCPU);
					rhMem.add(nMEM);
				}								
			}
			
			if(strDim.equals("CPU")){
				double lastcpurate = 1 - (double)rhCpu.get(rhCpu.size()-1)/nCPU;
				fitArray[i] = 1.0/(s+lastcpurate);
			}else{
				double lastmemrate = 1 - (double)rhMem.get(rhMem.size()-1)/nMEM;
				fitArray[i] = 1.0/(s+lastmemrate);
			}		
		}
		
		return fitArray;
		
	}

	/**
	 * 选择算子,适应度函数越高的别选择的概率越大
	 * @param population
	 * @param fitArray
	 * @return
	 */
	public static int[] ChosenParent(int[][] population, double[] fitArray){
		int k = population[0].length;
		int[] parent = new int[k];
		double[] refitArray = new double[fitArray.length];
		double sum = DataMath.sumData(fitArray);
		
		for(int i=0; i<fitArray.length; i++){  //计算个体的相对适应度值
			refitArray[i] = (double)fitArray[i]/sum;
		}
		
		double r = Math.random();
		double sumFitR = 0;
		for(int i=0; i<refitArray.length; i++){
			
			sumFitR += refitArray[i];
			//double sumFitL = sumFitR - refitArray[i];
			
			if(/*r>sumFitL &&*/ r<sumFitR){      //选择第i个个体作为下一代的父代
				for(int j=0; j<k; j++){
					parent[j] = population[i][j];
				}
				break;
			}
		}
		
		
		return parent;
	}
	
	/**
	 * 变异算子
	 * @param parent
	 * @return
	 */
	public static int[] GetChild(int[] parent){
		int k = parent.length;
		int[] child = new int[k];
		int o = (int)(Math.random()*3+1);
		
		/**单点基因移位变异**/
		/**
		 * 随机产生两个正整数，将两个数之间的基因依次后移，并把最后一个放到最前面
		 */
		if(o==1){
			
			int v = (int)(Math.random()*k);
			int u = (int)(Math.random()*k);
			
			if(v>u){                //保证v放的是小的一个随机数
				int temp = v;
				v = u;
				u = temp;
			}
			
			for(int i=0; i<v; i++){   //0~v部分数据直接赋值
				child[i] = parent[i];
			}
			child[v] = parent[u];
			
			for(int i=v+1; i<=u; i++){ //v~u之间的数据后移一位
				child[i] = parent[i-1];
			}
			
			for(int i=u+1; i<k; i++){  //u之后的数据直接赋值
				child[i] = parent[i];
			}
			
		/**单点基因倒位变异**/
		/**
		 * 将位于变异点之间的子串中的基因按照逆序排列
		 */
		}else if(o==2){
			
			int v = (int)(Math.random()*k);
			int u = (int)(Math.random()*k);
			if(v>u){                //保证v放的是小的一个随机数
				int temp = v;
				v = u;
				u = temp;
			}
			
			for(int i=0; i<v; i++){   //0~v部分数据直接赋值
				child[i] = parent[i];
			}
			int temp = u;
			for(int i=v; i<=u; i++){   //将v~u之间的数据倒序
				child[i] = parent[temp--];
			}
			
			for(int i=u+1; i<k; i++){  //u之后的数据直接赋值
				child[i] = parent[i];
			}
			
		/**多点基因换位变异**/	
		}else{
			
			int u = k/2;
			int p = (int)(Math.random()*u+1);
			int[] exc = new int[2*p];
			
			for(int i=0; i<2*p; i++){       //产生p对随机数
				exc[i] = (int)(Math.random()*k);
			}
			
			for(int i=0; i<k; i++){
				child[i] = parent[i];
			}
			
			int temp =0;
			for(int i=0; i<p; i++){ //开始交换
				
				temp = child[exc[i]];
				child[exc[i]] = child[exc[i+p]];
				child[exc[i+p]] = temp;
				
			}
		}
		
		return child;
	}
	
	/**
	 * 遗传算法引擎
	 * @param person
	 * @param n
	 * @param nCPU
	 * @param nMEM
	 * @return
	 */
	public static int[] GenAlg(int[] person, int n, int nCPU, int nMEM, double maxFitness, int maxEpoch, String strDim){
		int k = person.length;
		int[] optPerson = new int[k];
		
		/**1.生成初始种群**/
		int[][] parentPopulation = InitPopulation(person, n);
		
		/**2.开始遗传变异**/
		boolean end = false; //判断是否结束
		int s = 0;           //遗传的代数
		int[][] tempPopulation = new int[n+n/2][k];
		double[] tempFitArray = new double[n+n/2];
		while(!end){
			
			double[] parentfitArray = GetFitness(parentPopulation, nCPU, nMEM, strDim);			
			
			int[][] tempChildPopulation = new int[n/2][k];
			for(int t=0; t<n/2; t++){
				int[] parent = ChosenParent(parentPopulation, parentfitArray);
				int[] child = GetChild(parent);
				for(int i=0; i<k; i++){
					tempChildPopulation[t][i] = child[i];
				}
			}
			double[] tempChildFitArray = GetFitness(tempChildPopulation, nCPU, nMEM, strDim);
			
			
			for(int i=0; i<n+n/2; i++){
				if(i<n){  //复制父代适应度和种群
					for(int j=0; j<k; j++){
						tempPopulation[i][j] = parentPopulation[i][j];						
					}
					tempFitArray[i] = parentfitArray[i];
				}else{   //复制新产生子代种群和适应度
					for(int j=0; j<k; j++){
						tempPopulation[i][j] = tempChildPopulation[i-n][j];						
					}
					tempFitArray[i] = tempChildFitArray[i-n];
				}
			}
			
			parentPopulation = GetOptPopulation(tempPopulation, tempFitArray, n);
			
			s++;
			double realfitness = 1.0/Math.ceil(1.0/tempFitArray[0]);
			//System.out.println("第"+s+"代的最优个数为："+1.0/realfitness);
						
			if(s>=maxEpoch || realfitness>=maxFitness){
				System.out.println("最优服务器个数："+1.0/realfitness);
				end = true;
			}
			
		}
		
		for(int i=0; i<k; i++){
			optPerson[i] = tempPopulation[0][i];
		}
		
		return optPerson;
	}
	
	/**
	 * 根据种群适应度值选取最优的n个个体
	 * @param population
	 * @param fitArray
	 * @param n
	 * @return
	 */
	public static int[][] GetOptPopulation(int[][] population, double[] fitArray, int n){
		
		int k = population[0].length;
		int[][] optPopulation = new int[n][k];
		
		/**根据fitArray从大到小排序**/
		double tempf = 0;
		int tempP = 0;
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
	
	/**
	 * 根据服务器下标获取cpu
	 * @param i
	 * @return
	 */
	public static int Getcpu(int i){
		int cpu =0;
		if(i>=1 && i<=3)
			cpu = 1;
		if(i>=4 && i<=6)
			cpu = 2;
		if(i>=7 && i<=9)
			cpu = 4;
		if(i>=10 && i<=12)
			cpu = 8;
		if(i>=13 && i<=15)
			cpu = 16;
		return cpu;
	}
	
	/**
	 * 根据服务器下标获取mem
	 * @param i
	 * @return
	 */
	public static int Getmem(int i){
		int mem =0;
		if(i==1)
			mem = 1;
		if(i==2 || i==4)
			mem = 2;
		if(i==3 || i==5 || i==7)
			mem = 4;
		if(i==6 || i==8 || i==10)
			mem = 8;
		if(i==9 || i==11 || i==13)
			mem = 16;
		if(i==12 || i==14)
			mem = 32;
		if(i==15)
			mem = 64;
		
		return mem;
	}
	
	/**
 	 * 得到初始个体信息
 	 * @param fName
 	 * @return
 	 */
 	public static int[] getInitialPerson(String[] fName, int[] fData){
 		
 		int k = 0;  //虚拟机的总数
		for(int i=0; i<fData.length; i++){
			k += fData[i];
		}
 		
 		int[] person = new int[k];
		int p=0;
		for(int i=0 ; i<fName.length ; i++){  //求出预测虚拟机的规格，分别存放在两个数组中
			
			if(fName[i].equals("flavor1")){
				for(int j=0; j<fData[i]; j++){
					person[p++] = 1;
				}
			}
			if(fName[i].equals("flavor2")){
				for(int j=0; j<fData[i]; j++){
					person[p++] = 2;
				}
			}
			if(fName[i].equals("flavor3")){
				for(int j=0; j<fData[i]; j++){
					person[p++] = 3;
				}
			}
			if(fName[i].equals("flavor4")){
				for(int j=0; j<fData[i]; j++){
					person[p++] = 4;
				}
			}
			if(fName[i].equals("flavor5")){
				for(int j=0; j<fData[i]; j++){
					person[p++] = 5;
				}
			}
			if(fName[i].equals("flavor6")){
				for(int j=0; j<fData[i]; j++){
					person[p++] = 6;
				}
			}
			if(fName[i].equals("flavor7")){
				for(int j=0; j<fData[i]; j++){
					person[p++] = 7;
				}
			}
			if(fName[i].equals("flavor8")){
				for(int j=0; j<fData[i]; j++){
					person[p++] = 8;
				}
			}
			if(fName[i].equals("flavor9")){
				for(int j=0; j<fData[i]; j++){
					person[p++] = 9;
				}
			}
			if(fName[i].equals("flavor10")){
				for(int j=0; j<fData[i]; j++){
					person[p++] = 10;
				}
			}
			if(fName[i].equals("flavor11")){
				for(int j=0; j<fData[i]; j++){
					person[p++] = 11;
				}
			}
			if(fName[i].equals("flavor12")){
				for(int j=0; j<fData[i]; j++){
					person[p++] = 12;
				}
			}
			if(fName[i].equals("flavor13")){
				for(int j=0; j<fData[i]; j++){
					person[p++] = 13;
				}
			}
			if(fName[i].equals("flavor14")){
				for(int j=0; j<fData[i]; j++){
					person[p++] = 14;
				}
			}
			if(fName[i].equals("flavor15")){
				for(int j=0; j<fData[i]; j++){
					person[p++] = 15;
				}
			}			
		}
		return person;
 		
 	}

 	/**
 	 * 得到每个服务器上的虚拟机名称
 	 * @param person
 	 * @param nCPU
 	 * @param nMEM
 	 * @return
 	 */
 	public static List<String> GetsName(int[] person, int nCPU, int nMEM){
 		
 		int k = person.length;
 		
 		List<Integer> rhCpu = new ArrayList<Integer>();
		List<Integer> rhMem = new ArrayList<Integer>();
		List<String> sName = new ArrayList<String>();
		rhCpu.add(nCPU);
		rhMem.add(nMEM);
		
		int sum = 0;
		for(int j=0; j<k; j++){
			sum += person[j];
		}
		int s=0;
		
		while(sum>0){
			sum =0;
			String str ="";
			for(int l=0; l<k; l++){					
				int fcpu = Getcpu(person[l]);
				int fmem = Getmem(person[l]);					
				if(person[l]>0 && fcpu<=rhCpu.get(s) && fmem<=rhMem.get(s)){	
					String fName = GetFName(person[l]);
					str += fName+" ";
					person[l] = 0;
					rhCpu.set(s, rhCpu.get(s)-fcpu);
					rhMem.set(s, rhMem.get(s)-fmem);					
				}										
			}
			
			for(int l=0; l<k; l++){
				sum += person[l];
			}
			
			if(sum>0){
				s += 1;
				rhCpu.add(nCPU);
				rhMem.add(nMEM);
			}
			
			sName.add(str);
		}
 		
		return sName;
		
 	}
 	
 	/**
 	 * 将每个服务器中的字符串信息合并成字符加数字形式
 	 * @param sName
 	 * @return
 	 */
 	public static String[] getResult(List<String> sName){
 		
 		int num = sName.size()+1;
		String[] resultS = new String[num];
		resultS[0]=Integer.toString(sName.size());
				
		for(int i=0 ; i<sName.size() ; i++){			
			
			String[] array = sName.get(i).split(" ");
			String str = "";
			String temp = array[0];			
			int fRemain = array.length;
			while(fRemain>0){
				
				fRemain = 0;
				int n=0;
				for(int j=0 ; j<array.length ; j++){								
					if(array[j].equals(temp)){															
						n++;
						array[j] = " ";  //将遍历过的赋值为空
					}				
				}
				str += " "+ temp + " " + Integer.toString(n); 
				
				for(int j=0; j<array.length; j++){
					if(!array[j].equals(" "))
						fRemain++;
				}
				
				if(fRemain>0){					
					for(int j=0; j<array.length; j++){
						if(!array[j].equals(" ")){
							temp=array[j];
							break;
						}
					}					
				}				
			}
									
			resultS[i+1] =Integer.toString(i+1)+str/*+"  CPU利用率："+Double.toString(sRatio.get(i))*/;
		}
		
		return resultS;
 	}
 	
 	/**
 	 * 根据虚拟机的下标获取虚拟机的名字
 	 * @param i
 	 * @return
 	 */
 	public static String GetFName(int i){
 		
 		String fName = "";
 		if(i==1){
 			fName = "flavor1";
 		}
 		if(i==2){
 			fName = "flavor2";
 		}
 		if(i==3){
 			fName = "flavor3";
 		}
 		if(i==4){
 			fName = "flavor4";
 		}
 		if(i==5){
 			fName = "flavor5";
 		}
 		if(i==6){
 			fName = "flavor6";
 		}
 		if(i==7){
 			fName = "flavor7";
 		}
 		if(i==8){
 			fName = "flavor8";
 		}
 		if(i==9){
 			fName = "flavor9";
 		}
 		if(i==10){
 			fName = "flavor10";
 		}
 		if(i==11){
 			fName = "flavor11";
 		}
 		if(i==12){
 			fName = "flavor12";
 		}
 		if(i==13){
 			fName = "flavor13";
 		}
 		if(i==14){
 			fName = "flavor14";
 		}
 		if(i==15){
 			fName = "flavor15";
 		}
 		
 		return fName;
 	}
 	
 	
 	/**
 	 * 分配函数
 	 * @param fName
 	 * @param fData
 	 * @param nCPU
 	 * @param nMEM
 	 * @param n
 	 * @param minFitness
 	 * @param maxEpoch
 	 * @return
 	 */
 	public static String[] Distribute(String[] fName, int[] fData, int nCPU, int nMEM, String strDim){
 				
 		int[] person = getInitialPerson(fName, fData);
 		
 		int fsumCPU = 0, fsumMEM = 0;
 		
 		for(int i=0; i<person.length; i++){
 			fsumCPU += Getcpu(person[i]);
 			fsumMEM += Getmem(person[i]);
 		}
 		
 		double maxFitness = 1.0/Math.max((int)(Math.ceil((double)fsumCPU/nCPU)), (int)(Math.ceil((double)fsumMEM/nMEM)));
 		
 		int optNum = Math.max((int)(Math.ceil((double)fsumCPU/nCPU)), (int)(Math.ceil((double)fsumMEM/nMEM)));
 		
 		int[] optPerson = GenAlg(person, 10, nCPU, nMEM, maxFitness, 200, strDim);
 		
 		List<String> sName = GetsName(optPerson, nCPU, nMEM);
 		
 		
 		
 		double cpuratio = (double)fsumCPU/(sName.size()*nCPU);
 		double memratio = (double)fsumMEM/(sName.size()*nMEM);
 		System.out.println("最少的服务器个数："+optNum);
 		System.out.println("CPU利用率："+cpuratio);
 		System.out.println("MEM利用率："+memratio);
 		return getResult(sName);
 	}
 	
 	/**
 	 * 测试函数
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		LogUtil.printLog("Begin");
 		
		String[] fName={"flavor1","flavor2","flavor3","flavor4","flavor5","flavor6","flavor7","flavor8","flavor9","flavor10","flavor11","flavor12","flavor13","flavor14","flavor15"};
		
		//String[] fName={"flavor1","flavor3","flavor5","flavor7","flavor9","flavor11","flavor13","flavor15"};
		
		//int[] case6 ={44,121,22,187,17,41,67,193};
		
		int[] case5={199,130,95,177,59,62,158,119,108,4,171,185,149,9,87};
		
		//int[] case4={8,41,15,63,26,6,12,12,97,76,20,99,92,30,3};
		
		//int[] case3={8,49,44,11,29,2,10,17,48,42,42,12,27,12,20};
		
		//int[] case2={23,17,24,26,24,9,20,3,12,9,4,1,22,13,6};
		
		//int[] case1={8,3,2,3,9,5,2,7,9,5,4,9,3,4,0};
		
		String[] result = Distribute(fName, case5, 60, 150, "CPU");
		for(int i=0; i<result.length; i++){
			System.out.println(result[i]);
		}
		
					
		LogUtil.printLog("End");
 	}
 	
}
