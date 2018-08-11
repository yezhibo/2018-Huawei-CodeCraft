package com.filetool.main;


import com.elasticcloudservice.predict.Predict;
import com.filetool.util.FileUtil;
import com.filetool.util.LogUtil;

/**
 * 
 * 工具入口
 * 
 * @version [版本号, 2017-12-8]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class Main {
	public static void main(String[] args){

		if (args.length != 3) {
			System.err
					.println("please input args: ecsDataPath, inputFilePath, resultFilePath");
			return;
		}

		String ecsDataPath = args[0];
		String inputFilePath = args[1];
		String resultFilePath = args[2];

		//开始执行程序并记时
		LogUtil.printLog("Begin");

		// 读取输入文件
		String[] ecsContent = FileUtil.read(ecsDataPath, null);
		String[] inputContent = FileUtil.read(inputFilePath, null);

		// 功能实现入口
		String[] resultContents = Predict.predictVm(ecsContent, inputContent);

		// 写入输出文件
		if (hasResults(resultContents)) {
			FileUtil.write(resultFilePath, resultContents, false);
		} else {
			FileUtil.write(resultFilePath, new String[] { "NA" }, false);
		}
		//程序执行结束，打印执行时间
		LogUtil.printLog("End");
		
		
	}

	private static boolean hasResults(String[] resultContents) {
		if (resultContents == null) {
			return false;
		}
		//输出结果中不能有空白字符
		for (String contents : resultContents) {
			if (contents != null && !contents.trim().isEmpty()) {
				return true;
			}
		}
		return false;
	}

}
