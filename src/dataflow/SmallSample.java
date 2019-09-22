package dataflow;

import java.util.List;

public class SmallSample {
	
	private int status = 0;
	
	private String code = "0";
	
	public List<?> data;
	
	/**
	 * Constructor
	 * Initialize each field data.
	 * 
	 * @param status : System Status from 0 to 3;
	 * @param code : System Code from 0 to 254;
	 * @param data : Business data read & write via text file.
	 */
	public SmallSample(int status, String code, List<?> data){
		this.status = status;
		this.code = code;
		this.data = data;
	}

	/**
	 * Main method
	 * @param args : nothing
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public List<String> method01(int status, String code, List<?> data){
		return null;
	}
	
	public int method02(int status, String code, List<?> data){
		return 0;
	}
	
	

}
