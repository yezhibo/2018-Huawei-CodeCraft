import java.util.List;

public class GetTimeSeries {

	public static void main(String[] args) {
		
		String[] ecsContent = FileUtil.read("C:\\Users\\Yezhibo\\Desktop\\测试用例\\all统计\\12-1.txt", null);
		
		String[] fNameArray = {"flavor1","flavor2","flavor3","flavor4","flavor5","flavor6","flavor7","flavor8","flavor9","flavor10","flavor11","flavor12","flavor13","flavor14","flavor15","flavor16","flavor17","flavor18"};		
		
		String[] TimeSeries = new String[fNameArray.length];
		
		List<String> fhistory = InputPreprocess.getHistoryNameAndTime(ecsContent);
		
		for(int i=0 ; i<fNameArray.length ; i++){
			
			double[] dataArray = InputPreprocess.getFlavorArray(fNameArray[i], fhistory); //获取原始数据时间序列			
			
			//double[] detectArray = DataPreprocess.outlierDetect(dataArray);
			
			//double[] diff7Array = DataPreprocess.SevenDiff(detectArray);
			
			//double[] diff1Array = DataPreprocess.OneDiff(diff7Array);
			
			String str = "";
			
			String[] sArray = new String[dataArray.length];
			
			for(int j=0; j<dataArray.length; j++){
				
				sArray[j] = String.valueOf(dataArray[j]);
				
			}
			
			for(int j=0; j<sArray.length; j++){
				
				str += sArray[j]+"\t";
				
			}
			
			TimeSeries[i] = str;
			
		}
		
		// 写入输出文件
		
		FileUtil.write("C:\\Users\\Yezhibo\\Desktop\\测试用例\\all统计\\12-1TimeSeries.txt", TimeSeries, false);
		

	}

}
