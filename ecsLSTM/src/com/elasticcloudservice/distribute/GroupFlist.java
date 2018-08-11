package com.elasticcloudservice.distribute;

import java.util.ArrayList;
import java.util.List;

import com.elasticcloudservice.dataEntity.Flavor;

/**
 * @ProjectName ecsFuSaiTest
 * @author Yezhibo
 * @CreatTime 2018年4月24日下午8:40:05
 */
public class GroupFlist {
	
	public List<Flavor> mc1Flist;
	public List<Flavor> mc2Flist;
	public List<Flavor> mc4Flist;
	public List<Integer> mc1FArray;
	public List<Integer> mc2FArray;
	public List<Integer> mc4FArray;
	
	/**
	 * 将原始虚拟机按比例进行分组
	 * @param inputflist
	 * @param fDataArray
	 */
	public GroupFlist(List<Flavor> inputflist, int[] fDataArray){
		int fNum = inputflist.size();
		this.mc1Flist = new ArrayList<Flavor>();
		this.mc2Flist = new ArrayList<Flavor>();
		this.mc4Flist = new ArrayList<Flavor>();
		this.mc1FArray = new ArrayList<Integer>();
		this.mc2FArray = new ArrayList<Integer>();
		this.mc4FArray = new ArrayList<Integer>();
		//先将虚拟机序列进行分类
		for(int i=0; i<fNum; i++){
			Flavor flavor = new Flavor(inputflist.get(i).name, inputflist.get(i).cpu, inputflist.get(i).mem);
			int mc = flavor.mem/flavor.cpu;
			if(mc==1){
				this.mc1Flist.add(flavor);
				this.mc1FArray.add(fDataArray[i]);
			}else if(mc==2){
				this.mc2Flist.add(flavor);
				this.mc2FArray.add(fDataArray[i]);
			}else{
				this.mc4Flist.add(flavor);
				this.mc4FArray.add(fDataArray[i]);
			}
		}
		//对分组后的虚拟机序列进行排序
		SortFlavor(mc1Flist, mc1FArray);
		SortFlavor(mc2Flist, mc2FArray);
		SortFlavor(mc4Flist, mc4FArray);
	}
	
	public GroupFlist(List<Flavor> mc1f, List<Flavor> mc2f,List<Flavor> mc4f,List<Integer> mc1d,List<Integer> mc2d,List<Integer> mc4d ){
		this.mc1Flist = new ArrayList<Flavor>();
		this.mc2Flist = new ArrayList<Flavor>();
		this.mc4Flist = new ArrayList<Flavor>();
		this.mc1FArray = new ArrayList<Integer>();
		this.mc2FArray = new ArrayList<Integer>();
		this.mc4FArray = new ArrayList<Integer>();
		mc1Flist.addAll(mc1f);
		mc2Flist.addAll(mc2f);
		mc4Flist.addAll(mc4f);
		mc1FArray.addAll(mc1d);
		mc2FArray.addAll(mc2d);
		mc4FArray.addAll(mc4d);
	}
	
	/**
	 * 获取剩余虚拟机的mem cpu比例
	 * @return
	 */
	public double GetReMc(){
		double reMc = 0;
		int totalMem = 0;
		int totalCpu = 0;
		for(int i=0; i<mc1Flist.size(); i++){
			Flavor flavor = mc1Flist.get(i);
			totalMem += flavor.mem*mc1FArray.get(i);
			totalCpu += flavor.cpu*mc1FArray.get(i);
		}
		for(int i=0; i<mc2Flist.size(); i++){
			Flavor flavor = mc2Flist.get(i);
			totalMem += flavor.mem*mc2FArray.get(i);
			totalCpu += flavor.cpu*mc2FArray.get(i);
		}
		for(int i=0; i<mc4Flist.size(); i++){
			Flavor flavor = mc4Flist.get(i);
			totalMem += flavor.mem*mc4FArray.get(i);
			totalCpu += flavor.cpu*mc4FArray.get(i);
		}
		reMc = (double)totalMem/totalCpu;
		
		return reMc;
	}
	
	public int GetReMc1Num(){
		int reNum = 0;
		int n = mc1Flist.size();
		for(int i=0; i<n; i++)
			reNum += mc1FArray.get(i);
		return reNum;
	}
	
	public int GetReMc2Num(){
		int reNum = 0;
		int n = mc2Flist.size();
		for(int i=0; i<n; i++)
			reNum += mc2FArray.get(i);
		return reNum;
	}
	
	public int GetReMc4Num(){
		int reNum = 0;
		int n = mc4Flist.size();
		for(int i=0; i<n; i++)
			reNum += mc4FArray.get(i);
		return reNum;
	}
	
	public int GetMC1minCpu(){
		int minCpu = 0;
		int n = mc1FArray.size();
		for(int i=n-1; i>=0; i--){
			if(mc1FArray.get(i)>0){
				minCpu = mc1Flist.get(i).cpu;
				break;
			}
		}
		return minCpu;
	}
	
	public int GetMC2minCpu(){
		int minCpu = 0;
		int n = mc2FArray.size();
		for(int i=n-1; i>=0; i--){
			if(mc2FArray.get(i)>0){
				minCpu = mc2Flist.get(i).cpu;
				break;
			}
		}
		return minCpu;
	}
	
	public int GetMC4minCpu(){
		int minCpu = 0;
		int n = mc4FArray.size();
		for(int i=n-1; i>=0; i--){
			if(mc4FArray.get(i)>0){
				minCpu = mc4Flist.get(i).cpu;
				break;
			}
		}
		return minCpu;
	}
	
	public int GetMC1minMem(){
		int minMem = 0;
		int n = mc1FArray.size();
		for(int i=n-1; i>=0; i--){
			if(mc1FArray.get(i)>0){
				minMem = mc1Flist.get(i).mem;
				break;
			}
		}
		return minMem;
	}
	
	public int GetMC2minMem(){
		int minMem = 0;
		int n = mc2FArray.size();
		for(int i=n-1; i>=0; i--){
			if(mc2FArray.get(i)>0){
				minMem = mc2Flist.get(i).mem;
				break;
			}
		}
		return minMem;
	}
	
	public int GetMC4minMem(){
		int minMem = 0;
		int n = mc4FArray.size();
		for(int i=n-1; i>=0; i--){
			if(mc4FArray.get(i)>0){
				minMem = mc4Flist.get(i).mem;
				break;
			}
		}
		return minMem;
	}
		
	/**
	 * 对虚拟机集合按照cpu从大到小进行排序
	 * @param flist
	 * @param farray
	 */
	public void SortFlavor(List<Flavor> flist, List<Integer> farray){
		int n = flist.size();
		for(int i=0; i<n-1; i++){
			for(int j=i+1; j<n; j++){
				if(flist.get(i).cpu<flist.get(j).cpu){
					Flavor flavor = flist.get(i);
					flist.set(i, flist.get(j));
					flist.set(j, flavor);
					
					int data = farray.get(i);
					farray.set(i, farray.get(j));
					farray.set(j, data);
				}
			}
		}
	}
	
	/**
	 * 获取剩余的虚拟机个数
	 * @return
	 */
	public int GetTotalfNum(){
		int totalfNum = 0;
		for(int i=0; i<mc1FArray.size(); i++)
			totalfNum += mc1FArray.get(i);
		for(int i=0; i<mc2FArray.size(); i++)
			totalfNum += mc2FArray.get(i);
		for(int i=0; i<mc4FArray.size(); i++)
			totalfNum += mc4FArray.get(i);
		return totalfNum;
	}
	
	

}
