package plot;
import java.awt.Container;
import java.awt.Font;  
import java.awt.GridLayout;
import java.awt.ScrollPane;
import java.text.SimpleDateFormat;  
  



import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

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
	public  TimeSeriesChart(String Title, String xTitle, String yTitle, double[] histArray, double[] phistArray, double[] testArray, double[] predictArray){  
			
		    XYDataset xydataset = createDataset(histArray, phistArray, testArray, predictArray);  
		    
		    JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(Title, xTitle, yTitle, xydataset, true, true, true);  
			
		    XYPlot xyplot = (XYPlot) jfreechart.getPlot();  
		    
			DateAxis dateaxis = (DateAxis) xyplot.getDomainAxis();
			
			dateaxis.setDateFormatOverride(new SimpleDateFormat("yyy")); 
			
			frame1=new ChartPanel(jfreechart,true);  
			
			dateaxis.setLabelFont(new Font("Consolas",Font.BOLD,10));         //水平底部标题  
			
			dateaxis.setTickLabelFont(new Font("Consolas",Font.BOLD,10));  //垂直标题  
			
			ValueAxis rangeAxis=xyplot.getRangeAxis();//获取柱状  
			
			rangeAxis.setLabelFont(new Font("Consolas",Font.BOLD,10));  
			
			jfreechart.getLegend().setItemFont(new Font("Consolas", Font.BOLD, 10));  
			
			jfreechart.getTitle().setFont(new Font("Consolas",Font.BOLD,15));//设置标题字体  
		  
	}  
    
	/**
	 * 创建图表数据集
	 * @param realArray
	 * @param predictArray
	 * @return
	 */
    private static XYDataset createDataset(double[] histArray, double[] phistArray, double[] testArray, double[] predictArray) {   
	 		   
		TimeSeries timeseries = new TimeSeries("Real");  //创建真实值时间序列
		
		TimeSeries timeseries1 = new TimeSeries("Predict");  //创建预测值时间序列
		
		int n = histArray.length + testArray.length;
		
		int h = histArray.length;
		
		for(int i=0; i<n; i++){
			
			if(i<h){
				timeseries.add(new Year(i+1), histArray[i]); 				
				timeseries1.add(new Year(i+1), phistArray[i]); 
			}else{
				timeseries.add(new Year(i+1), testArray[i-h]); 				
				timeseries1.add(new Year(i+1), predictArray[i-h]); 
			}						
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
   
    /**
     * 开始绘图
     * @param real
     * @param predict
     */
    public static void PlotData(String[] fName,List<double[]> hist, List<double[]> phist, List<double[]> test, List<double[]> predict){ 
    	
	    JFrame frame=new JFrame("flavor销售预测数据统计图");  //创建第一层面板
	    
	    Container container = frame.getContentPane(); //第一层面板的容器
	    	    
	    ScrollPane spanel = new  ScrollPane(); //滚动条面板，第二层面板
	   
	    JPanel  jpanel = new JPanel(); //第三层面板
	    
	    int n = hist.size();  //获取虚拟机的规格数量	 
	    
	    jpanel.setLayout(new GridLayout(n,1,50,0)); //设置第三层面板的布局，15行 1列 
		
		for(int i=0; i<n; i++){
			
			double[] histArray = hist.get(i);
			
			double[] phistory = phist.get(i);
			
			double[] testArray = test.get(i);
			
			double[] predictArray = predict.get(i);
			
			TimeSeriesChart chart = new TimeSeriesChart(fName[i]+"Real-Predict", "Time", "Sales", histArray, phistory, testArray, predictArray);
			
			ChartPanel chartPanel = chart.getChartPanel();
			
			chartPanel.setSize(800, 50);
			
			jpanel.add(chartPanel);     //将图表添加到第三城面板中  
			
		}
		
		spanel.add(jpanel);   //将第三层面板添加到滚动条面板中
		
		spanel.setSize(1000, 100);  //设置滚动条面板的大小
			
		container.add(spanel);  //将滚动条面板添加到第一层面板中
				
	    frame.setBounds(200, 200, 1000, 500);   //设置面板的出现位置和大小
	    
		frame.setVisible(true);  
		    
    }       

    
    
}  