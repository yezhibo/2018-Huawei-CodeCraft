package com.elasticcloudservice.distribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.elasticcloudservice.dataEntity.Flavor;
import com.elasticcloudservice.dataEntity.InputContent;
import com.elasticcloudservice.dataEntity.Server;
import com.elasticcloudservice.dataTool.DataMath;
import com.filetool.util.FileUtil;
import com.filetool.util.LogUtil;

/**
 * @ProjectName ecsFuSaiTest
 * @author Yezhibo
 * @CreatTime 2018年4月18日上午11:18:34
 */
public class GADistribute {
	
	public String[] resultD;                   //最终输出结果
	public List<Flavor> fList;                 //输入虚拟机集合
	public List<Server> sList;                 //输入服务器集合
	public List<Flavor> optPerson;             //遗传变异后选出的最优个体
	public List<Server> optServerList;         //最优个体对应的放置结果
	public int[] fDataArray;				   //根据放置结果修改的预测结果
	
	/**
	 * 根据输入预测结果设置预测结果成员变量
	 * @param dataArray
	 */
	public void SetfDataArray(int[] dataArray){
		int n = dataArray.length;
		this.fDataArray = new int[n];
		for(int i=0; i<n; i++){
			this.fDataArray[i] = dataArray[i];
		}
	}
	
	/**
	 * 将最后一个服务器预测的虚拟机删除
	 * @param lastFList   最后一个服务器的虚拟机集合
	 */
	public void ClearLastServer(Server server){
		
		List<Flavor> lastFList = server.flist;
		int deletfNum = lastFList.size();
		int inputfNum = this.fList.size();
		
		for(int i=0; i<deletfNum; i++){           //开始遍历要删除的虚拟机，并从原始预测数据中删除
			String name = lastFList.get(i).name;
			for(int j=0; j<inputfNum; j++){
				if(this.fList.get(j).name.equals(name)){
					this.fDataArray[j]--;
					break;
				}
			}
		}
		
		/*将删除的虚拟机从最优个体中删除*/
		int n = this.optPerson.size();
		for(int i=n-1; i>n-deletfNum-1; i--){
			this.optPerson.remove(i);
		}	
	}

	
	/**
	 * 对虚拟机集合按cpu从大到小排序
	 * @param sortFlavor
	 */
	public void SortFlavor(List<Flavor> sortFlavor){
		int fNum = sortFlavor.size(); 
		for(int i=0; i<fNum-1; i++){
			for(int j=i+1; j<fNum; j++){
				if(sortFlavor.get(i).cpu<sortFlavor.get(j).cpu){   //按cpu或者mem排序最后结果都是一样的
					Flavor flavor = sortFlavor.get(i);
					sortFlavor.set(i, sortFlavor.get(j));
					sortFlavor.set(j, flavor);
				}
			}
		}
	}
	
	
	/**
	 * 将最后一个服务器补满
	 * @param lastFlist 最后一个服务器的虚拟机集合
	 */
	public void FillLastServer(Server server){
		
		/*1.先得到能放置的虚拟机的集合，并按大小进行排序*/
		int fNum = this.fList.size();                      //得到输入虚拟机集合
		List<Flavor> sortFlavor = new ArrayList<Flavor>();
		sortFlavor.addAll(this.fList);                     //对输入虚拟机集合进行排序
		for(int i=0; i<fNum-1; i++){
			for(int j=i+1; j<fNum; j++){
				if(sortFlavor.get(i).cpu<sortFlavor.get(j).cpu){   //按cpu或者mem排序最后结果都是一样的
					Flavor flavor = sortFlavor.get(i);
					sortFlavor.set(i, sortFlavor.get(j));
					sortFlavor.set(j, flavor);
				}
			}
		}
		
		/*2.填充的时候先用大的，大的如果装不下再使用小的，尽量保证预测值改动最小*/
		int i = 0;
		while(i<fNum){
			Flavor flavor = sortFlavor.get(i);    //得到虚拟机
			//判断虚拟机能否放进服务器
			if(server.PutFlavor(flavor)){
				this.optPerson.add(flavor);
				//对放进服务器中的虚拟机，找出在输入虚拟机集合中的位置并修改相应预测结果
				for(int j=0; j<fNum; j++){
					if(flavor.name.equals(this.fList.get(j).name))
						this.fDataArray[j]++;
				}
			}else{   //对应位置的虚拟机放不进服务器则遍历下一个
				i++;
			}
		}		
	}

	public int GetOptType(double mc){
		int opttype = 0;
		double dis1 = Math.abs(mc-1);
		double dis2 = Math.abs(mc-2);
		double dis4 = Math.abs(mc-4);
		if(dis1<dis2 && dis1<dis4)
			opttype = 1;
		else if(dis2<dis1 && dis2<dis4)
			opttype = 2;
		else
			opttype = 4;
		return opttype;
	}
	/**
	 * 按比例填充最后一个服务器
	 * @param server
	 */
	public void fillLastServer(Server server){
		/*1.首先按比例和大小将输入虚拟机集合分为3组*/
		int fNum = this.fList.size();                 //得到输入虚拟机集合
		List<Flavor> groupMC1 = new ArrayList<Flavor>();   //cpu:mem=1:1
		List<Flavor> groupMC2 = new ArrayList<Flavor>();   //cpu:mem=1:2
		List<Flavor> groupMC4 = new ArrayList<Flavor>();   //cpu:mem=1:4
		for(int i=0; i<fNum; i++){
			Flavor flavor = this.fList.get(i);
			int mc = flavor.mem/flavor.cpu;
			if(mc==1)
				groupMC1.add(flavor);
			if(mc==2)
				groupMC2.add(flavor);
			if(mc==4)
				groupMC4.add(flavor);
		}
		SortFlavor(groupMC1);            
		SortFlavor(groupMC2);
		SortFlavor(groupMC4);
		
		int minMc1Cpu = 0;
		int minMc1Mem = 0;		
		int minMc2Cpu = 0;
		int minMc2Mem = 0;		
		int minMc4Cpu = 0;
		int minMc4Mem = 0;
		if(!groupMC1.isEmpty()){
			minMc1Cpu = groupMC1.get(groupMC1.size()-1).cpu;
			minMc1Mem = groupMC1.get(groupMC1.size()-1).mem;
		}else{
			minMc1Cpu = 9999;
			minMc1Mem = 9999;
		}
		
		if(!groupMC2.isEmpty()){
			minMc2Cpu = groupMC2.get(groupMC2.size()-1).cpu;
			minMc2Mem = groupMC2.get(groupMC2.size()-1).mem;
		}else{
			minMc2Cpu = 9999;
			minMc2Mem = 9999;
		}
		
		if(!groupMC4.isEmpty()){
			minMc4Cpu = groupMC4.get(groupMC4.size()-1).cpu;
			minMc4Mem = groupMC4.get(groupMC4.size()-1).mem;
		}else{
			minMc4Cpu = 9999;
			minMc4Mem = 9999;
		}
		
		/*2.根据剩余资源的比例来装箱*/
		while(server.reCpu>0 && server.reMem>0){
			
			double mc = (double)server.reMem/server.reCpu;
			int opttype = GetOptType(mc);
			if(opttype==4 && !groupMC4.isEmpty() && minMc4Cpu<=server.reCpu && minMc4Mem<=server.reMem){
				int f4Num = 0; 
				while(opttype==4 && f4Num<groupMC4.size()){
					Flavor flavor = groupMC4.get(f4Num);
					if(server.PutFlavor(flavor)){
						mc = (double)server.reMem/server.reCpu;
						opttype = GetOptType(mc);
						String name = flavor.name;
						for(int j=0; j<fNum; j++){
							if(this.fList.get(j).name.equals(name)){
								this.fDataArray[j]++;
								break;
							}
						}
					}else
						f4Num++;
				}
			}else if(opttype==2 && !groupMC2.isEmpty() && minMc2Cpu<=server.reCpu && minMc2Mem<=server.reMem){
				int f2Num = 0; 
				while(opttype==2 && f2Num<groupMC2.size()){
					Flavor flavor = groupMC2.get(f2Num);
					if(server.PutFlavor(flavor)){
						mc = (double)server.reMem/server.reCpu;
						opttype = GetOptType(mc);
						String name = flavor.name;
						for(int j=0; j<fNum; j++){
							if(this.fList.get(j).name.equals(name)){
								this.fDataArray[j]++;
								break;
							}
						}
					}else
						f2Num++;
				}
			}else if(opttype==1 && !groupMC1.isEmpty() && minMc1Cpu<=server.reCpu && minMc1Mem<=server.reMem){
				int f1Num = 0; 
				while(opttype==1 && f1Num<groupMC1.size()){
					Flavor flavor = groupMC1.get(f1Num);
					if(server.PutFlavor(flavor)){
						mc = (double)server.reMem/server.reCpu;
						opttype = GetOptType(mc);
						String name = flavor.name;
						for(int j=0; j<fNum; j++){
							if(this.fList.get(j).name.equals(name)){
								this.fDataArray[j]++;
								break;
							}
						}
					}else
						f1Num++;
				}
			}else
				break;
		}
	}
	
	/**
	 * 根据放置结果设置最终输出结果
	 * @param sList  放置结果
	 */
 	public void SetresultD(List<Server> serverList){
		int sNum = serverList.size();   //使用服务器个数
		/*首先根据服务器类型进行分类*/
		int sType = this.sList.size();
		List<Server> Glist = new ArrayList<Server>();
		List<Server> LMlist = new ArrayList<Server>();
		List<Server> HPlist = new ArrayList<Server>();
		if(sType==1){
			
			for(int i=0; i<sNum; i++){
				Server server = serverList.get(i);
				if(server.name.equals(this.sList.get(0).name))
					Glist.add(server);
			}
			/*再转换*/
			List<String> resultList = new ArrayList<String>();
			
			String[] GeneralResult = ListToString(Glist);
			
			if(GeneralResult != null){
				for(int i=0; i<GeneralResult.length; i++){
					resultList.add(GeneralResult[i]);
				}
			}
		
			int size = resultList.size();
			this.resultD = new String[size];
			for(int i=0; i<size; i++){
				this.resultD[i] = resultList.get(i);
			}
				
		}else if(sType==2){
			/*先分类*/
			for(int i=0; i<sNum; i++){
				Server server = serverList.get(i);
				if(server.name.equals(this.sList.get(0).name))
					Glist.add(server);
				if(server.name.equals(this.sList.get(1).name))
					LMlist.add(server);
			}
			/*再转换*/
			List<String> resultList = new ArrayList<String>();
			
			String[] GeneralResult = ListToString(Glist);
			String[] LargeMemoryResult = ListToString(LMlist);
			
			
			if(GeneralResult != null){
				for(int i=0; i<GeneralResult.length; i++){
					resultList.add(GeneralResult[i]);
				}
				resultList.add("");
			}
			
			if(LargeMemoryResult != null){
				for(int i=0; i<LargeMemoryResult.length; i++){
					resultList.add(LargeMemoryResult[i]);
				}				
			}
									
			int size = resultList.size();
			this.resultD = new String[size];
			for(int i=0; i<size; i++){
				this.resultD[i] = resultList.get(i);
			}
			
		}else{	
			
			/*先分类*/
			for(int i=0; i<sNum; i++){
				Server server = serverList.get(i);
				if(server.name.equals(this.sList.get(0).name))
					Glist.add(server);
				if(server.name.equals(this.sList.get(1).name))
					LMlist.add(server);
				if(server.name.equals(this.sList.get(2).name))
					HPlist.add(server);
			}
			/*再转换*/
			List<String> resultList = new ArrayList<String>();
			
			String[] GeneralResult = ListToString(Glist);
			String[] LargeMemoryResult = ListToString(LMlist);
			String[] HighPerformanceResult = ListToString(HPlist);
			
			if(GeneralResult != null){
				for(int i=0; i<GeneralResult.length; i++){
					resultList.add(GeneralResult[i]);
				}
				resultList.add("");
			}
			
			if(LargeMemoryResult != null){
				for(int i=0; i<LargeMemoryResult.length; i++){
					resultList.add(LargeMemoryResult[i]);
				}
				resultList.add("");
			}
			
			if(HighPerformanceResult != null){
				for(int i=0; i<HighPerformanceResult.length; i++){
					resultList.add(HighPerformanceResult[i]);
				}
			}
			
			int size = resultList.size();
			this.resultD = new String[size];
			for(int i=0; i<size; i++){
				this.resultD[i] = resultList.get(i);
			}
		}		
	}
	
	/**
	 * 将服务器集合转换为字符串
	 * @param serverList  一个服务器集合
	 * @return
	 */
	public String[] ListToString(List<Server> serverList){
		
		int sNum = serverList.size();                    //服务器个数
		//如果服务器个数为0则返回null
		if(sNum==0) return null;                        
		String[] result = new String[sNum+1];           //放置结果数组
		String sName = serverList.get(0).name;          //服务器名称
		result[0] = sName+" "+Integer.toString(sNum);   //第一行存放 name + snum
		//开始计算每个服务器的放置结果
		for(int i=0; i<sNum; i++){
			Server server = serverList.get(i);                //得到第i个服务器
			//首先得到第i个服务器中的虚拟机集合
			List<Flavor> newfList = new ArrayList<Flavor>();
			newfList.addAll(server.flist);                    //得到第i个服务器的虚拟机集合
			//开始转换第i个服务器的放置结果
			String str = "";                                  //虚拟机的放置结果
			int reflavor = newfList.size();                   //虚拟机集合中剩余的虚拟机个数
			while(reflavor>0){
				String fname = newfList.get(0).name;          //集合中第一个虚拟机的名称
				int cont = 0;
				for(int j=0; j<newfList.size(); j++){         //开始遍历集合，寻找有没有相同名称的
					//如果发现有名称相同的则将该虚拟机从集合中删除
					if(newfList.get(j).name.equals(fname)){
						cont++;
						newfList.remove(j);
						j--;
					}
				}
				
				str += fname+" "+Integer.toString(cont)+" ";
				reflavor = newfList.size();
				
			}
			result[i+1] = sName+"-"+Integer.toString(i+1)+" "+str;
		}
		
		return result;
	}
	
	/**
	 * 产生种群初始化个体
	 * @param FList       输入虚拟机集合
	 * @param fDataArray  预测结果 
	 * @return
	 */
	public List<Flavor> GetInitPerson(List<Flavor> FList, int[] fDataArray){
		List<Flavor> initPerson = new ArrayList<Flavor>();
		int n = fDataArray.length;
		for(int i=0; i<n; i++){
			int count = fDataArray[i];
			for(int j=0; j<count; j++){
				Flavor flavor = FList.get(i);
				initPerson.add(flavor);
			}
		}
		return initPerson;
	}
	
	/**
	 * 对原始数据进行交叉排序
	 * @param FList        输入虚拟机集合
	 * @param fDataArray   预测结果              两个应该是一一对应的
	 * @return
	 */
	public List<Flavor> GetCrossPerson(List<Flavor> FList, int[] fDataArray){
		List<Flavor> crossPerson = new ArrayList<Flavor>();          //虚拟机集合，存放交叉排序后的虚拟机
		int fNum = FList.size();                                     //虚拟机的种类 
		int[] reFArray = new int[fNum];                              //用于存放排序后剩余的虚拟机数量
		for(int i=0; i<fNum; i++){
			reFArray[i] = fDataArray[i];
		}
		List<Flavor> sortFlist = new ArrayList<Flavor>();
		sortFlist.addAll(FList);
		//首先对虚拟机集合按cpu的大小从大到小排序
		for(int i=0; i<fNum-1; i++){
			for(int j=i+1; j<fNum; j++){
				Flavor tempflavor = null;
				int tempfd = 0;
				if(sortFlist.get(i).cpu<sortFlist.get(j).cpu){
					tempflavor = sortFlist.get(i);
					sortFlist.set(i, sortFlist.get(j));
					sortFlist.set(j, tempflavor);
					
					tempfd = reFArray[i];
					reFArray[i] = reFArray[j];
					reFArray[j] = tempfd;
				}
			}
		}
		int sum = DataMath.sumIntData(reFArray);                     //预测虚拟机的总个数
		//开始交叉排序
		while(sum>0){
			//开始一次遍历每种虚拟机，并将虚拟机按顺序放入集合
			for(int i=0; i<fNum; i++){
				if(reFArray[i]>0){         
					crossPerson.add(sortFlist.get(i));
					reFArray[i]--;
				}
			}
			sum = DataMath.sumIntData(reFArray);
		}
		
		return crossPerson;
	}
	
	/**
	 * 对输入个体进行随机打乱
	 * @param person      初始个体
	 * @return
	 */
	public List<Flavor> GetChaoticPerson(List<Flavor> person){
		List<Flavor> chaoticPerson = new ArrayList<Flavor>();
		chaoticPerson.addAll(person);
		int k = person.size();
		//1.产生k个随机数
		double[] r = new double[k];
		for(int j=0; j<k; j++){
			r[j] = Math.random();  //产生一个0~1的随机数
		}
		//2.按随机数的大小对初始个体进行排序
		for(int m=0; m<k-1; m++){
			for(int n=m+1; n<k; n++){
				double tempr = 0;
				Flavor tempflavor = null;
				if(r[m]<r[n]){
					tempr = r[m];
					r[m] = r[n];
					r[n] = tempr;
					
					tempflavor = chaoticPerson.get(m);
					chaoticPerson.set(m, chaoticPerson.get(n));
					chaoticPerson.set(n, tempflavor);
				}
			}
		}
		return chaoticPerson;
	}
	
	/**
	 * 获取初始种群
	 * @param initPerson  初始个体
	 * @param size        种群规模
	 * @return
	 */
	public List<List<Flavor>> GetInitPopulation(List<Flavor> inputFList, int[] fDataArray, int size){
		List<List<Flavor>> initPopulation = new ArrayList<List<Flavor>>();
		List<Flavor> initPerson = GetInitPerson(inputFList, fDataArray);     //初始个体
		/*1.对初始个体进行排序，加快种群的收敛速度*/
		int k = initPerson.size();
		for(int i=0; i<k-1; i++){
			for(int j=i+1; j<k; j++){
				if(initPerson.get(i).cpu<initPerson.get(j).cpu){   //按cpu或者mem排序最后结果都是一样的
					Flavor flavor = initPerson.get(i);
					initPerson.set(i, initPerson.get(j));
					initPerson.set(j, flavor);
				}
			}
		}
		/*2.开始产生初始种群*/
		
		/**
		 * 第一种：首先将初始种群按cpu的大小分为3组，然后对每组数据进行随机交换得到新个体
		 */
		/*int g = initPerson.get(0).cpu/3;
		List<Flavor> group1 = new ArrayList<Flavor>();
		List<Flavor> group2 = new ArrayList<Flavor>();
		List<Flavor> group3 = new ArrayList<Flavor>();
		for(int i=0; i<k; i++){			
			if(initPerson.get(i).cpu>initPerson.get(0).cpu-g && initPerson.get(i).cpu<=initPerson.get(0).cpu)				
				group1.add(initPerson.get(i));				
			else if(initPerson.get(i).cpu>initPerson.get(0).cpu-2*g && initPerson.get(i).cpu<=initPerson.get(0).cpu-g)				
				group2.add(initPerson.get(i));				
			else				
				group3.add(initPerson.get(i));										
		}
		
		//对三组个体进行随机交换
		initPopulation.add(initPerson);     //先将初始个体加入种群
		
		//具体排序思路是针对每组产生相同个数的随机数，然后按随机数的大小进行排序，
		//然后将排序后的虚拟机合并为一个个体
		 
		for(int i=1; i<size; i++){          //向种群中添加size-1个个体
			
			double[] r1 = new double[group1.size()];
			for(int j=0; j<group1.size(); j++){
				r1[j] = Math.random();  //产生一个0~1的随机数
			}
			Flavor tempflavor1 = null;  
			double tempr1 = 0;
			for(int j=0; j<group1.size()-1; j++){  //按照产生的随机数对第一组数进行排序
				for(int l=j+1; l<group1.size(); l++){
					if(r1[j]<r1[l]){
						tempr1 = r1[j];
						r1[j] = r1[l];
						r1[l] = tempr1;
						
						tempflavor1 = group1.get(j);
						group1.set(j, group1.get(l));
						group1.set(l, tempflavor1);
					}
				}
			}
			
			double[] r2 = new double[group2.size()];
			for(int j=0; j<group2.size(); j++){
				r2[j] = Math.random();  //产生一个0~1的随机数
			}
			Flavor tempflavor2 = null;  
			double tempr2 = 0;
			for(int j=0; j<group2.size()-1; j++){  //按照产生的随机数对第二组数进行排序
				for(int l=j+1; l<group2.size(); l++){
					if(r2[j]<r2[l]){
						tempr2 = r2[j];
						r2[j] = r2[l];
						r2[l] = tempr2;
						
						tempflavor2 = group2.get(j);
						group2.set(j, group2.get(l));
						group2.set(l, tempflavor2);
					}
				}
			}
			
			double[] r3 = new double[group3.size()];
			for(int j=0; j<group3.size(); j++){
				r3[j] = Math.random();  //产生一个0~1的随机数
			}
			Flavor tempflavor3 = null;  
			double tempr3 = 0;
			for(int j=0; j<group3.size()-1; j++){  //按照产生的随机数对第一组数进行排序
				for(int l=j+1; l<group3.size(); l++){
					if(r3[j]<r3[l]){
						tempr3 = r3[j];
						r3[j] = r3[l];
						r3[l] = tempr3;
						
						tempflavor3 = group3.get(j);
						group3.set(j, group3.get(l));
						group3.set(l, tempflavor3);
					}
				}
			}
			
			//将三组虚拟机合并为一个个体
			List<Flavor> person = new ArrayList<Flavor>();
			person.addAll(group1);
			person.addAll(group2);
			person.addAll(group3);
			
			//将个体装入种群
			initPopulation.add(person);
			
		}*/
		
		/**
		 * 第二种：1个从大到小排序的个体，size-1个随机打乱顺序个体
		 */
		/*initPopulation.add(initPerson);
		for(int i=1; i<size; i++){
			initPopulation.add(GetChaoticPerson(initPerson));
		}*/
		
		/**
		 * 第三种(适用于种群规模为5的)：从大到小来一个 然后根据这个的变异来一个  大小循环来一个 然后根据这个的变异来一个 再随机来一个
		 */
		List<Flavor> crossPerson = GetCrossPerson(inputFList, fDataArray);
		
		initPopulation.add(initPerson);
		initPopulation.add(GetChild(initPerson));
		initPopulation.add(GetChild(initPerson));
		initPopulation.add(GetChaoticPerson(initPerson));
		initPopulation.add(GetChaoticPerson(initPerson));
		initPopulation.add(GetChaoticPerson(initPerson));
		initPopulation.add(crossPerson);
		initPopulation.add(GetChild(crossPerson));
		initPopulation.add(GetChild(crossPerson));
		initPopulation.add(GetChild(crossPerson));
		
		return initPopulation;
	}
	
	/**
	 * 将虚拟机序列按顺序放入不同服务器中
	 * @param person  个体即虚拟机集合序列
	 * @return
	 */
	public List<Server> PutVMtoServer(List<Flavor> orgperson){
		/**
		 * 思路：
		 * 1.对于集合person来说，将集合按first fit算法
		 *   将集合中的虚拟机尝试放入三个服务器中，然后计算三个不同类型的服务器的利用率，最后使用利用率最高的服务器来放置
		 * 2.放置完成后将虚拟机从person集合中移除，继续重复第一步，直到集合中的虚拟机个数为0为止
		 */
		List<Server> serverList = new ArrayList<Server>(); //定义服务器序列
		List<Flavor> person = new ArrayList<Flavor>();
		person.addAll(orgperson);
		int reFlavor = person.size();
		while(reFlavor>0){
			
			//选出最大利用率的放置服务器
			double maxUseRato = 0;
			Server optServer = null;
			int sNum = this.sList.size();				
			for(int j=0; j<sNum; j++){      //尝试放入不同服务器中，选择利用率最高的服务器
				
				Server server = new Server(this.sList.get(j).name,
										   this.sList.get(j).totalCpu, 
										   this.sList.get(j).totalMem);
										
				for(int i=0; i<reFlavor; i++){  //把序列中所有能放进去的个体全都放进去
					Flavor flavor = person.get(i);
					server.PutFlavor(flavor);  
				}
				
				double useRato = 0.5*server.GetCpuUseRate()+0.5*server.GetMemUseRate();
				if(useRato>maxUseRato){
					maxUseRato = useRato;
					optServer = server;
				}
			}
			
			//开始放置
			optServer.Clear();      //清空之前放入的虚拟机，开始重新放
			Server newserver = new Server(optServer.name, optServer.totalCpu, optServer.totalMem);
			for(int j=0; j<person.size(); j++){
				Flavor flavor = person.get(j);
				if(newserver.PutFlavor(flavor)){
					person.remove(flavor);    //将放进服务器的虚拟机从集合中清除
					j--;
				}   				
			}
			
			serverList.add(newserver);   //将放置好的服务器添加进服务器集合
			reFlavor = person.size();
		}
		
		return serverList;
	}
	
	/**
	 * 获取种群的适应度值
	 * @param population  种群
	 * @return
	 */
	public double[] GetFitness(List<List<Flavor>> population){
		int size = population.size(); //种群规模
		double[] fitArray = new double[size];
		
		//开始计算种群内每个个体的适应度值
		for(int i=0; i<size; i++){
			
			List<Flavor> person = population.get(i);         //得到种群内的第i个个体						
			List<Server> serverList = PutVMtoServer(person); //得到个体的放置结果 			
			
			/**
			 * 计算服务器集合的总利用率即为个体的适应度值
			 */
			/*List<Flavor> fitperson = population.get(i);
			double cpuRato = 0;  //cpu的总的利用率
			double memRato = 0;  //mem的总的利用率
			int sumVcpu = 0;     //虚拟机的cpu总和
			int sumVmem = 0;     //虚拟机的mem总和
			int sumScpu = 0;     //服务器的cpu总和
			int sumSmem = 0;     //服务器的mem总和
			
			int flavorNum = fitperson.size();
			int serverNum = serverList.size();
			
			for(int j=0; j<flavorNum; j++){
				sumVcpu += fitperson.get(j).cpu;
				sumVmem += fitperson.get(j).mem;
			}
			for(int j=0; j<serverNum; j++){
				sumScpu += serverList.get(j).totalCpu;
				sumSmem += serverList.get(j).totalMem;
			}
			cpuRato = (double)sumVcpu/sumScpu;
			memRato = (double)sumVmem/sumSmem;*/
			
			/*计算前n-1个服务器利用率的平均数*/
			int n = serverList.size();
			double sumCpuRato = 0;
			double sumMemRato = 0;
			double cpuRato = 0;
			double memRato = 0;
			for(int j=0; j<n-1; j++){
				sumCpuRato += serverList.get(j).GetCpuUseRate();
				sumMemRato += serverList.get(j).GetMemUseRate();
			}
			cpuRato = sumCpuRato/(n-1);
			memRato = sumMemRato/(n-1);
			
			fitArray[i] = 0.5*cpuRato + 0.5*memRato;
		}
		return fitArray;
	}
	
	/**
	 * 根据种群的适应度值，利用轮盘赌法选择父代个体
	 * @param population   种群
	 * @param fitArray     种群适应度数组
	 * @return
	 */
	public List<Flavor> ChooseParent(List<List<Flavor>> population, double[] fitArray){
		List<Flavor> parent = new ArrayList<Flavor>();		
		double[] refitArray = new double[fitArray.length];//种群的相对适应度值
		double sum = DataMath.sumData(fitArray);
		for(int i=0; i<fitArray.length; i++){
			refitArray[i] = fitArray[i]/sum;
		}
		double r =Math.random();  //产生一个0~1之间的随机数
		double sumFitR = 0;
		for(int i=0; i<refitArray.length; i++){
			sumFitR += refitArray[i];
			if(r<sumFitR){
				parent = population.get(i);
				break;
			}
		}
		
		return parent;
	}
	
	/**
	 * 根据父代个体进行遗传变异得到子代个体
	 * @param parent  父代个体
	 * @return
	 */
	public List<Flavor> GetChild(List<Flavor> parent){
		List<Flavor> child = new ArrayList<Flavor>(parent.size());
		int k = parent.size();
		double o = Math.random();
		/**单点基因移位变异**/
		/**
		 * 随机产生两个正整数，将两个数之间的基因依次后移，并把最后一个放到最前面
		 */
		if(o>=0 && o<0.3){
			
			int v = (int)(Math.random()*k);
			int u = (int)(Math.random()*k);
			
			if(v>u){                //保证v放的是小的一个随机数
				int temp = v;
				v = u;
				u = temp;
			}
			
			for(int i=0; i<v; i++){   //0~v部分数据直接赋值
				//child.set(i, parent.get(i));
				child.add(parent.get(i));
			}
			//child.set(v, parent.get(u));
			child.add(parent.get(u));
			for(int i=v+1; i<=u; i++){ //v~u之间的数据后移一位
				//child.set(i, parent.get(i-1));
				child.add(parent.get(i-1));
			}
			
			for(int i=u+1; i<k; i++){  //u之后的数据直接赋值
				//child.set(i, parent.get(i));
				child.add(parent.get(i));
			}
			
		/**单点基因倒位变异**/
		/**
		 * 将位于变异点之间的子串中的基因按照逆序排列
		 */
		}else if(o>=0.3 && o<0.6){
			
			int v = (int)(Math.random()*k);
			int u = (int)(Math.random()*k);
			if(v>u){                //保证v放的是小的一个随机数
				int temp = v;
				v = u;
				u = temp;
			}
			
			for(int i=0; i<v; i++){   //0~v部分数据直接赋值
				//child.set(i, parent.get(i));
				child.add(parent.get(i));
			}
			int temp = u;
			for(int i=v; i<=u; i++){   //将v~u之间的数据倒序
				//child.set(i, parent.get(temp--));
				child.add(parent.get(temp--));
			}
			
			for(int i=u+1; i<k; i++){  //u之后的数据直接赋值
				//child.set(i, parent.get(i));
				child.add(parent.get(i));
			}
			
		/**多点基因换位变异**/	
		}else if(o>=0.6 && o<0.9){
			
			int u = k/2;
			int p = (int)(Math.random()*u+1);
			int[] exc = new int[2*p];
			
			for(int i=0; i<2*p; i++){       //产生p对随机数
				exc[i] = (int)(Math.random()*k);
			}
			
			for(int i=0; i<k; i++){        //首先将父代基因拿出来
				//child.set(i, parent.get(i));
				child.add(parent.get(i));
			}
			
			Flavor tempflavor = null;
			for(int i=0; i<p; i++){ //开始交换
				
				tempflavor = child.get(exc[i]);
				child.set(exc[i], child.get(exc[i+p]));
				child.set(exc[i+p], tempflavor);
				
			}
		}else if(o>=0.9 && o<1){
			child = GetChaoticPerson(parent);
		}
		return child;
	}
	
	/**
	 * 根据临时种群获取下一代种群
	 * @param population      临时种群
	 * @param fitArray        临时种群的适应度值
	 * @param n               下一代种群的规模
	 * @return
	 */
	public List<List<Flavor>> GetNextPopulation(List<List<Flavor>> population, double[] fitArray, int n){
		List<List<Flavor>> nextPopulation = new ArrayList<List<Flavor>>(n);
		
		/*根据适应度值从大到小对种群进行排序*/
		int size = population.size();
		double tempf = 0;
		List<Flavor> tempPenson = null;
		for(int i=0; i<size-1; i++){
			for(int j=i+1; j<size; j++){
				if(fitArray[i]<fitArray[j]){
					
					tempf = fitArray[i];
					fitArray[i] = fitArray[j];
					fitArray[j] = tempf;
					
					tempPenson = population.get(i);
					population.set(i, population.get(j));
					population.set(j, tempPenson);
				}
			}
		}
		
		/*将排序后的种群的前n个个体作为下一代个体*/
		nextPopulation.addAll(population.subList(0, n));     
		
		return nextPopulation;
	}
	
	/**
	 * 遗传算法引擎
	 * @param initPopulation   初始化种群
	 * @param maxEpoch         最大遗传代数
	 * @param maxFitness       最大适应度值
	 * @return
	 */
	public List<Flavor> GenAlg(List<List<Flavor>> parentPopulation, int maxEpoch, double maxFitness){
		
		int k = parentPopulation.get(0).size();                                //种群内个体虚拟机的数量
		int n = parentPopulation.size();                                       //种群内个体数量
		List<Flavor> optPerson = new ArrayList<Flavor>(k);                     //最优遗传个体
		List<List<Flavor>> tempPopulation = new ArrayList<List<Flavor>>(n+n/2);//临时种群
		double[] tempFitArray = new double[n+n/2];                             //临时种群的适应度值
		int iter = 0;                                                          //种群遗传代数
		double optFitness = 0;                                                 //种群内最优适应度值
		
		/*开始遗传变异*/
		while(iter<maxEpoch && optFitness<maxFitness){
			
			double[] parentFitArray = GetFitness(parentPopulation);                    //得到父代种群的适应度值
			List<List<Flavor>> childPopulation = new ArrayList<List<Flavor>>(n/2);     //子代种群
			
			//从父代中选出n/2个个体经过遗传变异作为子代个体
			for(int i=0; i<n/2; i++){
				List<Flavor> parent = ChooseParent(parentPopulation, parentFitArray);  //父代个体
				List<Flavor> child = GetChild(parent);                                 //子代个体
				childPopulation.add(child);                                            //将子代个体加入种群
			}
			
			//将父代种群和子代种群加入临时种群，并从中选出最优的n个个体作为下一代
			tempPopulation.addAll(parentPopulation);
			tempPopulation.addAll(childPopulation); 
			tempFitArray = GetFitness(tempPopulation);                  //临时种群的适应度值
			
			//根据临时种群获取到下一代种群
			parentPopulation = GetNextPopulation(tempPopulation, tempFitArray, n);
			
			optPerson = parentPopulation.get(0);        //父代种群内最优个体                   
			optFitness = tempFitArray[0];               //最优个体对应的最大适应度值
			iter++;	
			System.out.println("iter:"+iter+",fitness:"+optFitness);
		}
		return optPerson;
	}
	
	/**
	 * 构造函数    
	 * @param fDataArray       预测结果
	 * @param inputSList       输入服务器集合
	 * @param inputFList       输入虚拟机集合
	 */
	public  GADistribute(int[] fDataArray, List<Server> inputSList, List<Flavor> inputFList){
		
		this.fList = inputFList;           //设置服务器集合
		this.sList = inputSList;           //设置虚拟机集合
		SetfDataArray(fDataArray);         //设置预测结果变量
				
		int size = 10;            //种群规模
		double maxFitness = 0.98; //最大适应度值
		int maxEpoch = 50;        //最大遗传代数
		List<List<Flavor>> initPopulation = GetInitPopulation(inputFList, fDataArray, size);  //初始种群
		
		this.optPerson = GenAlg(initPopulation,maxEpoch, maxFitness);     //最优个体
		
		this.optServerList = PutVMtoServer(this.optPerson);     //放置结果		
		
		/*判断最后一个服务器的利用率是否低于给定阈值，若果太小则将对应预测虚拟机删除*/
		double minRato = 0.5;                //利用率最低阈值
		double maxRato = 0.5;                //利用率最高阈值
		int n = optServerList.size();        //放置使用服务器个数
		double lastRato = 0.5*optServerList.get(n-1).GetCpuUseRate()+0.5*optServerList.get(n-1).GetMemUseRate();   //最后一个服务器的利用率
		if(lastRato<=minRato){
			Server server = optServerList.get(n-1);   //获取最后一个服务器			
			ClearLastServer(server);                  //将最后一个服务器的虚拟机从预测结果中删除
			optServerList.remove(n-1);                //将最后一个服务器从服务器集合中移除	
		}else if(lastRato>maxRato){                   //如果最后一个服务器的利用率超过上限则对预测的服务器进行添加，使得最后的利用率最高
			Server server = optServerList.get(n-1);   //只修改最后一个服务器
			fillLastServer(server);                   //将最后一个服务器装满  
		}
		
		GettotalRato();
		
		SetresultD(this.optServerList);      //设置输出结果
		
	}
	
	/**
	 * 获取当前服务器集合的总利用率
	 * @return
	 */
	public void GettotalRato(){
		
		List<Flavor> fitperson = this.optPerson;
		List<Server> serverList = this.optServerList;
		double cpuRato = 0;  //cpu的总的利用率
		double memRato = 0;  //mem的总的利用率
		int sumVcpu = 0;     //虚拟机的cpu总和
		int sumVmem = 0;     //虚拟机的mem总和
		int sumScpu = 0;     //服务器的cpu总和
		int sumSmem = 0;     //服务器的mem总和
		
		int flavorNum = fitperson.size();
		int serverNum = serverList.size();
		
		for(int j=0; j<flavorNum; j++){
			sumVcpu += fitperson.get(j).cpu;
			sumVmem += fitperson.get(j).mem;
		}
		for(int j=0; j<serverNum; j++){
			sumScpu += serverList.get(j).totalCpu;
			sumSmem += serverList.get(j).totalMem;
		}
		cpuRato = (double)sumVcpu/sumScpu;
		memRato = (double)sumVmem/sumSmem;
		
		System.out.println("totalRato:"+(0.5*cpuRato+0.5*memRato));
		int n = serverList.size();
		for(int i=0; i<n; i++){
			System.out.println("Server"+i+" CPUrato:"+serverList.get(i).GetCpuUseRate()+
                    ",MEMrato:"+serverList.get(i).GetMemUseRate());
		}
		
	}
	
	/**
	 * 测试函数
	 * @param args
	 */
	public static void main(String[] args) {
		LogUtil.printLog("Begin");
		String[] inputContent = FileUtil.read("C:\\Users\\Yezhibo\\Desktop\\测试用例\\input5.txt", null);
		InputContent input = new InputContent(inputContent);
		//int[] fdataArray ={89,78,96,45,77,75,44,90,23,76,50,23,89,67,57,23,19,32};
		//int[] fdataArray ={34,23,48,23,34};
		//int[] fdataArray ={24,78,98,22,77};
		//int[] fdataArray ={45,20,15,56,12,5,8,89,23,34};
		int[] fdataArray ={12,20,34,33,12,56,77,89,66,34};
		GADistribute distribute=new GADistribute(fdataArray, input.inputSList, input.inputFList);
		//String[] resultS = distribute.resultD;
		//distribute.GettotalRato();
		System.out.println(Arrays.toString(distribute.fDataArray));
		LogUtil.printLog("End");
		/*for(int i=0; i<resultS.length; i++){
			System.out.println(resultS[i]);
		}*/
		
	}
	
}
