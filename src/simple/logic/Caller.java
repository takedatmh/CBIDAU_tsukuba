package simple.logic;

public class Caller {
	
	public void callerMethod01(){
		exec();
	}
	
	public void callerMethod02(){
		exec();
	}
	
	private void exec(){
		System.out.println("execution a private method.");
	}

}
