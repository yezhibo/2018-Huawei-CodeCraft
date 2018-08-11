package com.elasticcloudservice.distribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.elasticcloudservice.dataEntity.Flavor;
import com.elasticcloudservice.dataEntity.InputContent;
import com.elasticcloudservice.dataEntity.Server;
import com.filetool.util.FileUtil;
import com.filetool.util.LogUtil;

/**
 * @ProjectName ecsFuSaiTest
 * @author Yezhibo
 * @CreatTime 2018年4月24日下午7:57:31
 */
public class Distribute {
	
	public String[] resultD;                   //最终输出结果
	public List<Flavor> inputfList;                 //输入虚拟机集合
	public List<Server> inputsList;                 //输入服务器集合
	public List<Server> useServerList;         //最优个体对应的放置结果
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
	 * 根据放置结果设置最终输出结果
	 * @param sList  放置结果
	 */
 	public void SetresultD(){
		int sNum = useServerList.size();   //使用服务器个数
		/*首先根据服务器类型进行分类*/
		int sType = this.inputsList.size();
		List<Server> Glist = new ArrayList<Server>();
		List<Server> LMlist = new ArrayList<Server>();
		List<Server> HPlist = new ArrayList<Server>();
		if(sType==1){
			
			for(int i=0; i<sNum; i++){
				Server server = useServerList.get(i);
				if(server.name.equals(this.inputsList.get(0).name))
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
				Server server = useServerList.get(i);
				if(server.name.equals(this.inputsList.get(0).name))
					Glist.add(server);
				if(server.name.equals(this.inputsList.get(1).name))
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
				Server server = useServerList.get(i);
				if(server.name.equals(this.inputsList.get(0).name))
					Glist.add(server);
				if(server.name.equals(this.inputsList.get(1).name))
					LMlist.add(server);
				if(server.name.equals(this.inputsList.get(2).name))
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
	 * 选择最接近的比例
	 * @param mc
	 * @return
	 */
	public int ChooseType(Server server,GroupFlist groupFlist){
		int reMem = server.reMem;
		int reCpu = server.reCpu;
		double mc = (double)reMem/(reCpu+0.000000001);
		int opttype = 0;
		double dis1 = 0;        //服务器剩余资源比例与比例为1的虚拟机之间的距离
		double dis2 = 0;        //服务器剩余资源比例与比例为2的虚拟机之间的距离
		double dis4 = 0;        //服务器剩余资源比例与比例为4的虚拟机直接按的距离
		
		if(groupFlist.GetReMc1Num()>0 && groupFlist.GetMC1minCpu()<=reCpu && groupFlist.GetMC1minMem()<=reMem)
			dis1 = Math.abs(mc-1);
		else
			dis1 = 9999;
		if(groupFlist.GetReMc2Num()>0 && groupFlist.GetMC2minCpu()<=reCpu && groupFlist.GetMC2minMem()<=reMem)
			dis2 = Math.abs(mc-2);
		else
			dis2 = 9999;
		if(groupFlist.GetReMc4Num()>0 && groupFlist.GetMC4minCpu()<=reCpu && groupFlist.GetMC4minMem()<=reMem)
			dis4 = Math.abs(mc-4);
		else
			dis4 = 9999;
		
		if(dis1<dis2 && dis1<dis4)
			opttype = 1;
		else if(dis2<dis1 && dis2<dis4)
			opttype = 2;
		else if(dis4<dis1 && dis4<dis2)
			opttype = 4;
		else if(dis1==9999 && dis2==9999 && dis4==9999)
			opttype = 999;
		return opttype;
	}
	
	/**
	 * 选择服务器
	 * @param reMc
	 * @return
	 */
	public Server ChooseServer(GroupFlist groupFlist){
		Server optServer = null;
		int sNum = inputsList.size();
		double maxtotalRato = 0; 
		for(int i=0; i<sNum; i++){
			Server server = new Server(inputsList.get(i).name, inputsList.get(i).totalCpu, inputsList.get(i).totalMem);
			GroupFlist tempgroupFlist = new GroupFlist(groupFlist.mc1Flist, groupFlist.mc2Flist, groupFlist.mc4Flist, 
														groupFlist.mc1FArray, groupFlist.mc2FArray, groupFlist.mc4FArray);
			PutVmtoServer(server, tempgroupFlist);
			double totalRato = 0.5*server.GetCpuUseRate()+0.5*server.GetMemUseRate();
			server.Clear();
			if(totalRato>maxtotalRato){
				maxtotalRato = totalRato;				
				optServer = server;
			}else if(totalRato == maxtotalRato){
				double reMc = groupFlist.GetReMc();
				double mcs = (double)server.totalMem/server.totalCpu;
				double mcopts = (double)optServer.totalMem/optServer.totalCpu;
				if(mcopts<mcs){
					if(reMc>mcs)
						optServer = server;
				}else{
					if(reMc<mcopts)
						optServer = server;
				}
			}
		}
		return optServer;
	}
	
	/**
	 * 填充最后一个服务器选择最接近的比例
	 * @param mc
	 * @return
	 */
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
	 * 将虚拟机装入单个服务器中
	 * @param server
	 * @param groupFlist
	 */
	public void PutVmtoServer(Server server, GroupFlist groupFlist){
		while(server.reCpu>0 && server.reMem>0){
			
			List<Flavor> groupMC1 = groupFlist.mc1Flist;
			List<Flavor> groupMC2 = groupFlist.mc2Flist;
			List<Flavor> groupMC4 = groupFlist.mc4Flist;
			int f4Num = 0; 
			int f2Num = 0; 
			int f1Num = 0;
			int opttype = ChooseType(server,groupFlist);
			
			//用比例为4的虚拟机装服务器
			if(opttype==4){
										
				while(opttype==4 && f4Num<groupMC4.size() && groupFlist.GetReMc4Num()>0){
					Flavor flavor = groupMC4.get(f4Num);
					if(groupFlist.mc4FArray.get(f4Num)>0 && server.PutFlavor(flavor)){
						
						opttype = ChooseType(server,groupFlist);
						//每装一个虚拟机，就将该虚拟机从原始序列中删除
						groupFlist.mc4FArray.set(f4Num, groupFlist.mc4FArray.get(f4Num)-1);
					}else
						f4Num++;
				}
								
			}
			
			//用比例为2的虚拟机装服务器
			if(opttype==2){
				
				while(opttype==2 && f2Num<groupMC2.size() && groupFlist.GetReMc2Num()>0){
					Flavor flavor = groupMC2.get(f2Num);
					if(groupFlist.mc2FArray.get(f2Num)>0 && server.PutFlavor(flavor)){
						
						opttype = ChooseType(server,groupFlist);
						//每装一个虚拟机，就将该虚拟机从原始序列中删除
						groupFlist.mc2FArray.set(f2Num, groupFlist.mc2FArray.get(f2Num)-1);
					}else
						f2Num++;
				}
				
			}
			
			//用比例为1的虚拟机装服务器
			if(opttype==1){ 
				while(opttype==1 && f1Num<groupMC1.size()&&groupFlist.GetReMc1Num()>0){
					Flavor flavor = groupMC1.get(f1Num);
					if(groupFlist.mc1FArray.get(f1Num)>0 && server.PutFlavor(flavor)){
						
						opttype = ChooseType(server,groupFlist);
						//每装一个虚拟机，就将该虚拟机从原始序列中删除
						groupFlist.mc1FArray.set(f1Num, groupFlist.mc1FArray.get(f1Num)-1);
					}else
						f1Num++;
				}
			}
			//所有类型的虚拟机都放不进去该服务器
			if(opttype==999)
				break;
		}
	}
	
	/**
	 * 放置函数
	 */
	public void PutVmtoServerList(GroupFlist groupFlist){
		this.useServerList = new ArrayList<Server>();
		int refNum = groupFlist.GetTotalfNum();
		
		while(refNum>0){
					
			/*1.根据虚拟机剩余比例选择合适服务器*/
			Server server = ChooseServer(groupFlist);
			/*2.根据剩余资源的比例来装箱*/
			PutVmtoServer(server, groupFlist);			
			/*3.将装满的服务器放入使用的服务器队列中*/
			this.useServerList.add(server);
			refNum = groupFlist.GetTotalfNum();
			
		}
 	}
		
	/**
	 * 按比例填充最后一个服务器
	 * @param server
	 */
	public void FillServer(Server server){
		/*1.首先按比例和大小将输入虚拟机集合分为3组*/
		int fNum = this.inputfList.size();                 //得到输入虚拟机集合
		List<Flavor> groupMC1 = new ArrayList<Flavor>();   //cpu:mem=1:1
		List<Flavor> groupMC2 = new ArrayList<Flavor>();   //cpu:mem=1:2
		List<Flavor> groupMC4 = new ArrayList<Flavor>();   //cpu:mem=1:4
		for(int i=0; i<fNum; i++){
			Flavor flavor = this.inputfList.get(i);
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
							if(this.inputfList.get(j).name.equals(name)){
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
							if(this.inputfList.get(j).name.equals(name)){
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
							if(this.inputfList.get(j).name.equals(name)){
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
	 * 将最后一个服务器预测的虚拟机删除
	 * @param lastFList   最后一个服务器的虚拟机集合
	 */
	public void ClearServer(Server server){
		
		List<Flavor> lastFList = server.flist;
		int deletfNum = lastFList.size();
		int inputfNum = this.inputfList.size();
		
		for(int i=0; i<deletfNum; i++){           //开始遍历要删除的虚拟机，并从原始预测数据中删除
			String name = lastFList.get(i).name;
			for(int j=0; j<inputfNum; j++){
				if(this.inputfList.get(j).name.equals(name)){
					this.fDataArray[j]--;
					break;
				}
			}
		}
	}
		
	/**
	 * 获取当前服务器集合的总利用率
	 * @return
	 */
	public void GettotalRato(){
		
		List<Server> serverList = this.useServerList;
		double cpuRato = 0;  //cpu的总的利用率
		double memRato = 0;  //mem的总的利用率
		int sumVcpu = 0;     //虚拟机的cpu总和
		int sumVmem = 0;     //虚拟机的mem总和
		int sumScpu = 0;     //服务器的cpu总和
		int sumSmem = 0;     //服务器的mem总和
		
		int fNum = this.inputfList.size();
		int serverNum = serverList.size();
		
		for(int j=0; j<fNum; j++){
			sumVcpu += inputfList.get(j).cpu*fDataArray[j];
			sumVmem += inputfList.get(j).mem*fDataArray[j];
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
			System.out.println("Server"+i+serverList.get(i).name+" CPUrato:"+serverList.get(i).GetCpuUseRate()+
                    ",MEMrato:"+serverList.get(i).GetMemUseRate());
		}
		
	}
	
	/**
	 * 构造函数    
	 * @param fDataArray       预测结果
	 * @param inputSList       输入服务器集合
	 * @param inputFList       输入虚拟机集合
	 */
	public  Distribute(int[] fDataArray, List<Server> inputSList, List<Flavor> inputFList){
		
		this.inputfList = inputFList;           //设置服务器集合
		this.inputsList = inputSList;           //设置虚拟机集合
		SetfDataArray(fDataArray);              //设置预测结果变量
		
		GroupFlist groupFlist = new GroupFlist(inputFList, fDataArray);
		
		PutVmtoServerList(groupFlist);		
		
		/*判断最后一个服务器的利用率是否低于给定阈值，若果太小则将对应预测虚拟机删除*/		
		for(int i=0; i<useServerList.size(); i++){
			Server server = useServerList.get(i);
			double cpuRato = server.GetCpuUseRate();
			double memRato = server.GetMemUseRate();
			if(cpuRato<1 && memRato<1){
				double totalRato = 0.5*cpuRato + 0.5*memRato;
				if(totalRato<0.5){
					ClearServer(server);
					useServerList.remove(i);
					i--;
				}else{
					FillServer(server);
				}
			}
		}
		
		GettotalRato();
		
		SetresultD();      //设置输出结果		
	}
	
	/**
	 * 测试函数
	 * @param args
	 */
	public static void main(String[] args) {
		
		LogUtil.printLog("Begin");
		String[] inputContent = FileUtil.read("C:\\Users\\Yezhibo\\Desktop\\测试用例\\input4.txt", null);
		InputContent input = new InputContent(inputContent);
		//int[] fdataArray ={89,78,96,45,77,75,44,90,23,76,50,23,89,67,57,23,19,32};
		//int[] fdataArray ={34,23,48,23,34};
		//int[] fdataArray ={24,78,98,22,77};
		int[] fdataArray ={45,20,15,56,12,5,8,89,23,34};
		//int[] fdataArray ={12,20,34,33,12,56,77,89,66,34};
		Distribute distribute=new Distribute(fdataArray, input.inputSList, input.inputFList);
		String[] resultS = distribute.resultD;
		System.out.println(Arrays.toString(distribute.fDataArray));
		//distribute.GettotalRato();
		LogUtil.printLog("End");
		for(int i=0; i<resultS.length; i++){
			System.out.println(resultS[i]);
		}
		
	}
	
}
