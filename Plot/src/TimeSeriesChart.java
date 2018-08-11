import java.awt.Font;  
import java.awt.GridLayout;
import java.text.SimpleDateFormat;  
  



import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;  
import org.jfree.chart.ChartPanel;  
import org.jfree.chart.JFreeChart;  
import org.jfree.chart.axis.DateAxis;  
import org.jfree.chart.axis.ValueAxis;  
import org.jfree.chart.plot.XYPlot;  
import org.jfree.data.time.TimeSeries;  
import org.jfree.data.time.TimeSeriesCollection;  
import org.jfree.data.time.Year;
import org.jfree.data.xy.XYDataset;  
  
public class TimeSeriesChart {  
	
	ChartPanel frame1;   //类成员变量
		
	/**
	 * 构造函数
	 * @param Title   折线图标题
	 * @param xTitle  横坐标标题
	 * @param yTitle  纵坐标标题
	 * @param realArray     数据序列1
	 * @param predictArray  数据序列2
	 */
	public  TimeSeriesChart(String Title, String xTitle, String yTitle, double[] realArray, double[] predictArray){  
			
		    XYDataset xydataset = createDataset(realArray, predictArray);  
		    
		    JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(Title, xTitle, yTitle, xydataset, true, true, true);  
			
		    XYPlot xyplot = (XYPlot) jfreechart.getPlot();  
		    
			DateAxis dateaxis = (DateAxis) xyplot.getDomainAxis();
			
			dateaxis.setDateFormatOverride(new SimpleDateFormat("yyy")); 
			
			frame1=new ChartPanel(jfreechart,true);  
			
			dateaxis.setLabelFont(new Font("微软雅黑",Font.BOLD,12));         //水平底部标题  
			
			dateaxis.setTickLabelFont(new Font("宋体",Font.BOLD,12));  //垂直标题  
			
			ValueAxis rangeAxis=xyplot.getRangeAxis();//获取柱状  
			
			rangeAxis.setLabelFont(new Font("黑体",Font.BOLD,15));  
			
			jfreechart.getLegend().setItemFont(new Font("黑体", Font.BOLD, 15));  
			
			jfreechart.getTitle().setFont(new Font("宋体",Font.BOLD,20));//设置标题字体  
		  
		 }  
    
	/**
	 * 创建图表数据集
	 * @param realArray
	 * @param predictArray
	 * @return
	 */
    private static XYDataset createDataset(double[] realArray, double[] predictArray) {   
	 		   
		TimeSeries timeseries = new TimeSeries("真实值");  //创建真实值时间序列
		
		TimeSeries timeseries1 = new TimeSeries("预测值");  //创建预测值时间序列
		
		int n = realArray.length;
		
		for(int i=0; i<n; i++){
			
			timeseries.add(new Year(i+1), realArray[i]); 
			
			timeseries1.add(new Year(i+1), predictArray[i]); 
			
		}
	    		  		    		    
	    TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();  //创建时间序列集合
	    
	    timeseriescollection.addSeries(timeseries);  
	    
	    timeseriescollection.addSeries(timeseries1); 
	    
	    return timeseriescollection;  
	    
    } 
	
    /**
     * 获取类成员变量--面板
     * @return
     */
    public ChartPanel getChartPanel(){  
	  
        return frame1;      
        
    }  

    
    public static  void PlotData(List<double[]> real, List<double[]> predict){ 
    	
	    JFrame frame=new JFrame("flavor销售预测数据统计图");  //创建一个面板
	    
	    int n = real.size();  //获取虚拟机的规格数量
	    
		frame.setLayout(new GridLayout(n,1,10,0));  
		
		for(int i=0; i<n; i++){
			
			double[] realArray = real.get(i);
			
			double[] predictArray = predict.get(i);
			
			TimeSeriesChart chart = new TimeSeriesChart("flavor"+(i+1)+"预测数据", "日期", "台数", realArray, predictArray);
			
			frame.add(chart.getChartPanel());     //将图表添加到面板中  
			
		}
				
	    frame.setBounds(200, 200, 1000, 300);   //设置面板的出现位置和大小
	    
	    frame.setVisible(true);  
    }       
}  