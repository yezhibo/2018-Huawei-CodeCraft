package com.elasticcloudservice.datapreprocess;

/**
 * 矩阵类，封装与矩阵运算有关函数
 * @ProjectName AR80
 * @author Yezhibo
 * @CreatTime 2018年4月15日上午10:06:04
 */
public class Matrix {
			
	/**
	 * 矩阵乘积函数
	 * @param A
	 * @param B
	 * @return  A*B
	 */
	public static double[][] multiplyBetweenAB (double[][] A, double[][] B) {
		
		int mA = A.length, nA = A[0].length, mB = B.length, nB = B[0].length;
			    	    
		if (mB != nA) {
		     throw new IllegalArgumentException("Matrix inner dimensions must agree.");
		  }
	    
		double[][] X = new double[mA][nB];
		  
		double[] Bcolj = new double[nA];
		
		for (int j = 0; j < nB; j++){
			
		   for (int k = 0; k < nA; k++){ //先得到矩阵B的第j列元素
		      Bcolj[k] = B[k][j];
		   }
		   
		   for (int i = 0; i < mA; i++){
			   
		      double[] Arowi = A[i];     //得到矩阵A的第i行元素
		      double s = 0;
		      
		      for (int k = 0; k < nA; k++){
		         s += Arowi[k]*Bcolj[k];
		      }
		      
		      X[i][j] = s;
		   }
		}
      	     
	   return X;
   }
    
    /**
     * 矩阵的转置
     * @param newdata
     * @return
     */
    public static double[][] trans(double[][] newdata) {
    	
        double[][] newdata2 = new double[newdata[0].length][newdata.length];
        for(int i=0; i<newdata.length; i++) 
            for(int j=0; j<newdata[0].length; j++) {
                newdata2[j][i] = newdata[i][j];
            }
        return newdata2;
    }
    
    /**
	 * 求矩阵(h,v)坐标的位置的余子式
	 * @param data
	 * @param h
	 * @param v
	 * @return
	 */
    public static double[][] getConfactor(double[][] data, int h, int v) {
        int H = data.length;
        int V = data[0].length;
        double[][] newdata = new double[H-1][V-1];
        for(int i=0; i<newdata.length; i++) {
            if(i < h-1) {
                for(int j=0; j<newdata[i].length; j++) {
                    if(j < v-1) {
                        newdata[i][j] = data[i][j];
                    }else {
                        newdata[i][j] = data[i][j+1];
                    }
                }
            }else {
                for(int j=0; j<newdata[i].length; j++) {
                    if(j < v-1) {
                        newdata[i][j] = data[i+1][j];
                    }else {
                        newdata[i][j] = data[i+1][j+1];
                    }
                }
            }
        }

        return newdata;
    }
    

    /**
     * 计算矩阵data行列式的值
     * @param data
     * @return
     */
    public static double getMartrixResult(double[][] data) {
        /*
         * 二维矩阵计算
         */
        if(data.length == 2) {
            return data[0][0]*data[1][1] - data[0][1]*data[1][0];
        }
        /*
         * 二维以上的矩阵计算
         */
        double result = 0;
        int num = data.length;
        double[] nums = new double[num];
        for(int i=0; i<data.length; i++) {
            if(i%2 == 0) {
                nums[i] = data[0][i] * getMartrixResult(getConfactor(data, 1, i+1));
            }else {
                nums[i] = -data[0][i] * getMartrixResult(getConfactor(data, 1, i+1));
            }
        }
        for(int i=0; i<data.length; i++) {
            result += nums[i];
        }

        return result;
    }
    
    /**
     * 逆矩阵求解函数
     * @param data 矩阵
     * @return 矩阵的逆矩阵
     */
    public static double[][] getReverseMartrix(double[][] data) {
    	
        double[][] newdata = new double[data.length][data[0].length];
        
        double A = getMartrixResult(data);//得到矩阵的行列式值
                
        for(int i=0; i<data.length; i++) {
            for(int j=0; j<data[0].length; j++) {
                if((i+j)%2 == 0) {
                    newdata[i][j] = getMartrixResult(getConfactor(data, i+1, j+1)) / A;
                }else {
                    newdata[i][j] = -getMartrixResult(getConfactor(data, i+1, j+1)) / A;
                }

            }
        }
        
        newdata = trans(newdata);

        /*for(int i=0;i<newdata.length; i++) {
            for(int j=0; j<newdata[0].length; j++) {
                System.out.print(newdata[i][j]+ "   ");
            }
            System.out.println();
        }*/
        return newdata;
    }
    
}
