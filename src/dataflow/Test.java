package dataflow;

public class Test {
	
	public static void main(String[] args) {
		method();
	}
	
	protected static int method()
	{
		try {
			System.out.println("1. try statement");
			throw new Exception();
		} catch(Exception e){
			System.out.println("2. catch statement");
		}
		System.out.println("3. return statement");
		return 0;
	}
}
